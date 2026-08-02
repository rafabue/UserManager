package com.nayax.usermanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Encaminha as rotas do SPA para o {@code index.html}, para que um F5 em {@code /users/1/edit}
 * não resulte em 404.
 */
@Configuration
public class SpaFallbackConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/users/**").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
    }
}
