package com.usian.service;

import com.usian.mapper.TbItemCatMapper;
import com.usian.pojo.TbItemCat;
import com.usian.pojo.TbItemCatExample;
import com.usian.redis.RedisClient;
import com.usian.utils.CatNode;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper tbItemCatMapper ;

    @Value("${portal_catresult_redis_key}")
    private String portal_catresult_redis_key ;

    @Autowired
    private RedisClient redisClient;

    @Override
    public List<TbItemCat> selectItemCategoryByParentId(Long id) {
        TbItemCatExample example = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(id);
        criteria.andStatusEqualTo(1);
        return tbItemCatMapper.selectByExample(example);
    }


    /**
     * 查询首页商品分类
     * @return
     */
    @Override
    public CatResult selectItemCategoryAll() {
        //查询缓存
        CatResult catResultRedis = (CatResult)redisClient.get(portal_catresult_redis_key);
        if(catResultRedis!=null){
            return catResultRedis;
        }
        CatResult catResult = new CatResult();
        //查询商品分类
        catResult.setData(getCatList(0L));

        //添加到缓存
        redisClient.set(portal_catresult_redis_key,catResult);

        return catResult;
    }

   /* @Override
    public CatResult selectItemCategoryAll() {
        CatResult catResult = new CatResult();
        catResult.setData(getCatList(0L));
        return catResult ;
    }*/

    private List<?> getCatList(long parentId) {
        TbItemCatExample catExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = catExample.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<TbItemCat> tbItemCatList = tbItemCatMapper.selectByExample(catExample);
        ArrayList list = new ArrayList();
        int count = 0 ;
        for (TbItemCat tbItemCat : tbItemCatList){
            if (tbItemCat.getIsParent()){
                CatNode catNode = new CatNode();
                catNode.setItem(getCatList(tbItemCat.getId()));
                catNode.setName(tbItemCat.getName());
                list.add(catNode);
                count++ ;
                if (count==18){
                    break;
                }
            }else {
                list.add(tbItemCat.getName());
            }
        }
        return list ;
    }
}
