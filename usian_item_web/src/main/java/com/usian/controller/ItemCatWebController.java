package com.usian.controller;

import com.usian.feign.ItemServiceFeignClient;
import com.usian.pojo.TbItemCat;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backend/itemCategory/")
public class ItemCatWebController {
    @Autowired
    private ItemServiceFeignClient itemServiceFeignClient ;

    /**
     * 根据父节点获取子节点对象
     * @param id
     * @return
     */
    @RequestMapping("/selectItemCategoryByParentId")
    public Result selectItemCategoryByParentId(@RequestParam(value = "id",defaultValue = "0")Long id){
        List<TbItemCat> itemCatList = itemServiceFeignClient.selectItemCategoryByParentId(id);
        if (itemCatList!=null&&itemCatList.size()>0){
            return Result.ok(itemCatList);
        }
        return Result.error("查无结果！！");
    }
}
