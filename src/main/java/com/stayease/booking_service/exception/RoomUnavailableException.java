package com.stayease.booking_service.exception;

public class RoomUnavailableException extends BusinessException {
    public RoomUnavailableException(String message) {
        super(message);
    }
}

