package com.dongyimai.sellergoods.service.impl;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.dongyimai.entity.Result;
import com.dongyimai.group.Goods;
import com.dongyimai.mapper.*;

import com.dongyimai.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.dongyimai.pojo.TbGoodsExample.Criteria;
import com.dongyimai.sellergoods.service.GoodsService;

import com.dongyimai.entity.PageResult;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;




	@Override
	public List<TbItem> findItemByGoodsIdAndStatus(Long[] goodsIds, String status) {
		//当商品审核通过后 查询该商品对应规格数据,并将启用的规格数据倒入到solr中
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(example);

	}

	@Override
	public void updateStatus(Long[] ids,String status) {
		for (Long id : ids) {
			//根据商品id获取商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			if (goods != null) {
				//修改商品状态
				goods.setAuditStatus(status);
				//更新商品信息到数据库
				goodsMapper.updateByPrimaryKey(goods);
			}
		}
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//1、插入goods表
		goodsMapper.insert(goods.getTbGoods());
		//2、插入goodsDesc表
		//2.1、 goodsDesc 赋值主键
		goods.getTbGoodsDesc().setGoodsId(goods.getTbGoods().getId());
		goodsDescMapper.insert(goods.getTbGoodsDesc());

		//如果商品有规格那么拼接spec 如果没有规格的商品 做一条假规格数据
		saveItemList(goods);

	}

	public void setItemValues(Goods goods,TbItem tbItem){
		//3.1查询品牌
		TbBrand brand =brandMapper.selectByPrimaryKey(goods.getTbGoods().getBrandId());
		tbItem.setBrand(brand.getName());
		//3.2查询卖家
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getTbGoods().getSellerId());
		tbItem.setSeller(seller.getName());
		//3.3查询分类
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getTbGoods().getCategory3Id());
		tbItem.setCategory(itemCat.getName());
		tbItem.setCategoryid(itemCat.getId());

		//3.5 存sku图片 取图片列表中的第一张图片
		List<Map> imgList = JSON.parseArray(goods.getTbGoodsDesc().getItemImages(),Map.class);
		tbItem.setImage(imgList.get(0).get("url")+"");

		tbItem.setGoodsId(goods.getTbGoods().getId());//商品SPU编号
		tbItem.setSellerId(goods.getTbGoods().getSellerId());//商家编号
		tbItem.setCategoryid(goods.getTbGoods().getCategory3Id());//商品分类编号（3级）
		tbItem.setCreateTime(new Date());//创建日期
		tbItem.setUpdateTime(new Date());//修改日期
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){

		goods.getTbGoods().setAuditStatus("0");//设置未申请状态:如果是经过修改的商品，需要重新设置状态

		//修改商品表
		goodsMapper.updateByPrimaryKey(goods.getTbGoods());
		//修该商品规格表
		goodsDescMapper.updateByPrimaryKey(goods.getTbGoodsDesc());
		//先删除item表 再新增
		//删除
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getTbGoods().getId());
		itemMapper.deleteByExample(example);
		//新增
		saveItemList(goods);//插入商品SKU列表数据

	}

	private void saveItemList(Goods goods){
		if("1".equals(goods.getTbGoods().getIsEnableSpec())){
			//3、新增sku表格
			for (TbItem tbItem : goods.getItemList()) {
				//3.4 拼接标题
				String title = goods.getTbGoods().getGoodsName();
				Map<String,Object> map = JSON.parseObject(tbItem.getSpec());
				for(Map.Entry<String,Object> entry : map.entrySet()){
					title += entry.getKey() +" " + entry.getValue();
				}
				tbItem.setTitle(title);
				//存基本基本信息
				setItemValues(goods,tbItem);
				itemMapper.insert(tbItem);
			}

		}else{
			TbItem tbItem = new TbItem();
			//没有规格 就用名字做标题
			String title = goods.getTbGoods().getGoodsName();
			tbItem.setTitle(title);
			//假规格 避免脏数据
			tbItem.setPrice(goods.getTbGoods().getPrice());
			tbItem.setNum(99999);
			tbItem.setStatus("1");
			tbItem.setIsDefault("1");
			tbItem.setSpec("{}");

			setItemValues(goods,tbItem);
			itemMapper.insert(tbItem);
		}
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods =new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setTbGoods(tbGoods);
		goods.setTbGoodsDesc(tbGoodsDesc);
		//查询sku
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
//			goodsMapper.deleteByPrimaryKey(id);//真删除
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);

			//当商品删除后 需要将item表中的商品状态置为0 以免删除的商品 能被前台查询到 发生交易
			List<TbItem> itemList = findItemByGoodsIdAndStatus(ids,"1");
			for (TbItem tbItem : itemList) {
				tbItem.setStatus("0");
				itemMapper.updateByPrimaryKey(tbItem);
			}

		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusEqualTo(goods.getAuditStatus());
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
				criteria.andIsDeleteEqualTo("0");

		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
