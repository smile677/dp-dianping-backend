package com.smile67.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smile67.dto.Result;
import com.smile67.entity.Shop;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author smile67
 */
public interface IShopService extends IService<Shop> {
    /**
     * 根据id查询店铺
     * @param id 商铺id
     * @return 返回统一封装Result类
     */
    Result queryById(Long id);

    /**
     * 根据id修改店铺
     * @param shop 店铺实体
     * @return  返回统一封装Result类
     */
    Result update(Shop shop);

    /**
     * 根据店铺类型查询店铺
     * @param typeId 店铺类型id
     * @param current 当前页
     * @param x 经度
     * @param y 纬度
     * @return 返回统一封装Result类
     */
    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
