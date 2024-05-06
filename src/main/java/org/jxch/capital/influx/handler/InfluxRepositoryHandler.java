package org.jxch.capital.influx.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxTable;
import org.jxch.capital.influx.config.InfluxDBAutoConfig;
import org.jxch.capital.influx.point.InfluxPoints;
import org.jxch.capital.influx.repository.DefaultInfluxRepository;
import org.jxch.capital.influx.repository.InfluxFluxQuery;
import org.jxch.capital.influx.repository.InfluxRepository;
import org.jxch.capital.influx.util.StringU;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class InfluxRepositoryHandler implements InvocationHandler {
    private final DefaultInfluxRepository<?> repository;
    private final InfluxDBClient influxDBClient;
    private final InfluxDBAutoConfig config;
    private final Class<?> pointClazz;

    public InfluxRepositoryHandler(InfluxDBClient influxDBClient, InfluxDBAutoConfig config, Class<?> pointClazz) {
        this.influxDBClient = influxDBClient;
        this.config = config;
        this.pointClazz = pointClazz;
        this.repository = new DefaultInfluxRepository<>(pointClazz, influxDBClient, config);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(InfluxRepository.class)) {
            return method.invoke(repository, args);
        }

        if (method.isDefault()) {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);

            // 对于非public的类，其模式应该是MethodHandles.Lookup.PRIVATE
            Class<?> declaringClass = method.getDeclaringClass();
            return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        }

        if (!AnnotationUtil.hasAnnotation(method, InfluxFluxQuery.class)) {
            throw new UnsupportedOperationException("必须指定 InfluxFluxQuery 注解");
        }

        String flux = StringU.parameterExpression(AnnotationUtil.getAnnotation(method, InfluxFluxQuery.class).flux(), args);
        List<FluxTable> fluxTables = influxDBClient.getQueryApi().query(flux, config.getOrg());
        return InfluxPoints.toPointDto(fluxTables, pointClazz);
    }

}
