package com.smile67.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smile67.dto.Result;
import com.smile67.entity.VoucherOrder;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author smile67
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * 秒杀下单
     *
     * @param voucherId 优惠券id
     * @return 统一包装类
     */
    Result seckillVoucher(Long voucherId);

    /**
     * 创建优惠券订单
     *
     * @param voucherId
     * @return
     */
    Result createVoucherOrder(Long voucherId);
}
