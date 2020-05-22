package com.usian.service;

import com.usian.pojo.TbItem;
import com.usian.pojo.TbItemCat;
import com.usian.utils.PageResult;

import java.util.Map;

public interface ItemService {

    TbItem selectItemInfo(Long itemId);

    PageResult selectTbItemAllByPage(Integer page, Integer rows);

    Integer insertTbitem(TbItem tbItem, String desc, String itemParams);

    Integer deleteItemById(Long id);

    Map<String,Object> preUpdateItem(Long itemId);

    Integer updateTbItem(TbItem tbItem);
}
