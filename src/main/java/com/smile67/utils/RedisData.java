package com.smile67.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 包含逻辑过期字段的店铺信息实体对象
 * @author smile67
 * 继承Shop类的方式具有一定的侵入性(不使用这种方案)
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
