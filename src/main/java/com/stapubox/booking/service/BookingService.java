package com.stapubox.booking.service;

import com.stapubox.booking.dto.BookingRequest;
import com.stapubox.booking.dto.BookingResponse;
import com.stapubox.booking.model.Booking;
import com.stapubox.booking.model.Booking.BookingStatus;
import com.stapubox.booking.model.Slot;
import com.stapubox.booking.repository.BookingRepository;
import com.stapubox.booking.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        
        Slot slot = slotRepository.findAvailableSlotForBooking(request.getSlotId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Slot not found or not available for booking. Slot ID: " + request.getSlotId()));

        if (!slot.getIsAvailable()) {
            throw new IllegalStateException("Slot is no longer available. Slot ID: " + request.getSlotId());
        }

        if (bookingRepository.findBySlotId(slot.getId()).isPresent()) {
            throw new IllegalStateException("Slot already has a booking. Slot ID: " + slot.getId());
        }

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(slot.getPrice());

        slot.setIsAvailable(false);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Created booking with ID: {} for slot ID: {}", savedBooking.getId(), slot.getId());
        return mapToResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));
        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled. Booking ID: " + id);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        Slot slot = booking.getSlot();
        slot.setIsAvailable(true);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Cancelled booking with ID: {}", id);
        return mapToResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setSlotId(booking.getSlot().getId());
        response.setCustomerName(booking.getCustomerName());
        response.setCustomerEmail(booking.getCustomerEmail());
        response.setCustomerPhone(booking.getCustomerPhone());
        response.setStatus(booking.getStatus());
        response.setTotalAmount(booking.getTotalAmount());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        response.setCancelledAt(booking.getCancelledAt());
        return response;
    }
}

