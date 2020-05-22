package com.usian.service;

import com.usian.mapper.TbItemCatMapper;
import com.usian.pojo.TbItemCat;
import com.usian.pojo.TbItemCatExample;
import com.usian.utils.CatNode;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper tbItemCatMapper ;

    @Override
    public List<TbItemCat> selectItemCategoryByParentId(Long id) {
        TbItemCatExample example = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(id);
        criteria.andStatusEqualTo(1);
        return tbItemCatMapper.selectByExample(example);
    }

    @Override
    public CatResult selectItemCategoryAll() {
        CatResult catResult = new CatResult();
        catResult.setData(getCatList(0L));
        return catResult ;
    }

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
