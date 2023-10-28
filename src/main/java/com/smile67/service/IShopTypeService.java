package com.smile67.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smile67.dto.Result;
import com.smile67.entity.ShopType;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author smile67
 */
public interface IShopTypeService extends IService<ShopType> {
    /**
     * 根据类型查询店铺s
     * @return
     */
    Result queryList();
}
