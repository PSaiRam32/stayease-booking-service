package com.stayease.booking_service.service;

import com.stayease.booking_service.config.NotificationClient;
import com.stayease.booking_service.config.PaymentClient;
import com.stayease.booking_service.config.PropertyClient;
import com.stayease.booking_service.config.UserClient;
import com.stayease.booking_service.dto.request.*;
import com.stayease.booking_service.dto.response.*;
import com.stayease.booking_service.entity.*;
import com.stayease.booking_service.exception.*;
import com.stayease.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final PropertyClient propertyClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;
    private final UserClient userClient;

    @Transactional
    @Override
    public BookingResponse createBooking(BookingRequest request){
        Long userId=getCurrentUserId();
        log.info("Creating booking for user: {} roomId={}", userId, request.getRoomId());
        RoomDetailsResponse room=getRoomDetails(request.getRoomId()).getData();
        log.info("Room object = {}", room);
        log.info("Status = {}", room.getPropertyStatus());
        log.info("Capacity = {}", room.getSharingCapacity());
        validateBookingRequest(request, room);
        validateRoomCapacity(request,room);
        Booking booking=createAndSaveBooking(request,userId,room);
        log.info("Booking created with ID: {}", booking.getBookingId());
            UserResponse user = fetchUser(userId);
            PaymentOrderRequest paymentRequest = buildPaymentRequest(booking, user);
            PaymentOrderResponse paymentResponse;
            try {
                paymentResponse = callPaymentService(paymentRequest).getData();
            } catch(BusinessException ex){
                // I know how to recover only from this case.
                if(ex.getMessage().contains("already exists")){
                    log.info("Existing payment order found for booking={}",booking.getBookingId());
                    paymentResponse = paymentClient.getPaymentByBookingId(booking.getBookingId()).getData();
                }
                // I don't know how to recover from any other BusinessException.
                // So I pass it to the GlobalExceptionHandler.
                else{
                    throw ex;
                }
            }
            if(!isPaymentResponseValid(paymentResponse)){
                throw new PaymentFailedException("Payment order creation failed");
            }
            log.info("Payment order created successfully. paymentId={}, razorpayOrderId={}",paymentResponse.getPaymentId(), paymentResponse.getRazorpayOrderId());
            return mapToBookingResponse(booking);
    }

    @Override
    public BookingResponse getBooking(Long id){
        log.info("Fetching booking with ID: {}",id);
        Booking booking = getActiveBooking(id);
        validateOwnership(booking);
        log.debug("Booking retrieved successfully: {}",id);
        return mapToBookingResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId){
        log.info("Fetching all bookings for user ID: {}", userId);
        List<BookingResponse> bookings = bookingRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .toList();
        log.debug("Retrieved {} bookings for user ID: {}", bookings.size(), userId);
        return bookings;
    }

//    @Override
//    @Transactional
//    public void updateBookingStatus(Long bookingId, BookingStatusUpdate request) {
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new BusinessException("Booking not found"));
//        booking.setStatus(BookingStatus.valueOf(request.getStatus().toUpperCase()));
//        bookingRepository.save(booking);
//    }

    @Transactional
    @Override
    public void checkInBooking(Long bookingId){
        Booking booking=getActiveBooking(bookingId);
        validateOwnership(booking);
        if(booking.getStatus()!=BookingStatus.CONFIRMED){
            throw new BusinessException("Only confirmed bookings can be checked in.");
        }
        if(LocalDate.now().isBefore(booking.getCheckInDate())){
            throw new BusinessException("Check-in is not allowed before the booking start date.");
        }
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
        notifyBooking(booking,BookingStatus.CHECKED_IN,"CHECK_IN",null);
        log.info("Booking checked in successfully. bookingId={}", bookingId);
    }

    @Transactional
    @Override
    public void checkOutBooking(Long bookingId){
        Booking booking=getActiveBooking(bookingId);
        validateOwnership(booking);
        if (booking.getStatus()!=BookingStatus.CHECKED_IN){
            throw new BusinessException("Only checked-in bookings can be checked out.");
        }
        booking.setStatus(BookingStatus.CHECKED_OUT);
        bookingRepository.save(booking);
        notifyBooking(booking,BookingStatus.CHECKED_OUT,"CHECK_OUT",null);
        log.info("Booking checked out successfully. bookingId={}", bookingId);
    }

    @Transactional
    @Override
    public void completeBooking(Long bookingId){
        Booking booking=getActiveBooking(bookingId);
        if (booking.getStatus()!=BookingStatus.CHECKED_OUT){
            throw new BusinessException("Only checked-out bookings can be completed.");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
        notifyBooking(booking,BookingStatus.COMPLETED,"COMPLETED",null);
        log.info("Booking completed successfully. bookingId={}", bookingId);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, BookingCancellationRequest request){
        Booking booking = getActiveBooking(bookingId);
        validateOwnership(booking);
        validateCancellation(booking);
        booking.setStatus(BookingStatus.CANCELLATION_IN_PROGRESS);
        booking.setCancellationReason(request.getCancellationReason());
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);
        RefundResponse refund = refundBooking(booking.getBookingId()).getData();
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        notifyBooking(booking,BookingStatus.CANCELLED,"CANCELLED",refund);
        log.info("Booking cancelled successfully. bookingId={}", bookingId);
    }

    @Override
    @Transactional
    public List<BookingResponse> getBookingHistory(){
        Long userId=getCurrentUserId();
        return bookingRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookings(){
        Long userId = getCurrentUserId();
        return bookingRepository
                .findByUserIdAndStatusAndIsActiveTrueAndCheckInDateGreaterThanEqual(
                        userId,BookingStatus.CONFIRMED,LocalDate.now())
                .stream()
                .map(this::mapToBookingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getCompletedBookings(){
        Long userId = getCurrentUserId();
        return bookingRepository.findByUserIdAndStatusAndIsActiveTrue(userId,BookingStatus.COMPLETED)
                .stream()
                .map(this::mapToBookingResponse)
                .toList();
    }
    @Transactional
    @Override
    public void rescheduleBooking(Long bookingId, BookingRescheduleRequest request){
        Booking booking=getActiveBooking(bookingId);
        validateOwnership(booking);
        if (EnumSet.of(BookingStatus.CANCELLED,BookingStatus.COMPLETED,
                        BookingStatus.CHECKED_IN,BookingStatus.CHECKED_OUT,BookingStatus.FAILED)
                .contains(booking.getStatus())){
            throw new BusinessException("This booking cannot be modified.");
        }
        RoomDetailsResponse room=getRoomDetails(booking.getRoomId()).getData();
        validateReschedule(booking,request,room);
        long days=ChronoUnit.DAYS.between(request.getCheckInDate(),request.getExpectedVacateDate());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setExpectedVacateDate(request.getExpectedVacateDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setBookingAmount(days * room.getPrice());
        bookingRepository.save(booking);
        notifyBooking(booking,BookingStatus.CONFIRMED,"RESCHEDULED",null);
        log.info("Booking rescheduled successfully. bookingId={}", bookingId);
    }

    @Transactional
    @Override
    public void confirmBooking(Long bookingId){
        Booking booking=getActiveBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING){
            log.warn("Booking already processed: {}", bookingId);
            return; // idempotent
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        log.info("Booking CONFIRMED: {}", bookingId);
        notifyBooking(booking,BookingStatus.CONFIRMED,"CONFIRMED",null);
        log.info("Booking confirmed successfully. bookingId={}", bookingId);
    }


    @Transactional
    public void failBooking(Long bookingId) {
        log.info("Initiating failure handling for booking: {}", bookingId);
        Booking booking=getActiveBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Skipping failBooking - already processed. bookingId={}, status={}", bookingId, booking.getStatus());
            return;
        }
        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);
        log.info("Booking marked as FAILED. bookingId={}", bookingId);
//        releaseRoom(booking);
        notifyBooking(booking,BookingStatus.FAILED,"FAILED",null);
        log.info("Booking Failed. bookingId={}", bookingId);
    }

    //Feign Helper

//    @Retry(name="paymentRetry")
    @CircuitBreaker(name="paymentCB",fallbackMethod="paymentFallback")
    public ApiResponse<PaymentOrderResponse> callPaymentService(PaymentOrderRequest request) {
        log.info("Calling Payment Service (Feign) for booking: {}", request.getBookingId());
        return paymentClient.createPaymentOrder(request);
    }

    public ApiResponse<PaymentOrderResponse> paymentFallback(PaymentOrderRequest request,Throwable ex){
        log.error("Payment service FAILED (Fallback triggered) for booking: {}",request.getBookingId(), ex);
        throw new BusinessException("Payment service is currently unavailable.");
    }

//    @Retry(name="paymentRetry")
    @CircuitBreaker(name="paymentCB",fallbackMethod="refundFallback")
    private ApiResponse<RefundResponse> refundBooking(Long bookingId){
        log.info("Calling Payment Service to refund booking {}", bookingId);
        return paymentClient.refundBooking(bookingId);
    }

    private ApiResponse<RefundResponse> refundFallback(Long bookingId,Throwable ex){
        log.error("Refund service unavailable for booking={}", bookingId, ex);
        throw new BusinessException("Payment service is currently unavailable.");
    }

    @Retry(name="userRetry")
    @CircuitBreaker(name="userCB",fallbackMethod="userFallback")
    private UserResponse fetchUser(Long userId){
        log.info("Calling User Service for userId={}", userId);
        return userClient.getUser(userId);
    }

    private UserResponse userFallback(Long userId,Throwable ex){
        log.error("User service FAILED. Fallback triggered for userId={}", userId,ex);
        throw new BusinessException("User service is currently unavailable.");
    }

    @Retry(name = "propertyRetry")
    @CircuitBreaker(name = "propertyCB", fallbackMethod = "roomDetailsFallback")
    private ApiResponse<RoomDetailsResponse> getRoomDetails(Long roomId) {
        log.info("Fetching room details for roomId={}", roomId);
        return propertyClient.getRoomDetails(roomId);
    }

    private ApiResponse<RoomDetailsResponse> roomDetailsFallback(Long roomId,Throwable ex){
        log.error("Property service unavailable for roomId={}", roomId, ex);
        throw new ResourceNotFoundException("Property service is currently unavailable.");
    }

//    @Retry(name = "propertyRetry")
//    @CircuitBreaker(name = "propertyCB", fallbackMethod = "propertyReserveFallback")
//    private void reserveRoomSafe(Long roomId) {
//        log.info("Reserving room for roomId={}", roomId);
//        propertyClient.reserveRoom(roomId);
//    }

//    private void propertyReserveFallback(Long roomId, Throwable ex) {
//        log.error("Property service FAILED (reserve). roomId={}", roomId, ex);
//        throw new BusinessException("Unable to reserve room at this time");
//    }

//    @Retry(name = "propertyRetry")
//    @CircuitBreaker(name = "propertyCB", fallbackMethod = "propertyReleaseFallback")
//    private void releaseRoomSafe(Long roomId) {
//        log.info("Releasing room for roomId={}", roomId);
//        propertyClient.releaseRoom(roomId);
//    }

//    private void propertyReleaseFallback(Long roomId, Throwable ex) {
//        log.error("CRITICAL: Failed to release room. roomId={}", roomId, ex);
//        // Do NOT throw → avoid breaking flow
//    }


    // Validation Helpers

    private void validateBookingRequest(BookingRequest request,RoomDetailsResponse room){
        if (!request.getExpectedVacateDate().isAfter(request.getCheckInDate())){
            throw new BusinessException("Check-out date must be after check-in date.");
        }
        if (room.getPropertyStatus() != PropertyStatus.ACTIVE) {
            throw new BusinessException("Property is not available for booking.");
        }
        if (request.getNumberOfGuests() > room.getSharingCapacity()){
            throw new BusinessException("Requested guests exceed room capacity.");
        }
        if (request.getCheckInDate().isBefore(LocalDate.now())){
            throw new BusinessException("Check-in date cannot be in the past.");
        }
    }

    //calculating availability dynamically.
    private void validateRoomCapacity(BookingRequest request,RoomDetailsResponse room){
        Integer occupiedBeds=bookingRepository.getOccupiedBedsForDateRange(
                        room.getRoomId(),request.getCheckInDate(),request.getExpectedVacateDate());
        if (occupiedBeds==null){
            occupiedBeds=0;
        }
        int remainingBeds=room.getSharingCapacity() - occupiedBeds;
        if (request.getNumberOfGuests() > remainingBeds) {
            throw new BusinessException("Only " + remainingBeds + " beds are available for the selected dates.");
        }
    }

    private void validateCancellation(Booking booking){
        if(booking.getStatus()==BookingStatus.CANCELLED){
            throw new BusinessException("Booking has already been cancelled.");
        }
        if(booking.getStatus()==BookingStatus.CANCELLATION_IN_PROGRESS){
            throw new BusinessException("Cancellation is already in progress.");
        }
        if(booking.getStatus()==BookingStatus.COMPLETED){
            throw new BusinessException("Completed bookings cannot be cancelled.");
        }
        if(booking.getStatus()==BookingStatus.CHECKED_IN){
            throw new BusinessException("Checked-in bookings cannot be cancelled.");
        }
        if(booking.getStatus()==BookingStatus.CHECKED_OUT){
            throw new BusinessException("Checked-out bookings cannot be cancelled.");
        }
        if(booking.getStatus()==BookingStatus.FAILED){
            throw new BusinessException("Failed bookings cannot be cancelled.");
        }
        if(!LocalDate.now().isBefore(booking.getCheckInDate())){
            throw new BusinessException("Booking cannot be cancelled on or after the check-in date.");
        }
    }


    private void validateReschedule(Booking booking,BookingRescheduleRequest request,RoomDetailsResponse room){
        BookingRequest bookingRequest=BookingRequest.builder()
                        .propertyId(booking.getPropertyId())
                        .roomId(booking.getRoomId())
                        .checkInDate(request.getCheckInDate())
                        .expectedVacateDate(request.getExpectedVacateDate())
                        .numberOfGuests(request.getNumberOfGuests())
                        .build();
        validateBookingRequest(bookingRequest, room);
        if (booking.getCheckInDate().equals(request.getCheckInDate()) &&
                booking.getExpectedVacateDate().equals(request.getExpectedVacateDate()) &&
                booking.getNumberOfGuests().equals(request.getNumberOfGuests())){
            throw new BusinessException("No changes detected in booking.");
        }
        validateRoomCapacityForReschedule(booking,request,room);
    }

    private void validateRoomCapacityForReschedule(Booking booking,BookingRescheduleRequest request,RoomDetailsResponse room){
        Integer occupiedBeds=bookingRepository.getOccupiedBedsForDateRangeExcludingBooking(
                room.getRoomId(),booking.getBookingId(),request.getCheckInDate(),request.getExpectedVacateDate());
        if (occupiedBeds==null){
            occupiedBeds=0;
        }
        int remainingBeds=room.getSharingCapacity()-occupiedBeds;
        if (request.getNumberOfGuests()>remainingBeds) {
            throw new BusinessException("Only " + remainingBeds + " beds are available for the selected dates.");
        }
    }

    private void validateOwnership(Booking booking){
        Long currentUser=getCurrentUserId();
        if (!booking.getUserId().equals(currentUser)) {
            log.warn("Unauthorized access attempt by user: {} for booking: {}", currentUser,booking.getBookingId());
            throw new BusinessException("Unauthorized access to this booking");
        }
    }

    //Update Helpers

    private Booking createAndSaveBooking(BookingRequest request,Long userId,RoomDetailsResponse room){
        long numberOfDays=ChronoUnit.DAYS.between(request.getCheckInDate(),request.getExpectedVacateDate());
        double bookingAmount=numberOfDays * room.getPrice();
        Booking booking = Booking.builder()
                .userId(userId)
                .ownerId(room.getOwnerId())
                .propertyId(room.getPropertyId())
                .roomId(request.getRoomId())
                .checkInDate(request.getCheckInDate())
                .expectedVacateDate(request.getExpectedVacateDate())
                .bookingAmount(bookingAmount)
                .numberOfGuests(request.getNumberOfGuests())
                .status(BookingStatus.PENDING)
                .build();
        return bookingRepository.save(booking);
    }
    private PaymentOrderRequest buildPaymentRequest(Booking booking,UserResponse user){
        PaymentOrderRequest req = new PaymentOrderRequest();
        req.setBookingId(booking.getBookingId());
        req.setUserId(booking.getUserId());
        req.setAmount(booking.getBookingAmount());
        req.setDescription("Booking payment for room " + booking.getRoomId());
        req.setCustomerEmail(user.getEmail());
        req.setCustomerPhone(user.getPhone());
        req.setPaymentMethod("ONLINE");
        return req;
    }

//    private void handlePaymentFailure(Booking booking){
//        booking.setStatus(BookingStatus.FAILED);
//        bookingRepository.save(booking);
//    }

    //Notification Helpers
//    private void sendNotification(Booking booking,UserResponse user,BookingStatus status,String message){
//        if (user.getEmail()==null){
//            log.warn("Skipping notification: Email not available for bookingId={}", booking.getBookingId());
//            return;
//        }
//        try{
//            NotificationRequest notification=new NotificationRequest();
//            notification.setBookingId(booking.getBookingId());
//            notification.setUserId(user.getUserid());
//            notification.setType("EMAIL");
//            notification.setStatus(status.name());
//            notification.setMessage(message);
//            notification.setEmail(user.getEmail());
//            notification.setPhoneNumber(user.getPhone());
//            notificationClient.sendNotification(notification);
//            log.info("Notification sent successfully. bookingId={}, status={}", booking.getBookingId(), status);
//        }
//        catch(Exception ex){
//            log.error("Notification failed. bookingId={}, status={}", booking.getBookingId(), status, ex);
//        }
//    }

    private void sendNotification(NotificationRequest notification){
        if(notification==null){
            return;
        }
        if(notification.getEmail()==null||notification.getEmail().isBlank()){
            log.warn("Skipping notification because email is missing.");
            return;
        }
        try{
            notificationClient.sendNotification(notification);
            log.info("Notification sent successfully. bookingId={}",notification.getBookingId());
        }
        catch(Exception ex){
            log.error("Notification failed. bookingId={}",notification.getBookingId(),ex);
        }

    }

    private void notifyBooking(Booking booking,BookingStatus status,String messageType, RefundResponse refund){
        try {
            UserResponse user=fetchUser(booking.getUserId());
            String message;
            switch(messageType){
                case "CHECK_IN":
                    message=buildCheckInMessage(user,booking);
                    break;
                case "CHECK_OUT":
                    message=buildCheckOutMessage(user,booking);
                    break;
                case "COMPLETED":
                    message=buildCompletedMessage(user,booking);
                    break;
                case "RESCHEDULED":
                    message=buildRescheduleMessage(user,booking);
                    break;
                case "CONFIRMED":
                    message=buildConfirmMessage(user,booking);
                    break;
                case "FAILED":
                    message=buildFailureMessage(user,booking);
                    break;
                case "CANCELLED":
                    message=buildCancelMessage(user,booking,refund);
                    break;
                default:
                    log.warn("Unsupported notification type: {}",messageType);
                    return;
            }
            sendNotification(buildNotification(booking,user,status,message));
        }
        catch (Exception ex){
            log.error("{} notification failed. bookingId={}",messageType,booking.getBookingId(),ex);
        }
    }

    private NotificationRequest buildNotification(Booking booking,UserResponse user,BookingStatus status,String message){
        NotificationRequest notification=new NotificationRequest();
        notification.setBookingId(booking.getBookingId());
        notification.setUserId(user.getUserid());
        notification.setType("EMAIL");
        notification.setStatus(status.name());
        notification.setMessage(message);
        notification.setEmail(user.getEmail());
        notification.setPhoneNumber(user.getPhone());
        return notification;
    }

    private String buildConfirmMessage(UserResponse user,Booking booking){
        return String.format(
                """
                Dear %s,
                
                Great news!
    
                Your booking has been confirmed successfully.
    
                Booking Details
                ----------------------------------
                Booking ID      : %d
                Room ID         : %d
                Check-in Date   : %s
                Check-out Date  : %s
                Guests          : %d
                Booking Amount  : ₹%.2f
    
                We look forward to welcoming you.
    
                Thank you for choosing StayEase.
    
                Best Regards,
                StayEase Team
                """,
                user.getName(),booking.getBookingId(),booking.getRoomId(),booking.getCheckInDate(),
                booking.getExpectedVacateDate(),booking.getNumberOfGuests(),booking.getBookingAmount());
    }


    private String buildCancelMessage(UserResponse user,Booking booking,RefundResponse refund){
        return String.format(
                """
                Dear %s,
    
                Your booking has been cancelled successfully.
    
                Booking Details
                ----------------------------------
                Booking ID        : %d
                Room ID           : %d
                Cancellation Date : %s
    
                Refund Details
                ----------------------------------
                Refund Amount     : %s %.2f
                Refund Status     : %s
    
                The refund will be processed according to your payment provider's timeline.
    
                Thank you for choosing StayEase.
    
                Regards,
                StayEase Team
                """,
                user.getName(),booking.getBookingId(),booking.getRoomId(),booking.getCancelledAt(),
                refund.getCurrency(),refund.getAmount(),refund.getStatus()
        );
    }

    private String buildRescheduleMessage(UserResponse user,Booking booking){
        return String.format(
                """
                Dear %s,
    
                Your booking has been successfully rescheduled.
    
                Updated Booking Details
                ----------------------------------
                Booking ID      : %d
                Room ID         : %d
                Check-in Date   : %s
                Check-out Date  : %s
                Guests          : %d
                Total Amount    : ₹%.2f
    
                Please use these updated booking details during check-in.
    
                Thank you for choosing StayEase.
    
                Best Regards,
                StayEase Team
                """,
                user.getName(),booking.getBookingId(),booking.getRoomId(),booking.getCheckInDate(),
                booking.getExpectedVacateDate(),booking.getNumberOfGuests(),booking.getBookingAmount()
        );
    }

    private String buildFailureMessage(UserResponse user,Booking booking){
        return String.format(
                """
                Dear %s,
    
                Unfortunately, we couldn't complete your booking because the payment was unsuccessful.
    
                Booking Details
                ----------------------------------
                Booking ID      : %d
                Room ID         : %d
                Booking Amount  : ₹%.2f
    
                No payment has been confirmed for this booking.
    
                Please retry your payment to continue with your reservation.
    
                If you continue experiencing issues, please contact our support team.
    
                Regards,
                StayEase Team
                """,
                user.getName(),booking.getBookingId(),booking.getRoomId(),booking.getBookingAmount()
        );
    }

    private String buildCheckInMessage(UserResponse user,Booking booking){
        return String.format(
                """
                Dear %s,
    
                Welcome to StayEase!
    
                Your check-in has been completed successfully.
    
                Booking Details
                ----------------------------------
                Booking ID : %d
                Room ID    : %d
    
                We hope you have a comfortable and enjoyable stay.
    
                Best Regards,
                StayEase Team
                """,
                user.getName(),booking.getBookingId(),booking.getRoomId()
        );
    }

    private String buildCheckOutMessage(UserResponse user,Booking booking){
        return String.format(
                """
                Dear %s,
    
                Your check-out has been completed successfully.
    
                Thank you for staying with StayEase.
    
                We hope to welcome you again soon.
    
                Booking ID : %d
    
                Regards,
                StayEase Team
                """,
                user.getName(),booking.getBookingId()
        );
    }

    private String buildCompletedMessage(UserResponse user,Booking booking){
        return String.format(
                """
                Dear %s,
    
                Your stay has been completed successfully.
    
                Thank you for choosing StayEase.
    
                We sincerely hope you enjoyed your experience.
    
                Booking ID : %d
    
                We look forward to serving you again.
    
                Best Regards,
                StayEase Team
                """,
                user.getName(),booking.getBookingId()
        );
    }

    //Rep Helpers

    private Booking getActiveBooking(Long bookingId){
        return bookingRepository.findByBookingIdAndIsActiveTrue(bookingId)
                .orElseThrow(() -> {
                    log.warn("Active booking not found with ID: {}", bookingId);
                    return new BusinessException("Booking not found");
                });
    }

    private Long getCurrentUserId(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()){
            throw new IllegalStateException("No authenticated user found.");
        }
        return Long.parseLong(authentication.getName());
    }

    private boolean isPaymentResponseValid(PaymentOrderResponse response){
        return response != null && response.getPaymentId() != null && response.getRazorpayOrderId() != null;
    }


    private BookingResponse mapToBookingResponse(Booking booking){
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUserId())
                .propertyId(booking.getPropertyId())
                .roomId(booking.getRoomId())
                .ownerId(booking.getOwnerId())
                .bookingStatus(booking.getStatus())
                .bookingAmount(booking.getBookingAmount())
                .bookingDate(booking.getBookingDate())
                .checkInDate(booking.getCheckInDate())
                .expectedVacateDate(booking.getExpectedVacateDate())
                .numberOfGuests(booking.getNumberOfGuests())
                .build();
    }


//    private void reserveRoomOrThrow(Long roomId) {
//        try {
//            ApiResponse<Boolean> available = checkAvailability(roomId);
//            if (!available.getData()) {
//                throw new RoomUnavailableException("Room not available");
//            }
//            reserveRoomSafe(roomId);
//        } catch (RoomUnavailableException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            log.error("Property service failed", ex);
//            throw new BusinessException("Unable to process booking at this time");
//        }
//   }

//    @Override
//    public List<BookingResponseDTO> getUserBookings() {
//        String userId = getLoggedInUser();
//        log.info("Fetching all bookings for user: {}", userId);
//        List<BookingResponseDTO> bookings = bookingRepository.findByUserIdAndIsActiveTrue(userId)
//                .stream()
//                .map(this::mapToDTO)
//                .toList();
//        log.debug("Retrieved {} bookings for user: {}", bookings.size(), userId);
//        return bookings;
//    }

//    private void releaseRoom(Booking booking) {
//        try {
//            releaseRoomSafe(booking.getRoomId());
//            log.info("Room released successfully for bookingId={}", booking.getBookingId());
//        } catch (Exception ex) {
//            log.error("CRITICAL: Failed to release room. bookingId={}, roomId={}",
//                    booking.getBookingId(), booking.getRoomId(), ex);
//        }
//    }

//    @Transactional
//    public BookingResponse cancelBooking(Long bookingId) {
//        log.info("Cancelling booking with ID: {}", bookingId);
//        Booking booking = getActiveBooking(bookingId);
//        validateOwnership(booking);
//        if (booking.getStatus() == BookingStatus.CANCELLED) {
//            throw new BusinessException("Booking already cancelled");
//        }
//        if (booking.getStatus() != BookingStatus.CONFIRMED) {
//            throw new BusinessException("Only confirmed bookings can be cancelled");
//        }
//        releaseRoom(booking);
//        booking.setStatus(BookingStatus.CANCELLED);
//        booking = bookingRepository.save(booking);
//        log.info("Booking cancelled successfully: {}", bookingId);
//        try {
//            UserResponseDTO user = fetchUser(booking.getUserId());
//            String message = buildCancelMessage(user, booking);
//            sendNotification(booking, user, BookingStatus.CANCELLED, message);
//        } catch (Exception ex) {
//            log.error("Notification failed for bookingId={}", bookingId, ex);
//        }
//        return mapToBookingResponse(booking);
//    }


//    private String buildCancelMessage(UserResponseDTO user, Booking booking) {
//        return String.format(
//                "Dear %s,\n\n" +
//                        "Your booking has been successfully cancelled.\n\n" +
//                        "Booking ID: %d\nRoom ID: %d\n\n" +
//                        "Refund (if applicable) will be processed shortly.\n\n" +
//                        "Regards,\nStayEase Team",
//                user.getName(),
//                booking.getBookingId(),
//                booking.getRoomId()
//        );
//    }

//    private BookingResponse mapToDTO(Booking booking) {
//        return BookingResponse.builder()
//                .bookingId(booking.getBookingId())
//                .userId(booking.getUserId())
//                .propertyId(booking.getPropertyId())
//                .roomId(booking.getRoomId())
//                .bookingStatus(booking.getStatus())
//                .bookingAmount(booking.getBookingAmount())
//                .build();
//    }
}