package com.stayease.booking_service.dto.response;

import com.stayease.booking_service.entity.BookingStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingResponse{
    private Long bookingId;
    private Long userId;
    private Long propertyId;
    private Long roomId;
    private Long ownerId;
    private BookingStatus bookingStatus;
    private Double bookingAmount;
    private LocalDateTime bookingDate;
    private LocalDate checkInDate;
    private LocalDate expectedVacateDate;
    private Integer numberOfGuests;
}