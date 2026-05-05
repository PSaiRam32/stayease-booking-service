package com.stayease.booking_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingResponseDTO {

    private Long bookingId;
    private String userId;
    private Long propertyId;
    private Long roomId;
//    private String propertyName;
    private String bookingStatus;
    private Double totalPrice;
}