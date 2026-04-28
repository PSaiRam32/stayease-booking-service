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
    private static final String PAYMENT_SUCCESS = "SUCCESS";

    @Retry(name = "userRetry")
    @CircuitBreaker(name = "userCB", fallbackMethod = "userFallback")
    private UserResponseDTO fetchUser(String userId) {
        log.info("Calling User Service for userId={}", userId);
        return userClient.getUser(userId);
    }

    private UserResponseDTO userFallback(String userId, Throwable ex) {
        log.error("User service FAILED. Fallback triggered for userId={}", userId, ex);
        UserResponseDTO user = new UserResponseDTO();
        user.setUserid(0L);
        user.setName("Customer");
        user.setEmail(null);
        user.setPhone(null);
        return user;
    }

    @Retry(name = "propertyRetry")
    @CircuitBreaker(name = "propertyCB", fallbackMethod = "propertyAvailabilityFallback")
    private ApiResponse<Boolean> checkAvailability(Long roomId) {
        log.info("Checking room availability for roomId={}", roomId);
        return propertyClient.checkAvailability(roomId);
    }

    private ApiResponse<Boolean> propertyAvailabilityFallback(Long roomId, Throwable ex) {
        log.error("Property service FAILED (availability). roomId={}", roomId, ex);
        ApiResponse<Boolean> fallback = new ApiResponse<>();
        fallback.setData(false);
        fallback.setStatus("FAILED");
        fallback.setMessage("Property service unavailable");

        return fallback;
    }

    @Retry(name = "propertyRetry")
    @CircuitBreaker(name = "propertyCB", fallbackMethod = "propertyReserveFallback")
    private void reserveRoomSafe(Long roomId) {
        log.info("Reserving room for roomId={}", roomId);
        propertyClient.reserveRoom(roomId);
    }

    private void propertyReserveFallback(Long roomId, Throwable ex) {
        log.error("Property service FAILED (reserve). roomId={}", roomId, ex);
        throw new BusinessException("Unable to reserve room at this time");
    }

    @Retry(name = "propertyRetry")
    @CircuitBreaker(name = "propertyCB", fallbackMethod = "propertyReleaseFallback")
    private void releaseRoomSafe(Long roomId) {
        log.info("Releasing room for roomId={}", roomId);
        propertyClient.releaseRoom(roomId);
    }

    private void propertyReleaseFallback(Long roomId, Throwable ex) {
        log.error("CRITICAL: Failed to release room. roomId={}", roomId, ex);
        // Do NOT throw → avoid breaking flow
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
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        String userId = getLoggedInUser();
        log.info("Creating booking for user: {} roomId={}", userId, request.getRoomId());
        validateBookingRequest(request, userId);
        reserveRoomOrThrow(request.getRoomId());
        Booking booking = createAndSaveBooking(request, userId);
        log.info("Booking created with ID: {}", booking.getBookingId());
        try {
            UserResponseDTO user = fetchUser(userId);
            PaymentOrderRequestDTO paymentRequest = buildPaymentRequest(booking, user);
            ApiResponse<PaymentOrderResponseDTO> paymentResponse = callPaymentService(paymentRequest);
            if (!isPaymentResponseValid(paymentResponse)) {
                log.error("Payment order creation failed for booking: {}", booking.getBookingId());
                throw new PaymentFailedException("Payment order creation failed");
            }
            PaymentOrderResponseDTO order = paymentResponse.getData();
            log.info("Payment order created. id={}, razorpayId={}", order.getId(), order.getRazorpayOrderId());
            return mapToDTO(booking);
        } catch (Exception ex) {
            log.error("Error during payment order creation for booking: {}", booking.getBookingId(), ex);
            handlePaymentFailure(booking, request.getRoomId());
            throw new BusinessException("Booking failed due to payment service error");
        }
    }

    private void validateBookingRequest(BookingRequestDTO request, String userId) {
        if (request.getTotalPrice() <= 0) {
            throw new BusinessException("Invalid booking amount");
        }
        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        bookingRepository.findByUserIdAndRoomIdAndStatusIn(userId, request.getRoomId(), activeStatuses)
                .ifPresent(b -> { throw new BusinessException("Booking already exists for this room"); });
    }
    private void reserveRoomOrThrow(Long roomId) {
        try {
            ApiResponse<Boolean> available = checkAvailability(roomId);
            if (!available.getData()) {
                throw new RoomUnavailableException("Room not available");
            }
            reserveRoomSafe(roomId);
        } catch (RoomUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Property service failed", ex);
            throw new BusinessException("Unable to process booking at this time");
        }
    }

    private Booking createAndSaveBooking(BookingRequestDTO request, String userId) {
        Booking booking = Booking.builder()
                .userId(userId)
                .roomId(request.getRoomId())
                .totalPrice(request.getTotalPrice())
                .status(BookingStatus.PENDING)
                .propertyId(request.getPropertyId())
                .build();
        return bookingRepository.save(booking);
    }
    private PaymentOrderRequestDTO buildPaymentRequest(Booking booking, UserResponseDTO user) {
        PaymentOrderRequestDTO req = new PaymentOrderRequestDTO();
        req.setBookingId(booking.getBookingId());
        req.setAmount(booking.getTotalPrice());
        req.setDescription("Booking payment for room " + booking.getRoomId());
        req.setCustomerEmail(user.getEmail());
        req.setCustomerPhone(user.getPhone());
        req.setPaymentMethod("ONLINE");
        return req;
    }
    private void handlePaymentFailure(Booking booking, Long roomId) {
        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);
        releaseRoom(booking);
    }

    private boolean isPaymentResponseValid(ApiResponse<PaymentOrderResponseDTO> resp) {
        return resp != null
                && resp.getStatus() != null
                && PAYMENT_SUCCESS.equalsIgnoreCase(resp.getStatus())
                && resp.getData() != null
                && resp.getData().getRazorpayOrderId() != null;
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

    private Booking getActiveBooking(Long bookingId) {
        return bookingRepository.findByBookingIdAndIsActiveTrue(bookingId)
                .orElseThrow(() -> {
                    log.warn("Active booking not found with ID: {}", bookingId);
                    return new BusinessException("Booking not found");
                });
    }

    private void validateOwnership(Booking booking) {
        String currentUser = getLoggedInUser();
        if (!booking.getUserId().equals(currentUser)) {
            log.warn("Unauthorized access attempt by user: {} for booking: {}", currentUser, booking.getBookingId());
            throw new BusinessException("Unauthorized access to this booking");
        }
    }

    private String getLoggedInUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Transactional
    public void updateBookingStatus(Long bookingId, BookingStatusUpdateDTO request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Booking not found"));
        booking.setStatus(BookingStatus.valueOf(request.getStatus().toUpperCase()));
        bookingRepository.save(booking);
    }

    private void releaseRoom(Booking booking) {
        try {
            releaseRoomSafe(booking.getRoomId());
            log.info("Room released successfully for bookingId={}", booking.getBookingId());
        } catch (Exception ex) {
            log.error("CRITICAL: Failed to release room. bookingId={}, roomId={}",
                    booking.getBookingId(), booking.getRoomId(), ex);
        }
    }

    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId) {
        log.info("Cancelling booking with ID: {}", bookingId);
        Booking booking = getActiveBooking(bookingId);
        validateOwnership(booking);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking already cancelled");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed bookings can be cancelled");
        }
        releaseRoom(booking);
        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);
        log.info("Booking cancelled successfully: {}", bookingId);
        try {
            UserResponseDTO user = fetchUser(booking.getUserId());
            String message = buildCancelMessage(user, booking);
            sendNotification(booking, user, BookingStatus.CANCELLED, message);
        } catch (Exception ex) {
            log.error("Notification failed for bookingId={}", bookingId, ex);
        }
        return mapToDTO(booking);
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
        try {
            UserResponseDTO user = fetchUser(booking.getUserId());
//            UserResponseDTO user = userClient.getUser(booking.getUserId());
            String message = buildConfirmMessage(user, booking);
            sendNotification(booking, user, BookingStatus.CONFIRMED, message);
        } catch (Exception ex) {
            log.error("Post-confirm external call failed bookingId={}", bookingId, ex);
        }
    }

    @Transactional
    public void failBooking(Long bookingId) {
        log.info("Initiating failure handling for booking: {}", bookingId);
        Booking booking = getActiveBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Skipping failBooking - already processed. bookingId={}, status={}",
                    bookingId, booking.getStatus());
            return;
        }
        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);
        log.info("Booking marked as FAILED. bookingId={}", bookingId);
        releaseRoom(booking);
        try {
            UserResponseDTO user = fetchUser(booking.getUserId());
//            UserResponseDTO user = userClient.getUser(booking.getUserId());
            String message = buildFailureMessage(user, booking);
            sendNotification(booking, user, BookingStatus.FAILED, message);
        } catch (Exception ex) {
            log.error("Notification failed for bookingId={}", bookingId, ex);
        }
    }
    

    private void sendNotification(Booking booking, UserResponseDTO user, BookingStatus status, String message) {
        if (user.getEmail() == null) {
            log.warn("Skipping notification: Email not available for bookingId={}", booking.getBookingId());
            return;
        }
        try {
            NotificationRequestDTO notification = new NotificationRequestDTO();
            notification.setBookingId(booking.getBookingId());
            notification.setUserId(user.getUserid());
            notification.setType("EMAIL");
            notification.setStatus(status.name());
            notification.setMessage(message);
            notification.setEmail(user.getEmail());
            notification.setPhoneNumber(user.getPhone());
            notificationClient.sendNotification(notification);
            log.info("Notification sent successfully. bookingId={}, status={}", booking.getBookingId(), status);

        } catch (Exception ex) {
            log.error("Notification failed. bookingId={}, status={}", booking.getBookingId(), status, ex);
        }
    }
    private String buildConfirmMessage(UserResponseDTO user, Booking booking) {
        return String.format(
                "Dear %s,\n\n" +
                        "We are pleased to inform you that your booking has been successfully confirmed.\n\n" +
                        "Booking Details:\n" +
                        "Booking ID   : %d\n" +
                        "Room ID      : %d\n" +
                        "Total Amount : ₹%.2f\n\n" +
                        "We look forward to hosting you.\n\n" +
                        "Best Regards,\nStayEase Team",
                user.getName(),
                booking.getBookingId(),
                booking.getRoomId(),
                booking.getTotalPrice()
        );
    }
    private String buildCancelMessage(UserResponseDTO user, Booking booking) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your booking has been successfully cancelled.\n\n" +
                        "Booking ID: %d\nRoom ID: %d\n\n" +
                        "Refund (if applicable) will be processed shortly.\n\n" +
                        "Regards,\nStayEase Team",
                user.getName(),
                booking.getBookingId(),
                booking.getRoomId()
        );
    }
    private String buildFailureMessage(UserResponseDTO user, Booking booking) {
        return String.format(
                "Dear %s,\n\n" +
                        "We regret to inform you that your booking could not be completed due to a payment failure.\n\n" +
                        "Booking Details:\n" +
                        "Booking ID : %d\n" +
                        "Room ID    : %d\n\n" +
                        "Please try again with a different payment method or contact support if the issue persists.\n\n" +
                        "We apologize for the inconvenience.\n\n" +
                        "Best Regards,\n" +
                        "StayEase Team",
                user.getName(),
                booking.getBookingId(),
                booking.getRoomId()
        );
    }

    private BookingResponseDTO mapToDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUserId())
                .roomId(booking.getRoomId())
                .status(booking.getStatus().name())
                .totalPrice(booking.getTotalPrice())
                .build();
    }
}