package com.dongyimai.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.dongyimai.entity.PageResult;
import com.dongyimai.mapper.TbBrandMapper;
import com.dongyimai.pojo.TbBrand;
import com.dongyimai.pojo.TbBrandExample;
import com.dongyimai.sellergoods.service.BrandService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> selectOptionList(){
        return brandMapper.selectOptionList();
    }

    @Override
    public List<TbBrand> findAll() {

        return brandMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        //1、pagehelper 添加分页属性
        PageHelper.startPage(pageNum,pageSize);

        Page<TbBrand> page = (Page<TbBrand>)brandMapper.selectByExample(null);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void save(TbBrand brand) {
        brandMapper.insert(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(TbBrand brand) {
        brandMapper.updateByPrimaryKey(brand);
    }

    @Override
    public void dele(Long[] ids) {
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult search(int page, int rows, TbBrand brand) {
        //1、分页属性
        PageHelper.startPage(page,rows);

        //2、拼接条件查询
        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();

        if(brand != null){
            if(brand.getName() != null && brand.getName().length() > 0){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if(brand.getFirstChar()!=null && !"".equals(brand.getFirstChar())){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }

        //3、查询
        Page<TbBrand> pageResult = (Page<TbBrand>)brandMapper.selectByExample(example);

        return new PageResult(pageResult.getTotal(),pageResult.getResult());
    }
}
