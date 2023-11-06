package com.smile67.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.smile67.utils
 * @Author: smile67~
 * @CreateDateTime: 2023/11/6 - 11 - 06 - 10:26
 * @Description: TODO
 * @Version: 1.0
 */
public class SampleRedisLock implements ILock {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private String name;

    public SampleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public static final String KEY_PREFIX = "lock:";

    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程标识
        long threadId = Thread.currentThread().getId();
        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId + "", timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        // 释放锁
        stringRedisTemplate.delete(KEY_PREFIX + name);
    }
}
