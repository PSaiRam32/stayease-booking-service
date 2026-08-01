package com.stayease.booking_service.integration;

import com.stayease.booking_service.config.NotificationClient;
import com.stayease.booking_service.dto.request.NotificationRequest;
import com.stayease.booking_service.dto.response.ApiResponse;
import com.stayease.booking_service.dto.response.NotificationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceGateway {

    private final NotificationClient notificationClient;

    @Retry(name = "notification-service")
    @CircuitBreaker(name = "notification-service", fallbackMethod = "sendNotificationFallback")
    public ApiResponse<NotificationResponse> sendNotification(NotificationRequest request){
        log.info("Calling Notification Service");
        return notificationClient.sendNotification(request);
    }

    public ApiResponse<NotificationResponse> sendNotificationFallback(NotificationRequest request,Exception ex){
        log.error("Notification Service unavailable", ex);
        throw new RuntimeException("Notification Service is temporarily unavailable."+ ex);
    }
}