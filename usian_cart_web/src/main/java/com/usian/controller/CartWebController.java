package com.usian.controller;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.usian.CartServiceFeign;
import com.usian.feign.ItemServiceFeignClient;
import com.usian.pojo.TbItem;
import com.usian.utils.CookieUtils;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/frontend/cart")
public class CartWebController {

    @Value("${CART_COOKIE_KEY}")
    private String CART_COOKIE_KEY;

    @Value("${CART_COOKIE_EXPIRE}")
    private Integer CART_COOKIE_EXPIRE;

    @Autowired
    private ItemServiceFeignClient itemServiceFeignClient;

    @Autowired
    private CartServiceFeign cartServiceFeign;

    /**
     * 向购物车中添加商品
     * @param itemId
     * @param userId
     * @param num
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/addItem")
    public Result addItem(Long itemId , String userId , @RequestParam(defaultValue = "1")Integer num, HttpServletRequest request,
                          HttpServletResponse response){
        try{
            if (StringUtils.isBlank(userId)){
                //未登录情况下添加商品到购物车
                //从cookie中查询商品列表
                Map<String, TbItem> cart = getCartFromCookie(request);
                //添加商品到购物车
                addItemToCart(cart,itemId,num);
                //把购物车商品列表写入到cookie
                addClientCookie(request,response,cart);
            }else {
                //登录情况下将商品添加到购物车
                //从redis中查询商品列表信息
                Map<String, TbItem> cart = getCartFromRedis(userId);
                //将商品添加到购物车中
                addItemToCart(cart,itemId,num);
                //将购物车缓存到redis中
                Boolean addItemToRedis = addCartToRedis(userId,cart);
                if (addItemToRedis){
                    return Result.ok();
                }
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("添加失败");
        }
    }

    /**
     * 显示购物车信息
     * @param userId
     * @param response
     * @param request
     * @return
     */
    @RequestMapping("/showCart")
    public Result showCart(String userId,HttpServletResponse response ,HttpServletRequest request){
        try{
            List<TbItem> list = new ArrayList<>();
            if (StringUtils.isBlank(userId)){
                //未登录情况下
                Map<String, TbItem> cart = getCartFromCookie(request);
                Set<String> keys = cart.keySet();
                for (String key : keys){
                    list.add(cart.get(key));
                }
            }else {
                //登录情况下
                Map<String, TbItem> cartFromRedis = getCartFromRedis(userId);
                Set<String> keySet = cartFromRedis.keySet();
                for (String key : keySet){
                    list.add(cartFromRedis.get(key));
                }
            }
            return Result.ok(list);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("error");
        }
    }

    /**
     * 修改购物车中商品数量
     * @param userId
     * @param itemId
     * @param num
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/updateItemNum")
    public Result updateItemNum(String userId,Long itemId,Integer num,
                                HttpServletRequest request,HttpServletResponse response){
        try{
            if (StringUtils.isBlank(userId)){
                //未登录情况下
                //从cookie中获取购物车
                Map<String, TbItem> cart = getCartFromCookie(request);
                //修改购物车中商品信息、再添加进购物车
                TbItem tbItem = cart.get(itemId.toString());
                tbItem.setNum(num);
                cart.put(itemId.toString(),tbItem);
                //把购物车商品列表写入到cookie中
                addClientCookie(request,response,cart);
            }else {
                //登录情况下
                // 1、从redis中查询商品列表。
                Map<String, TbItem> cartFromRedis = getCartFromRedis(userId);
                TbItem tbItem = cartFromRedis.get(itemId);
                if (tbItem!=null){
                    tbItem.setNum(num);
                }
                //将新的购物车缓存到redis中
                addCartToRedis(userId,cartFromRedis);
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("修改失败");
        }
    }

    /**
     * 删除购物车中商品
     * @param itemId
     * @param userId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/deleteItemFromCart")
    public Result deleteItemFromCart(Long itemId, String userId, HttpServletRequest request, HttpServletResponse response){
        try {
            if (StringUtils.isBlank(userId)){
                //未登录情况
                Map<String, TbItem> cart = getCartFromCookie(request);
                cart.remove(itemId.toString());
                addClientCookie(request,response,cart);
            }else {
                //登录情况下
                Map<String, TbItem> cartFromRedis = getCartFromRedis(userId);
                cartFromRedis.remove(itemId.toString());
                // 将新的购物车缓存进redis中
                addCartToRedis(userId,cartFromRedis);
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }

    /**
     * 从redis中获取购物车信息
     * @param userId
     */
    private Map<String,TbItem> getCartFromRedis(String userId) {
        Map<String, TbItem> cartFromRedis = cartServiceFeign.selectCartByUserId(userId);
        if (cartFromRedis!=null && cartFromRedis.size()>0){
            return cartFromRedis;
        }
        return new HashMap<String,TbItem>();
    }

    /**
     * 将购物车商品列表添加到redis中
     * @param userId
     * @param cart
     * @return
     */
    private Boolean addCartToRedis(String userId, Map<String, TbItem> cart) {
        return cartServiceFeign.insertCart(userId,cart);
    }


    /**
     * 把购物车商品列表写入到cookie中
     * @param request
     * @param response
     * @param cart
     */
    private void addClientCookie(HttpServletRequest request, HttpServletResponse response, Map<String, TbItem> cart) {
        String cartJson = JsonUtils.objectToJson(cart);
        CookieUtils.setCookie(request,response,CART_COOKIE_KEY,cartJson,CART_COOKIE_EXPIRE,true);
    }

    /**
     * 将商品添加到购物车
     * @param cart
     * @param itemId
     * @param num
     */
    private void addItemToCart(Map<String, TbItem> cart, Long itemId, Integer num) {
        //从购物车中取出商品
        TbItem tbItem = cart.get(String.valueOf(itemId));
        if (tbItem!=null){
            //如果商品列表中存在该商品，将数量相加
            tbItem.setNum(tbItem.getNum()+num);
        }else {
            //如果不存在该商品，通过itemId查询到商品信息并添加到购物车列表
            tbItem = itemServiceFeignClient.selectItemInfo(itemId);
            tbItem.setNum(num);
        }
        cart.put(itemId.toString(),tbItem);
    }

    /**
     * 从cookie中获取购物车信息
     * @param request
     * @return
     */
    private Map<String, TbItem> getCartFromCookie(HttpServletRequest request) {
        String cartJson = CookieUtils.getCookieValue(request, CART_COOKIE_KEY, true);
        if (StringUtils.isNotBlank(cartJson)){
            //购物车已存在，将json字符串转换为map集合
            Map<String,TbItem> map = JsonUtils.jsonToMap(cartJson, TbItem.class);
            return map ;
        }
        //如果购物车不存在，新建购物车
        return new HashMap<String,TbItem>();
    }
}
