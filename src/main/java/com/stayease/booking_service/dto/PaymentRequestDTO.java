package com.stayease.booking_service.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDTO {

    @NotNull
    private Long bookingId;
    @NotNull
    @Min(1)
    private Double amount;
    private String paymentMethod;

    public PaymentRequestDTO(Long bookingId, Double amount) {
        this.bookingId = bookingId;
        this.amount = amount;
    }
}