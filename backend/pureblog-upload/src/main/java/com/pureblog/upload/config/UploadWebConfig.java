package com.pureblog.upload.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 将 /static/uploads/** 映射到本地 ./uploads/ 目录，
 * 仅用于本地存储阶段；切到 OSS / S3 时此配置可删除。
 */
@Configuration
public class UploadWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get("uploads").toAbsolutePath().toString();
        registry.addResourceHandler("/static/uploads/**")
                .addResourceLocations("file:" + location + "/");
    }
}
