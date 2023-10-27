package com.smile67.utils;

import com.smile67.dto.UserDTO;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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
    /**
     * 浏览器发送请求 到 DispatcherServlet查询处理器 之后进行拦截
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // return HandlerInterceptor.super.preHandle(request, response, handler);
        // 1. 获取session
        HttpSession session = request.getSession();
        // 2. 获取session中的用户
        UserDTO user = (UserDTO) session.getAttribute("user");
        // 2. 判断用户是否存在
        if (user == null) {
            // 3. 不存在，拦截，返回401状态码（无权访问）
            response.setStatus(401);
            return false;
        }
        // 4. 存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(user);
        // 5.放行
        return true;
    }

    /**
     * 处理器执行之后，视图渲染之前
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 视图渲染之后，浏览器收到响应之前
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        // 移除ThreadLocal中的用户
        UserHolder.removeUser();
    }
}
