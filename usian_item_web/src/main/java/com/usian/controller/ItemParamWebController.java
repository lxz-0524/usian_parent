package com.usian.controller;

import com.usian.feign.ItemServiceFeignClient;
import com.usian.pojo.TbItemParam;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/itemParam")
public class ItemParamWebController {
    @Autowired
    private ItemServiceFeignClient itemServiceFeignClient ;

    /**
     * 根据商品分类 ID 查询规格参数模板
     */
    @RequestMapping("/selectItemParamByItemCatId/{itemId}")
    public Result selectItemParamByItemCatId(@PathVariable("itemId")Long itemId){
        TbItemParam tbItemParam = this.itemServiceFeignClient.selectItemParamByItemCatId(itemId);
        if (tbItemParam!=null){
            return Result.ok(tbItemParam);
        }
        return Result.error("查无结果！！");
    }

    @RequestMapping("/selectItemParamAll")
    public Result selectItemParamAll(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "3")Integer rows){
        PageResult pageResult = this.itemServiceFeignClient.selectItemParamAll(page, rows);
        if (pageResult!=null&&pageResult.getResult().size()>0){
            return Result.ok(pageResult);
        }
        return Result.error("查无结果");
    }

    @RequestMapping("/insertItemParam")
    public Result insertItemParam(Long itemCatId,String paramData){
        Integer count = this.itemServiceFeignClient.insertItemParam(itemCatId, paramData);
        if (count==1){
            return Result.ok();
        }
        return Result.error("添加失败：该类目已有规格模版");
    }

    @RequestMapping("/deleteItemParamById")
    public Result deleteItemParamById(Long id){
        Integer dnum = this.itemServiceFeignClient.deleteItemParamById(id);
        if (dnum==1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }
}
