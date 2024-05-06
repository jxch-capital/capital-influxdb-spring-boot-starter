package org.jxch.capital.influx.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.jxch.capital.influx.config.InfluxDBAutoConfig;
import org.jxch.capital.influx.config.InfluxRepositoryScan;
import org.jxch.capital.influx.point.InfluxPoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@ActiveProfiles("test")
@InfluxRepositoryScan(basePackages = {"org.jxch.capital.influx.repository"})
@SpringBootTest(classes = InfluxDBAutoConfig.class)
public class TestRepositoryTest {
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private InfluxDBAutoConfig influxDBAutoConfig;

    @Test
    public void write() {
        TestPoint point1 = new TestPoint().setCode("test1").setTime(new Date()).setHigh(1.1).setLow(0.1).setOpen(0.2).setClose(0.9).setVolume(1234.2);
        TestPoint point2 = new TestPoint().setCode("test2").setTime(new Date()).setHigh(2.1).setLow(1.1).setOpen(1.2).setClose(1.9).setVolume(2234.2);
        testRepository.write(List.of(point1, point2));
    }

    @Test
    public void queryByExampleAndTimeBetween() {
        Date start = Date.from(LocalDate.now().plusDays(-1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date end = new Date();
        TestPoint point1 = new TestPoint().setCode("test1");
        List<TestPoint> testPoints = testRepository.queryByTagExampleAndTimeBetween(point1, start, end);
        log.info("{}", testPoints);
    }

    @Test
    public void queryByFlux() {
        Date start = Date.from(LocalDate.now().plusDays(-1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date end = new Date();
        List<TestPoint> testPoints = testRepository.queryByFlux(influxDBAutoConfig.getBucket(), start.getTime(), end.getTime(),
                InfluxPoints.getMeasurement(TestPoint.class), "code", "test2");
        log.info("{}", testPoints);
    }

    @Test
    public void deletePointsByTimeBetween() {
        Date start = Date.from(LocalDate.now().plusDays(-1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date end = new Date();
        testRepository.deletePointsByTimeBetween(start, end);
    }

}
