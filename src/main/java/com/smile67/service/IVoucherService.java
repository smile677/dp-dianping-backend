package com.smile67.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smile67.dto.Result;
import com.smile67.entity.Voucher;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author smile67
 */
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
