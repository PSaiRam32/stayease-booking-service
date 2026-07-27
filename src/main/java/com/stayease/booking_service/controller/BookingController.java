package com.stayease.booking_service.controller;

import com.stayease.booking_service.dto.request.BookingCancellationRequest;
import com.stayease.booking_service.dto.request.BookingRequest;
import com.stayease.booking_service.dto.request.BookingRescheduleRequest;
import com.stayease.booking_service.dto.response.*;
import com.stayease.booking_service.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
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
public class BookingController{

    private final BookingService bookingService;

    @PostMapping("/createbooking")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary="Create Booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request){
            BookingResponse response=bookingService.createBooking(request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking created successfully", response));
    }

    @GetMapping("/getbooking/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary="Get Booking by Booking ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long bookingId){
            BookingResponse response=bookingService.getBooking(bookingId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking fetched successfully", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary="Create Booking by User ID")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByUserId(@PathVariable Long userId){
            List<BookingResponse> response=bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Bookings fetched successfully", response));
    }

    @PutMapping("/{bookingId}/check-in")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    @Operation(summary="Check In")
    public ResponseEntity<ApiResponse<Void>> checkIn(@PathVariable Long bookingId){
        bookingService.checkInBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "CheckIn Completed successfully.", null));
    }

    @PutMapping("/{bookingId}/check-out")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    @Operation(summary="Check Out")
    public ResponseEntity<ApiResponse<Void>> checkOut(@PathVariable Long bookingId){
        bookingService.checkOutBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Checkout Completed successfully.", null));
    }

    @PutMapping("/{bookingId}/complete")
    @Operation(summary="Completed Booking")
    public ResponseEntity<ApiResponse<Void>> completeBooking(@PathVariable Long bookingId){
        bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking Completed successfully.", null));
    }

    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    @Operation(summary="Cancel Booking")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long bookingId,@Valid @RequestBody BookingCancellationRequest request){
        bookingService.cancelBooking(bookingId,request);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking Cancelled  successfully.", null));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    @Operation(summary="Get Booking History")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingHistory(){
        List<BookingResponse> response=bookingService.getBookingHistory();
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking history fetched successfully.", response));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    @Operation(summary="Get Upcoming Bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUpcomingBookings(){
        List<BookingResponse> response=bookingService.getUpcomingBookings();
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Upcoming Bookings fetched successfully.", response));
    }

    @GetMapping("/completed")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    @Operation(summary="Get Completed Bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getCompletedBookings(){
        List<BookingResponse> response=bookingService.getCompletedBookings();
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Completed Bookings fetched successfully.", response));
    }


    @PutMapping("/{bookingId}/reschedule")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    @Operation(summary="Reschedule Bookings")
    public ResponseEntity<ApiResponse<Void>> rescheduleBooking(@PathVariable Long bookingId,@Valid @RequestBody BookingRescheduleRequest request){
        bookingService.rescheduleBooking(bookingId,request);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking rescheduled successfully.", null));
    }

    //Endpoints used in Payment Servic
    @PutMapping("/{bookingId}/confirm")
    @Operation(summary="Payment Internal - Confirm Bookings")
    public ResponseEntity<ApiResponse<Void>> confirmBooking(@PathVariable Long bookingId){
        bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS","Booking Confirmed Successfully",null));
    }

    @PutMapping("/{bookingId}/fail")
    @Operation(summary="Payment Internal - Fail Bookings")
    public ResponseEntity<ApiResponse<Void>> failBooking(@PathVariable Long bookingId){
        bookingService.failBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS","Booking Failed",null));
    }

    @GetMapping("/ownerbookings/{ownerId}")
    @Operation(summary="Owner Internal - View Bookings")
    public ResponseEntity<ApiResponse<List<OwnerBookingResponse>>> viewallbookingsforownedproperties(@Valid @PathVariable Long ownerId){
        List<OwnerBookingResponse>  response=bookingService.bookingsByOwnerId(ownerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS","Bookings fetched successfully",response));
    }

    @GetMapping("/ownerbookinghistory/{ownerId}")
    @Operation(summary="Owner Internal - Get Booking History")
    public ResponseEntity<ApiResponse<List<OwnerBookingResponse>>> getBookingHistory(@PathVariable Long ownerId){
        List<OwnerBookingResponse> response=bookingService.getOwnerBookingHistory(ownerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS","Booking history fetched successfully",response));
    }

    @GetMapping("/revenuesummary/{ownerId}")
    @Operation(summary="Owner Internal - Revenue Summary")
    public ResponseEntity<ApiResponse<RevenueSummaryResponse>> getRevenueSummary(@PathVariable Long ownerId){
        RevenueSummaryResponse response=bookingService.getRevenueSummary(ownerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS","Revenue summary fetched successfully",response));
    }

    @GetMapping("/occupied-room-count/{ownerId}")
    @Operation(summary="Owner Internal - Get Occupied Room Count")
    public ResponseEntity<ApiResponse<OccupiedRoomCountResponse>> getOccupiedRoomCount(@PathVariable Long ownerId){
        OccupiedRoomCountResponse response=bookingService.getOccupiedRoomCount(ownerId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS","Occupied room count fetched successfully",response));
    }

    @GetMapping("/userssummary/{userId}")
    @Operation(summary="User Internal - Get User Dashboard Details")
    public ResponseEntity<ApiResponse<UserBookingDashboardResponse>>  getUserDashboard(@PathVariable Long userId){
        UserBookingDashboardResponse response=bookingService.getUserDashboard(userId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS","Users summary fetched successfully",response));
    }

}