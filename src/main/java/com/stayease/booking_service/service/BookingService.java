package com.stayease.booking_service.service;

import com.stayease.booking_service.dto.*;
import java.util.List;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO request);
    BookingResponseDTO getBooking(Long id);
    BookingResponseDTO cancelBooking(Long id);
    List<BookingResponseDTO> getUserBookings();
}