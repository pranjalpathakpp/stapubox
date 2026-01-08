package com.stapubox.booking.repository;

import com.stapubox.booking.model.Slot;
import com.stapubox.booking.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByVenueId(Long venueId);

    List<Slot> findByVenueIdAndSlotDate(Long venueId, LocalDate slotDate);

    @Query("SELECT s FROM Slot s WHERE s.venue.id = :venueId " +
           "AND s.slotDate = :date " +
           "AND s.startTime < :endTime " +
           "AND s.endTime > :startTime")
    List<Slot> findOverlappingSlots(
            @Param("venueId") Long venueId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT s FROM Slot s WHERE s.venue.id = :venueId " +
           "AND s.slotDate = :date " +
           "AND s.isAvailable = true " +
           "AND s.startTime >= :startTime " +
           "AND s.endTime <= :endTime")
    List<Slot> findAvailableSlotsInTimeRange(
            @Param("venueId") Long venueId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :slotId AND s.isAvailable = true")
    Optional<Slot> findAvailableSlotForBooking(@Param("slotId") Long slotId);
}


