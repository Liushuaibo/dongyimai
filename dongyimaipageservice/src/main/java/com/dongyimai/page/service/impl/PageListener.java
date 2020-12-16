package com.dongyimai.page.service.impl;

import com.dongyimai.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage= (TextMessage)message;
        try {
            String id = textMessage.getText();
            System.out.println("接收到消息："+id);
            itemPageService.genItemHtml(Long.parseLong(id));

            System.out.println("页面生成完毕...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
