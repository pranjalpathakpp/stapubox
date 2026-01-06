package com.stapubox.booking.repository;

import com.stapubox.booking.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findBySportCode(String sportCode);

    @Query("SELECT DISTINCT v FROM Venue v " +
           "WHERE v.sportCode = :sportCode " +
           "AND v.id IN (" +
           "  SELECT s.venue.id FROM Slot s " +
           "  WHERE s.slotDate = :date " +
           "  AND s.isAvailable = true " +
           "  AND s.startTime <= :startTime " +
           "  AND s.endTime >= :endTime" +
           ")")
    List<Venue> findAvailableVenuesBySportAndTimeRange(
            @Param("sportCode") String sportCode,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}

