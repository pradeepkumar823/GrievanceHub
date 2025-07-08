package com.grievancehub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@org.jetbrains.annotations.NotNull ResourceHandlerRegistry registry) {
        // Dynamically resolve correct folder path (platform independent)
        String uploadPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", "uploads").toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath); // This will map /uploads/** to your actual uploads directory
    }
}
