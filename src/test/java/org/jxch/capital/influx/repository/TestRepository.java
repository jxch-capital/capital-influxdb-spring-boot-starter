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
