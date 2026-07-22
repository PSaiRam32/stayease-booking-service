package com.stayease.booking_service.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
public class RoomAvailabilityResponse{
    private Long roomId;
    private Integer occupiedBeds;
}