package com.stayease.booking_service.repository;


import com.stayease.booking_service.entity.Booking;
import com.stayease.booking_service.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingIdAndIsActiveTrue(Long bookingId);
    List<Booking> findByUserIdAndIsActiveTrue(Long userId);
//    Optional<Booking> findByUserIdAndRoomIdAndStatusIn(Long userId,Long roomId,List<BookingStatus> statuses);
    @Query("""
        SELECT COALESCE(SUM(b.numberOfGuests),0) FROM Booking b WHERE b.roomId = :roomId
        AND b.status IN (BookingStatus.PENDING,BookingStatus.CONFIRMED)
        AND b.checkInDate < :expectedVacateDate
        AND b.expectedVacateDate > :checkInDate
      """)
    Integer getOccupiedBedsForDateRange(@Param("roomId") Long roomId,@Param("checkInDate") LocalDate checkInDate,
            @Param("expectedVacateDate") LocalDate expectedVacateDate);
    List<Booking> findByUserIdAndStatusAndIsActiveTrueAndCheckInDateGreaterThanEqual(
            Long userId,BookingStatus status,LocalDate checkInDate);
    List<Booking> findByUserIdAndStatusAndIsActiveTrue(Long userId,BookingStatus status);
    @Query("""
            SELECT COALESCE(SUM(b.numberOfGuests),0)
            FROM Booking b
            WHERE b.roomId=:roomId
            AND b.bookingId <> :bookingId
            AND b.status IN ('PENDING','CONFIRMED')
            AND b.checkInDate < :expectedVacateDate
            AND b.expectedVacateDate > :checkInDate
            """)
    Integer getOccupiedBedsForDateRangeExcludingBooking(Long roomId,Long bookingId,LocalDate checkInDate,LocalDate expectedVacateDate);
}