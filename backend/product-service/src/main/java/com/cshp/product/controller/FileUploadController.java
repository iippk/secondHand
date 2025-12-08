package com.cshp.product.controller;

import com.cshp.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/product/file")
public class FileUploadController {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Value("${file.upload.max-size:5242880}")
    private long maxFileSize;

    // 支持的图片格式 - 兼容 Java 8
    private static final Set<String> ALLOWED_EXTENSIONS;

    static {
        // 使用传统方式初始化 Set
        Set<String> extensions = new HashSet<>();
        extensions.add("jpg");
        extensions.add("jpeg");
        extensions.add("png");
        extensions.add("gif");
        extensions.add("webp");
        extensions.add("bmp");
        ALLOWED_EXTENSIONS = Collections.unmodifiableSet(extensions);
    }

    /**
     * 单文件上传 - 返回文件名而不是完整URL
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传文件: {}, 大小: {} bytes",
                    file.getOriginalFilename(), file.getSize());

            // 验证文件
            if (file.isEmpty()) {
                return Result.error(400, "文件不能为空");
            }

            // 验证文件大小
            if (file.getSize() > maxFileSize) {
                return Result.error(400, "文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error(400, "文件名不能为空");
            }

            String extension = getFileExtension(originalFilename);
            if (!isImageFile(extension)) {
                return Result.error(400, "只支持图片文件（jpg, jpeg, png, gif, webp, bmp）");
            }

            // 创建上传目录
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fullUploadPath = uploadPath + File.separator + dateDir;
            File uploadDir = new File(fullUploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                log.info("创建目录: {}, 结果: {}", fullUploadPath, created);
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + "." + extension;
            String filePath = fullUploadPath + File.separator + fileName;

            // 保存文件 - 使用更好的方式
            Path targetPath = Paths.get(filePath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 构建存储的路径（相对路径）
            String storedPath = dateDir + "/" + fileName;

            log.info("文件上传成功: {}", storedPath);

            // 返回结果 - 包含文件名和存储路径
            Map<String, String> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("storedPath", storedPath); // 存储这个到数据库
            result.put("fullUrl", urlPrefix + "/" + storedPath); // 完整访问URL

            return Result.success("上传成功", result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 多文件上传
     */
    @PostMapping("/upload/batch")
    public Result<Map<String, Object>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, String>> successFiles = new ArrayList<>();
        List<String> errorFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Result<Map<String, String>> result = uploadFile(file);
                if (result.getCode() == 200 && result.getData() != null) {
                    successFiles.add(result.getData());
                } else {
                    errorFiles.add(file.getOriginalFilename() + ": " + result.getMessage());
                }
            } catch (Exception e) {
                errorFiles.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successFiles);
        result.put("errors", errorFiles);

        if (successFiles.isEmpty()) {
            return Result.error(400, "所有文件上传失败");
        }

        String message = "成功上传 " + successFiles.size() + " 个文件";
        if (!errorFiles.isEmpty()) {
            message += ", " + errorFiles.size() + " 个文件失败";
            log.warn("部分文件上传失败: {}", errorFiles);
        }

        return Result.success(message, result);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public Result<String> deleteFile(@RequestParam("filePath") String filePath) {
        try {
            // 直接从文件路径删除
            String fullFilePath = uploadPath + File.separator + filePath;
            File file = new File(fullFilePath);

            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    log.info("文件删除成功: {}", fullFilePath);
                    return Result.success("文件删除成功");
                } else {
                    return Result.error(500, "文件删除失败");
                }
            } else {
                return Result.error(404, "文件不存在: " + filePath);
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", filePath, e);
            return Result.error(500, "文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取上传配置信息（调试用）
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getUploadConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("uploadPath", uploadPath);
        config.put("urlPrefix", urlPrefix);
        config.put("maxFileSize", maxFileSize);
        config.put("absoluteUploadPath", new File(uploadPath).getAbsolutePath());

        File uploadDir = new File(uploadPath);
        config.put("uploadDirExists", uploadDir.exists());
        config.put("uploadDirWritable", uploadDir.canWrite());

        // 统计文件数量
        if (uploadDir.exists()) {
            int fileCount = countFiles(uploadDir);
            config.put("totalFiles", fileCount);
        }

        return Result.success("配置信息", config);
    }

    /**
     * 递归统计文件数量
     */
    private int countFiles(File dir) {
        int count = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        count++;
                    } else if (file.isDirectory()) {
                        count += countFiles(file);
                    }
                }
            }
        }
        return count;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }
}