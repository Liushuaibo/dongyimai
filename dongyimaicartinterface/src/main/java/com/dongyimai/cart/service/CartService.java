package com.dongyimai.cart.service;

import com.dongyimai.entity.Cart;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */

    public List<Cart> addGoodsToCart(List<Cart> cartList,Long itemId,Integer num);

    public List<Cart> findCartListFromRedis(String username);

    public void saveCartToRedis(String username,List<Cart> cartList);

    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
