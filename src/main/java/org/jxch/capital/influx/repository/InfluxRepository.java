package org.jxch.capital.influx.repository;


import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public interface InfluxRepository<T> {

    void write(List<T> points);

    List<T> queryByTagExampleAndTimeBetween(T example, Date startTime, Date endTime);

    void deletePointsByTimeBetween(Date startTime, Date endTime);

    void deletePointByTime(@NotNull Date time);

}
