package com.smile67.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;

/**
 * 短信发送工具类
 * @author smile67~
 * @Date: 2023/10/26 - 10 - 26 - 23:45
 * @Description: com.smile67.utils
 * @version: 1.0
 */
public class SMSUtils {
        /**
         * 发送短信
         * @param signName 签名
         * @param templateCode 模板
         * @param phoneNumbers 手机号
         * @param param 参数
         */
        public static void sendMessage(String signName, String templateCode,String phoneNumbers,String param){
            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5tGiaPH1QubctUjCBhxF", "3wFofabbXghlyMy1IeDZhn38Mz5rAk");
            IAcsClient client = new DefaultAcsClient(profile);
            SendSmsRequest request = new SendSmsRequest();
            request.setSysRegionId("cn-hangzhou");
            request.setPhoneNumbers(phoneNumbers);
            // 店铺点评登录验证码
            request.setSignName(signName);
            //SMS_278280112
            request.setTemplateCode(templateCode);
            request.setTemplateParam("{\"code\":\""+param+"\"}");
            try {
                SendSmsResponse response = client.getAcsResponse(request);
                System.out.println("短信发送成功");
            }catch (ClientException e) {
                e.printStackTrace();
            }
        }
}
