package com.stapubox.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {
    private Long id;
    private Long venueId;
    private String venueName;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;
    private Double price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

