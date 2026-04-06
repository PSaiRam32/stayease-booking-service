package com.stayease.booking_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailRequestDTO {

    @NotNull
    private Long bookingId;
    @NotBlank
    private String email;
    private String type;
    @NotBlank
    private String message;


}


