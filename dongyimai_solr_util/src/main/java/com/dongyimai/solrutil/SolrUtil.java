package com.dongyimai.solrutil;

import com.alibaba.fastjson.JSON;
import com.dongyimai.mapper.TbItemMapper;
import com.dongyimai.pojo.TbItem;
import com.dongyimai.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemDate(){
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = tbItemMapper.selectByExample(example);

        for (TbItem item : itemList) {
            //规格转型
            Map specMap = JSON.parseObject(item.getSpec());
            item.setSpecMap(specMap);

            System.out.println(item.getId() +" --- "+item.getTitle());
        }

        //同步solr
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("同步完成...");
    }

    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");

        SolrUtil solrUtil = ac.getBean("solrUtil",SolrUtil.class);

        solrUtil.importItemDate();
    }
}
