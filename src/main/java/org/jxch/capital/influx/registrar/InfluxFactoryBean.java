package org.jxch.capital.influx.registrar;

import com.influxdb.client.InfluxDBClient;
import org.jxch.capital.influx.config.InfluxDBAutoConfig;
import org.jxch.capital.influx.handler.InfluxRepositoryHandler;
import org.jxch.capital.influx.repository.InfluxRepository;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;

public class InfluxFactoryBean implements FactoryBean<Object> {
    private final Class<?> influxRepositoryClazz;

    @Autowired
    private InfluxDBClient influxDBClient;

    @Autowired
    private InfluxDBAutoConfig config;

    public InfluxFactoryBean(Class<?> influxRepositoryClazz) {
        this.influxRepositoryClazz = influxRepositoryClazz;
    }

    @Override
    public Object getObject() {
        Class<?> pointClazz = Arrays.stream(ResolvableType.forClass(influxRepositoryClazz).getInterfaces())
                .filter(intf -> Objects.nonNull(intf.getRawClass()) && InfluxRepository.class.isAssignableFrom(intf.getRawClass()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("必须实现 InfluxRepository 接口"))
                .resolveGeneric(0);

        return Proxy.newProxyInstance(influxRepositoryClazz.getClassLoader(),
                new Class<?>[]{influxRepositoryClazz},
                new InfluxRepositoryHandler(influxDBClient, config, pointClazz));
    }

    @Override
    public Class<?> getObjectType() {
        return influxRepositoryClazz;
    }

}
