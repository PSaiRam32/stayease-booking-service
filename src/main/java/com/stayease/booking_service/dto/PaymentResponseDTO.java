package com.stayease.booking_service.dto;


import lombok.Data;

@Data
public class PaymentResponseDTO {

    private Long id;
    private Long bookingId;
    private Double amount;
    private String paymentStatus;
}

