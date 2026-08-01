package com.stayease.booking_service.integration;


import com.stayease.booking_service.config.UserClient;
import com.stayease.booking_service.dto.response.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceGateway {

    private final UserClient userClient;

    @Retry(name = "user-service")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserFallback")
    public UserResponse getUser(Long userId){
        log.info("Calling User Service");
        return userClient.getUser(userId);
    }

    public UserResponse getUserFallback(Long userId,Exception ex){
        log.error("User Service unavailable", ex);
        throw new RuntimeException("User Service is temporarily unavailable."+ex);
    }
}