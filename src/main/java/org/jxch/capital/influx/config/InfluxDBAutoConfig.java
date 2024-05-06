package org.jxch.capital.influx.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ComponentScan("org.jxch.capital.influx")
public class InfluxDBAutoConfig {
    public final static String INFLUX_SEMAPHORE = "INFLUX_SEMAPHORE";
    public final static String INFLUX_CLIENT = "INFLUX_CLIENT";
    @Value("${capital.influx.url:http://localhost:8086}")
    private String url;
    @Value("${capital.influx.username}")
    private String username;
    @Value("${capital.influx.password}")
    private String password;
    @Value("${capital.influx.token}")
    private String token;
    @Value("${capital.influx.org}")
    private String org;
    @Value("${capital.influx.bucket}")
    private String bucket;

    @Bean(INFLUX_CLIENT)
    @ConditionalOnMissingBean(InfluxDBClient.class)
    public InfluxDBClient influxDBClient() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(url)
                .bucket(bucket)
                .org(org)
                .authenticateToken(token.toCharArray())
                .build();
        return InfluxDBClientFactory.create(options);
    }

}
