package com.cshp.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径
        // 将 /uploads/** 映射到文件存储目录
        
        // 处理相对路径，转换为绝对路径
        File uploadDir = new File(uploadPath);
        String absolutePath = uploadDir.getAbsolutePath();
        
        // 确保路径以斜杠结尾
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }
        
        // Windows路径需要特殊处理
        if (File.separator.equals("\\")) {
            absolutePath = absolutePath.replace("\\", "/");
        }
        
        String pathPattern = urlPrefix + "/**";
        String resourceLocation = "file:" + absolutePath;
        
        log.info("配置静态资源映射: {} -> {}", pathPattern, resourceLocation);
        
        registry.addResourceHandler(pathPattern)
                .addResourceLocations(resourceLocation);
    }
}

