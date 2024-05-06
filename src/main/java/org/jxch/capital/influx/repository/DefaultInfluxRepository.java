package org.jxch.capital.influx.repository;

import cn.hutool.core.date.DateUtil;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.DeletePredicateRequest;
import org.jetbrains.annotations.NotNull;
import org.jxch.capital.influx.config.InfluxDBAutoConfig;
import org.jxch.capital.influx.point.InfluxPoints;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DefaultInfluxRepository<T> implements InfluxRepository<T> {
    private final Class<T> pointClazz;
    private final InfluxDBClient influxDBClient;
    private final InfluxDBAutoConfig config;

    public DefaultInfluxRepository(Class<T> pointClazz, InfluxDBClient influxDBClient, InfluxDBAutoConfig config) {
        this.pointClazz = pointClazz;
        this.influxDBClient = influxDBClient;
        this.config = config;
    }

    @Override
    public void write(List<T> points) {
        influxDBClient.getWriteApiBlocking().writePoints(points.stream().map(InfluxPoints::toInfluxPoint).toList());
    }

    @Override
    public List<T> queryByTagExampleAndTimeBetween(T example, Date startTime, Date endTime) {
        StringBuilder fluxQuery = new StringBuilder(String.format("""
                from(bucket: "%s")
                    |> range(start: time(v:"%s"), stop: time(v:"%s"))
                    |> filter(fn: (r) => r._measurement == "%s")
                """, config.getBucket(), DateUtil.format(startTime, "yyyy-MM-dd'T'HH:mm:ss'Z'"), DateUtil.format(endTime, "yyyy-MM-dd'T'HH:mm:ss'Z'"),
                InfluxPoints.getMeasurement(pointClazz)));

        if (InfluxPoints.hasAnyNonNullTagValue(example)) {
            for (Map.Entry<String, String> entry : InfluxPoints.getTags(example).entrySet()) {
                fluxQuery.append(String.format("    |> filter(fn: (r) => r.%s == \"%s\")\n", entry.getKey(), entry.getValue()));
            }
        }

        return InfluxPoints.toPointDto(influxDBClient.getQueryApi().query(fluxQuery.toString(), config.getOrg()), pointClazz);
    }

    @Override
    public void deletePointsByTimeBetween(Date startTime, Date endTime) {
        DeletePredicateRequest request = new DeletePredicateRequest()
                .start(startTime.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .stop(endTime.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .predicate(String.format("_measurement=\"%s\"", InfluxPoints.getMeasurement(pointClazz))); // 删除条件

        influxDBClient.getDeleteApi().delete(request, config.getOrg(), config.getBucket());
    }

    @Override
    public void deletePointByTime(@NotNull Date time) {
        deletePointsByTimeBetween(time, time);
    }

}
