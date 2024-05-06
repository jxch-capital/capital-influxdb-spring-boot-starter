package org.jxch.capital.influx.point;

import com.influxdb.client.domain.WritePrecision;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InfluxPointTime {

    WritePrecision writePrecision() default WritePrecision.MS;

}
