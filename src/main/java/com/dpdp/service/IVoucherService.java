package com.dpdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dpdp.dto.Result;
import com.dpdp.entity.Voucher;

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
