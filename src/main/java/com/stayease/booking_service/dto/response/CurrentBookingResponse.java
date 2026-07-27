package com.stayease.booking_service.dto.response;

import com.stayease.booking_service.entity.BookingStatus;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentBookingResponse{
    private Long bookingId;
    private Long propertyId;
    private String propertyName;
    private String address;
    private Long roomId;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status;
    private Double bookingAmount;
}