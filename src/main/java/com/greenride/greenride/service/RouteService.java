package com.greenride.greenride.service;

import com.greenride.greenride.domain.Route;
import com.greenride.greenride.domain.RouteStatus;
import com.greenride.greenride.domain.User;
import com.greenride.greenride.domain.ports.GeolocationPort;
import com.greenride.greenride.dto.Coordinates;
import com.greenride.greenride.dto.RouteEstimate;
import com.greenride.greenride.repository.RouteRepository;
import com.greenride.greenride.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final GeolocationPort geolocationPort;

    public RouteService(RouteRepository routeRepository,
                        UserRepository userRepository,
                        NotificationService notificationService,
                        GeolocationPort geolocationPort) {
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.geolocationPort = geolocationPort;
    }

    public Route createRoute(String username, String start, String destination,
                             LocalDateTime departureTime, Integer seats, Double price,
                             String carModel, Integer carYear, Integer carHp, String fuelType,
                             String licenseImgName, String idCardImgName) {

        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // 1. USE ADAPTER
        Coordinates startCoords = geolocationPort.geocodeAddress(start);
        Coordinates endCoords = geolocationPort.geocodeAddress(destination);
        RouteEstimate estimate = geolocationPort.calculateRouteEstimate(startCoords, endCoords);

        Route route = new Route();
        route.setDriver(driver);
        route.setStartAddress(start);
        route.setDestinationAddress(destination);
        route.setDepartureTime(departureTime);
        route.setAvailableSeats(seats);
        route.setPrice(price);

        // 2. SET DATA (Fixing getter usage)
        route.setDistanceMeters(estimate.getDistanceMeters());
        route.setEstimatedDurationSeconds(estimate.getTravelTimeSeconds());

        route.setStatus(RouteStatus.CREATED);

        // 3. SET CAR DETAILS
        route.setCarModel(carModel);
        route.setCarYear(carYear);
        route.setCarHp(carHp);
        route.setFuelType(fuelType);
        route.setLicenseImage(licenseImgName);
        route.setIdCardImage(idCardImgName);

        return routeRepository.save(route);
    }

    public List<Route> getAllAvailableRoutes() {
        return routeRepository.findAll().stream()
                .filter(r -> r.getStatus() == RouteStatus.CREATED && r.getAvailableSeats() > 0)
                .collect(Collectors.toList());
    }

    public List<Route> getRoutesByDriver(String username) {
        // Fixing the repository call
        return routeRepository.findByDriverUsername(username);
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + id));
    }

    public List<Route> searchRoutes(String from, String to) {
        return getAllAvailableRoutes().stream()
                .filter(route -> {
                    boolean matchesFrom = (from == null || from.isEmpty()) ||
                            route.getStartAddress().toLowerCase().contains(from.toLowerCase());
                    boolean matchesTo = (to == null || to.isEmpty()) ||
                            route.getDestinationAddress().toLowerCase().contains(to.toLowerCase());
                    return matchesFrom && matchesTo;
                })
                .collect(Collectors.toList());
    }

    // FIX: Changed FINISHED -> COMPLETED and restored manual notification logic
    public void finishRoute(Long id) {
        Route route = getRouteById(id);

        // 1. Fix Enum Name
        route.setStatus(RouteStatus.COMPLETED);
        routeRepository.save(route);

        // 2. Fix Notification Logic (Restore the loop)
        if (route.getBookings() != null) {
            for (com.greenride.greenride.domain.Booking b : route.getBookings()) {
                String msg = "🏁 The driver has finished the ride! Click here to rate your trip.";
                // This calls the existing method in NotificationService
                notificationService.sendImmediateAlert(b.getPassenger(), msg, route.getId());
            }
        }
    }

    public void updateRouteTime(Route route) {
        routeRepository.save(route);
    }
}