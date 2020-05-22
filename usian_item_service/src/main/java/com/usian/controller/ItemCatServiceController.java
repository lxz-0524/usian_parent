package com.usian.controller;

import com.usian.pojo.TbItemCat;
import com.usian.service.ItemCatService;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/service/itemCat/")
@RestController
public class ItemCatServiceController {

    @Autowired
    private ItemCatService itemCatService ;
    /**
     * 根据父节点查询子节点
     */
    @RequestMapping("/selectItemCategoryByParentId")
    public List<TbItemCat> selectItemCategoryByParentId(@RequestParam("id")Long id){
        return itemCatService.selectItemCategoryByParentId(id);
    }


    @RequestMapping("/selectItemCategoryAll")
    public CatResult selectItemCategoryAll(){
        return this.itemCatService.selectItemCategoryAll();
    }
}
