package com.stayease.booking_service.integration;


import com.stayease.booking_service.config.PropertyClient;
import com.stayease.booking_service.dto.response.ApiResponse;
import com.stayease.booking_service.dto.response.RoomDetailsResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyServiceGateway {

    private final PropertyClient propertyClient;

    @Retry(name = "property-service")
    @CircuitBreaker(name = "property-service", fallbackMethod = "getRoomDetailsFallback")
    public ApiResponse<RoomDetailsResponse> getRoomDetails(Long roomId){
        log.info("Calling Property Service");
        return propertyClient.getRoomDetails(roomId);
    }

    public ApiResponse<RoomDetailsResponse> getRoomDetailsFallback(Long roomId,Exception ex){
        log.error("Property Service unavailable", ex);
        throw new RuntimeException("Property Service is temporarily unavailable."+ex);
    }
}
