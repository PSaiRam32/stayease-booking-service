package com.stayease.booking_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse{
    private Long refundId;
    private Long paymentId;
    private Long bookingId;
    private Double amount;
    private String currency;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
}