package com.usian.controller;

import com.usian.pojo.TbItem;
import com.usian.pojo.TbItemDesc;
import com.usian.service.ItemService;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/service/item/")
@RestController
public class ItemServiceController {

    @Autowired
    private ItemService itemService ;

    /**
     * 根据id查询商品信息
     * @param itemId
     * @return
     */
    @RequestMapping("/selectItemInfo")
    public TbItem selectItemInfo(@RequestParam Long itemId){
        return itemService.selectItemInfo(itemId) ;
    }

    /**
     * 分页查询所有商品信息
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectTbItemAllByPage")
    public PageResult selectTbItemAllByPage(@RequestParam(defaultValue = "1")Integer page,@RequestParam(defaultValue = "3")Integer rows){
        // String phoneString = (String)redisTemplate.boundHashOps("phoneList").get(model.getId() + model.getModelname() + pageNum + pageSize);
       return itemService.selectTbItemAllByPage(page,rows) ;
    }

    /**
     *
     * 商品添加
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/insertTbItem")
    public Integer insertTbItem(@RequestBody TbItem tbItem, String desc, String itemParams){
        return itemService.insertTbitem(tbItem,desc,itemParams) ;
    }

    @RequestMapping("/preUpdateItem")
    public Map<String,Object> preUpdateItem(@RequestParam Long itemId){
        return this.itemService.preUpdateItem(itemId);
    }

    @RequestMapping("/updateTbItem")
    public Integer updateTbItem(@RequestBody TbItem tbItem){
        return this.itemService.updateTbItem(tbItem);
    }
    /**
     * 删除商品
     * @param itemId
     * @return
     */
    @RequestMapping("/deleteItemById")
    public Integer deleteItemById(Long itemId){
        return itemService.deleteItemById(itemId);
    }

    /**
     * 根据商品 ID 查询商品描述
     */
    @RequestMapping("/selectItemDescByItemId")
    public TbItemDesc selectItemDescByItemId(Long itemId){
        return this.itemService.selectItemDescByItemId(itemId);
    }



}
