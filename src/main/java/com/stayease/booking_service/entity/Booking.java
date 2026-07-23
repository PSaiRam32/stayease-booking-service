package com.stayease.booking_service.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long propertyId;
    @Column(nullable = false)
    private Long roomId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    @Column(nullable = false)
    private Double bookingAmount;
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
    @Column(name = "booking_date")
    private LocalDateTime bookingDate;
    private LocalDate checkInDate;
    private LocalDate expectedVacateDate;
    private Integer numberOfGuests;
    private Long ownerId;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    @PrePersist
    public void prePersist() {
        this.bookingDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
