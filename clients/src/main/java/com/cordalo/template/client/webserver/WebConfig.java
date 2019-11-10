package com.cordalo.template.client.webserver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/**
 * https://docs.spring.io/spring-boot/docs/1.4.3.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-auto-configuration
 * remove
 *  @EnableWebMvc
 */
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

    /* checkout
    https://stackoverflow.com/questions/42393211/how-can-i-serve-static-html-from-spring-boot
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/**") // « /css/myStatic.css
                .addResourceLocations("classpath:/static/") // Default Static Location
                ;

                //.setCachePeriod( 3600 )
                //.resourceChain(true) // 4.1
                //.addResolver(new GzipResourceResolver()) // 4.1
                //.addResolver(new PathResourceResolver());

    }
     */

 }