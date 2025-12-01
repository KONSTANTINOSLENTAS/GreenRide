package com.greenride.greenride.infrastructure.adapters;

import com.greenride.greenride.dto.Coordinates;
import com.greenride.greenride.dto.RouteEstimate;
import com.greenride.greenride.domain.ports.GeolocationPort;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class MapsApiAdapter implements GeolocationPort {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String MOCK_SERVICE_URL = "http://localhost:8081/api/mock/route-estimate";

    @Override
    public RouteEstimate calculateRouteEstimate(Coordinates start, Coordinates destination) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("startLat", String.valueOf(start.getLatitude()));
            requestBody.put("startLon", String.valueOf(start.getLongitude()));

            Map<String, Object> response = restTemplate.postForObject(
                    MOCK_SERVICE_URL,
                    requestBody,
                    Map.class
            );

            // 3. Map the response to our Domain Object
            if (response != null) {
                // Safely cast numbers (JSON numbers can be Integer or Long)
                Number dist = (Number) response.get("distanceMeters");
                Number time = (Number) response.get("travelTimeSeconds");
                return new RouteEstimate(dist.longValue(), time.longValue());
            }

            return new RouteEstimate(0L, 0L); // Fallback

        } catch (Exception e) {
            System.err.println("External Service Failed: " + e.getMessage());
            return new RouteEstimate(0L, 0L);
        }
    }

    @Override
    public Coordinates geocodeAddress(String address) {
        return new Coordinates(37.9838, 23.7275); // Coordinates for Athens
    }
}