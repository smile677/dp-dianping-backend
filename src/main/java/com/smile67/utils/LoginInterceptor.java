package com.smile67.utils;

import cn.hutool.core.bean.BeanUtil;
import com.smile67.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.smile67.utils.RedisConstants.LOGIN_USER_KEY;
import static com.smile67.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * 登录拦截器
 *
 * @author smile67~
 * @Description: com.smile67.utils
 * @version: 1.0
 */
public class LoginInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

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
        // 1. 从请求头中获取token
        String token = request.getHeader("authorization");
        if (token == null) {
            //  2.token为空 ，拦截，返回401状态码（无权访问）
            response.setStatus(401);
            return false;
        }
        // 3. 基于token获取redis中的用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
        // 4. 判断用户是否存在
        if (userMap.isEmpty()) {
            // 4. 不存在，拦截，返回401状态码（无权访问）
            response.setStatus(401);
            return false;
        }
        // 5. 将查询到的userMap转成userDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 6. 存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);
        // 7.更新有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 8.放行
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
