package com.smile67.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smile67.dto.Result;
import com.smile67.entity.Follow;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author smile67
 */
public interface IFollowService extends IService<Follow> {
    /**
     * 尝试关注用户
     *
     * @param followUserId 关注用户id
     * @param isFollow     是否关注
     * @return 统一返回包装结果
     */
    Result follow(Long followUserId, Boolean isFollow);

    /**
     * 是否关注用户
     *
     * @param followUserId 关注用户id
     * @return 统一返回包装结果
     */
    Result isFollow(Long followUserId);

    /**
     * 共同关注
     * @param id 目标用户
     * @return 统一返回包装结果
     */
    Result followCommons(Long id);
}
