package com.greenride.greenride.service;

import com.greenride.greenride.domain.Route;
import com.greenride.greenride.domain.RouteStatus;
import com.greenride.greenride.domain.User;
import com.greenride.greenride.repository.RouteRepository;
import com.greenride.greenride.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final OpenRouteService openRouteService;
    private final NotificationService notificationService; // <--- Notification Support

    public RouteService(RouteRepository routeRepository,
                        UserRepository userRepository,
                        OpenRouteService openRouteService,
                        NotificationService notificationService) {
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.openRouteService = openRouteService;
        this.notificationService = notificationService;
    }

    public Route createRoute(String username, String start, String destination,
                             LocalDateTime departureTime, Integer seats, Double price,
                             String carModel, Integer carYear, Integer carHp, String fuelType,
                             String licenseImgName, String idCardImgName) { // <--- NEW PARAMS

        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // 1. API Call (Keep existing)
        Map<String, Object> routeData = openRouteService.getRouteDetails(start, destination);
        Double distanceDouble = (Double) routeData.get("distanceMeters");
        Double durationDouble = (Double) routeData.get("durationSeconds");

        Route route = new Route();
        route.setDriver(driver);
        route.setStartAddress(start);
        route.setDestinationAddress(destination);
        route.setDepartureTime(departureTime);
        route.setAvailableSeats(seats);
        route.setPrice(price);
        route.setDistanceMeters(distanceDouble.longValue());
        route.setEstimatedDurationSeconds(durationDouble.longValue());
        route.setStatus(RouteStatus.CREATED);

        // 2. SET NEW DETAILS
        route.setCarModel(carModel);
        route.setCarYear(carYear);
        route.setCarHp(carHp);
        route.setFuelType(fuelType);
        route.setLicenseImage(licenseImgName);
        route.setIdCardImage(idCardImgName);

        return routeRepository.save(route);
    }

    public List<Route> getAllAvailableRoutes() {
        return routeRepository.findAll();
    }

    public List<Route> getRoutesByDriver(String username) {
        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return routeRepository.findAllByDriverId(driver.getId());
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

    // --- FINISH ROUTE (Triggers Notifications) ---
    public void finishRoute(Long id) {
        Route route = getRouteById(id);
        route.setStatus(RouteStatus.COMPLETED);
        routeRepository.save(route);

        // NOTIFY PASSENGERS INSTANTLY
        if (route.getBookings() != null) {
            for (com.greenride.greenride.domain.Booking b : route.getBookings()) {
                String msg = "🏁 The driver has finished the ride! Click here to rate your trip.";
                notificationService.sendImmediateAlert(b.getPassenger(), msg, route.getId());
            }
        }
    }

    // --- THIS WAS MISSING (Fixes your error) ---
    public void updateRouteTime(Route route) {
        routeRepository.save(route);
    }
}