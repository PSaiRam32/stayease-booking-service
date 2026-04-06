package com.stayease.booking_service.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String authHeader = request.getHeader("Authorization");
                String correlationId = request.getHeader("X-Correlation-Id");

                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                }

                if (correlationId != null) {
                    requestTemplate.header("X-Correlation-Id", correlationId);
                }
            }
        };
    }
}