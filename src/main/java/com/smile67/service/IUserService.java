package com.smile67.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smile67.dto.LoginFormDTO;
import com.smile67.dto.Result;
import com.smile67.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author smile67
 */
public interface IUserService extends IService<User> {
    /**
     * 发送验证码
     * @param phone
     * @param session
     * @return
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 验证码登录+注册
     * @param loginForm
     * @param session
     * @return
     */
    Result login(LoginFormDTO loginForm, HttpSession session);
}
