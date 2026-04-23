package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(
        name = "property-service",
        url = "${services.property.url}",
        configuration = FeignClientConfig.class
)
public interface PropertyClient {

    @GetMapping("properties/rooms/availability/{roomId}")
    ApiResponse<Boolean> checkAvailability(@PathVariable Long roomId);

    @PutMapping("/properties/rooms/reserverroom/{roomId}")
    void reserveRoom(@PathVariable Long roomId);

    @PutMapping("/properties/rooms/releaseroom/{roomId}")
    void releaseRoom(@PathVariable Long roomId);


}