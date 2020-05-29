package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.*;
import com.usian.pojo.*;
import com.usian.redis.RedisClient;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper tbItemMapper ;

    @Autowired
    private TbItemCatMapper tbItemCatMapper ;

    @Autowired
    private TbItemDescMapper tbItemDescMapper ;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper ;

    @Autowired
    private RedisClient redisClient ;

    @Value("${portal_itemresult_redis_key}")
    private String portal_itemresult_redis_key ;

    /***
     * 根据id查询商品信息
     * @param itemId
     * @return
     */
    @Override
    public TbItem selectItemInfo(Long itemId) {
        return tbItemMapper.selectByPrimaryKey(itemId) ;
    }

    /**
     * 分页查询商品列表信息
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectTbItemAllByPage(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        TbItemExample example = new TbItemExample();
        example.setOrderByClause("updated DESC");
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo((byte)1);
        List<TbItem> tbItems =this.tbItemMapper.selectByExample(example);
        PageResult result = new PageResult();
        PageInfo<TbItem> pageInfo = (PageInfo<TbItem>)redisClient.get(portal_itemresult_redis_key);
        if (pageInfo==null){
            pageInfo = new PageInfo<>(tbItems);
            redisClient.set(portal_itemresult_redis_key,pageInfo);
        }
        result.setPageIndex(page);
        result.setTotalPage(pageInfo.getTotal());
        result.setResult(tbItems);
        return result;
    }

    @Override
    public Integer insertTbitem(TbItem tbItem, String desc, String itemParams) {
        //补齐TbItem数据
        Long itemId = IDUtils.genItemId();
        Date date = new Date();
        tbItem.setId(itemId);
        tbItem.setCreated(date);
        tbItem.setStatus((byte)1);
        tbItem.setUpdated(date);
        Integer tbItemNum = tbItemMapper.insertSelective(tbItem);

        //补齐商品描述对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(itemId);
        tbItemDesc.setUpdated(date);
        tbItemDesc.setCreated(date);
        tbItemDesc.setItemDesc(desc);
        Integer descNum = tbItemDescMapper.insertSelective(tbItemDesc);
        //补齐商品规格参数
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(itemId);
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setCreated(date);
        tbItemParamItem.setUpdated(date);
        Integer itemPatamItemNum = tbItemParamItemMapper.insertSelective(tbItemParamItem);
        return tbItemNum+descNum+itemPatamItemNum;
    }

    @Override
    public Integer deleteItemById(Long id) {
        return tbItemMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String,Object> preUpdateItem(Long itemId) {
        Map<String, Object> map = new HashMap<>();
        //根据商品 ID 查询商品
        TbItem item = this.tbItemMapper.selectByPrimaryKey(itemId);
        map.put("item", item);
        //根据商品 ID 查询商品描述
        TbItemDesc itemDesc = this.tbItemDescMapper.selectByPrimaryKey(itemId);
        map.put("itemDesc", itemDesc.getItemDesc());
        //根据商品 ID 查询商品类目
        TbItemCat itemCat = this.tbItemCatMapper.selectByPrimaryKey(item.getCid());
        map.put("itemCat", itemCat.getName());
        //根据商品 ID 查询商品规格参数
        TbItemParamItemExample example = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = example.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        List<TbItemParamItem> list =
                this.tbItemParamItemMapper.selectByExampleWithBLOBs(example);
        if (list != null && list.size() > 0) {
            map.put("itemParamItem", list.get(0).getParamData());
        }
        return map ;
    }

    @Override
    public Integer updateTbItem(TbItem tbItem) {

        return tbItemMapper.updateByPrimaryKeySelective(tbItem);
    }

}
