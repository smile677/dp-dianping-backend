package com.smile67.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smile67.dto.Result;
import com.smile67.entity.SeckillVoucher;
import com.smile67.entity.VoucherOrder;
import com.smile67.mapper.VoucherOrderMapper;
import com.smile67.service.ISeckillVoucherService;
import com.smile67.service.IVoucherOrderService;
import com.smile67.utils.RedisIdWorker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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

    /**
     * 设计到两张表：seckillVoucher 和 VoucherOrder
     *
     * @param voucherId 优惠券id
     * @return 统一通用返回类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        // 扣减库存
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
        //Long userId = UserHolder.getUser().getId();
        // voucherOrder.setUserId(userId);
        // TODO JMeter测试使用 正常使用的时候换成上面的代码
        voucherOrder.setUserId(1010L);
        save(voucherOrder);
        // 返回订单id
        return Result.ok(orderId);
    }
}
