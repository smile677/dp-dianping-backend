package com.smile67.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smile67.dto.Result;
import com.smile67.entity.ShopType;
import com.smile67.mapper.ShopTypeMapper;
import com.smile67.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.smile67.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;
import static com.smile67.utils.RedisConstants.CACHE_SHOP_TYPE_TTL;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author smile67
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        // 1. 从redis中查询店铺数据
        List<String> shopTypeListJson = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        // 2. 判断店铺类型是否存在（命中缓存）
        if (CollectionUtil.isNotEmpty(shopTypeListJson)) {
            // 3. 存在,直接返回
            List<ShopType> shopTypeList = shopTypeListJson.stream()
                    .map(item -> JSONUtil.toBean(item, ShopType.class))
                    .sorted(Comparator.comparing(ShopType::getSort))
                    .collect(Collectors.toList());
            return Result.ok(shopTypeList);
        }
        // 4. 不存在,从数据库中查询
        List<ShopType> shopTypeList = lambdaQuery().orderByAsc(ShopType::getSort).list();
        // 5. 没有,返回空集合
        if (CollectionUtil.isEmpty(shopTypeList)) {
            return Result.fail("店铺分类为空");
        }
        // 6. 有,写入redis
        // 使用stream流将bean集合转换为json集合
        shopTypeListJson = shopTypeList.stream()
                .sorted(Comparator.comparing(ShopType::getSort))
                .map(shopType -> JSONUtil.toJsonStr(shopType))
                .collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY, shopTypeListJson);
        stringRedisTemplate.expire(CACHE_SHOP_TYPE_KEY,CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
        // 7. 返回数据
        return Result.ok(shopTypeList);
    }
}
