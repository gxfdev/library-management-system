package com.library.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setSupportedMediaTypes(Arrays.asList(
            MediaType.TEXT_PLAIN,
            MediaType.TEXT_HTML,
            new MediaType("application", "json", StandardCharsets.UTF_8),
            new MediaType("application", "javascript", StandardCharsets.UTF_8)
        ));
        converters.add(0, stringConverter);

        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setDefaultCharset(StandardCharsets.UTF_8);
                jacksonConverter.setSupportedMediaTypes(Arrays.asList(
                    new MediaType("application", "json", StandardCharsets.UTF_8),
                    new MediaType("application", "*+json", StandardCharsets.UTF_8)
                ));
            }
        }
        log.info("HTTP消息转换器已配置: 强制UTF-8编码");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + resolvedUploadDir + "/");
    }
}
