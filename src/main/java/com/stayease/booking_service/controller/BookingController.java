package com.stayease.booking_service.controller;

import com.stayease.booking_service.dto.*;
import com.stayease.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(
            @Valid @RequestBody BookingRequestDTO request) {
        return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", "Booking created",
                        bookingService.createBooking(request))
        );
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", "Booking fetched",
                        bookingService.getBooking(id))
        );
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", "Booking cancelled",
                        bookingService.cancelBooking(id))
        );
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getUserBookings() {
        return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", "User bookings fetched",
                        bookingService.getUserBookings())
        );
    }

}