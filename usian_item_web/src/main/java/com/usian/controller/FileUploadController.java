package com.usian.controller;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.usian.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileUploadController {
    @Autowired
    private FastFileStorageClient fastFileStorageClient ;

    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg","image/jpg","image/png");

    /**
     * 上传图片
     * @param file
     * @return
     */
    @RequestMapping("/upload")
    public Result fileUpload(MultipartFile file) {
        //1、校验文件类型
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            if (!CONTENT_TYPES.contains(contentType)){
                return Result.error("文件类型不合法"+filename);
            }
            //2、校验文件内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if(bufferedImage==null){
                return Result.error("文件内容不合法"+filename);
            }
            //3、上传文件 保存到服务器
            String last = StringUtils.substringAfterLast(filename, ".");
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(),file.getSize(),last,null);
            //4、返回图片的url   http://image.usian.com.+返回的路径
            System.out.println("http://image.usian.com/"+storePath.getFullPath());
            return Result.ok("http://image.usian.com/"+storePath.getFullPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.error("服务器内部错误");
    }

}
