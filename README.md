# capital-influxdb-spring-boot-starter
influxdb集成springboot

## 代码示例
0. 导入依赖
```xml
<dependency>
    <groupId>io.github.jxch</groupId>
    <artifactId>capital-influxdb-spring-boot-starter</artifactId>
    <version>3.2.5-alpha.1</version>
</dependency>
```
1. 配置influxdb链接
```yaml
capital:
  influx:
    url: http://localhost:48086
    token: 960135cf-e65e-4a35-83a3-3205b91e4f06
    org: capital-influxdb-spring-boot-starter
    bucket: capital-influxdb-spring-boot-starter
    username: capital-influxdb-spring-boot-starter
    password: capital-influxdb-spring-boot-starter
```
2. 配置Repository接口的包扫描路径
```java
@InfluxRepositoryScan(basePackages = {"org.jxch.capital.influx.repository"})
```
3. 在包扫描路径内定义Repository接口（支持自定义的 flux 语句；同时内置默认JPA形式的增查删）
```java
package org.jxch.capital.influx.repository;

import java.util.List;

public interface TestRepository extends InfluxRepository<TestPoint> {

    @InfluxFluxQuery(flux = """
            from(bucket: "?1")
                    |> range(start: ?2, stop: ?3)
                    |> filter(fn: (r) => r._measurement == "?4")
                    |> filter(fn: (r) => r.?5 == "?6")
            """)
    List<TestPoint> queryByFlux(String bucket, Long start, Long stop, String measurement, String tagName, String tagValue);


}
```
4. 定义Point实体类
```java
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
```
5. 注入Repository
```java
    @Autowired
    private TestRepository testRepository;
```
6. 测试增删查
```java
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
```
