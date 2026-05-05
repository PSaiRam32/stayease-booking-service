package com.stayease.booking_service.controller;

import com.stayease.booking_service.dto.*;
import com.stayease.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/createbooking")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(
            @Valid @RequestBody BookingRequestDTO request) {
        log.info("POST /bookings/createbooking - Creating booking for room: {}", request.getRoomId());
        try {
            BookingResponseDTO response = bookingService.createBooking(request);
            log.info("Booking created successfully with ID: {}", response.getBookingId());
            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Booking created successfully", response)
            );
        } catch (Exception ex) {
            log.error("Error creating booking: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @GetMapping("/getbooking/{bookingId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBooking(@PathVariable Long bookingId) {
        log.info("GET /bookings/getbooking/{} - Fetching booking details", bookingId);
        try {
            BookingResponseDTO response = bookingService.getBooking(bookingId);
            log.info("Booking retrieved successfully: {}", bookingId);
            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Booking fetched successfully", response)
            );
        } catch (Exception ex) {
            log.error("Error fetching booking: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @PutMapping("/cancelbooking/{bookingId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> cancelBooking(@PathVariable Long bookingId) {
        log.info("PUT /bookings/cancelbooking/{} - Cancelling booking", bookingId);
        try {
            BookingResponseDTO response = bookingService.cancelBooking(bookingId);
            log.info("Booking cancelled successfully: {}", bookingId);
            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Booking cancelled successfully", response)
            );
        } catch (Exception ex) {
            log.error("Error cancelling booking: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

//    @GetMapping("/getuserbookinguser")
//    @PreAuthorize("hasRole('ROLE_USER')")
//    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getUserBookings() {
//        log.info("GET /bookings/getuserbookinguser - Fetching all user bookings");
//        try {
//            List<BookingResponseDTO> response = bookingService.getUserBookings();
//            log.info("Retrieved {} bookings for user", response.size());
//            return ResponseEntity.ok(
//                    new ApiResponse<>("SUCCESS", "User bookings fetched successfully", response)
//            );
//        } catch (Exception ex) {
//            log.error("Error fetching user bookings: {}", ex.getMessage(), ex);
//            throw ex;
//        }
//    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getBookingsByUserId(@PathVariable Long userId) {
        log.info("GET /bookings/user/{} - Fetching bookings for user", userId);
        try {
            List<BookingResponseDTO> response = bookingService.getBookingsByUserId(userId);
            log.info("Retrieved {} bookings for user ID: {}", response.size(), userId);
            return ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", "Bookings fetched successfully", response)
            );
        } catch (Exception ex) {
            log.error("Error fetching bookings for user ID: {}: {}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @PutMapping("/status/{bookingId}")
    public ResponseEntity<ApiResponse<String>> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestBody BookingStatusUpdateDTO request) {
        bookingService.updateBookingStatus(bookingId, request);
        return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", "Booking status updated", "OK")
        );
    }

    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResponse> confirmBooking(@PathVariable Long bookingId) {
        bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{bookingId}/fail")
    public ResponseEntity<ApiResponse> failBooking(@PathVariable Long bookingId) {
        bookingService.failBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}