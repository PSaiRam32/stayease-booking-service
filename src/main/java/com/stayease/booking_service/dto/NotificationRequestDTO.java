package com.stayease.booking_service.dto;

import com.stayease.booking_service.entity.BookingStatus;
import lombok.Data;
import java.util.List;

@Data
public class NotificationRequestDTO {

    private Long bookingId;
    private String email;
    private String phoneNumber;
    private String status; // BOOKING_CONFIRMED / PAYMENT_FAILED / BOOKING_CANCELLED
    private String type;
    private String message;
    private Long userId;
}