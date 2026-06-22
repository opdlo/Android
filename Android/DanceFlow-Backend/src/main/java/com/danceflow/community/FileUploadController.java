package com.danceflow.community;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<String> urls = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                // 用 UUID 重命名防冲突
                String originalName = file.getOriginalFilename();
                String extension = "";
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }
                String newFileName = UUID.randomUUID().toString() + extension;

                // 按日期分目录
                String dateDir = java.time.LocalDate.now().toString();
                File targetDir = new File(uploadDir, dateDir);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }

                // 保存文件
                Path filePath = Paths.get(uploadDir, dateDir, newFileName);
                Files.write(filePath, file.getBytes());

                // 生成访问 URL
                urls.add("/uploads/" + dateDir + "/" + newFileName);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("urls", urls);
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }
}
