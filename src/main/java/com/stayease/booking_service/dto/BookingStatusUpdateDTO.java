package com.stayease.booking_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    private String status;

    private String remarks;
}

