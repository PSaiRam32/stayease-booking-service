package com.stayease.booking_service.service;

import com.stayease.booking_service.config.NotificationClient;
import com.stayease.booking_service.config.PaymentClient;
import com.stayease.booking_service.config.PropertyClient;
import com.stayease.booking_service.config.UserClient;
import com.stayease.booking_service.dto.*;
import com.stayease.booking_service.entity.*;
import com.stayease.booking_service.exception.BusinessException;
import com.stayease.booking_service.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.List;
import java.util.Optional;

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
        Boolean available = propertyClient.checkAvailability(request.getRoomId());
        if (Boolean.FALSE.equals(available)) {
            throw new BusinessException("Room not available");
        }
        propertyClient.reserveRoom(request.getRoomId());
        Booking booking = Booking.builder()
                .userId(userId)
                .roomId(request.getRoomId())
                .totalPrice(request.getTotalPrice())
                .status(BookingStatus.PENDING)
                .build();
        booking = bookingRepository.save(booking);
        try {
            PaymentRequestDTO paymentRequest = new PaymentRequestDTO(booking.getId(), booking.getTotalPrice());
            PaymentResponseDTO paymentResponse = callPaymentService(paymentRequest);
            if (!"SUCCESS".equalsIgnoreCase(paymentResponse.getPaymentStatus())) {
                booking.setStatus(BookingStatus.FAILED);
                bookingRepository.save(booking);
                propertyClient.releaseRoom(request.getRoomId());
                return mapToDTO(booking);
            }
            booking.setStatus(BookingStatus.CONFIRMED);
            booking = bookingRepository.save(booking);
            EmailRequestDTO emailRequest = new EmailRequestDTO();
            UserResponseDTO user = userClient.getUser(userId);
            emailRequest.setEmail(user.getEmail());
            emailRequest.setMessage("Booking confirmed. ID: " + booking.getId());
            notificationClient.sendEmail(emailRequest);
            return mapToDTO(booking);
        } catch (Exception ex) {
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            try {
                propertyClient.releaseRoom(request.getRoomId());
            } catch (Exception releaseEx) {
                System.out.println("Failed to release room: " + releaseEx.getMessage());
            }
            throw new BusinessException("Booking failed. Rolled back.");
        }
    }

    @Override
    public BookingResponseDTO getBooking(Long id) {
        Booking booking = getActiveBooking(id);
        validateOwnership(booking);
        return mapToDTO(booking);
    }

    @Override
    public BookingResponseDTO cancelBooking(Long id) {
        Booking booking = getActiveBooking(id);
        validateOwnership(booking);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking already cancelled");
        } else if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed bookings can be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return mapToDTO(bookingRepository.save(booking));
    }

    @Override
    public List<BookingResponseDTO> getUserBookings() {
        String userId = getLoggedInUser();
        return bookingRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
    private Booking getActiveBooking(Long id) {
        return bookingRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BusinessException("Booking not found"));
    }

    private void validateOwnership(Booking booking) {
        if (!booking.getUserId().equals(getLoggedInUser())) {
            throw new BusinessException("Unauthorized access");
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
    public PaymentResponseDTO callPaymentService(PaymentRequestDTO request) {
        return paymentClient.initiatePayment(request);
    }

    public PaymentResponseDTO paymentFallback(PaymentRequestDTO request, Throwable ex) {
        System.out.println("Payment service failed. Triggering fallback: " + ex.getMessage());
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentStatus("FAILED");
        return response;
    }
}