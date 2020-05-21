package com.usian.controller;

import com.usian.pojo.TbItemParam;
import com.usian.service.ItemParamService;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/itemParam")
public class ItemParamController {

    @Autowired
    private ItemParamService itemParamService ;

    /**
     * 根据商品分类 ID 查询规格参数模板
     */
    @RequestMapping("/selectItemParamByItemCatId")
    public TbItemParam selectItemParamByItemCatId(Long itemId){
        return itemParamService.selectItemParamByItemCatId(itemId);
    }

    @RequestMapping("/selectItemParamAll")
    public PageResult selectItemParamAll(Integer page,Integer rows){
        return itemParamService.selectItemParamAll(page,rows);
    }

    @RequestMapping("/insertItemParam")
    public Integer insertItemParam(@RequestParam Long itemCatId, @RequestParam String paramData){
        return itemParamService.insertItemParam(itemCatId,paramData);
    }

    @RequestMapping("/deleteItemParamById")
    public Integer deleteItemParamById(@RequestParam Long id){
        return itemParamService.deleteItemParamById(id);
    }
}
