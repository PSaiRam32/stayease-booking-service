package com.stayease.booking_service.dto.request;

import lombok.Data;

@Data
public class NotificationRequest{
    private Long bookingId;
    private String email;
    private String phoneNumber;
    private String status; // BOOKING_CONFIRMED / PAYMENT_FAILED / BOOKING_CANCELLED
    private String type;
    private String message;
    private Long userId;
}