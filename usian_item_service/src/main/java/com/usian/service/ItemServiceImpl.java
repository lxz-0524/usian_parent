package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.*;
import com.usian.pojo.*;
import com.usian.redis.RedisClient;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.amqp.core.AmqpTemplate;
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

    @Value("${ITEM_INFO}")
    private String ITEM_INFO;

    @Value("${BASE}")
    private String BASE;

    @Value("${DESC}")
    private String DESC;

    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;

    @Autowired
    private RedisClient redisClient ;

    @Autowired
    private AmqpTemplate amqpTemplate ;

    @Value("${portal_itemresult_redis_key}")
    private String portal_itemresult_redis_key ;

    @Value("${SETNX_BASE_LOCK_KEY}")
    private String SETNX_BASE_LOCK_KEY ;

    @Value("${SETNX_DESC_LOCK_KEY}")
    private String SETNX_DESC_LOCK_KEY ;

    /***
     * 根据id查询商品信息
     * @param itemId
     * @return
     */
    @Override
    public TbItem selectItemInfo(Long itemId) {
        //查询缓存
        TbItem tbItem =(TbItem) redisClient.get(ITEM_INFO + ":" + itemId + ":" + BASE);
        if (tbItem!=null){
            return tbItem ;
        }
        /********************解决缓存击穿************************/
        if (redisClient.setnx(SETNX_BASE_LOCK_KEY+":"+itemId,itemId,30)){
            tbItem = tbItemMapper.selectByPrimaryKey(itemId);
            /********************解决缓存穿透************************/
            if(tbItem == null){
                //把空对象保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":"+ BASE,tbItem);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":"+ BASE,30);
            }
            //把数据库查询到的数据缓存到redis
            redisClient.set(ITEM_INFO + ":" + itemId + ":" + BASE,tbItem);
            //设置缓存有效期
            redisClient.expire(ITEM_INFO + ":" + itemId + ":" + BASE,ITEM_INFO_EXPIRE);
            redisClient.del(SETNX_BASE_LOCK_KEY+":"+itemId);
            return tbItem;
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemInfo(itemId);
        }
    }

    /**
     * 根据商品id查询商品描述信息
     * @param itemId
     * @return
     */

    @Override
    public TbItemDesc selectItemDescByItemId(Long itemId){
        //查询缓存
        TbItemDesc tbItemDesc = (TbItemDesc) redisClient.get(ITEM_INFO + ":" + itemId + ":" + DESC);
        if (tbItemDesc!=null){
            return tbItemDesc ;
        }
        /***************************解决缓存击穿*******************************/
        if (redisClient.setnx(SETNX_DESC_LOCK_KEY+":"+itemId,itemId,30)){
            TbItemDescExample tbItemDescExample = new TbItemDescExample();
            TbItemDescExample.Criteria criteria = tbItemDescExample.createCriteria();
            criteria.andItemIdEqualTo(itemId);
            List<TbItemDesc> descList = tbItemDescMapper.selectByExampleWithBLOBs(tbItemDescExample);
            if (descList!=null&& descList.size()>0){
                //把数据缓存到redis
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC,descList.get(0));
                //设置缓存有效时间
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,ITEM_INFO_EXPIRE);
                return descList.get(0);
            }
            /********************解决缓存穿透************************/
            //把空对象保存到缓存
            redisClient.set(ITEM_INFO + ":" + itemId + ":"+ DESC,null);
            //设置缓存的有效期
            redisClient.expire(ITEM_INFO + ":" + itemId + ":"+ DESC,30);
            redisClient.del(SETNX_DESC_LOCK_KEY+":"+itemId);
            return tbItemDesc ;
        }else {
            //获取锁失败
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemDescByItemId(itemId);
        }
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
        //tbItem.setPrice(tbItem.getPrice()*100);
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
        //发送mq，完成索引库同步
        amqpTemplate.convertAndSend("item_exchange","item.add",itemId);
        return tbItemNum+descNum+itemPatamItemNum;
    }

    @Override
    public Integer deleteItemById(Long id) {
        redisClient.del(ITEM_INFO + ":" + id + ":" + DESC);
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
        redisClient.del(ITEM_INFO + ":" + tbItem.getId() + ":" + DESC);
        return tbItemMapper.updateByPrimaryKeySelective(tbItem);
    }

}
