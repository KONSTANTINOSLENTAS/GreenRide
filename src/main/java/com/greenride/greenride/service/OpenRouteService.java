package com.greenride.greenride.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Service
public class OpenRouteService {

    // 🔴 MAKE SURE YOUR API KEY IS HERE
    private final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjhjZmIxZTBhNWY3YzRmYjA4NzQ3ZTE4OGI0MDQ0MzM3IiwiaCI6Im11cm11cjY0In0=";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public double[] getCoordinates(String address) {
        try {
            // FIX: Add &boundary.country=GR to force Greece results only
            String url = "https://api.openrouteservice.org/geocode/search?api_key=" + API_KEY
                    + "&text=" + address
                    + "&boundary.country=GR";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            // Check if we found anything
            if (root.path("features").size() == 0) {
                System.out.println("❌ Error: No coordinates found for " + address + " in Greece.");
                return new double[]{0.0, 0.0};
            }

            JsonNode coordinates = root.path("features").get(0).path("geometry").path("coordinates");

            double lon = coordinates.get(0).asDouble();
            double lat = coordinates.get(1).asDouble();

            System.out.println("✅ Geocoded '" + address + "' to: " + lat + ", " + lon);

            return new double[]{lon, lat};

        } catch (Exception e) {
            System.out.println("Geocoding failed for " + address + ": " + e.getMessage());
            return new double[]{23.7275, 37.9838}; // Fallback to Athens
        }
    }

    public Map<String, Object> getRouteDetails(String startAddress, String endAddress) {
        Map<String, Object> result = new HashMap<>();

        try {
            double[] startCoords = getCoordinates(startAddress);
            double[] endCoords = getCoordinates(endAddress);

            // FIX: Ensure we aren't routing 0,0 to 0,0
            if (startCoords[0] == 0.0 || endCoords[0] == 0.0) {
                throw new RuntimeException("Invalid coordinates found");
            }

            String url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + API_KEY
                    + "&start=" + startCoords[0] + "," + startCoords[1]
                    + "&end=" + endCoords[0] + "," + endCoords[1];

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode summary = root.path("features").get(0).path("properties").path("summary");

            result.put("distanceMeters", summary.path("distance").asDouble());
            result.put("durationSeconds", summary.path("duration").asDouble());
            result.put("elevation", "Calculated");

        } catch (Exception e) {
            System.out.println("Routing failed: " + e.getMessage());
            // Fallback so the app doesn't crash
            result.put("distanceMeters", 0.0);
            result.put("durationSeconds", 0.0);
            result.put("elevation", "N/A");
        }

        return result;
    }
}