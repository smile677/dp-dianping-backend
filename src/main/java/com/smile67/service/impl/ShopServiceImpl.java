package com.smile67.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smile67.dto.Result;
import com.smile67.entity.Shop;
import com.smile67.mapper.ShopMapper;
import com.smile67.service.IShopService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        // 1.根据id从redis中查询店铺
        String shopJson = stringRedisTemplate.opsForValue().get("cache:shop:" + id);
        // 2.命中，直接返回店铺信息
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = BeanUtil.toBean(shopJson, Shop.class);
            Result.ok(shop);
        }
        // 3.未命中，直接查询数据库
        Shop shop = getById(id);
        // 4.不存在，返回错误信息
        if (shop == null) {
            return Result.fail("店铺不存在！");
        }
        // 5.存在，将店铺信息写入到Redis中
        stringRedisTemplate.opsForValue().set("cache:shop:"+ id, JSONUtil.toJsonStr(shop));
        // 6.返回
        return Result.ok(shop);
    }
}
