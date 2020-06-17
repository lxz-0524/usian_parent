package com.usian.controller;

import com.usian.CartServiceFeign;
import com.usian.feign.OrderServiceFeign;
import com.usian.pojo.OrderInfo;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderShipping;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单服务controller
 */
@RestController
@RequestMapping("/frontend/order")
public class OrderWebController {
    @Autowired
    private CartServiceFeign cartServiceFeign ;

    @Autowired
    private OrderServiceFeign orderServiceFeign ;

    @RequestMapping("/goSettlement")
    public Result goSettlement(String[] ids, String userId){
        //获取购物车
        Map<String, TbItem> cart = cartServiceFeign.selectCartByUserId(userId);
        //从购物车中获取选中的商品
        List<TbItem> list = new ArrayList<>();
        for (String id :ids){
            list.add(cart.get(id));
        }
        if (list.size()>0){
            return Result.ok(list);
        }
        return Result.error("操作失败");
    }

    @RequestMapping("/insertOrder")
    public Result insertOrder(String orderItem, TbOrder tbOrder , TbOrderShipping tbOrderShipping){
        //因为一个request中只包含一个request body. 所以feign不支持多个@RequestBody。
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderItem(orderItem);
        orderInfo.setTbOrderShipping(tbOrderShipping);
        orderInfo.setTbOrder(tbOrder);
        String orderId = orderServiceFeign.insertOrder(orderInfo);
        if (orderId!=null){
            return Result.ok(orderId);
        }
        return Result.error("创建订单失败");
    }
}
