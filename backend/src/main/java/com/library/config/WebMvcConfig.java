package com.library.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private String resolvedUploadDir;

    @PostConstruct
    void init() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.resolvedUploadDir = path.toString();
        log.info("静态资源目录映射: /uploads/** -> file:{}/", resolvedUploadDir);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + resolvedUploadDir + "/");
    }
}
