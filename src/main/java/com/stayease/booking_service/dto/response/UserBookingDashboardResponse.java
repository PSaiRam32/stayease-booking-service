package com.stayease.booking_service.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBookingDashboardResponse{
    private Long totalBookings;
    private Long upcomingBookings;
    private Long currentBookings;
    private Long completedBookings;
    private Long cancelledBookings;
    private CurrentBookingResponse currentBooking;
}