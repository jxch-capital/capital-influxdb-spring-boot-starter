package org.jxch.capital.influx.repository;

import com.influxdb.client.domain.WritePrecision;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jxch.capital.influx.point.InfluxPointMeasurement;
import org.jxch.capital.influx.point.InfluxPointTag;
import org.jxch.capital.influx.point.InfluxPointTime;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@ToString
@Accessors(chain = true)
@InfluxPointMeasurement("kline_test")
public class TestPoint {
    @InfluxPointTag
    private String code;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @InfluxPointTime(writePrecision = WritePrecision.MS)
    private Date time;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double volume;
}
