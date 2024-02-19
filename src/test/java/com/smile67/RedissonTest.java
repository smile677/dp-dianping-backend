package com.smile67;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.smile67
 * @Author: smile67~
 * @CreateDateTime: 2023/11/8 - 11 - 08 - 15:35
 * @Description: TODO
 * @version: 1.0
 */
@Slf4j
@SpringBootTest(args = "--mpw.key=d1fc9fe46a4b3b6e")
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;
    private RedissonClient redissonClient2;
    private RedissonClient redissonClient3;
    private RLock lock;

    @Autowired
    private IShopService shopService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        RLock lock1 = redissonClient.getLock("order");
        RLock lock2 = redissonClient2.getLock("order");
        RLock lock3 = redissonClient3.getLock("order");

        // 创建联锁 multiLock
        lock = redissonClient.getMultiLock(lock1, lock2, lock3);
    }

    @Test
    void method1() throws InterruptedException {
        // 尝试获取锁
        boolean isLock = lock.tryLock(1L, TimeUnit.SECONDS);
        if (!isLock) {
            log.error("获取锁失败。。。。1");
            return;
        }
        try {
            log.info("获取锁成功。。。。1");
            method2();
            log.info("开始执行业务。。。。1");
        } finally {
            log.warn("准备释放锁。。。。1");
            lock.unlock();
        }
    }

    void method2() {
        // 尝试获取锁
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.error("获取锁失败。。。。2");
            return;
        }
        try {
            log.info("获取锁成功。。。。2");
            log.info("开始执行业务。。。。2");
        } finally {
            log.warn("准备释放锁。。。。2");
            lock.unlock();
        }
    }

    /**
     * 导入附近商铺数据到redis中功能
     */
    @Test
    void loadShopData() {
        // 1. 查询店铺信息
        List<Shop> list = shopService.list();
        // 2. 把商铺分组，按照typeId分组，typeId一致的放在一组
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        // 3. 分批完成写入Redis
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            // 3.1 获取类型id
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY + typeId;
            // 3.2 获取同类型的店铺的集合
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            // 3.3 写入redis geoadd key 经度 维度 member
            for (Shop shop : value) {
                // 这种存入方式比较慢
                // stringRedisTemplate.opsForGeo().add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString());
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),
                        new Point(shop.getX(), shop.getY())
                ));
            }
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }
}
