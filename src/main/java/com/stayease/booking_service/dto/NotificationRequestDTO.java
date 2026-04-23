package com.stayease.booking_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationRequestDTO {

    private Long bookingId;
    private String email;
    private String phoneNumber;
    private String type;      // BOOKING_CONFIRMED / PAYMENT_FAILED / BOOKING_CANCELLED
    private String message;
    private List<String> channels; // ["EMAIL", "SMS"]
}