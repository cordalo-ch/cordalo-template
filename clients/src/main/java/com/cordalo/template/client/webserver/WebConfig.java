package com.cordalo.template.client.webserver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:63342", // webserver intelliJ
                        "http://65.52.142.219:10801", // azure
                        "http://65.52.142.219:10802",
                        "http://65.52.142.219:10803",
                        "http://65.52.142.219:10804",
                        "http://65.52.142.219:10805",
                        "http://localhost:10801",  // local docker
                        "http://localhost:10802",
                        "http://localhost:10803",
                        "http://localhost:10804",
                        "http://localhost:10805",
                        "*"
                );
    }
}