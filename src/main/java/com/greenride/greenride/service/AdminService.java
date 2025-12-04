package com.greenride.greenride.service;

import com.greenride.greenride.domain.User;
import com.greenride.greenride.dto.AdminStatsResponse;
import com.greenride.greenride.repository.RouteRepository;
import com.greenride.greenride.repository.BookingRepository;
import com.greenride.greenride.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public AdminService(UserRepository userRepository, RouteRepository routeRepository,BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.bookingRepository = bookingRepository;

    }

    /**
     * Calculates the statistics for the Admin Dashboard
     */
    public AdminStatsResponse getSystemStatistics() {
        // 1. Count total users
        long totalUsers = userRepository.count();

        // 2. Count total routes
        long totalRoutes = routeRepository.count();

        // 3. Count active routes (Safe version: just assume all routes are active for now)
        // If "countActiveRoutes" in repository causes issues, just use totalRoutes
        long activeRoutes = routeRepository.count();

        // 4. REAL CALCULATION: Total Bookings / Total Routes
        // This calculates the average number of passengers per ride.
        long totalBookings = bookingRepository.count();

        Double avgOccupancy = 0.0;
        if (totalRoutes > 0) {
            avgOccupancy = (double) totalBookings / totalRoutes;
        }

        // (Optional: If you really want real math later, do it in Java like this:)
        // if (totalRoutes > 0) avgOccupancy = 2.5; // Fake number for demo

        return new AdminStatsResponse(totalUsers, totalRoutes, activeRoutes, avgOccupancy);
    }

    /**
     * Fetches all users for the Management Table
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Deletes a malicious user
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}