package com.stayease.booking_service.service;

import com.stayease.booking_service.config.NotificationClient;
import com.stayease.booking_service.config.PaymentClient;
import com.stayease.booking_service.config.PropertyClient;
import com.stayease.booking_service.config.UserClient;
import com.stayease.booking_service.dto.*;
import com.stayease.booking_service.entity.*;
import com.stayease.booking_service.exception.*;
import com.stayease.booking_service.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyClient propertyClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        String userId = getLoggedInUser();
        log.info("Creating booking for user: {} with room ID: {}", userId, request.getRoomId());
        if (request.getTotalPrice() <= 0) {
            throw new BusinessException("Invalid booking amount");
        }
        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        Optional<Booking> existingBooking =
                bookingRepository.findByUserIdAndRoomIdAndStatusIn(
                        userId,
                        request.getRoomId(),
                        activeStatuses
                );
        if (existingBooking.isPresent()) {
            throw new BusinessException("Booking already exists for this room");
        }
        ApiResponse<Boolean> available = propertyClient.checkAvailability(request.getRoomId());
        if (!available.getData()) {
            throw new RoomUnavailableException("Room not available for booking");
        }
        try {
            propertyClient.reserveRoom(request.getRoomId());
        } catch (Exception ex) {
            throw new BusinessException("Failed to reserve room");
        }
        Booking booking = Booking.builder()
                .userId(userId)
                .roomId(request.getRoomId())
                .totalPrice(request.getTotalPrice())
                .status(BookingStatus.PENDING)
                .propertyId(request.getPropertyId())
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking created with ID: {} and status: PENDING", booking.getId());
        try {
            PaymentOrderRequestDTO paymentRequest = new PaymentOrderRequestDTO();
            paymentRequest.setBookingId(booking.getId());
            paymentRequest.setAmount(booking.getTotalPrice());
            paymentRequest.setPaymentMethod("ONLINE");
            log.info("Calling Payment Service to create order for booking: {}", booking.getId());
            ApiResponse<PaymentOrderResponseDTO> paymentResponse = callPaymentService(paymentRequest);
            if (paymentResponse == null || !"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
                log.error("Payment order creation failed for booking: {}", booking.getId());
                booking.setStatus(BookingStatus.FAILED);
                bookingRepository.save(booking);
                try {
                    propertyClient.releaseRoom(request.getRoomId());
                } catch (Exception releaseEx) {
                    log.error("Failed to release room after payment failure", releaseEx);
                }
                throw new PaymentFailedException("Payment order creation failed");
            }
            PaymentOrderResponseDTO order = paymentResponse.getData();

            log.info("Payment order created successfully. OrderId: {}, RazorpayOrderId: {}",
                    order.getId(), order.getRazorpayOrderId());
            return BookingResponseDTO.builder()
                    .bookingId(booking.getId())
                    .userId(booking.getUserId())
                    .roomId(booking.getRoomId())
                    .status(booking.getStatus().name())
                    .totalPrice(booking.getTotalPrice())
                    .build();
        } catch (Exception ex) {
            log.error("Error during payment order creation for booking: {}", booking.getId(), ex);
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            try {
                propertyClient.releaseRoom(request.getRoomId());
            } catch (Exception releaseEx) {
                log.error("Failed to release room after exception", releaseEx);
            }
            throw new BusinessException("Booking failed due to payment service error");
        }
    }

    @Override
    public BookingResponseDTO getBooking(Long id) {
        log.info("Fetching booking with ID: {}", id);
        Booking booking = getActiveBooking(id);
        validateOwnership(booking);
        log.debug("Booking retrieved successfully: {}", id);
        return mapToDTO(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO cancelBooking(Long id) {
        log.info("Cancelling booking with ID: {}", id);
        Booking booking = getActiveBooking(id);
        validateOwnership(booking);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking already cancelled");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed bookings can be cancelled");
        }
        try {
            propertyClient.releaseRoom(booking.getRoomId());
            booking.setStatus(BookingStatus.CANCELLED);
            booking = bookingRepository.save(booking);
            log.info("Booking cancelled successfully: {}", id);
            NotificationRequestDTO notification = new NotificationRequestDTO();
            notification.setBookingId(booking.getId());
            notification.setType("BOOKING_CANCELLED");
            notification.setMessage("Your booking has been cancelled. Booking ID: " + booking.getId());
            UserResponseDTO user = userClient.getUser(booking.getUserId());
            notification.setEmail(user.getEmail());
            notification.setPhoneNumber(user.getPhone());
            notification.setChannels(List.of("EMAIL", "SMS"));
            notificationClient.sendNotification(notification);
            return mapToDTO(booking);
        } catch (Exception ex) {
            log.error("Error cancelling booking: {}", id, ex);
            throw new BusinessException("Failed to cancel booking. Please try again.");
        }
    }

    @Override
    public List<BookingResponseDTO> getUserBookings() {
        String userId = getLoggedInUser();
        log.info("Fetching all bookings for user: {}", userId);
        List<BookingResponseDTO> bookings = bookingRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
        log.debug("Retrieved {} bookings for user: {}", bookings.size(), userId);
        return bookings;
    }

    private Booking getActiveBooking(Long id) {
        return bookingRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> {
                    log.warn("Active booking not found with ID: {}", id);
                    return new BusinessException("Booking not found");
                });
    }

    private void validateOwnership(Booking booking) {
        String currentUser = getLoggedInUser();
        if (!booking.getUserId().equals(currentUser)) {
            log.warn("Unauthorized access attempt by user: {} for booking: {}", currentUser, booking.getId());
            throw new BusinessException("Unauthorized access to this booking");
        }
    }

    private String getLoggedInUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private BookingResponseDTO mapToDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .roomId(booking.getRoomId())
                .status(booking.getStatus().name())
                .totalPrice(booking.getTotalPrice())
                .build();
    }

    @Retry(name = "paymentRetry")
    @CircuitBreaker(name = "paymentCB", fallbackMethod = "paymentFallback")
    public ApiResponse<PaymentOrderResponseDTO> callPaymentService(PaymentOrderRequestDTO request) {
        log.info("Calling Payment Service (Feign) for booking: {}", request.getBookingId());
        return paymentClient.createPaymentOrder(request);
    }

    public ApiResponse<PaymentOrderResponseDTO> paymentFallback(
            PaymentOrderRequestDTO request,
            Throwable ex) {
        log.error("Payment service FAILED (Fallback triggered) for booking: {}",
                request.getBookingId(), ex);
        ApiResponse<PaymentOrderResponseDTO> fallbackResponse = new ApiResponse<>();
        fallbackResponse.setStatus("FAILED");
        fallbackResponse.setMessage("Payment service unavailable");
        fallbackResponse.setData(null);
        return fallbackResponse;
    }

    @Transactional
    public void updateBookingStatus(Long bookingId, BookingStatusUpdateDTO request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        booking.setStatus(BookingStatus.valueOf(request.getStatus().toUpperCase()));
        bookingRepository.save(booking);
    }

    @Transactional
    public void confirmBooking(Long bookingId) {
        Booking booking = getActiveBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Booking already processed: {}", bookingId);
            return; // idempotent
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        log.info("Booking CONFIRMED: {}", bookingId);
        System.out.println("Booking Confirmed: " + bookingId);
        UserResponseDTO user = userClient.getUser(booking.getUserId());
        NotificationRequestDTO notification = new NotificationRequestDTO();
        notification.setBookingId(bookingId);
        notification.setType("BOOKING_CONFIRMED");
        notification.setMessage("Your booking is confirmed. Booking ID: " + bookingId);
        notification.setEmail(user.getEmail());
        notification.setPhoneNumber(user.getPhone());
        notification.setChannels(List.of("EMAIL", "SMS"));
        notificationClient.sendNotification(notification);
    }

    @Transactional
    public void failBooking(Long bookingId) {
        Booking booking = getActiveBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            return;
        }
        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);
        try {
            propertyClient.releaseRoom(booking.getRoomId());
        } catch (Exception ex) {
            log.error("Failed to release room during payment failure", ex);
        }
        log.info("Booking FAILED: {}", bookingId);
    }
}