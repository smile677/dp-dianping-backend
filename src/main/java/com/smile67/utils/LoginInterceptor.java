package com.smile67.utils;

import cn.hutool.core.bean.BeanUtil;
import com.smile67.dto.UserDTO;
import com.smile67.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 *
 * @author smile67~
 * @Description: com.smile67.utils
 * @version: 1.0
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // return HandlerInterceptor.super.preHandle(request, response, handler);
        // 1. 获取session
        HttpSession session = request.getSession();
        // 2. 获取session中的用户
        User user = (User) session.getAttribute("user");
        // 2. 判断用户是否存在
        if (user == null) {
            // 3. 不存在，拦截，返回401状态码（无权访问）
            response.setStatus(401);
            return false;
        }
        // 4. 存在，保存用户信息到ThreadLocal
        UserDTO userDTO = new UserDTO();
        BeanUtil.copyProperties(user, userDTO);
        UserHolder.saveUser(userDTO);
        // 5.放行
        return  true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        // 移除ThreadLocal中的用户
        UserHolder.removeUser();
    }




}
