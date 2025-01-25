package com.ljw.yuntubackend.config;

import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.github.benmanes.caffeine.cache.Cache;

import java.util.concurrent.TimeUnit;

/**
 * @author 刘佳伟
 * @date 2025/1/25 16:46
 * @Description
 */
@Component
public class CaffeineConfig {

    @Bean
    public Cache<String, String> caffeineCache() {
        return Caffeine.newBuilder().initialCapacity(1024)
                .maximumSize(10000L)
                // 缓存 5-10 分钟移除
                .expireAfterWrite(300 + RandomUtil.randomInt(0, 300), TimeUnit.SECONDS)
                .build();
    }

}
