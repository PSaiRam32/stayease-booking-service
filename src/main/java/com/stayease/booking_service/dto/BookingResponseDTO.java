package com.stayease.booking_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingResponseDTO {

    private Long bookingId;
    private String userId;
    private Long roomId;
    private String status;
    private Double totalPrice;
}