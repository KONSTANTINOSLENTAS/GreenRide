package com.greenride.greenride.controller;

import com.greenride.greenride.domain.Route;
import com.greenride.greenride.dto.CreateRouteRequest;
import com.greenride.greenride.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    public ResponseEntity<Route> createRoute(@RequestBody CreateRouteRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // Pass DEFAULT values for the new fields to fix the compilation error
        Route createdRoute = routeService.createRoute(
                currentUsername,
                request.getStartAddress(),
                request.getDestinationAddress(),
                request.getDepartureTime(),
                request.getAvailableSeats(),
                request.getPrice(),

                //  DEFAULT VALUES
                "Standard Car",  // Car Model
                2024,            // Car Year
                100,             // Horsepower
                "Petrol",        // Fuel Type
                "default.png",   // License Image
                "default.png"    // ID Card Image
        );

        return ResponseEntity.ok(createdRoute);
    }

    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllAvailableRoutes());
    }
}