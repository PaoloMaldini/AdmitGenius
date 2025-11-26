package com.admitgenius.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private String getUploadDir() {
        // 使用项目目录下的data/uploads文件夹
        // 获取项目根目录（backend的父目录）
        String projectRoot = System.getProperty("user.dir");
        if (projectRoot.endsWith("backend")) {
            projectRoot = projectRoot.substring(0, projectRoot.length() - 8); // 移除 "/backend"
        }
        return projectRoot + "/data/uploads/";
    }

    @GetMapping("/avatars/{fileName:.+}")
    public ResponseEntity<Resource> downloadAvatar(@PathVariable String fileName) {
        String uploadDir = getUploadDir();
        System.out.println("请求头像文件: " + fileName);
        System.out.println("上传目录: " + uploadDir);

        try {
            // 验证文件名安全性，防止路径遍历攻击
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                System.out.println("不安全的文件名: " + fileName);
                return ResponseEntity.badRequest().build();
            }

            Path avatarDir = Paths.get(uploadDir + "avatars/");
            Path filePath = avatarDir.resolve(fileName).normalize();

            System.out.println("完整文件路径: " + filePath.toString());
            System.out.println("目录是否存在: " + Files.exists(avatarDir));
            System.out.println("文件是否存在: " + Files.exists(filePath));

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                System.out.println("文件存在且可读: " + fileName);

                // 根据文件扩展名设置正确的Content-Type
                String contentType = "image/jpeg"; // 默认
                if (fileName.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                System.out.println("文件不存在或不可读: " + fileName);
                System.out.println("资源存在: " + resource.exists());
                System.out.println("资源可读: " + resource.isReadable());

                // 列出目录内容进行调试
                try {
                    if (Files.exists(avatarDir)) {
                        System.out.println("目录内文件列表:");
                        Files.list(avatarDir).forEach(file -> System.out.println(" - " + file.getFileName()));
                    }
                } catch (IOException e) {
                    System.out.println("无法列出目录内容: " + e.getMessage());
                }

                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            System.out.println("URL格式错误: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("文件访问异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}