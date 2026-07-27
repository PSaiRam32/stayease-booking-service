package com.stayease.booking_service.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryResponse{
    private Double totalRevenue;
    private Double completedRevenue;
    private Double pendingRevenue;
    private Long totalBookings;
    private Long completedBookings;
    private Long pendingBookings;
    private Long cancelledBookings;
    private Double averageBookingValue;
}