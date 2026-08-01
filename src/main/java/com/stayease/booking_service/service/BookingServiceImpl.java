package com.stayease.booking_service.service;

import com.stayease.booking_service.dto.request.*;
import com.stayease.booking_service.dto.response.*;
import com.stayease.booking_service.entity.*;
import com.stayease.booking_service.exception.*;
import com.stayease.booking_service.integration.NotificationServiceGateway;
import com.stayease.booking_service.integration.PaymentServiceGateway;
import com.stayease.booking_service.integration.PropertyServiceGateway;
import com.stayease.booking_service.integration.UserServiceGateway;
import com.stayease.booking_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final NotificationServiceGateway notificationServiceGateway;
    private final UserServiceGateway userServiceGateway;
    private final PropertyServiceGateway propertyServiceGateway;
    private final PaymentServiceGateway paymentServiceGateway;

    @Transactional
    @Override
    public BookingResponse createBooking(BookingRequest request){
        Long userId=getCurrentUserId();
        log.info("Creating booking for user: {} roomId={}", userId, request.getRoomId());
        RoomDetailsResponse room=propertyServiceGateway.getRoomDetails(request.getRoomId()).getData();
        log.info("Room object = {}", room);
        log.info("Status = {}", room.getPropertyStatus());
        log.info("Capacity = {}", room.getSharingCapacity());
        validateBookingRequest(request, room);
        validateDuplicateBooking(userId,request);
        validateRoomCapacity(request,room);
        Booking booking=createAndSaveBooking(request,userId,room);
        try{
            UserResponse user=userServiceGateway.getUser(userId);
            PaymentOrderRequest paymentRequest=buildPaymentRequest(booking,user);
            PaymentOrderResponse paymentResponse;
            try{
                paymentResponse=paymentServiceGateway.createPaymentOrder(paymentRequest).getData();
            }
            catch(BusinessException ex){
                if(ex.getMessage().contains("already exists")){
                    paymentResponse=paymentServiceGateway.getPaymentByBookingId(booking.getBookingId()).getData();
                }
                else{
                    throw ex;
                }
            }
            if(!isPaymentResponseValid(paymentResponse)){
                throw new PaymentFailedException("Payment order creation failed");
            }
            log.info("Payment order created successfully.");
            return mapToBookingResponse(booking);
        }
        catch(Exception ex){
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            log.error("Booking {} failed during payment initialization",booking.getBookingId(),ex);
            throw ex;
        }
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

    @Transactional
    @Override
    public void checkInBooking(Long bookingId){
        Booking booking=getActiveBooking(bookingId);
        validateOwnership(booking);
        if (booking.getStatus()==BookingStatus.CHECKED_IN){
            log.info("Booking {} already checked in.", bookingId);
            return;
        }
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
        if(booking.getStatus()==BookingStatus.CHECKED_OUT){
            log.info("Booking {} already checked out.",bookingId);
            return;
        }
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
        if(booking.getStatus()==BookingStatus.COMPLETED){
            log.info("Booking {} already completed.", bookingId);
            return;
        }
        if(booking.getStatus()!=BookingStatus.CHECKED_OUT){
            throw new BusinessException("Only checked-out bookings can be completed.");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setIsActive(false);
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
        if (booking.getStatus()==BookingStatus.CANCELLED ||
                booking.getStatus()==BookingStatus.CANCELLATION_IN_PROGRESS){
            return;
        }
        booking.setStatus(BookingStatus.CANCELLATION_IN_PROGRESS);
        booking.setCancellationReason(request.getCancellationReason());
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);
        RefundResponse refund=paymentServiceGateway.refundBooking(booking.getBookingId()).getData();
        if(refund==null){
            throw new BusinessException("Refund failed.");
        }
        if(!"COMPLETED".equalsIgnoreCase(refund.getStatus())
                && !"PROCESSING".equalsIgnoreCase(refund.getStatus())){
            throw new BusinessException("Refund could not be initiated.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setIsActive(false);
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
        RoomDetailsResponse room=propertyServiceGateway.getRoomDetails(booking.getRoomId()).getData();
        validateReschedule(booking,request,room);
        long days=ChronoUnit.DAYS.between(request.getCheckInDate(),request.getExpectedVacateDate())+1;
        booking.setCheckInDate(request.getCheckInDate());
        booking.setExpectedVacateDate(request.getExpectedVacateDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setBookingAmount(days * room.getPrice() * request.getNumberOfGuests());
        bookingRepository.save(booking);
        notifyBooking(booking,BookingStatus.RESCHEDULED,"RESCHEDULED",null);
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
    public void failBooking(Long bookingId){
        log.info("Initiating failure handling for booking: {}", bookingId);
        Booking booking=getActiveBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING){
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

    @Override
    public List<OwnerBookingResponse > bookingsByOwnerId(Long OwnerId){
        List<Booking> bookings=bookingRepository.findByOwnerIdAndIsActiveTrue(OwnerId);
        return bookings.stream()
                .map(this::mapToOwnerBookingResponse)
                .toList();
    }

    @Override
    public List<OwnerBookingResponse> getOwnerBookingHistory(Long ownerId){
        return bookingRepository.findByOwnerIdAndStatusAndIsActiveTrue(ownerId,BookingStatus.COMPLETED)
                .stream()
                .map(this::mapToOwnerBookingResponse)
                .toList();
    }

    @Override
    public RevenueSummaryResponse getRevenueSummary(Long ownerId){
        List<Booking> bookings=bookingRepository.findByOwnerIdAndIsActiveTrue(ownerId);
        double totalRevenue=bookings.stream()
                .mapToDouble(Booking::getBookingAmount)
                .sum();

        double completedRevenue=bookings.stream()
                .filter(b -> b.getStatus()==BookingStatus.COMPLETED)
                .mapToDouble(Booking::getBookingAmount)
                .sum();

        double pendingRevenue=bookings.stream()
                .filter(b -> b.getStatus()==BookingStatus.PENDING)
                .mapToDouble(Booking::getBookingAmount)
                .sum();

        long totalBookings=bookings.size();
        long completedBookings=bookings.stream()
                .filter(b -> b.getStatus()==BookingStatus.COMPLETED)
                .count();

        long pendingBookings=bookings.stream()
                .filter(b -> b.getStatus()==BookingStatus.PENDING)
                .count();

        long cancelledBookings=bookings.stream()
                .filter(b -> b.getStatus()==BookingStatus.CANCELLED)
                .count();

        double averageBookingValue=totalBookings==0?0:totalRevenue/totalBookings;
        return RevenueSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .completedRevenue(completedRevenue)
                .pendingRevenue(pendingRevenue)
                .totalBookings(totalBookings)
                .completedBookings(completedBookings)
                .pendingBookings(pendingBookings)
                .cancelledBookings(cancelledBookings)
                .averageBookingValue(averageBookingValue)
                .build();
    }

    @Override
    public OccupiedRoomCountResponse getOccupiedRoomCount(Long ownerId){
        Long occupiedRooms=bookingRepository.countOccupiedRooms(ownerId);
        return OccupiedRoomCountResponse.builder()
                .occupiedRooms(occupiedRooms)
                .build();
    }

    @Override
    public UserBookingDashboardResponse getUserDashboard(Long userId){
        List<Booking> bookings=bookingRepository.findByUserIdAndIsActiveTrue(userId);
        LocalDate today=LocalDate.now();
        long totalBookings=bookings.size();
        long upcomingBookings=0;
        long completedBookings=0;
        long cancelledBookings=0;
        for (Booking booking : bookings){
            switch (booking.getStatus()){
                case COMPLETED -> completedBookings++;
                case CANCELLED -> cancelledBookings++;
                case CONFIRMED -> {
                    if (booking.getCheckInDate() != null&& booking.getCheckInDate().isAfter(today)){
                        upcomingBookings++;
                    }
                }
                default -> {
                    // Ignore remaining statuses
                }
            }
        }
        Optional<Booking> currentBookingOptional=bookingRepository.findCurrentBooking(userId);
        CurrentBookingResponse currentBooking=currentBookingOptional
                .map(this::mapToCurrentBookingResponse)
                .orElse(null);
        return UserBookingDashboardResponse.builder()
                .totalBookings(totalBookings)
                .upcomingBookings(upcomingBookings)
                .currentBookings(currentBooking!=null?1L:0L)
                .completedBookings(completedBookings)
                .cancelledBookings(cancelledBookings)
                .currentBooking(currentBooking)
                .build();
    }

    // Validation Helpers

    private void validateBookingRequest(BookingRequest request,RoomDetailsResponse room){
        if(!request.getExpectedVacateDate().isAfter(request.getCheckInDate())){
            throw new BusinessException("Check-out date must be after check-in date.");
        }
        if(room.getPropertyStatus() != PropertyStatus.ACTIVE){
            throw new BusinessException("Property is not available for booking.");
        }
        if(request.getNumberOfGuests() > room.getSharingCapacity()){
            throw new BusinessException("Requested guests exceed room capacity.");
        }
        if(request.getCheckInDate().isBefore(LocalDate.now())){
            throw new BusinessException("Check-in date cannot be in the past.");
        }
    }


    private void validateDuplicateBooking(Long userId, BookingRequest request){
        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING,BookingStatus.CONFIRMED,BookingStatus.CHECKED_IN,
                BookingStatus.RESCHEDULED,BookingStatus.CANCELLATION_IN_PROGRESS);
        if (bookingRepository.existsByUserIdAndRoomIdAndCheckInDateAndExpectedVacateDateAndStatusInAndIsActiveTrue(
                userId,request.getRoomId(),request.getCheckInDate(),request.getExpectedVacateDate(),activeStatuses)){
            throw new BusinessException("An active booking already exists for the selected room and dates.");
        }
    }

    private void validateDuplicateBookingForReschedule(Booking booking,BookingRescheduleRequest request){
        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN,
                BookingStatus.RESCHEDULED,
                BookingStatus.CANCELLATION_IN_PROGRESS
        );

        boolean duplicate=bookingRepository
                        .existsByUserIdAndRoomIdAndCheckInDateAndExpectedVacateDateAndBookingIdNotAndStatusInAndIsActiveTrue(
                                booking.getUserId(),
                                booking.getRoomId(),
                                request.getCheckInDate(),
                                request.getExpectedVacateDate(),
                                booking.getBookingId(),
                                activeStatuses);
        if (duplicate){
            throw new BusinessException("You already have another booking for this room and date range.");
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
            log.info("Booking {} already cancelled.", booking.getBookingId());
            return;
//            throw new BusinessException("Booking has already been cancelled.");
        }
        if(booking.getStatus()==BookingStatus.CANCELLATION_IN_PROGRESS){
            log.info("Cancellation already in progress for booking {}.",booking.getBookingId());
            return;
//            throw new BusinessException("Cancellation is already in progress.");
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
        validateDuplicateBookingForReschedule(booking,request);
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
        long numberOfDays=ChronoUnit.DAYS.between(request.getCheckInDate(),request.getExpectedVacateDate())+1;
        double bookingAmount=numberOfDays * room.getPrice() *request.getNumberOfGuests();
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


    private void sendNotification(NotificationRequest notification){
        if(notification==null){
            return;
        }
        if(notification.getEmail()==null||notification.getEmail().isBlank()){
            log.warn("Skipping notification because email is missing.");
            return;
        }
        try{
            notificationServiceGateway.sendNotification(notification);
            log.info("Notification sent successfully. bookingId={}",notification.getBookingId());
        }
        catch(Exception ex){
            log.error("Notification failed. bookingId={}",notification.getBookingId(),ex);
        }

    }

    private void notifyBooking(Booking booking,BookingStatus status,String messageType, RefundResponse refund){
        try {
            UserResponse user=userServiceGateway.getUser(booking.getUserId());
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

    private OwnerBookingResponse mapToOwnerBookingResponse(Booking booking){
        return OwnerBookingResponse.builder()
                .bookingId(booking.getBookingId())
                .propertyId(booking.getPropertyId())
                .roomId(booking.getRoomId())
                .userId(booking.getUserId())
                .bookingStatus(booking.getStatus())
                .bookingAmount(booking.getBookingAmount())
                .numberOfGuests(booking.getNumberOfGuests())
                .checkInDate(booking.getCheckInDate())
                .expectedVacateDate(booking.getExpectedVacateDate())
                .bookingDate(booking.getBookingDate())
                .build();
    }

    private CurrentBookingResponse mapToCurrentBookingResponse(Booking booking) {
        return CurrentBookingResponse.builder()
                .bookingId(booking.getBookingId())
                .propertyId(booking.getPropertyId())
                .roomId(booking.getRoomId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getExpectedVacateDate())
                .status(booking.getStatus())
                .bookingAmount(booking.getBookingAmount())
                .build();
    }
}