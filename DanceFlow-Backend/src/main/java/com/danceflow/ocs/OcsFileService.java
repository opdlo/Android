package com.danceflow.ocs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务 - 本地存储
 * 
 * 文件保存在 ./uploads/ 目录，通过 /uploads/** 静态路径访问。
 * 部署到服务器上正常工作，也可在前面加 Nginx/CDN 做加速。
 *
 * 如需接入腾讯云 COS，参考以下步骤：
 * 1. build.gradle 添加依赖: implementation 'com.qcloud.cos:cos_api:5.6.227'
 * 2. application.yml 配置 cos.*
 * 3. 参考 com.qcloud.cos.COSClient SDK 上传（SDK 自带签名）
 */
@Service
public class OcsFileService {

    @Value("${file.upload-dir:./uploads}")
    private String localUploadDir;

    public String upload(MultipartFile file, String fileName) throws Exception {
        return upload(file, fileName, null);
    }

    public String upload(MultipartFile file, String fileName, String subDir) throws Exception {
        String dateDir = java.time.LocalDate.now().toString();
        String dir = localUploadDir;
        String urlPath = "/uploads";
        if (subDir != null && !subDir.isEmpty()) {
            dir = dir + "/" + subDir;
            urlPath = urlPath + "/" + subDir;
        }
        dir = dir + "/" + dateDir;
        urlPath = urlPath + "/" + dateDir;
        java.io.File targetDir = new java.io.File(dir);
        if (!targetDir.exists()) targetDir.mkdirs();
        java.nio.file.Path filePath = java.nio.file.Paths.get(dir, fileName);
        java.nio.file.Files.write(filePath, file.getBytes());
        return urlPath + "/" + fileName;
    }
}