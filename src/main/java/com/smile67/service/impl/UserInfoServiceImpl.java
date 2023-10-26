package com.smile67.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smile67.entity.UserInfo;
import com.smile67.mapper.UserInfoMapper;
import com.smile67.service.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author smile67
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
