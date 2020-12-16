package com.dongyimai.sellergoods.service.impl;
import java.util.List;

import com.dongyimai.group.Specification;
import com.dongyimai.mapper.TbSpecificationOptionMapper;
import com.dongyimai.pojo.TbSpecificationOption;
import com.dongyimai.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.dongyimai.mapper.TbSpecificationMapper;
import com.dongyimai.pojo.TbSpecification;
import com.dongyimai.pojo.TbSpecificationExample;
import com.dongyimai.pojo.TbSpecificationExample.Criteria;
import com.dongyimai.sellergoods.service.SpecificationService;

import com.dongyimai.entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		//1、添加规格表
		specificationMapper.insert(specification.getTbSpecification());
		//2、规格选项表
		for(TbSpecificationOption option : specification.getTbSpecificationOptionList()){
			//2.1 选项表对象 添加 外键id
			option.setSpecId(specification.getTbSpecification().getId());
			//2.2 添加选项表
			specificationOptionMapper.insert(option);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//1、修改 规格表
		specificationMapper.updateByPrimaryKey(specification.getTbSpecification());
		//2、修改 规格选项表
		//2.1 先删除
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(specification.getTbSpecification().getId());
		specificationOptionMapper.deleteByExample(example);
		//2.2 后新增
		for (TbSpecificationOption option : specification.getTbSpecificationOptionList()) {
			//2.2.1 装配 外键
			option.setSpecId(specification.getTbSpecification().getId());
			specificationOptionMapper.insert(option);
		}

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//1、返回值对象
		Specification specification = new Specification();
		//2、查询规格表
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		//3、规格选项表
		TbSpecificationOptionExample optionExample = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = optionExample.createCriteria();
		criteria.andSpecIdEqualTo(id);
		List<TbSpecificationOption> optionList = specificationOptionMapper.selectByExample(optionExample);
		//4、封装结果
		specification.setTbSpecification(tbSpecification);
		specification.setTbSpecificationOptionList(optionList);

		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {

		for(Long id:ids){
			//1、先删除 规格选项表
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
			//2、再删除 规格表
			specificationMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbSpecification> selectOptionList() {
		return specificationMapper.selectOptionList();
	}

}
