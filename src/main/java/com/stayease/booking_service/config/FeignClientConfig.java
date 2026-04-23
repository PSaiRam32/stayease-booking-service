package com.stayease.booking_service.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
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
                    log.debug("Authorization header propagated to Feign client");
                }
                if (correlationId != null) {
                    requestTemplate.header("X-Correlation-Id", correlationId);
                    log.debug("Correlation ID propagated to Feign client: {}", correlationId);
                }
                log.debug("Feign request prepared for {}", requestTemplate.request().url());
            }
        };
    }
}