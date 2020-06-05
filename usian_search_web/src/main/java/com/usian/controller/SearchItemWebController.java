package com.usian.controller;

import com.usian.feign.SearchItemFeign;
import com.usian.pojo.SearchItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/frontend/searchItem")
public class SearchItemWebController {

    @Autowired
    private SearchItemFeign searchItemFeign ;

    /**
     * 一键导入商品信息至ES
     * @return
     */
    @RequestMapping("/importAll")
    public boolean importAll(){
        return searchItemFeign.importAll();
    }

    /**
     * 搜索显示商品信息
     * @param q
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping("/list")
    public List<SearchItem> selectByQ(String q, @RequestParam(defaultValue = "1")Long page,
                                      @RequestParam(defaultValue = "20")Integer pageSize){
        System.out.println("q:"+q+page+"   "+pageSize);
        return searchItemFeign.selectByQ(q,page,pageSize);
    }
}
