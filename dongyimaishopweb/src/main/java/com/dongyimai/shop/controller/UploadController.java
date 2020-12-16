package com.dongyimai.shop.controller;

import com.dongyimai.entity.Result;
import com.dongyimai.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){

        //1、获取扩展名
        String fullName = file.getOriginalFilename();
        String extName = fullName.substring(fullName.lastIndexOf(".")+1);
        //2、上传 需要使用Common中的工具类
        try {
            //2.1 创建上传的对象
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:conf/fdfs_client.conf");
            //2.2 执行上传
            String path = fastDFSClient.uploadFile(file.getBytes(),extName);

            //2.3 拼接返回地址 http://192.168.188.146/group1/M00/02/04/dsafidsafdsajf.sh
            String url = FILE_SERVER_URL + path;
            System.out.println("url : " + url);

            return new Result(true,url);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"上传失败");
        }

    }

}
