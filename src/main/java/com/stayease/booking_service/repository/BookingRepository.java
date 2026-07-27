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
    List<Booking> findByOwnerIdAndIsActiveTrue(Long ownerId);
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
    List<Booking> findByOwnerIdAndStatusAndIsActiveTrue(Long ownerId,BookingStatus status);
    @Query("""
        SELECT COUNT(DISTINCT b.roomId)
        FROM Booking b
        WHERE b.ownerId = :ownerId
        AND b.status = 'CHECKED_IN'
        AND b.isActive = true
        """)
    Long countOccupiedRooms(Long ownerId);
    @Query("""
       SELECT b
       FROM Booking b
       WHERE b.userId = :userId
       AND b.isActive = true
       AND b.status IN ('CONFIRMED','CHECKED_IN')
       AND CURRENT_DATE BETWEEN b.checkInDate
       AND b.expectedVacateDate
       """)
    Optional<Booking> findCurrentBooking(@Param("userId") Long userId);
}