package com.stayease.booking_service.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingCancellationRequest{
    @NotBlank(message = "Cancellation reason is required.")
    private String cancellationReason;
}