package com.vehiclerental.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns(
                "/dashboard",
                "/profile", "/profile/**",
                "/bookings", "/bookings/**",
                "/payments", "/payments/**",
                "/reviews", "/reviews/**",
                "/complaints", "/complaints/**",
                "/notifications", "/notifications/**",
                "/api/notifications/**",
                "/admin", "/admin/**");
    }
}
