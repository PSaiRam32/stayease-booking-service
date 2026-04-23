package com.stayease.booking_service.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PaymentOrderResponseDTO {

    private Long id;
    private Long bookingId;
    private Double amount;
    private String razorpayOrderId;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
}

