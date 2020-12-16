package com.dongyimai.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dongyimai.entity.PageResult;
import com.dongyimai.entity.Result;
import com.dongyimai.group.Goods;
import com.dongyimai.pojo.TbGoods;
import com.dongyimai.sellergoods.service.GoodsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){

		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		try {

			goods.getTbGoods().setSellerId(name);
			goods.getTbGoods().setAuditStatus("0");
			goods.getTbGoods().setIsDelete("0");//0表示未删除 1表示已删除
			goods.getTbGoods().setIsMarketable("1");//1表示上架 0表示下架
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			//可选逻辑
			//判断当前登录用户是否为商品添加用户 如果是  具有修改权限  如果不是 非法操作
			//获取当前登录人
			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			//查询商品添加的用户
			Goods goods1 = goodsService.findOne(goods.getTbGoods().getId());
			if (!goods1.getTbGoods().getSellerId().equals(name) || !goods.getTbGoods().getSellerId().equals(name)){
				return new Result(false,"非法操作!");
			}

			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){

		//获取当前登录人
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId(name);

		return goodsService.findPage(goods, page, rows);		
	}
	
}
