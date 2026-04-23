package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.ApiResponse;
import com.stayease.booking_service.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${services.notification.url}",
        configuration = FeignClientConfig.class
)
public interface NotificationClient {

    @PostMapping("/notifications/send")
    ApiResponse<String> sendNotification(
            @RequestBody NotificationRequestDTO request
    );
}