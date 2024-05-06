package org.jxch.capital.influx.config;

import org.jxch.capital.influx.registrar.InfluxRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(InfluxRegistrar.class)
public @interface InfluxRepositoryScan {

    String[] basePackages();

}
