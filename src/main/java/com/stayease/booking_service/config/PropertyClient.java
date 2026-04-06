package com.stayease.booking_service.config;

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

    @GetMapping("/properties/{id}/availability")
    boolean checkAvailability(@PathVariable Long id);

    @PutMapping("/properties/rooms/{roomId}/reserve")
    void reserveRoom(@PathVariable Long roomId);

    @PutMapping("/properties/rooms/{roomId}/release")
    void releaseRoom(@PathVariable Long roomId);


}