package com.ljw.yuntubackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 刘佳伟
 * @date 2025/1/23 21:33
 * @Description
 */
@Data
@ConfigurationProperties(value = "oss.client")
@Configuration
public class OssClientConfig {

    private String region;
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    @Bean
    public OSS ossClient(){
        DefaultCredentialProvider credentialsProvider =
                CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId,accessKeySecret);
        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }
}