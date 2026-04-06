package com.stayease.booking_service.repository;


import com.stayease.booking_service.entity.Booking;
import com.stayease.booking_service.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByIdAndIsActiveTrue(Long id);
    List<Booking> findByUserIdAndIsActiveTrue(String userId);
    Optional<Booking> findByUserIdAndRoomIdAndStatusIn(
            String userId,
            Long roomId,
            List<BookingStatus> statuses
    );
}