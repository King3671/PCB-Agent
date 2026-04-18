package com.cj.imageagent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射：访问路径 /uploads/** --> 对应磁盘路径 F:/IdeaProject/ImageAgent/src/main/java/com/cj/imageagent/uploadFile/
        // 注意：磁盘路径必须以 file: 开头，且最后要有斜杠
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:F:/IdeaProject/ImageAgent/src/main/java/com/cj/imageagent/uploadFile/");
    }
}
