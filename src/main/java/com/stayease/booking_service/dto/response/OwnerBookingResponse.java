package com.stayease.booking_service.dto.response;


import com.stayease.booking_service.entity.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class OwnerBookingResponse{
    private Long bookingId;
    private Long propertyId;
    private Long roomId;
    private Long userId;
    private BookingStatus bookingStatus;
    private Double bookingAmount;
    private Integer numberOfGuests;
    private LocalDate checkInDate;
    private LocalDate expectedVacateDate;
    private LocalDateTime bookingDate;
}