package com.smile67.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.smile67.config
 * @Author: smile67~
 * @CreateDateTime: 2023/11/7 - 11 - 07 - 19:10
 * @description: 配置Redisson客户端
 * @version: 1.0
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedissonClient redissonClient() {
        // 配置类
        Config config = new Config();
        // 添加redis地址
        config.useSingleServer().setAddress("redis://43.136.29.239:6379")/*.setPassword("")*/;
        // 创建客户端
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient2() {
        // 配置类
        Config config = new Config();
        // 添加redis地址
        config.useSingleServer().setAddress("redis://43.136.29.239:6380");
        // 创建客户端
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient3() {
        // 配置类
        Config config = new Config();
        // 添加redis地址
        config.useSingleServer().setAddress("redis://43.136.29.239:6381");
        // 创建客户端
        return Redisson.create(config);
    }

}
