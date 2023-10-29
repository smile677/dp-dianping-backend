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
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
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
            Shop shop = BeanUtil.toBean(shopJson, Shop.class);
            Result.ok(shop);
        }
        // 2.2 空字符串
        if (shopJson != null) {
            // 返回一个错误信息
            return Result.fail("缓存中店铺信息不存在");
        }
        // 2.3 null
        // 3.未命中，直接查询数据库
        Shop shop = getById(id);
        // 4.不存在，返回错误信息
        if (shop == null) {
            // 将空值写入redis
                stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "", CACHE_NULL_TTL,TimeUnit.MINUTES);
            // 返回错误信息
            return Result.fail("数据库中店铺信息不存在！");
        }
        // 5.存在，将店铺信息写入到Redis中
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 6.返回
        return Result.ok(shop);
    }
    @Override
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
