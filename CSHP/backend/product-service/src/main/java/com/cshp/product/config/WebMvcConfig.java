package com.cshp.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
        String pathPattern = urlPrefix + "/**";
        String resourceLocation = "file:" + uploadPath + "/";
        
        registry.addResourceHandler(pathPattern)
                .addResourceLocations(resourceLocation);
    }
}

