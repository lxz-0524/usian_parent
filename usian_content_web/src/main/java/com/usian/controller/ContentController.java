package com.usian.controller;

import com.usian.ContentServiceFeign;
import com.usian.pojo.TbContent;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/backend/content")
public class ContentController {

    @Autowired
    private ContentServiceFeign contentServiceFeign ;

    @RequestMapping("/selectTbContentAllByCategoryId")
    public Result selectTbContentAllByCategoryId(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "15") Integer rows, Long categoryId){
        PageResult tbContentList = contentServiceFeign.selectTbContentAllByCategoryId(page,rows,categoryId);
        if (tbContentList!=null&&tbContentList.getResult().size()>0){
            return Result.ok(tbContentList);
        }
        return Result.error("查无结果");
    }


    @RequestMapping("/insertTbContent")
    public Result insertTbContent(TbContent tbContent){
        Integer insertNum = contentServiceFeign.insertTbContent(tbContent);
        if (insertNum==1){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    @RequestMapping("/deleteContentByIds")
    public Result deleteContentByIds(Long ids){
        Integer deleteNum = contentServiceFeign.deleteContentByIds(ids);
        if (deleteNum==1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }
}
