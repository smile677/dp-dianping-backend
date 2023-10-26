package com.smile67.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smile67.dto.Result;
import com.smile67.entity.User;
import com.smile67.mapper.UserMapper;
import com.smile67.service.IUserService;
import com.smile67.utils.RegexUtils;
import com.smile67.utils.SMSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

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
        SMSUtils.sendMessage("点评网站登录验证码", "SMS_461810192", phone, code);
        // 6.返回ok
        return Result.ok();
    }
}
