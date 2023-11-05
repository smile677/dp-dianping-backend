package com.smile67;

import com.baomidou.mybatisplus.core.toolkit.AES;
import com.smile67.entity.Shop;
import com.smile67.service.impl.ShopServiceImpl;
import com.smile67.utils.CacheClient;
import com.smile67.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.smile67.utils.RedisConstants.CACHE_SHOP_KEY;

@SpringBootTest(args = "--mpw.key=d1fc9fe46a4b3b6e")
class DpDianPingApplicationTests {
    /**
     * 数据库信息加密，防止公司人员跑路多年后删库
     */
    @Test
    void contextLoads() {
        // 1.生成 16 位随机 AES 秘钥
        String randomKey = AES.generateRandomKey();
        System.out.println("randomKey = " + randomKey);
        // 2.利用秘钥对用户名进行加密
        String username = AES.encrypt("root", randomKey);
        System.out.println("username = " + username);
        // 3.利用秘钥对用户名密码进行加密
        String password = AES.encrypt("123456", randomKey);
        System.out.println("password = " + password);
        // 4.利用秘钥对远程服务器地址进行加密
        String host = AES.encrypt("43.136.29.239", randomKey);
        System.out.println("host = " + host);
        // 5.利用秘钥对远程redis密码进行加密
//            String password = AES.encrypt("123456", randomKey);
//            System.out.println("password = " + password);
        // 6.利用秘钥对SMS服务的accessKeyId进行加密
        String accessKeyId = AES.encrypt("LTAI5tGiaPH1QubctUjCBhxF", randomKey);
        System.out.println("accessKeyId = " + accessKeyId);
        // 7.利用秘钥对SMS服务的secret进行加密
        String secret = AES.encrypt("3wFofabbXghlyMy1IeDZhn38Mz5rAk", randomKey);
        System.out.println("secret = " + secret);

        // 解密用户名和密码
        String decryptedAccessKeyId = AES.decrypt(accessKeyId, randomKey);
        System.out.println(decryptedAccessKeyId);
        String decryptedSecret = AES.decrypt(secret, randomKey);
        System.out.println(decryptedSecret);
    }

    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopServiceImpl shopService;

    @Test
    void testSaveShop() throws InterruptedException {
        shopService.saveShop2Redis(1L, 10L);
    }

    /**
     * 模拟后台存入热点key（缓存重建，数据预热）
     *
     * @throws InterruptedException 中断异常
     */
    @Test
    void testSaveShop2() throws InterruptedException {
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY + 1L, shop, 10L, TimeUnit.MINUTES);
    }

    @Resource
    private RedisIdWorker redisIdWorker;

    private ExecutorService es = Executors.newFixedThreadPool(500);

    /**
     * 测试并发情况下id生成的性能
     */
    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        // 定义任务
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id=" + id);
            }
            latch.countDown();
        };
        //  计时
        long begin = System.currentTimeMillis();
        //提交任务
        for (int i = 0; i < 300; i++) {
            //线程池是异步的
            es.submit(task);
        }
        // 等待所有的线程结束为止
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }
}
