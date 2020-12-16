package com.dongyimai.sellergoods.service;

import com.dongyimai.entity.PageResult;
import com.dongyimai.pojo.TbBrand;

import java.util.List;

public interface BrandService {

    public List<TbBrand> findAll();

    public PageResult findPage(int pageNum,int pageSize);

    public void save(TbBrand brand);

    public TbBrand findOne(Long id);

    public void update(TbBrand brand);

    public void dele(Long[] ids);

    public PageResult search(int page, int rows,TbBrand brand);

    public List<TbBrand> selectOptionList();

}
