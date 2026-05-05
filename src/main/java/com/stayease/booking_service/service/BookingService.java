package com.stayease.booking_service.service;

import com.stayease.booking_service.dto.*;
import java.util.List;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO request);
    BookingResponseDTO getBooking(Long bookingId);
    BookingResponseDTO cancelBooking(Long bookingId);
//    List<BookingResponseDTO> getUserBookings();
    void updateBookingStatus(Long bookingId, BookingStatusUpdateDTO request);
    void confirmBooking(Long bookingId);
    void failBooking(Long bookingId);
    List<BookingResponseDTO> getBookingsByUserId(Long userId);
}