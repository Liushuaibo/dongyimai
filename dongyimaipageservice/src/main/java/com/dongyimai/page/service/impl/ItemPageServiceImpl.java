package com.dongyimai.page.service.impl;


import com.dongyimai.mapper.TbGoodsDescMapper;
import com.dongyimai.mapper.TbGoodsMapper;
import com.dongyimai.mapper.TbItemCatMapper;
import com.dongyimai.mapper.TbItemMapper;
import com.dongyimai.page.service.ItemPageService;
import com.dongyimai.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    private String pagedir = "d:/item/";

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //1、查询数据库 取得商品数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);

            //2、获取模版对象
            Configuration configuration = freemarkerConfig.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");

            //3、创建map容器 存储数据
            Map modelMap = new HashMap();
            modelMap.put("goods",goods);
            modelMap.put("goodsDesc",goodsDesc);

            //查询分类
            TbItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id());
            TbItemCat itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id());
            TbItemCat itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
            modelMap.put("itemCat1",itemCat1.getName());
            modelMap.put("itemCat2",itemCat2.getName());
            modelMap.put("itemCat3",itemCat3.getName());


            //对规格产品进行查询
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andGoodsIdEqualTo(goodsId);
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            System.out.println(itemList);
            modelMap.put("itemList",itemList);
            System.out.println(modelMap.values());

            //4、创建流 d:/item/345955.html
            FileWriter writer = new FileWriter(pagedir+goodsId+".html");

            //5、模版生成
            template.process(modelMap,writer);

            //6、关流
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void deleteItemHtml(Long[] ids) {

        for (Long id : ids) {
            new File(pagedir+id+".html").delete();
        }
    }


}
