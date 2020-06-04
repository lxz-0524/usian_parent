package com.usian.controller;

import com.usian.feign.SearchItemFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/frontend/searchItem")
public class SearchItemWebController {

    @Autowired
    private SearchItemFeign searchItemFeign ;

    @RequestMapping("/importAll")
    public boolean importAll(){
        return searchItemFeign.importAll();
    }
}
