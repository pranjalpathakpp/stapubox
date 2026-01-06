package com.stapubox.booking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SportService {
    private static final String SPORTS_API_URL = "https://stapubox.com/sportslist/";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SportService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<SportInfo> getAllSports() {
        try {
            String response = restTemplate.getForObject(SPORTS_API_URL, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);
            
            List<SportInfo> sports = new ArrayList<>();

            JsonNode dataNode = jsonNode.has("data") ? jsonNode.get("data") : jsonNode;
            
            if (dataNode.isArray()) {
                for (JsonNode sport : dataNode) {
                    SportInfo sportInfo = new SportInfo();

                    if (sport.has("sport_id")) {
                        sportInfo.setId(sport.get("sport_id").asText());
                    } else if (sport.has("id")) {
                        sportInfo.setId(sport.get("id").asText());
                    }

                    if (sport.has("sport_code")) {
                        sportInfo.setCode(sport.get("sport_code").asText());
                    } else if (sport.has("code")) {
                        sportInfo.setCode(sport.get("code").asText());
                    }

                    if (sport.has("sport_name")) {
                        sportInfo.setName(sport.get("sport_name").asText());
                    } else if (sport.has("name")) {
                        sportInfo.setName(sport.get("name").asText());
                    }
                    
                    sports.add(sportInfo);
                }
            }
            
            log.info("Fetched {} sports from external API", sports.size());
            return sports;
        } catch (Exception e) {
            log.error("Error fetching sports from API: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean isValidSportCode(String sportCode) {
        if (sportCode == null || sportCode.trim().isEmpty()) {
            return false;
        }
        
        List<SportInfo> sports = getAllSports();

        if (sports.isEmpty()) {
            log.warn("Sports API returned empty list. Cannot validate sport code: {}", sportCode);
            return false;
        }
        
        return sports.stream()
                .anyMatch(sport -> {
                    String code = sport.getCode();
                    String id = sport.getId();
                    return (code != null && sportCode.equalsIgnoreCase(code)) || 
                           (id != null && sportCode.equalsIgnoreCase(id));
                });
    }

    public static class SportInfo {
        private String id;
        private String code;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

