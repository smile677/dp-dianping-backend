package com.smile67.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smile67.dto.Result;
import com.smile67.entity.Blog;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author smile67
 */
public interface IBlogService extends IService<Blog> {
    /**
     * 通过博客id查询博客
     *
     * @param id 博客id
     * @return 统一返回结果
     */
    Result getBlougById(Long id);

    /**
     * 查询热榜笔记
     *
     * @param current 当前页
     * @return 统一返回结果
     */
    Result getHotBlog(Integer current);

    /**
     * 点赞与取消点赞
     *
     * @param id 博客笔记id
     * @return 统一返回结果
     */
    Result likeBlog(Long id);

    /**
     * 查询top5
     *
     * @param id 博客id
     * @return 统一返回结果
     */
    Result getBlogLikes(Long id);
}
