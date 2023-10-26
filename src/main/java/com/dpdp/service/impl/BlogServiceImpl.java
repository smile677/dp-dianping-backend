package com.dpdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dpdp.entity.Blog;
import com.dpdp.mapper.BlogMapper;
import com.dpdp.service.IBlogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author smile67
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
