package com.stayease.booking_service.dto.response;

import lombok.Data;

@Data
public class UserResponse{
    private Long userid;
    private String name;
    private String email;
    private String role;
    private String phone;
}
