package com.smile67.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.smile67.utils.RedisConstants.CACHE_NULL_TTL;
import static com.smile67.utils.RedisConstants.LOCK_SHOP_KEY;

/**
 * @author smile67~
 */
@Slf4j
@Component
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 将任意ava对象序列化为json并存储在string:类型的key中，并且可以设置TTL过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间
     * @param unit  单位
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 将任意Java对象序列化为json并存储在string类型的key中，并且可以设置逻辑过期时间，用于处理缓存击穿问题
     *
     * @param key   键
     * @param value 值
     * @param time  时间
     * @param unit  单位
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑过期
        RedisData redisDate = new RedisData();
        redisDate.setData(value);
        redisDate.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 写入Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisDate));
    }

    /**
     * 根据指定的ky查询缓存，并反序列化为指定类型，利用缓存空值的方式解决缓存穿透问题
     *
     * @param keyPrefix  前缀
     * @param id         id
     * @param type       返回值类型
     * @param dbFallback 调用数据库时使用的方法
     * @param time       时间
     * @param unit       时间单位
     * @param <R>        返回值类型
     * @param <ID>       id类型
     * @return 返回R
     */
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.根据id从redis中查询店铺
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.命中，
        // 2.1 有值
        if (StrUtil.isNotBlank(json)) {
            // isNotBlank:
            // null->false
            // ""  ->false
            // \t\n->false
            // abc ->true
            return JSONUtil.toBean(json, type);
        }
        // 2.2 空字符串
        if (json != null) {
            // 返回一个错误信息
            return null;
        }
        // 2.3 null
        // 3.未命中，直接查询数据库
        R r = dbFallback.apply(id);
        // 4.不存在，返回错误信息
        if (r == null) {
            // 将空值写入redis
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }
        // 5.存在，将店铺信息写入到Redis中
        this.set(key, r, time, unit);
        // 6.返回
        return r;
    }

    /**
     * 缓存重建执行器
     */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 根据指定的ky查询缓存，并反序列化为指定类型，需要利用逻辑过期解决缓存击穿问题
     *
     * @param keyPrefix  前缀
     * @param id         id
     * @param type       返回值类型
     * @param dbFallback 调用数据库时使用的方法
     * @param time       时间
     * @param unit       时间单位
     * @param <R>        返回值类型
     * @param <ID>       id类型
     * @return 返回R
     */
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.根据id从redis中查询店铺
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否命中
        if (StrUtil.isBlank(json)) {
            // 3. 未命中，直接返回空
            return null;
        }
        // 4.命中，需要先将json反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        R r = JSONUtil.toBean(data, type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 未过期，直接返回店铺信息
            return r;
        }
        // 5.2 过期，需要缓存重建
        // 6. 缓存重建
        // 6.1 获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 6.2 判断是否获取锁成功
        if (isLock) {
            // 6.3 获取锁成功应该再次检测redis缓存是否过期，做DoubleCheck
            if (expireTime.isAfter(LocalDateTime.now())) {
                return r;
            }
            // 6.4 成功，开启独立的线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 缓存重建
                    // 查询数据库
                    R r1 = dbFallback.apply(id);
                    // 写入redis
                    this.setWithLogicalExpire(key, r1, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        // 7. 返回过期的商铺信息
        return r;
    }

    /**
     * 获取互斥锁
     *
     * @param key 键
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.MINUTES);
        // 避免 flag 为 null 导致空指针异常
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放互斥锁
     *
     * @param key 键
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

}
