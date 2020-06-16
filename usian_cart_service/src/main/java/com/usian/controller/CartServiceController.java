package com.usian.controller;

import com.usian.pojo.TbItem;
import com.usian.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/service/cart")
public class CartServiceController {

    @Autowired
    private CartService cartService ;

    /**
     * 根据用户userId查询用户购物车
     * @param userId
     * @return
     */
    @RequestMapping("/selectCartByUserId")
    public Map<String, TbItem> selectCartByUserId(@RequestParam String userId){
        return cartService.selectCartByUserId(userId);
    }

    /**
     * 将购物车缓存到redis中
     * @param userId
     * @param cart
     * @return
     */
    @RequestMapping("/insertCart")
    public Boolean insertCart(String userId,@RequestBody Map<String, TbItem> cart){
        return cartService.insertCart(userId,cart);
    }

}
