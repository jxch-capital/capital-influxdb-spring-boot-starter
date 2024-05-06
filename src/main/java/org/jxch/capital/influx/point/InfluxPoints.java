package org.jxch.capital.influx.point;

import cn.hutool.core.annotation.AnnotationUtil;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.SneakyThrows;
import org.jxch.capital.influx.util.ReflectionsU;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InfluxPoints {

    public static String getMeasurement(Class<?> clazz) {
        return clazz.getAnnotation(InfluxPointMeasurement.class).value();
    }

    public static String getMeasurement(Object obj) {
        return getMeasurement(obj.getClass());
    }

    public static List<Field> getAllTagFields(Object obj) {
        return Arrays.stream(obj.getClass().getDeclaredFields()).filter(field -> AnnotationUtil.hasAnnotation(field, InfluxPointTag.class)).toList();
    }

    public static List<Field> getAllTagNonNullFields(Object obj) {
        return getAllTagFields(obj).stream().filter(field -> Objects.nonNull(ReflectionsU.getFieldValueNullable(obj, field))).toList();
    }

    public static boolean hasAnyNonNullTagValue(Object obj) {
        return !getAllTagNonNullFields(obj).isEmpty();
    }

    public static Map<String, String> getTags(Object obj) {
        return getAllTagNonNullFields(obj).stream().collect(Collectors.toMap(field ->
                        ReflectionsU.hasInfluxPointTagAlias(field) ? ReflectionsU.getInfluxPointTagAlias(field) : field.getName(),
                field -> ReflectionsU.getFieldValueNotNull(obj, field).toString()));
    }

    private static Field getInfluxPointTimeField(Object obj) {
        return Arrays.stream(obj.getClass().getDeclaredFields()).filter(field -> Objects.nonNull(AnnotationUtils.findAnnotation(field, InfluxPointTime.class)))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("必须使用 InfluxPointTime 注解标注 time 字段"));
    }

    public static Long getTime(Object obj) {
        long ms = ((Date) ReflectionsU.getFieldValueNotNull(obj, getInfluxPointTimeField(obj), "InfluxPointTime is null.")).getTime();
        return switch (getWritePrecision(obj)) {
            case MS -> ms;
            case S -> TimeUnit.MILLISECONDS.toSeconds(ms);
            case US -> TimeUnit.MILLISECONDS.toMicros(ms);
            case NS -> TimeUnit.MILLISECONDS.toNanos(ms);
        };
    }

    public static WritePrecision getWritePrecision(Object obj) {
        return getInfluxPointTimeField(obj).getAnnotation(InfluxPointTime.class).writePrecision();
    }

    public static Map<String, Object> getFields(Object obj) {
        return Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(field -> Objects.nonNull(ReflectionsU.getFieldValueNullable(obj, field)))
                .filter(ReflectionsU::isInfluxPointField)
                .collect(Collectors.toMap(field -> ReflectionsU.hasInfluxPointFieldAlias(field) ? ReflectionsU.getInfluxPointFieldAlias(field) : field.getName(),
                        field -> ReflectionsU.getFieldValueNotNull(obj, field)));
    }

    public static Point toInfluxPoint(Object obj) {
        return Point.measurement(getMeasurement(obj))
                .addTags(getTags(obj))
                .addFields(getFields(obj))
                .time(getTime(obj), getWritePrecision(obj));
    }

    @SneakyThrows
    public static <POINT> List<POINT> toPointDto(List<FluxTable> fluxTables, Class<POINT> clazz) {
        Map<Instant, POINT> pointMap = fluxTables.stream().flatMap(fluxTable -> fluxTable.getRecords().stream())
                .collect(Collectors.toMap(FluxRecord::getTime, record -> ReflectionsU.newInstance(clazz), (a, b) -> a));

        for (FluxTable fluxTable : fluxTables) {
            for (FluxRecord record : fluxTable.getRecords()) {
                POINT point = pointMap.get(record.getTime());
                Date time = Date.from(Objects.requireNonNull(record.getTime()));
                ReflectionsU.setFieldValue(point, getInfluxPointTimeField(point).getName(), time);

                String field = Objects.requireNonNull(record.getValueByKey("_field")).toString();
                Object value = record.getValueByKey("_value");
                ReflectionsU.setFieldValue(point, field, value);

                for (Field tagField : getAllTagFields(point)) {
                    ReflectionsU.setFieldValue(point, tagField.getName(), record.getValueByKey(tagField.getName()));
                }
            }
        }

        return pointMap.values().stream().toList();
    }

}
