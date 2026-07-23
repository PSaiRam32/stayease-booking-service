package com.stayease.booking_service.dto.response;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderResponse {
    private Long paymentId;
    private Long bookingId;
    private Long userId;
    private Double amount;
    private Double refundAmount;
    private String currency;
    private String receiptNumber;
    private String razorpayOrderId;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime refundedAt;
}

