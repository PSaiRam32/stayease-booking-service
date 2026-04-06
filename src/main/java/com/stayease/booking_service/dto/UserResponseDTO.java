package com.stayease.booking_service.dto;

import lombok.Data;

@Data
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String role;
}
