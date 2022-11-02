package com.example.idempotency.config;

import com.example.idempotency.aop.IdempotenceInterceptor;

import com.example.idempotency.service.IdempotenceV1Service;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class MvcConfig implements WebMvcConfigurer {
    IdempotenceV1Service idempotenceV1Service;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new IdempotenceInterceptor(idempotenceV1Service));
    }

}
