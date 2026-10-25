package com.brix.campus.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置：把本地 {@code uploads/} 目录映射到 {@code /uploads/**}，
 * 保证图片可直接通过 URL 访问，且重启后旧图仍可访问（PRD N07）。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    /** 上传根目录（相对 backend/ 工作目录） */
    @Value("${brix.upload.dir:./uploads}")
    private String uploadDir;

    /** 启动时确保上传根目录存在，避免首次上传失败 */
    @PostConstruct
    public void initUploadDir() {
        Path path = resolveUploadRoot();
        try {
            Files.createDirectories(path);
            log.info("上传根目录已就绪：{}", path);
        } catch (IOException e) {
            log.error("创建上传根目录失败：{}", path, e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = resolveUploadRoot().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }

    /**
     * 解析上传根目录的绝对路径。
     *
     * @return 规范化后的绝对路径
     */
    public Path resolveUploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }
}
