package com.usian.feign;

import com.usian.pojo.TbItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("usian-item-service")
public interface ItemServiceFeignClient {

    @RequestMapping("/service/item/selectItemInfo")//路径拼接...../?itemId=123
    public TbItem selectItemInfo(@RequestParam("itemId") Long itemId);

    /**
     *  @RequestMapping("/service/item/selectItemInfo/{itemid}")//路径拼接...../123
     *     public TbItem selectItemInfo(@PathVariable("itemId") Integer itemId);
     */
}
