package com.stapubox.booking.service;

import com.stapubox.booking.dto.VenueRequest;
import com.stapubox.booking.dto.VenueResponse;
import com.stapubox.booking.model.Sport;
import com.stapubox.booking.model.Venue;
import com.stapubox.booking.repository.SportRepository;
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
public class VenueService {
    private final VenueRepository venueRepository;
    private final SportRepository sportRepository;
    private final SportService sportService;

    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
    
        if (!sportService.isValidSportCode(request.getSportCode())) {
            throw new IllegalArgumentException("Invalid sport code: " + request.getSportCode());
        }

        Sport sport = sportRepository.findByCode(request.getSportCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sport not found in database. Please ensure sports are seeded. Code: " + request.getSportCode()));

        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setLocation(request.getLocation());
        venue.setSportCode(sport.getCode());
        venue.setSport(sport);
        venue.setDescription(request.getDescription());
        venue.setCapacity(request.getCapacity());

        Venue savedVenue = venueRepository.save(venue);
        log.info("Created venue with ID: {} for sport: {}", savedVenue.getId(), sport.getCode());
        return mapToResponse(savedVenue);
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VenueResponse getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found with ID: " + id));
        
        if (venue.getSport() != null) {
            venue.getSport().getId();
        }
        return mapToResponse(venue);
    }

    @Transactional
    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new IllegalArgumentException("Venue not found with ID: " + id);
        }
        venueRepository.deleteById(id);
        log.info("Deleted venue with ID: {}", id);
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

