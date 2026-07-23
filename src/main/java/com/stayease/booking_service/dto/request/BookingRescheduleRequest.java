package com.stayease.booking_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
public class BookingRescheduleRequest{
    @NotNull
    private LocalDate checkInDate;
    @NotNull
    private LocalDate expectedVacateDate;
    @Min(1)
    private Integer numberOfGuests;
}