package com.stapubox.booking.controller;

import com.stapubox.booking.dto.SlotRequest;
import com.stapubox.booking.dto.SlotResponse;
import com.stapubox.booking.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues/{venueId}/slots")
@RequiredArgsConstructor
public class SlotController {
    private final SlotService slotService;

    @PostMapping
    public ResponseEntity<SlotResponse> createSlot(
            @PathVariable Long venueId,
            @Valid @RequestBody SlotRequest request) {
        SlotResponse response = slotService.createSlot(venueId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SlotResponse>> getSlotsByVenue(@PathVariable Long venueId) {
        List<SlotResponse> slots = slotService.getSlotsByVenue(venueId);
        return ResponseEntity.ok(slots);
    }
}

