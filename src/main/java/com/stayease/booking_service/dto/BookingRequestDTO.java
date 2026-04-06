package com.stayease.booking_service.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequestDTO {
    private Long userId;
    private Long propertyId;
    @NotNull(message = "Room ID is required")
    private Long roomId;
    @NotNull(message = "Total price is required")
    @Min(value = 100, message = "Minimum price should be 100")
    @Max(value = 100000, message = "Price exceeds limit")
    private Double totalPrice;
}