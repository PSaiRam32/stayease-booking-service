package com.stayease.booking_service.config;
import feign.*;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.Duration;

@Slf4j
@Configuration
public class FeignConfig{

    @Value("${feign.client.config.default.connect-timeout}")
    private long connectTimeout;
    @Value("${feign.client.config.default.read-timeout}")
    private long readTimeout;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return  Logger.Level.FULL;
    }

    @Bean
    public Retryer retryer(){
        return new Retryer.Default(1000,2000,3);
    }

    @Bean
    public Request.Options options(){
        return new Request.Options(Duration.ofMillis(connectTimeout),Duration.ofMillis(readTimeout), true);
    }

    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            log.debug("Processing Feign request interception");
            ServletRequestAttributes attributes=(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes!=null){
                HttpServletRequest request=attributes.getRequest();
                String authHeader=request.getHeader("Authorization");
                String correlationId=request.getHeader("X-Correlation-Id");
                if(authHeader!=null){
                    log.debug("Adding Authorization header to Feign request");
                    requestTemplate.header("Authorization",authHeader);
                }
                if(correlationId!=null){
                    log.debug("Adding X-Correlation-Id header to Feign request: {}", correlationId);
                    requestTemplate.header("X-Correlation-Id", correlationId);
                }
            } else{
                log.debug("No request attributes found for Feign request interception");
            }
        };
    }

    @Bean
    public ErrorDecoder decode() {
        return (methodKey, response) -> {
            int status=response.status();
            return switch (status) {
                case 400 -> new RuntimeException("Bad Request from downstream service");
                case 404 -> new RuntimeException("Resource not found in downstream service");
                case 500 -> new RuntimeException("Internal server error in downstream service");
                default -> new RuntimeException("Feign client error: " + status);
            };
        };
    }

}
