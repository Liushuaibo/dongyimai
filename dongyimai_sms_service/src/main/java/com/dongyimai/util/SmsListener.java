package com.dongyimai.util;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component("smsListener")
public class SmsListener implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    @Override
    public void onMessage(Message message) {
        if(message instanceof MapMessage){

            MapMessage map=(MapMessage)message;

            try {
                System.out.println("电话号:"+map.getString("mobile")+"----code:"+map.getString("code"));
                //向手机发送验证码
                HttpResponse response = smsUtil.sendSms(map.getString("mobile"), map.getString("code"));

                // 结果是 00000 则为正常
                System.out.println("短信发送完成---状态码data:"+response.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
