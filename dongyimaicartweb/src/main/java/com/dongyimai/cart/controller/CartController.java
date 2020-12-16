package com.dongyimai.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.dongyimai.cart.service.CartService;
import com.dongyimai.entity.Cart;
import com.dongyimai.entity.Result;
import com.dongyimai.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){

        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //1.取cookie
        String cartListString = CookieUtil.getCookieValue(request, "cartList","UTF-8");
        //2.如果cartListString 为空 创建一个新的数组
        if (cartListString == null || cartListString.length() <= 0){
            cartListString = "[]";
        }
        //3.json转换
        List<Cart> cookieList = JSON.parseArray(cartListString,Cart.class);

        //用户处于未登录状态返回cookie 的购物车
        if("anonymousUser".equals(username)){
            return cookieList;

        }else{
            //用户处于登录状态 返回redis购物车
            List<Cart> redisList = cartService.findCartListFromRedis(username);
            //假若登录状态 那么取redis购物车之前 需要将redis和cookie购物车进行合并
            if(cookieList.size() > 0 ){
                //将cookieList放第二个参数 有助于提高效率
                redisList = cartService.mergeCartList(redisList,cookieList);
                //清除cookie
                CookieUtil.deleteCookie(request,response,"cookieList");
                //将合并好的购物车 装入到redis中
                cartService.saveCartToRedis(username,redisList);
            }

            return redisList;
        }
    }

    @RequestMapping("/addGoodsToCart")
    public Result addGoodsToCart(Long itemId,Integer num){
        //跨域请求
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        try {
            //1.查询cookie购物车列表
            List<Cart> cartList =findCartList();
            //2.调用添加cookie购物车的方法 最追加新的商品给原购物车 完善购物车
            cartList = cartService.addGoodsToCart(cartList, itemId, num);

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //用户处于未登录状态 将购物车存入 cookie
            if("anonymousUser".equals(username)){
                //3、存入到cookie中
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"utf-8");
            }else{
                cartService.saveCartToRedis(username,cartList);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

}
