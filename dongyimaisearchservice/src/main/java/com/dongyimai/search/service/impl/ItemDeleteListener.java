package com.dongyimai.search.service.impl;

import com.dongyimai.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] Ids = (Long[]) objectMessage.getObject();

            System.out.println("ItemDeleteListener监听接收到消息..." + Ids);

            itemSearchService.deleteByGoodsIds(Ids);

            System.out.println("成功删除索引库中的记录");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
