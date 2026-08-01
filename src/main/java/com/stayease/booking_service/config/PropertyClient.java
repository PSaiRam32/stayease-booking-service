package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.response.ApiResponse;
import com.stayease.booking_service.dto.response.RoomDetailsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="property-service",configuration=FeignConfig.class)
public interface PropertyClient{

    @GetMapping("/properties/rooms/{roomId}")
    ApiResponse<RoomDetailsResponse> getRoomDetails(@PathVariable Long roomId);

}