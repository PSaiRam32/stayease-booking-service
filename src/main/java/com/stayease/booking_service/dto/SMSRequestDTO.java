package com.stayease.booking_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SMSRequestDTO {

    @NotNull
    private Long bookingId;
    @NotBlank
    private String userId;
    private String type;
    @NotBlank
    private String message;
}