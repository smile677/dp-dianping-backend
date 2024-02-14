package com.smile67.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smile67.dto.Result;
import com.smile67.entity.VoucherOrder;
import com.smile67.mapper.VoucherOrderMapper;
import com.smile67.service.ISeckillVoucherService;
import com.smile67.service.IVoucherOrderService;
import com.smile67.utils.RedisIdWorker;
import com.smile67.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author smile67
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;

    public static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    // 加载类的时候就提前加载好脚本，避免执行方法时才执行加载脚本使用IO影响性能
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * 表明在类加载完成后就马上执行
     */
    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    /**
     * 线程池需要执行的业务
     */
    private class VoucherOrderHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // 1. 获取队列中的订单信息
                    VoucherOrder voucherOrder = orderTasks.take();
                    // 2. 创建订单
                    handleVoucherOrder(voucherOrder);
                } catch (Exception e) {
                    log.debug("处理订单异常：" + e);
                }
            }
        }

        private void handleVoucherOrder(VoucherOrder voucherOrder) {
            // 1.获取用户
            Long userId = voucherOrder.getUserId();
            // 2.创建锁对象
            RLock lock = redissonClient.getLock("lock:order:" + userId);
            // 3.获取锁
            boolean isLock = lock.tryLock();
            // 4.判断是否获取锁成功
            if (!isLock) {
                // 获取锁失败，返回错误或者重试
                log.error("不允许重复下单！");
                return;
            }
            try {
                proxy.createVoucherOrder(voucherOrder);
            } finally {
                // 释放锁
                lock.unlock();
            }
        }
    }

    private IVoucherOrderService proxy;

    /**
     * 秒杀优惠券
     *
     * @param voucherId 优惠券id
     * @return 统一包装类
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 获取用户id
        Long userId = UserHolder.getUser().getId();
        // 1. 执行lua脚本
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString());
        // 2. 判断结果是为0
        int r = result.intValue();
        if (r != 0) {
            // 2.1 不为0，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 2.2  为0 有购买资格，把下单信息保存到阻塞队列
        // 创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //  订单 id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //  代金券 id
        voucherOrder.setVoucherId(voucherId);
        //  用户 id
        voucherOrder.setUserId(userId);
        // 放入阻塞队列
        orderTasks.add(voucherOrder);

        // 3.获取代理对象（事务），方便子对象使用
        proxy = (IVoucherOrderService) AopContext.currentProxy();

        // 4. 返回订单id
        return Result.ok(orderId);
    }

    /**
     * 设计到两张表：seckillVoucher 和 VoucherOrder
     *
     * @param voucherOrder 优惠券id
     * @return 统一通用返回类
     */
    /*
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 查询优惠券信息
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 尚未开始
            return Result.fail("秒杀尚未开始！");
        }
        // 判断秒杀是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 秒杀已经结束
            return Result.fail("秒杀已经结束");
        }
        // 判断库存是否充足
        if (voucher.getStock() <= 0) {
            return Result.fail("库存不足");
        }
        Long userId = UserHolder.getUser().getId();
        // 创建锁对象
        // SampleRedisLock lock = new SampleRedisLock("order:" + userId, stringRedisTemplate);
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        // 获取锁
        //  方便断电调试 1200s ->5s
        // boolean isLock = lock.tryLock(1200L);
        boolean isLock = lock.tryLock();
        // 判断获取锁是否成功
        if (!isLock) {
            //  获取锁失败，返回错误信息
            return Result.fail("不允许重复下单！");
        }
        //  获取锁成功
        try {
            // TODO 代理对象这部分，复习Spring事务失效的几种可能性
            // 获取代理对象（事物）
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            // 释放锁
            lock.unlock();
        }
    }*/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 一人一单, 因为是子线程所以不能使用UserHoulder
        Long userId = voucherOrder.getUserId();

        // 判断订单是否存在
        Integer count = query().eq("user_id", userId).eq("voucher_id", voucherOrder.getVoucherId()).count();
        if (count > 0) {
            // 库存已经存在，返回异常结果
            log.error("用户已经购买过一次！");
            return;
        }

        // 库存不存在 扣减库存
        boolean success = seckillVoucherService.update()
                // set stock = stock -1
                .setSql("stock = stock - 1")
                // where voucher_id = ? and stock = voucher.getStock()
                //.eq("voucher_id", voucherId).eq("stock", voucher.getStock())
                .eq("voucher_id", voucherOrder.getVoucherId()).gt("stock", 0)
                .update();

        if (!success) {
            // 扣减失败
            log.error("库存不足！");
            return;
        }
        save(voucherOrder);

        // 返回订单id
        return;
    }

    /*@Transactional
    @Override
    public Result createVoucherOrder(Long voucherId) {
        // 一人一单
        //  查询订单(根据user_id 和 voucher_id)
        Long userId = UserHolder.getUser().getId();

        //  判断订单是否存在
        Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            // 库存已经存在，返回异常结果
            return Result.fail("用户已经购买过一次了");
        }

        // 库存不存在 扣减库存
        boolean success = seckillVoucherService
                .update()
                // set stock = stock -1
                .setSql("stock = stock - 1")
                // where voucher_id = ? and stock = voucher.getStock()
                //.eq("voucher_id", voucherId).eq("stock", voucher.getStock())
                .eq("voucher_id", voucherId).gt("stock", 0)

                .update();
        if (!success) {
            // 扣减失败
            return Result.fail("库存不足");
        }

        // 创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //  订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //  代金券id
        voucherOrder.setVoucherId(voucherId);
        //  用户id
        voucherOrder.setUserId(userId);
        //  JMeter测试使用 正常使用的时候换成上面的代码
        save(voucherOrder);

        // 返回订单id
        return Result.ok(orderId);
    }*/
}
