package com.stayease.booking_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotNull(message = "Property ID is required")
    private Long propertyId;
    @NotNull(message = "Room ID is required")
    private Long roomId;
    @NotNull
    @FutureOrPresent(message = "Check-in date must be today or in the future.")
    private LocalDate checkInDate;
    @NotNull
    private LocalDate expectedVacateDate;
    @NotNull
    @Min(value = 1, message = "At least one guest is required.")
    private Integer numberOfGuests;
}