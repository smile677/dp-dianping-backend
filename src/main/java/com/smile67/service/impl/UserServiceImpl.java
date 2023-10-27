package com.smile67.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smile67.dto.LoginFormDTO;
import com.smile67.dto.Result;
import com.smile67.entity.User;
import com.smile67.mapper.UserMapper;
import com.smile67.service.IUserService;
import com.smile67.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.smile67.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author smile67
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.不符合
            return Result.fail("手机号码格式错误！");
        }
        // 3.符合,生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.保存验证码
        session.setAttribute("code", code);
        // 5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        // SMSUtils.sendMessage("点评网站登录验证码", "SMS_461810192", phone, code);
        // 6.返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 反向校验可以避免if的多层嵌套，钻石型代码
        String phone = loginForm.getPhone();
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 不符合,返回
            return Result.fail("手机号码格式错误！");
        }
        // 2. 校验验证码
        Object cacheCode = session.getAttribute("code");
        String code = loginForm.getCode();
        // 3. 不一致
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
            return Result.fail("手机验证码不正确");
        }
        // 4. 一致，根据手机号查询
        User user = query().eq("phone", phone).one();
        // 5. 判断用户是否存在
        if (user == null) {
            // 6. 不存在，创建新用户
            user = createUserWithPhone(phone);
        }
        // 7. 保存用户信息到session中
        session.setAttribute("user", user);
        // 因为是用cookie和session实现的登录，所以不需要返回登录凭证
        // 基于session登录，session的原理就是cookie,每一个 session都会有要给session_id
        // 第一次请求访问tomcat的时候服务端就会给把session_id塞到到cookie中
        // 后面的请求都会携带session_id的cookie进行请求
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        // 1. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2. 保存用户
        save(user);
        return user;
    }
}
