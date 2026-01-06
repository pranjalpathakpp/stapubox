package com.stapubox.booking.service;

import com.stapubox.booking.dto.SlotRequest;
import com.stapubox.booking.dto.SlotResponse;
import com.stapubox.booking.model.Slot;
import com.stapubox.booking.model.Venue;
import com.stapubox.booking.repository.SlotRepository;
import com.stapubox.booking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotService {
    private final SlotRepository slotRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public SlotResponse createSlot(Long venueId, SlotRequest request) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found with ID: " + venueId));

        if (request.getStartTime().isAfter(request.getEndTime()) || 
            request.getStartTime().equals(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        List<Slot> overlappingSlots = slotRepository.findOverlappingSlots(
                venueId, request.getSlotDate(), request.getStartTime(), request.getEndTime());

        if (!overlappingSlots.isEmpty()) {
            throw new IllegalArgumentException(
                    "Slot overlaps with existing slot(s). Overlapping slots: " + 
                    overlappingSlots.stream()
                            .map(s -> s.getId().toString())
                            .collect(Collectors.joining(", ")));
        }

        Slot slot = new Slot();
        slot.setVenue(venue);
        slot.setSlotDate(request.getSlotDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setPrice(request.getPrice());
        slot.setIsAvailable(true);

        Slot savedSlot = slotRepository.save(slot);
        log.info("Created slot with ID: {} for venue ID: {}", savedSlot.getId(), venueId);
        return mapToResponse(savedSlot);
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsByVenue(Long venueId) {
        return slotRepository.findByVenueId(venueId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SlotResponse mapToResponse(Slot slot) {
        SlotResponse response = new SlotResponse();
        response.setId(slot.getId());
        response.setVenueId(slot.getVenue().getId());
        response.setVenueName(slot.getVenue().getName());
        response.setSlotDate(slot.getSlotDate());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        response.setIsAvailable(slot.getIsAvailable());
        response.setPrice(slot.getPrice());
        response.setCreatedAt(slot.getCreatedAt());
        response.setUpdatedAt(slot.getUpdatedAt());
        return response;
    }
}

