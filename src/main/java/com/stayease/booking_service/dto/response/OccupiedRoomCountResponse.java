package com.stayease.booking_service.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupiedRoomCountResponse{
    private Long occupiedRooms;
}
