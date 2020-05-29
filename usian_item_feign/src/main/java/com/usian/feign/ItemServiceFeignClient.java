package com.usian.feign;

import com.usian.pojo.TbItem;
import com.usian.pojo.TbItemCat;
import com.usian.pojo.TbItemParam;
import com.usian.utils.CatResult;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("usian-item-service")
public interface ItemServiceFeignClient {
    /**
     * 通过id获取商品详细信息
     * @param itemId
     * @return
     */
    @RequestMapping("/service/item/selectItemInfo")//路径拼接...../?itemId=123
    public TbItem selectItemInfo(@RequestParam("itemId") Long itemId);

    /**
     * 获取商品列表信息
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/service/item/selectTbItemAllByPage")
    public PageResult selectTbItemAllByPage(@RequestParam(defaultValue = "1")Integer page,@RequestParam(defaultValue = "3")Integer rows);

    /**
     * 通过父id得到子对象
     * @param id
     * @return
     */
    @RequestMapping("/service/itemCat/selectItemCategoryByParentId")
    List<TbItemCat> selectItemCategoryByParentId(@RequestParam("id") Long id);

    /**
     * 根据商品分类 ID 查询规格参数模板
     * @param itemId
     * @return
     */
    @RequestMapping("/service/itemParam/selectItemParamByItemCatId")
    public TbItemParam selectItemParamByItemCatId(@RequestParam("itemId") Long itemId);

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/service/item/insertTbItem")
    public Integer insertTbItem(@RequestBody TbItem tbItem, @RequestParam String desc,
                                @RequestParam String itemParams);

    @RequestMapping("/service/item/deleteItemById")
    public Integer deleteItemById(@RequestParam Long itemId);

    @RequestMapping("/service/itemParam/selectItemParamAll")
    public PageResult selectItemParamAll(@RequestParam Integer page, @RequestParam Integer rows);

    @RequestMapping("/service/itemParam/insertItemParam")
    public Integer insertItemParam(@RequestParam Long itemCatId,@RequestParam String paramData);

    @RequestMapping("/service/itemParam/deleteItemParamById")
    Integer deleteItemParamById(@RequestParam Long id);

    @RequestMapping("/service/itemCat/selectItemCategoryAll")
    CatResult selectItemCategoryAll();

    @RequestMapping("/service/item/preUpdateItem")
    Map<String,Object> preUpdateItem(@RequestParam Long itemId);

    @RequestMapping("/service/item/updateTbItem")
    Integer updateTbItem(@RequestBody TbItem tbItem);


    /**
     *  @RequestMapping("/service/item/selectItemInfo/{itemid}")//路径拼接...../123
     *     public TbItem selectItemInfo(@PathVariable("itemId") Integer itemId);
     */
}
