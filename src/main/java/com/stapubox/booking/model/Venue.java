package com.stapubox.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues", indexes = {
    @Index(name = "idx_venue_sport_code", columnList = "sport_code"),
    @Index(name = "idx_venue_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Venue name is required")
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank(message = "Location is required")
    @Column(nullable = false, length = 500)
    private String location;

    @NotBlank(message = "Sport code is required")
    @Column(name = "sport_code", nullable = false, length = 50)
    private String sportCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sport_id", foreignKey = @ForeignKey(name = "fk_venue_sport"))
    private Sport sport;

    @Column(length = 1000)
    private String description;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Slot> slots = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


