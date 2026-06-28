package edu.jxnu.microcoursebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String storageLocation;

    public WebConfig(@Value("${app.storage.root:storage}") String storageRoot) {
        String location = Path.of(storageRoot).toAbsolutePath().normalize().toUri().toString();
        this.storageLocation = location.endsWith("/") ? location : location + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations(storageLocation);
    }
}

