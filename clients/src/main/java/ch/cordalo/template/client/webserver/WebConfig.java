/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.client.webserver;

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