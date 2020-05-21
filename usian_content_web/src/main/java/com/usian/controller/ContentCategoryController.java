package com.usian.controller;

import com.usian.ContentServiceFeign;
import com.usian.pojo.TbContentCategory;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backend/content")
public class ContentCategoryController {

    @Autowired
    private ContentServiceFeign contentServiceFeign ;

    /**
     *根据当前节点 ID 查询子节点
     * @param id
     * @return
     */
    @RequestMapping("/selectContentCategoryByParentId")
    public Result selectContentCategoryByParentId(@RequestParam(defaultValue = "0")Long id){
        List<TbContentCategory> contentCategoryList = contentServiceFeign.selectContentCategoryByParentId(id);
        if (contentCategoryList!=null&&contentCategoryList.size()>0){
            return Result.ok(contentCategoryList);
        }
        return Result.error("查无结果");
    }

    @RequestMapping("/insertContentCategory")
    public Result insertContentCategory(TbContentCategory tbContentCategory){
        Integer num = contentServiceFeign.insertContentCategory(tbContentCategory);
        if (num!=null){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    @RequestMapping("/deleteContentCategoryById")
    public Result deleteContentCategoryById(Long categoryId){
        System.out.println("categoryId: "+categoryId);
        Integer status = contentServiceFeign.deleteContentCategoryById(categoryId);
        if (status==200){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

    @RequestMapping("/updateContentCategory")
    public Result updateContentCategory(TbContentCategory tbContentCategory){
         Integer updateContent = contentServiceFeign.updateContentCategory(tbContentCategory);
         if (updateContent==1){
                return Result.ok();
         }
         return Result.error("修改失败");
    }

}
