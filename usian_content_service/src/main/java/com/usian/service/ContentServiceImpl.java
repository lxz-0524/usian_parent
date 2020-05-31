package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbContentMapper;
import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentExample;
import com.usian.redis.RedisClient;
import com.usian.utils.AdNode;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper tbContentMapper ;
    @Value("${AD_CATEGORY_ID}")
    private Long AD_CATEGORY_ID;

    @Value("${AD_HEIGHT}")
    private Integer AD_HEIGHT;

    @Value("${AD_WIDTH}")
    private Integer AD_WIDTH;

    @Value("${AD_HEIGHTB}")
    private Integer AD_HEIGHTB;

    @Value("${AD_WIDTHB}")
    private Integer AD_WIDTHB;

    @Value("${PORTAL_AD_KEY}")
    private String PORTAL_AD_KEY ;

    @Autowired
    private RedisClient redisClient ;
    @Override
    public PageResult selectTbContentAllByCategoryId(Integer page, Integer rows, Long categoryId) {
        PageHelper.startPage(page,rows);
        TbContentExample contentExample = new TbContentExample();
        contentExample.setOrderByClause("updated DESC");
        TbContentExample.Criteria criteria = contentExample.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        List<TbContent> contentList = tbContentMapper.selectByExampleWithBLOBs(contentExample);
        PageInfo<TbContent> tbContentPageInfo = new PageInfo<>(contentList);
        PageResult pageResult = new PageResult();
        pageResult.setResult(tbContentPageInfo.getList());
        pageResult.setPageIndex(tbContentPageInfo.getPageNum());
        pageResult.setTotalPage(tbContentPageInfo.getTotal());
        return pageResult;
    }

    @Override
    public Integer insertTbContent(TbContent tbContent) {
        tbContent.setCreated(new Date());
        tbContent.setUpdated(new Date());
        Integer num = tbContentMapper.insertSelective(tbContent);
        redisClient.hdel(PORTAL_AD_KEY,AD_CATEGORY_ID.toString());
        return num;
    }

    @Override
    public Integer deleteContentByIds(Long ids) {
        Integer dnum = tbContentMapper.deleteByPrimaryKey(ids);
        redisClient.hdel(PORTAL_AD_KEY,AD_CATEGORY_ID.toString());
        return dnum;
    }

    @Override
    public List<AdNode> selectFrontendContentByAD() {

        //从redis缓存获取key对应的数据
        List<AdNode> adNodeListRedis = (List<AdNode>) redisClient.hget(PORTAL_AD_KEY, AD_CATEGORY_ID.toString());
        if (adNodeListRedis!=null){
            //如果redis缓存中获取的数据不为空，则直接返回缓存中的值；
            return adNodeListRedis ;
        }
        //如果缓存中获取到的值为空，则从数据库获取数据并保存到redis中
        TbContentExample contentExample = new TbContentExample();
        TbContentExample.Criteria criteria = contentExample.createCriteria();
        criteria.andCategoryIdEqualTo(AD_CATEGORY_ID);
        List<TbContent> tbContentList = tbContentMapper.selectByExample(contentExample);
        List<AdNode> adNodeList = new ArrayList<>();
        for (TbContent tbContent:tbContentList) {
            AdNode adNode = new AdNode();
            adNode.setSrc(tbContent.getPic());
            adNode.setSrcB(tbContent.getPic2());
            adNode.setHref(tbContent.getUrl());
            adNode.setHeight(AD_HEIGHT);
            adNode.setWidth(AD_WIDTH);
            adNode.setHeightB(AD_HEIGHTB);
            adNode.setWidthB(AD_WIDTHB);
            adNodeList.add(adNode);
        }
        //将数据库获取到的数据，保存到redis中
        redisClient.hset(PORTAL_AD_KEY,AD_CATEGORY_ID.toString(),adNodeList);
        return adNodeList ;
    }
}
