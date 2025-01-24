package com.ljw.yuntubackend.config;

import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "tos.client")
@Configuration
public class TosClientConfig {
    String endpoint ;
    String region ;
    String accessKey ;
    String secretKey ;
    String bucketName ;

    @Bean
    public TosClient tosClient(){
        TOSV2 tosv2 = new TOSV2ClientBuilder().build(region, endpoint, accessKey, secretKey);
        return new TosClient(tosv2, bucketName);
    }

    @AllArgsConstructor
    @Data
    public static class TosClient{
        TOSV2 tosv2 ;
        String bucketName;
    }
}
