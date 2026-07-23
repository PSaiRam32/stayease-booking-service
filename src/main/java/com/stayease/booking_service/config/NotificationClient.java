package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.response.ApiResponse;
import com.stayease.booking_service.dto.request.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name="notification-service",
        url="${services.notification.url}",
        configuration=FeignConfig.class
)
public interface NotificationClient{
    @PostMapping("/notifications/send")
    ApiResponse<String> sendNotification(@RequestBody NotificationRequest request);
}