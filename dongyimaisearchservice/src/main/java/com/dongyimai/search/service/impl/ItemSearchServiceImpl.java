package com.dongyimai.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dongyimai.pojo.TbItem;
import com.dongyimai.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    //删除solr索引库
    @Override
    public void deleteByGoodsIds(Long[] ids) {
        System.out.println("删除商品ID"+ids);
        Query query=new SimpleQuery();
        org.springframework.data.solr.core.query.Criteria criteria=
                new org.springframework.data.solr.core.query.Criteria("item_goodsid").in(ids);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    @Override
    public void importList(List<TbItem> itemList) {

        for (TbItem tbItem : itemList) {
            //规格转型
            Map specMap = JSON.parseObject(tbItem.getSpec());
            tbItem.setSpecMap(specMap);
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }


    //3、查询规格和品牌
    public Map searchBrandAndSpec(String category){

        System.out.println("category : " + category);
        Map map = new HashMap();
        //1、从redis中找到 分类 对应的 typeId
        Long typeId = (Long)redisTemplate.boundHashOps("itemCat").get(category);
        System.out.println("typeId : "  + typeId);
        //2、根据typeId 查询品牌和规格
        if(typeId != null){
            //2.1 取品牌
            List<Map> brandList = (List<Map>)redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            //2.2 取规格
            List<Map> specList = (List<Map>)redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }

        return map;

    }

    //2、查询分类
    public List searchCategoryList(Map searchMap){
        //1、定义查询结果对象
        List list = new ArrayList();
        //2、定义solr查询对象
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //3、拼接分组对象
        GroupOptions options = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(options);
        //4、执行查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query,TbItem.class);
        //5、获取分组
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        //6、获取分组kv
        Page<GroupEntry<TbItem>> pageEntry = groupResult.getGroupEntries();
        //7、获取分组集合
        List<GroupEntry<TbItem>> pageList = pageEntry.getContent();
        //8、循环遍历 取分组的值 放入返回的结果对象中
        for (GroupEntry<TbItem> tbItemGroupEntry : pageList) {
            list.add(tbItemGroupEntry.getGroupValue());
        }
        //9、返回分类集合
        return list;
    }

    //1、查询高亮
    //根据关键字查询，对查询的结果进行高亮
    private Map searchList(Map searchMap){
        Map map=new HashMap();

        //1、创建一个支持高亮查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //2、设定需要高亮处理字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        //3、设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='font-size: 15px ;color: red;'>");
        //4、设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //5、关联高亮选项到高亮查询器对象
        query.setHighlightOptions(highlightOptions);

        //6、设定查询条件 根据关键字查询
        //创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联查询条件到查询器对象
        query.addCriteria(criteria);

        //二、过滤分类
        if(!"".equals(searchMap.get("category"))){
            //创建过滤条件
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            //装载过滤条件
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            //将过滤条件加入在查询条件中
            query.addFilterQuery(filterQuery);
        }


        //三、过滤品牌
        if(!"".equals(searchMap.get("brand"))){
            //创建过滤条件
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            //装载过滤条件
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            //将过滤条件加入在查询条件中
            query.addFilterQuery(filterQuery);
        }

        
        //四、过滤规格
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap = (Map)searchMap.get("spec");
            for(Map.Entry<String,String> entry : specMap.entrySet()){
                //item_spec_网络 移动3G
                Criteria criteria1 = new Criteria("item_spec_"+entry.getKey()).is(entry.getValue());
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }
        }

        //五、过滤价格
        if(!"".equals(searchMap.get("price"))){

            String price = (String) searchMap.get("price");
            String[] arr = price.split("-");
            String beginPrice = arr[0];
            String endPrice = arr[1];

            if (!"0".equals(beginPrice)){
                //创建过滤条件 gt >  it <
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(beginPrice);
                //装载过滤条件
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                //将过滤条件加入在查询条件中
                query.addFilterQuery(filterQuery);
            }

            if (!"*".equals(endPrice)){
                //创建过滤条件 gt >  it <
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(endPrice);
                //装载过滤条件
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                //将过滤条件加入在查询条件中
                query.addFilterQuery(filterQuery);
            }


        }

        //六、分页
        Integer pageNo = Integer.parseInt(searchMap.get("pageNo")+"");
        if (pageNo == null){
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null){
            pageSize =20;
        }

        //封装起始页
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //七、排序
        String sortFiled = (String) searchMap.get("sortField");
        if (sortFiled != null && !"".equals(sortFiled)){
            String sortValue = (String) searchMap.get("sort");
            if ("ASC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortFiled);
                query.addSort(sort);
            }

            if ("DESC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortFiled);
                query.addSort(sort);
            }
        }


        //7、发出带高亮数据查询请求
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //8、获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        //9、遍历高亮集合
        for(HighlightEntry<TbItem> highlightEntry:highlightEntryList){
            //获取基本数据对象
            TbItem tbItem = highlightEntry.getEntity();
            if(highlightEntry.getHighlights().size()>0&&highlightEntry.getHighlights().get(0).getSnipplets().size()>0) {
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                //高亮结果集合
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对应的高亮结果，设置到商品标题
                tbItem.setTitle(snipplets.get(0));
            }

        }

        map.put("totalPages",page.getTotalPages());
        map.put("total",page.getTotalElements());

        //把带高亮数据集合存放map
        map.put("rows",page.getContent());
        return map;
    }


    @Override
    public Map<String, Object> search(Map searchMap) {

        //多条件查询去空格
        String keywords = (String)searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace("-",""));

        //定义返回值对象
        Map<String,Object> map = new HashMap<String,Object>();
//        //创建solr的查询对象
//        Query query=new SimpleQuery("*:*");
//        //拼接查询条件
//        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
//        //向查询条件中 添加查询条件
//        query.addCriteria(criteria);
//        //执行查询
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//        //将结果封装到map容器中
//        map.put("rows", page.getContent());

        //查询高亮
        map.putAll(searchList(searchMap));

        //查询分组
         List categoryList = searchCategoryList(searchMap);
         map.put("categoryList",categoryList);

        //查询品牌和规格
        if(!"".equals(searchMap.get("category"))){
            map.putAll(searchBrandAndSpec(searchMap.get("category")+""));
        }else{
            if(categoryList.size() > 0){
                map.putAll(searchBrandAndSpec(categoryList.get(0)+""));
            }
        }
        return map;
    }
}
