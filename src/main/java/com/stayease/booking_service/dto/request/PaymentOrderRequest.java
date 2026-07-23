package com.stayease.booking_service.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentOrderRequest{
    @NotNull(message = "Booking ID is required")
    @Min(value = 1, message = "Booking ID must be greater than 0")
    private Long bookingId;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.1", message = "Amount must be greater than 0")
    private Double amount;
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    private String description;
    private String customerEmail;
    private String customerPhone;
    private Long userId;
}