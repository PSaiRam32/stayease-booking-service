package com.stayease.booking_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationResponse{
    private Long id;
    private Long bookingId;
    private String userId;
    private String type;
    private String status;
}