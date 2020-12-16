package com.dongyimai.content.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.dongyimai.mapper.TbContentMapper;
import com.dongyimai.pojo.TbContent;
import com.dongyimai.pojo.TbContentExample;
import com.dongyimai.pojo.TbContentExample.Criteria;
import com.dongyimai.content.service.ContentService;

import com.dongyimai.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<TbContent> findContentList(Long categoryId) {

		//1、从redis缓存中取值
		List<TbContent> contentList = (List<TbContent>)redisTemplate.boundHashOps("contentList").get(categoryId);

		System.out.println("contentList : " + contentList);

		//2、如果contentList 为null 去mysql中取值
		if(contentList == null){
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);
			criteria.andStatusEqualTo("1");
			contentList = contentMapper.selectByExample(example);
			//2.1 将数据查询回来 放入到 redis中 下次取值就不需要mysql进行攻击
			redisTemplate.boundHashOps("contentList").put(categoryId,contentList);
			System.out.println("从mysql中取出的数据...");

		}else{
			System.out.println("从redis中取出的数据...");
		}
		return contentList;
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清除缓存
		redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){

		//content	id=18 	categoryId=1 首页轮播 ==> content	id=18 	categoryId=2 今日推荐
		//redis 1-List 2-list2
		//1、先查询原来 id=18 对应的分类 categoryId = 1
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		//删除原来该对象对应categoryId
		redisTemplate.boundHashOps("contentList").delete(categoryId);

		//2、判断要修改的categoryId 和 原来的categoryId 是否为同一个
		if(categoryId != content.getCategoryId()){
			redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());
		}

		//3、修改mysql 一定要放在 查询原来的 categoryId 之后
		contentMapper.updateByPrimaryKey(content);

		redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			contentMapper.deleteByPrimaryKey(id);
			//清除缓存
			redisTemplate.boundHashOps("contentList").delete(id);
		}
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
