package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.EmailRequestDTO;
import com.stayease.booking_service.dto.SMSRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${services.notification.url}",
        configuration = FeignClientConfig.class
)
public interface NotificationClient {

    @PostMapping("/notifications/email")
    void sendEmail(@RequestBody EmailRequestDTO request);

    @PostMapping("/notifications/sms")
    void sendSms(@RequestBody SMSRequestDTO request);
}