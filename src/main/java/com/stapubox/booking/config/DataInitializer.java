package com.stapubox.booking.config;

import com.stapubox.booking.model.Sport;
import com.stapubox.booking.repository.SportRepository;
import com.stapubox.booking.service.SportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SportRepository sportRepository;
    private final SportService sportService;

    @Override
    public void run(String... args) {
        log.info("Initializing sports data from external API");

        var sportsFromAPI = sportService.getAllSports();
        
        if (sportsFromAPI.isEmpty()) {
            log.warn("No sports fetched from external API. Sports will be validated at venue creation time.");
            return;
        }

        int seededCount = 0;
        for (var sportInfo : sportsFromAPI) {
            String code = sportInfo.getCode();
            String id = sportInfo.getId();
            String name = sportInfo.getName();

            String sportCode = (code != null && !code.trim().isEmpty()) ? code : id;
            
            if (sportCode != null && !sportCode.trim().isEmpty()) {
                if (!sportRepository.existsByCode(sportCode)) {
                    Sport sport = new Sport();
                    sport.setCode(sportCode);

                    sport.setName(name != null && !name.trim().isEmpty() 
                        ? name 
                        : sportCode);
                    sportRepository.save(sport);
                    seededCount++;
                    log.debug("Seeded sport from API: code={}, name={}, id={}", sportCode, name, id);
                }
            }
        }
        
        log.info("Sports data initialization complete. Seeded {} sports from external API.", seededCount);
    }
}

