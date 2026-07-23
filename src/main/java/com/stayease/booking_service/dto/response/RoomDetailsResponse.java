package com.stayease.booking_service.dto.response;

import com.stayease.booking_service.entity.PropertyStatus;
import com.stayease.booking_service.entity.WashroomType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailsResponse{
    private Long roomId;
    private Long propertyId;
    private Long ownerId;
    private Integer sharingCapacity;
    private Double price;
//    private Integer availableCount;
    private WashroomType washroomType;
    private PropertyStatus propertyStatus;
}