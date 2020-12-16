package com.dongyimai.protal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dongyimai.content.service.ContentService;
import com.dongyimai.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference
    private ContentService contentService;

    @RequestMapping("/findContentList")
    public List<TbContent> findContentList(Long categoryId){
        return contentService.findContentList(categoryId);
    }
}
