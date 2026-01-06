package com.stapubox.booking.service;

import com.stapubox.booking.dto.AvailabilityRequest;
import com.stapubox.booking.dto.VenueResponse;
import com.stapubox.booking.model.Venue;
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
public class AvailabilityService {
    private final VenueRepository venueRepository;

    @Transactional(readOnly = true)
    public List<VenueResponse> getAvailableVenues(AvailabilityRequest request) {
      
        if (request.getStartTime().isAfter(request.getEndTime()) || 
            request.getStartTime().equals(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        List<Venue> availableVenues = venueRepository.findAvailableVenuesBySportAndTimeRange(
                request.getSportCode(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        return availableVenues.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private VenueResponse mapToResponse(Venue venue) {
        VenueResponse response = new VenueResponse();
        response.setId(venue.getId());
        response.setName(venue.getName());
        response.setLocation(venue.getLocation());
        response.setSportCode(venue.getSportCode());
        response.setSportId(venue.getSport() != null ? venue.getSport().getId().toString() : null);
        response.setDescription(venue.getDescription());
        response.setCapacity(venue.getCapacity());
        response.setCreatedAt(venue.getCreatedAt());
        response.setUpdatedAt(venue.getUpdatedAt());
        return response;
    }
}

