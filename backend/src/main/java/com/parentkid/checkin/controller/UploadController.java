package com.parentkid.checkin.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.parentkid.checkin.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));

        String datePath = DateUtil.format(new Date(), "yyyyMMdd");
        String fileName = IdUtil.simpleUUID() + ext;
        String relativePath = "/uploads/" + datePath + "/" + fileName;

        File dir = new File(uploadPath + "/" + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            Map<String, String> result = new HashMap<>();
            result.put("url", relativePath);
            result.put("fileName", fileName);
            return Result.success("上传成功", result);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
