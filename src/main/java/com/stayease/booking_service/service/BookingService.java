package com.stayease.booking_service.service;

import com.stayease.booking_service.dto.request.BookingCancellationRequest;
import com.stayease.booking_service.dto.request.BookingRequest;
import com.stayease.booking_service.dto.request.BookingRescheduleRequest;
import com.stayease.booking_service.dto.response.*;

import java.util.List;

public interface BookingService{
    BookingResponse createBooking(BookingRequest request);
    BookingResponse getBooking(Long bookingId);
    List<BookingResponse> getBookingsByUserId(Long userId);
    void checkInBooking(Long bookingId);
    void checkOutBooking(Long bookingId);
    void completeBooking(Long bookingId);
    void cancelBooking(Long bookingId, BookingCancellationRequest request);
    List<BookingResponse> getBookingHistory();
    List<BookingResponse> getUpcomingBookings();
    List<BookingResponse> getCompletedBookings();
    void rescheduleBooking(Long bookingId, BookingRescheduleRequest request);
    void confirmBooking(Long bookingId);
    void failBooking(Long bookingId);
    List<OwnerBookingResponse > bookingsByOwnerId(Long ownerId);
    List<OwnerBookingResponse> getOwnerBookingHistory(Long ownerId);
    RevenueSummaryResponse getRevenueSummary(Long ownerId);
    OccupiedRoomCountResponse getOccupiedRoomCount(Long ownerId);
    UserBookingDashboardResponse getUserDashboard(Long userId);
}