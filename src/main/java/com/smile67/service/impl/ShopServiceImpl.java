package com.smile67.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smile67.dto.Result;
import com.smile67.entity.Shop;
import com.smile67.mapper.ShopMapper;
import com.smile67.service.IShopService;
import com.smile67.utils.CacheClient;
import com.smile67.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.smile67.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author smile67
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private CacheClient cacheClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        // 缓存穿透
        // Shop shop = queryWithPassThrough(id);
//        Shop shop = cacheClient
//                .queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, id2 -> getById(id2), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 互斥锁解决缓存击穿
//        Shop shop = queryWithMutex(id);
        Shop shop = cacheClient
                .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);

        // 逻辑过期字段解决缓存击穿
        // Shop shop = queryWithLogicalExpire(id);
        if (shop == null) {
            return Result.fail("商铺不存在");
        }

        // 6.返回
        return Result.ok(shop);
    }

    /**
     * 缓存重建执行器
     */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 逻辑过期解决缓存击穿问题
     *
     * @param id 商铺id
     * @return 店铺信息
     */
    public Shop queryWithLogicalExpire(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // 1.根据id从redis中查询店铺
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否命中
        if (StrUtil.isBlank(shopJson)) {
            // 3. 未命中，直接返回空
            return null;
        }
        // 4.命中，需要先将json反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        Shop shop = JSONUtil.toBean(data, Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 未过期，直接返回店铺信息
            return shop;
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
                return shop;
            }
            // 6.4 成功，开启独立的线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 本来应该设置成30min 方便测试设置成20s
                    // 期待缓存过期后，进行缓存重建，测试是否触发安全问题，方便观察效果
                    saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        // 6.4 返回过期的商铺信息
        return shop;
    }

    /**
     * 互斥锁解决缓存击穿问题
     *
     * @param id 商铺id
     * @return 商铺信息
     */
    public Shop queryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // 1.根据id从redis中查询店铺
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.命中，
        // 2.1 有值
        if (StrUtil.isNotBlank(shopJson)) {
            // isNotBlank:
            // null->false
            // ""  ->false
            // \t\n->false
            // abc ->true √
            //直接返回商品信息
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 2.2 空字符串
        if (shopJson != null) {
            // 返回一个错误信息
            return null;
        }
        // 2.3 null（即，未命中）

        // 3. 未命中，实现缓存重建
        String lockKey = null;
        Shop shop;
        try {
            // 3.1 获取互斥锁
            lockKey = LOCK_SHOP_KEY + id;
            boolean isLock = tryLock(lockKey);
            // 3.2 判断是否获取到互斥锁
            if (!isLock) {
                // 3.3 失败，休眠并重试
                Thread.sleep(50);
                // 重新查询(使用递归实现)
                return queryWithMutex(id);
            }
            // 3.4 成功
            // 3.4.1 再次检测 redis 缓存是否存在，若存在则不需要重建缓存
            String cacheShopJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(cacheShopJson)) {
                // 不为空，直接返回
                return BeanUtil.toBean(cacheShopJson, Shop.class);
            }
            // 3.4.2 根据 id 直接查询数据库
            //热点key满足两个条件 1.高并发 2.缓存重建的时间比较久√
            // 模拟重建延时，延时越高，并发并发出现的线程也越多-->检验锁的可靠性
            shop = getById(id);
            // 4.不存在，返回错误信息
            if (shop == null) {
                // 将空值写入redis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                // 返回错误信息
                return null;
            }
            // 5.存在，将店铺信息写入到Redis中
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        } finally {
            // 6. 释放互斥锁
            unlock(lockKey);
        }
        // 7. 返回
        return shop;
    }

    /**
     * 缓存穿透
     *
     * @param id 商铺id
     * @return 商铺
     */
    public Shop queryWithPassThrough(Long id) {
        // 1.根据id从redis中查询店铺
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        // 2.命中，
        // 2.1 有值
        if (StrUtil.isNotBlank(shopJson)) {
            // isNotBlank:
            // null->false
            // ""  ->false
            // \t\n->false
            // abc ->true
            return BeanUtil.toBean(shopJson, Shop.class);
        }
        // 2.2 空字符串
        if (shopJson != null) {
            // 返回一个错误信息
            return null;
        }
        // 2.3 null
        // 3.未命中，直接查询数据库
        Shop shop = getById(id);
        // 4.不存在，返回错误信息
        if (shop == null) {
            // 将空值写入redis
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }
        // 5.存在，将店铺信息写入到Redis中
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 6.返回
        return shop;
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

    /**
     * 缓存重建 数据预热
     *
     * @param id            商铺id
     * @param expireSeconds 逻辑过期时间
     */
    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
        // 1.查询店铺数据
        Shop shop = getById(id);
        // 模拟缓存延迟,延迟越长越荣誉出现线程安全问题
        Thread.sleep(200);
        // 2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 3.写入Redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));

    }

    /**
     * @param shop 店铺实体
     * @return 统一封装Result类
     * 当redis删除失败的时候，回滚不让数据库进行更新
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1. 更新数据库
        updateById(shop);
        // 2. 删除Redis中的缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }
}
