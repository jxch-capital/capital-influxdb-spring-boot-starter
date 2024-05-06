package org.jxch.capital.influx.point;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InfluxPointField {

    boolean ignore() default false;

    String alias() default "";

}
