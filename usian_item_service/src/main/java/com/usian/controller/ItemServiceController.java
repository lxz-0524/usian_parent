package com.usian.controller;

import com.usian.pojo.TbItem;
import com.usian.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/service/item/")
@RestController
public class ItemServiceController {

    @Autowired
    private ItemService itemService ;

    @RequestMapping("/selectItemInfo")
    public TbItem selectItemInfo(@RequestParam Long itemId){
        return itemService.selectItemInfo(itemId) ;
    }
}
