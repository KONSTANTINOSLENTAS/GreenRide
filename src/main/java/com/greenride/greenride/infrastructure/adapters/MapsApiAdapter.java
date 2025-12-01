package com.greenride.greenride.infrastructure.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenride.greenride.domain.ports.GeolocationPort;
import com.greenride.greenride.dto.Coordinates;
import com.greenride.greenride.dto.RouteEstimate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MapsApiAdapter implements GeolocationPort {

    // Your Real API Key
    private final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjhjZmIxZTBhNWY3YzRmYjA4NzQ3ZTE4OGI0MDQ0MzM3IiwiaCI6Im11cm11cjY0In0=";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Coordinates geocodeAddress(String address) {
        try {
            // Add &boundary.country=GR to force Greece results
            String url = "https://api.openrouteservice.org/geocode/search?api_key=" + API_KEY
                    + "&text=" + address
                    + "&boundary.country=GR";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.path("features").size() == 0) {
                System.out.println("❌ Error: No coordinates found for " + address);
                // Fallback to Athens center
                return new Coordinates(37.9838, 23.7275);
            }

            JsonNode coordsNode = root.path("features").get(0).path("geometry").path("coordinates");
            double lon = coordsNode.get(0).asDouble();
            double lat = coordsNode.get(1).asDouble();

            System.out.println("✅ Geocoded '" + address + "' to: " + lat + ", " + lon);
            return new Coordinates(lat, lon);

        } catch (Exception e) {
            System.out.println("Geocoding failed for " + address + ": " + e.getMessage());
            return new Coordinates(37.9838, 23.7275); // Fallback
        }
    }

    @Override
    public RouteEstimate calculateRouteEstimate(Coordinates start, Coordinates destination) {
        try {
            // Ensure we aren't routing 0,0 to 0,0
            if (start.getLatitude() == 0.0 || destination.getLatitude() == 0.0) {
                return new RouteEstimate(0L, 0L);
            }

            // Note: OpenRouteService expects "lon,lat"
            String url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + API_KEY
                    + "&start=" + start.getLongitude() + "," + start.getLatitude()
                    + "&end=" + destination.getLongitude() + "," + destination.getLatitude();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode summary = root.path("features").get(0).path("properties").path("summary");

            double distance = summary.path("distance").asDouble();
            double duration = summary.path("duration").asDouble();

            return new RouteEstimate((long) distance, (long) duration);

        } catch (Exception e) {
            System.out.println("Routing failed: " + e.getMessage());
            return new RouteEstimate(0L, 0L);
        }
    }
}