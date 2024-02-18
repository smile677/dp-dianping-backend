package com.smile67.utils;

import lombok.Data;

import java.util.List;

/**
 * 滚动分页实体类
 */
@Data
public class ScrollResult {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
