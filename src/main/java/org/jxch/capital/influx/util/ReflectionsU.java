package org.jxch.capital.influx.util;

import cn.hutool.core.annotation.AnnotationUtil;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jxch.capital.influx.point.InfluxPointField;
import org.jxch.capital.influx.point.InfluxPointTag;
import org.jxch.capital.influx.point.InfluxPointTime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReflectionsU {

    public static boolean hasInfluxPointTagAlias(Field field) {
        return !Objects.requireNonNull(AnnotationUtil.getAnnotation(field, InfluxPointTag.class)).alias().isBlank();
    }

    public static String getInfluxPointTagAlias(Field field) {
        return Objects.requireNonNull(AnnotationUtil.getAnnotation(field, InfluxPointTag.class)).alias();
    }

    public static InfluxPointField getInfluxPointField(Field field) {
        return AnnotationUtil.getAnnotation(field, InfluxPointField.class);
    }

    public static boolean hasInfluxPointField(Field field) {
        return Objects.nonNull(getInfluxPointField(field));
    }

    public static boolean hasInfluxPointFieldAlias(Field field) {
        return AnnotationUtil.hasAnnotation(field, InfluxPointField.class) &&
                !Objects.requireNonNull(AnnotationUtil.getAnnotation(field, InfluxPointField.class)).alias().isBlank();
    }

    public static String getInfluxPointFieldAlias(Field field) {
        return Objects.requireNonNull(AnnotationUtil.getAnnotation(field, InfluxPointField.class)).alias();
    }

    public static boolean hasInfluxPointTime(Field field) {
        return AnnotationUtil.hasAnnotation(field, InfluxPointTime.class);
    }

    public static boolean isInfluxPointField(Field field) {
        return (hasInfluxPointField(field) && !getInfluxPointField(field).ignore())
                || (!hasInfluxPointField(field) && !hasInfluxPointTime(field));
    }

    @SneakyThrows
    public static Object getFieldValueNotNull(Object object, Field field) {
        field.setAccessible(true);
        return Objects.requireNonNull(field.get(object));
    }

    @SneakyThrows
    public static Object getFieldValueNotNull(Object object, Field field, String msg) {
        try {
            return getFieldValueNotNull(object, field);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(msg);
        }
    }

    @SneakyThrows
    public static Object getFieldValueNullable(Object object, Field field) {
        field.setAccessible(true);
        return field.get(object);
    }

    @SneakyThrows
    public static <T> void setFieldValue(@NotNull T obj, String fieldName, Object value) {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @SneakyThrows
    public static <T> T newInstance(@NotNull Class<T> clazz) {
        return clazz.getDeclaredConstructor().newInstance();
    }

    public static List<Class<?>> getGenericsReturnType(@NotNull Method method) {
        List<Class<?>> types = new ArrayList<>();
        Type returnType = method.getGenericReturnType();

        if (returnType instanceof ParameterizedType) {
            for (Type typeArgument : ((ParameterizedType) returnType).getActualTypeArguments()) {
                types.add((Class<?>) typeArgument);
            }
        }

        return types;
    }

    public static Class<?> getSingleGenericReturnType(@NotNull Method method) {
        return getGenericsReturnType(method).get(0);
    }

}
