package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "${services.user.url}",
        configuration = FeignClientConfig.class
)
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponseDTO getUser(@PathVariable String id);
}