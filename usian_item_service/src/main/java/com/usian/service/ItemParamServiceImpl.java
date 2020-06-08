package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.mapper.TbItemParamMapper;
import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamExample;
import com.usian.pojo.TbItemParamItem;
import com.usian.pojo.TbItemParamItemExample;
import com.usian.redis.RedisClient;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ItemParamServiceImpl implements ItemParamService {

    @Value("${ITEM_INFO}")
    private String ITEM_INFO;

    @Value("${PARAM}")
    private String PARAM;

    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;

    @Value("${SETNX_PARAM_LOCK_KEY}")
    private String SETNX_PARAM_LOCK_KEY ;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private TbItemParamMapper tbItemParamMapper ;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper ;

    @Override
    public TbItemParam selectItemParamByItemCatId(Long itemId) {
        TbItemParamExample paramExample = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = paramExample.createCriteria();
        criteria.andItemCatIdEqualTo(itemId);
        List<TbItemParam> tbItemParams = tbItemParamMapper.selectByExampleWithBLOBs(paramExample);
        if (tbItemParams!=null&&tbItemParams.size()>0){
            return tbItemParams.get(0);
        }
        return null;
    }

    @Override
    public PageResult selectItemParamAll(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        TbItemParamExample paramExample = new TbItemParamExample();
        paramExample.setOrderByClause("updated DESC");
        List<TbItemParam> tbItemParams = tbItemParamMapper.selectByExampleWithBLOBs(paramExample);
        PageInfo<TbItemParam> pageInfo = new PageInfo<>(tbItemParams);
        PageResult pageResult = new PageResult();
        pageResult.setTotalPage(Long.valueOf(pageInfo.getPages()));
        pageResult.setPageIndex(pageInfo.getPageNum());
        pageResult.setResult(pageInfo.getList());
        return pageResult;
    }

    @Override
    public Integer insertItemParam(Long itemCatId, String paramData) {
        //1、判断该类别的商品是否有规格模板
        TbItemParamExample itemParamExample = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = itemParamExample.createCriteria();
        criteria.andItemCatIdEqualTo(itemCatId);
        List<TbItemParam> tbItemParamList = tbItemParamMapper.selectByExample(itemParamExample);
        if (tbItemParamList.size()>0){
            return 0 ;
        }
        //2、保存规格模板
        Date date = new Date();
        TbItemParam tbItemParam = new TbItemParam();
        tbItemParam.setParamData(paramData);
        tbItemParam.setItemCatId(itemCatId);
        tbItemParam.setCreated(date);
        tbItemParam.setUpdated(date);
        return tbItemParamMapper.insertSelective(tbItemParam);
    }

    @Override
    public Integer deleteItemParamById(Long id) {
        return tbItemParamMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据商品id查询商品规格参数信息
     * @param itemId
     * @return
     */
    @Override
    public TbItemParamItem selectTbItemParamItemByItemId(Long itemId) {
        TbItemParamItem tbItemParamItem = (TbItemParamItem) redisClient.get(ITEM_INFO + ":" + itemId + ":" + PARAM);
        if (tbItemParamItem!=null){
            return  tbItemParamItem ;
        }
        /**************************解决缓存穿透******************************/
        if (redisClient.setnx(SETNX_PARAM_LOCK_KEY+":"+itemId,itemId,30)){
            TbItemParamItemExample example = new TbItemParamItemExample();
            TbItemParamItemExample.Criteria criteria = example.createCriteria();
            criteria.andItemIdEqualTo(itemId);
            List<TbItemParamItem> tbItemParamItems = tbItemParamItemMapper.selectByExampleWithBLOBs(example);
            if (tbItemParamItems!=null&&tbItemParamItems.size()>0){
                tbItemParamItem = tbItemParamItems.get(0);
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + PARAM,tbItemParamItem);
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + PARAM,ITEM_INFO_EXPIRE);
                return tbItemParamItems.get(0) ;
            }else {
                /********************解决缓存穿透************************/
                //把空对象保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":"+ PARAM,null);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":"+ PARAM,30);
            }
            redisClient.del(SETNX_PARAM_LOCK_KEY+":"+itemId);
            return tbItemParamItem  ;
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectTbItemParamItemByItemId(itemId);
        }
    }
}
