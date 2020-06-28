package com.usian.controller;

import com.usian.feign.ItemServiceFeignClient;
import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/backend/item")
@Api("商品管理接口")
public class ItemWebController {

    @Autowired
    private ItemServiceFeignClient itemServiceFeignClient ;

    /**
     * 根据id查询商品信息
     * @param itemId
     * @return
     */
    @RequestMapping(value = "/selectItemInfo",method = RequestMethod.POST)
    @ApiOperation(value = "查询商品基本信息",notes = "根据itemId查询该商品的基本信息")
    @ApiImplicitParam(name="itemId",required = true,type = "Long",value = "商品id")
    @ApiResponses({
            @ApiResponse(code = 200,message = "查询成功"),
            @ApiResponse(code = 500,message = "查无结果")
    })
    public Result getTbItemBiPrimaryKey(Long itemId){
        TbItem tbItem = itemServiceFeignClient.selectItemInfo(itemId);
        if (tbItem!=null){
           return Result.ok(tbItem);
        }
        return Result.error("查无结果");
    }

    /**
     * 分页查询商品信息
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("/selectTbItemAllByPage")
    @ApiOperation(value = "查询商品并分页处理",notes = "分页查询商品信息，每页显示2条")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",required = false,type = "Integer",value = "页码",defaultValue = "1"),
            @ApiImplicitParam(name="rows",required = false,type = "Integer",value = "每页多少条",defaultValue = "2")
    })
    @ApiResponses({
            @ApiResponse(code = 200,message = "查询成功"),
            @ApiResponse(code = 500,message = "查无结果")
    })
    public Result selectTbItemAllByPage(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "3") Integer rows){
        PageResult pageResult = itemServiceFeignClient.selectTbItemAllByPage(page, rows);
        if (pageResult!=null&&pageResult.getResult()!=null&&pageResult.getResult().size()>0){
            return Result.ok(pageResult);
        }
        return Result.error("查无结果");
    }

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @PostMapping("/insertTbItem")
    @ApiOperation(value = "添加商品",notes = "添加商品及描述和规格参数信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="desc",required = true,type = "String",value = "商品描述信息"),
            @ApiImplicitParam(name="itemParams",required = true,type = "String",value = "商品规格参数")
    })
    @ApiResponses({
            @ApiResponse(code = 200,message = "添加成功"),
            @ApiResponse(code = 500,message = "添加失败")
    })
    public Result insertTbItem(TbItem tbItem,String desc,String itemParams){
        Integer insertbItemNum = itemServiceFeignClient.insertTbItem(tbItem, desc, itemParams);
        if (insertbItemNum==3){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    @RequestMapping("/preUpdateItem")
    public Result preUpdateItem(Long itemId){
        Map<String,Object> updateRes =itemServiceFeignClient.preUpdateItem(itemId);
        if (updateRes.size()>0){
            return Result.ok(updateRes);
        }
        return Result.error("查无结果");
    }

    @RequestMapping("/updateTbItem")
    public Result updateTbItem(TbItem tbItem){
        Integer updateNum = itemServiceFeignClient.updateTbItem(tbItem);
        if (updateNum==1){
            return Result.ok();
        }
        return Result.error("修改失败");
    }

    @RequestMapping("/deleteItemById")
    public Result deleteItemById(Long itemId){
        Integer num = itemServiceFeignClient.deleteItemById(itemId);
        if (num==1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

}
