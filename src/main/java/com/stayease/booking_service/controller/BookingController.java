package com.stayease.booking_service.controller;

import com.stayease.booking_service.dto.request.BookingCancellationRequest;
import com.stayease.booking_service.dto.request.BookingRequest;
import com.stayease.booking_service.dto.request.BookingRescheduleRequest;
import com.stayease.booking_service.dto.response.ApiResponse;
import com.stayease.booking_service.dto.response.BookingResponse;
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
public class BookingController{

    private final BookingService bookingService;

    @PostMapping("/createbooking")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request){
            BookingResponse response=bookingService.createBooking(request);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking created successfully", response));
    }

    @GetMapping("/getbooking/{bookingId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long bookingId){
            BookingResponse response=bookingService.getBooking(bookingId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking fetched successfully", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingsByUserId(@PathVariable Long userId){
            List<BookingResponse> response=bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Bookings fetched successfully", response));
    }

    @PutMapping("/{bookingId}/check-in")
    public ResponseEntity<ApiResponse<Void>> checkIn(@PathVariable Long bookingId){
        bookingService.checkInBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "CheckIn Completed successfully.", null));
    }

    @PutMapping("/{bookingId}/check-out")
    public ResponseEntity<ApiResponse<Void>> checkOut(@PathVariable Long bookingId){
        bookingService.checkOutBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Checkout Completed successfully.", null));
    }

    @PutMapping("/{bookingId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeBooking(@PathVariable Long bookingId){
        bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking Completed successfully.", null));
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long bookingId,@Valid @RequestBody BookingCancellationRequest request){
        bookingService.cancelBooking(bookingId,request);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking Cancelled  successfully.", null));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingHistory(){
        List<BookingResponse> response=bookingService.getBookingHistory();
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking history fetched successfully.", response));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUpcomingBookings(){
        List<BookingResponse> response=bookingService.getUpcomingBookings();
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Upcoming Bookings fetched successfully.", response));
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getCompletedBookings(){
        List<BookingResponse> response=bookingService.getCompletedBookings();
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Completed Bookings fetched successfully.", response));
    }


    @PutMapping("/{bookingId}/reschedule")
    public ResponseEntity<ApiResponse<Void>> rescheduleBooking(@PathVariable Long bookingId,@Valid @RequestBody BookingRescheduleRequest request){
        bookingService.rescheduleBooking(bookingId,request);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking rescheduled successfully.", null));
    }

    //Endpoints used in Payment Servic
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmBooking(@PathVariable Long bookingId){
        bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS","Booking Confirmed Successfully",null));
    }

    @PutMapping("/{bookingId}/fail")
    public ResponseEntity<ApiResponse<Void>> failBooking(@PathVariable Long bookingId){
        bookingService.failBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS","Booking Failed",null));
    }

    //    @PutMapping("/cancelbooking/{bookingId}")
//    @PreAuthorize("hasRole('ROLE_USER')")
//    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long bookingId,@RequestBody ){
//            BookingResponse response = bookingService.cancelBooking(bookingId);
//            log.info("Booking cancelled successfully: {}", bookingId);
//            return ResponseEntity.ok(
//                    new ApiResponse<>("SUCCESS", "Booking cancelled successfully", response)
//            );
//    }

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

    //    @PutMapping("/status/{bookingId}")
//    public ResponseEntity<ApiResponse<String>> updateBookingStatus(@PathVariable Long bookingId,@RequestBody BookingStatusUpdate request) {
//        bookingService.updateBookingStatus(bookingId, request);
//        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Booking status updated", "OK"));
//    }

}