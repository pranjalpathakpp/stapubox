package com.stapubox.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {
    private Long id;
    private String name;
    private String location;
    private String sportCode;
    private String sportId;
    private String description;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

