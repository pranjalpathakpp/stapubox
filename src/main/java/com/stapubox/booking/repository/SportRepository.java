package com.stapubox.booking.repository;

import com.stapubox.booking.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
    Optional<Sport> findByCode(String code);
    boolean existsByCode(String code);
}

