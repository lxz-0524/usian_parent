package com.usian;

import com.usian.pojo.TbItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("usian-cart-service")
public interface CartServiceFeign {

    @RequestMapping("/service/cart/selectCartByUserId")
    public Map<String, TbItem> selectCartByUserId(@RequestParam String userId) ;

    @RequestMapping("/service/cart/insertCart")
    public Boolean insertCart(@RequestParam String userId, Map<String, TbItem> cart);
}
