package com.ljw.yuntubackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan(value = "com.ljw.yuntubackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class YuntuBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuntuBackendApplication.class, args);
    }

}
