package com.smile67.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smile67.entity.SeckillVoucher;
import com.smile67.mapper.SeckillVoucherMapper;
import com.smile67.service.ISeckillVoucherService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author smile67
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}
