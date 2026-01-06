package com.stapubox.booking.controller;

import com.stapubox.booking.dto.AvailabilityRequest;
import com.stapubox.booking.dto.VenueResponse;
import com.stapubox.booking.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @GetMapping("/available")
    public ResponseEntity<List<VenueResponse>> getAvailableVenues(
            @Valid @ModelAttribute AvailabilityRequest request) {
        List<VenueResponse> venues = availabilityService.getAvailableVenues(request);
        return ResponseEntity.ok(venues);
    }
}

