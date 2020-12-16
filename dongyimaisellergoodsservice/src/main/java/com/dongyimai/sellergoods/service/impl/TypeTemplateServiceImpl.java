package com.dongyimai.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.dongyimai.mapper.TbSpecificationOptionMapper;
import com.dongyimai.pojo.TbSpecificationOption;
import com.dongyimai.pojo.TbSpecificationOptionExample;
import com.dongyimai.sellergoods.service.SpecificationOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.dongyimai.mapper.TbTypeTemplateMapper;
import com.dongyimai.pojo.TbTypeTemplate;
import com.dongyimai.pojo.TbTypeTemplateExample;
import com.dongyimai.pojo.TbTypeTemplateExample.Criteria;
import com.dongyimai.sellergoods.service.TypeTemplateService;

import com.dongyimai.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	//将品牌列表规格列表存入到redis中
	public void saveToRedis(){
		//1.查询所有的模板
		List<TbTypeTemplate> typeList = findAll();
		//2.循环遍历
		for (TbTypeTemplate tbTypeTemplate : typeList) {
			//2.1 将品牌放入到redis中
			List<Map> brandList = JSON.parseArray(tbTypeTemplate.getBrandIds(),Map.class);
			redisTemplate.boundHashOps("brandList").put(tbTypeTemplate.getId(),brandList);
			//2.2 将规格放入redis中
			List<Map> specList = findSpecList(tbTypeTemplate.getId());
			redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(),specList);
		}

		System.out.println("品牌和规格列表更新完毕...");
	}


	@Override
	public List<Map> findSpecList(Long id) {
		//1.查询id对象对应模板中的规格
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//2.从模板对象获取规格属性  由于是一个json的字符串 所以需要转型
		//[{"id":27,"text":"网络","options":[{},{}]},{"id":32,"text":"机身内存"},"options":[{},{}]]
		List<Map> list= JSON.parseArray(tbTypeTemplate.getSpecIds(),Map.class);
		//3.遍历规格集合  查询规格对应的属性
		if(list!=null) {
			for (Map map : list) {
				//{"id":27,"text":"网络","options":[]}
				Long specid = new Long((Integer) map.get("id"));
				//根据规格id获取规格选项
				TbSpecificationOptionExample example = new TbSpecificationOptionExample();
				TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
				criteria.andSpecIdEqualTo(specid);
				List<TbSpecificationOption> optionList = specificationOptionMapper.selectByExample(example);
				map.put("options", optionList);
			}
		}
		return list;
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}	
		}

		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);

			//更新品牌和规格的缓存
			saveToRedis();

			return new PageResult(page.getTotal(), page.getResult());
	}
	
}
