package com.stapubox.booking.repository;

import com.stapubox.booking.model.Booking;
import com.stapubox.booking.model.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findBySlotId(Long slotId);

    List<Booking> findByCustomerEmail(String customerEmail);

    List<Booking> findByStatus(BookingStatus status);
}

