package com.usian.service;

import com.usian.mapper.TbContentCategoryMapper;
import com.usian.pojo.TbContentCategory;
import com.usian.pojo.TbContentCategoryExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private TbContentCategoryMapper tbContentCategoryMapper ;

    @Override
    public List<TbContentCategory> selectContentCategoryByParentId(Long id) {
        TbContentCategoryExample categoryExample = new TbContentCategoryExample();
        //categoryExample.setOrderByClause("updated DESC");
        TbContentCategoryExample.Criteria criteria = categoryExample.createCriteria();
        criteria.andParentIdEqualTo(id);
        List<TbContentCategory> contentCategoryList = tbContentCategoryMapper.selectByExample(categoryExample);
        return contentCategoryList;
    }

    @Override
    public Integer insertContentCategory(TbContentCategory tbContentCategory) {
        Date date = new Date();
        tbContentCategory.setUpdated(date);
        tbContentCategory.setCreated(date);
        tbContentCategory.setIsParent(false);
        tbContentCategory.setSortOrder(1);
        tbContentCategory.setStatus(1);
        Integer contentCategoryNum = tbContentCategoryMapper.insert(tbContentCategory);
        TbContentCategory parentContentCategory = tbContentCategoryMapper.selectByPrimaryKey(tbContentCategory.getParentId());
        if (!parentContentCategory.getIsParent()){
            parentContentCategory.setIsParent(true);
            parentContentCategory.setUpdated(new Date());
            tbContentCategoryMapper.updateByPrimaryKeySelective(parentContentCategory);
        }
        return contentCategoryNum ;
    }

    @Override
    public Integer deleteContentCategoryById(Long categoryId) {
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(categoryId);
        if (tbContentCategory.getIsParent()){
            return 0;
        }
        tbContentCategoryMapper.deleteByPrimaryKey(categoryId);
        TbContentCategoryExample categoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = categoryExample.createCriteria();
        criteria.andParentIdEqualTo(tbContentCategory.getId());
        List<TbContentCategory> contentCategoryList = tbContentCategoryMapper.selectByExample(categoryExample);
        if (contentCategoryList.size()==0){
            TbContentCategory parentCategory = new TbContentCategory();
            parentCategory.setId(tbContentCategory.getParentId());
            parentCategory.setIsParent(false);
            parentCategory.setUpdated(new Date());
            this.tbContentCategoryMapper.updateByPrimaryKeySelective(parentCategory);
        }
        return 200;
    }

    @Override
    public Integer updateContentCategory(TbContentCategory tbContentCategory) {
        tbContentCategory.setUpdated(new Date());
        return tbContentCategoryMapper.updateByPrimaryKeySelective(tbContentCategory);
    }
}
