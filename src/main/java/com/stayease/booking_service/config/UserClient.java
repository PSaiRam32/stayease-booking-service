package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="user-service",configuration=FeignConfig.class)
public interface UserClient{
    @GetMapping("/users/{id}")
    UserResponse getUser(@PathVariable Long id);
}