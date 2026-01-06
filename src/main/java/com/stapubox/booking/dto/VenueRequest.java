package com.stapubox.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VenueRequest {
    @NotBlank(message = "Venue name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Sport code is required")
    private String sportCode;

    private String sportId;
    private String description;
    private Integer capacity;
}

