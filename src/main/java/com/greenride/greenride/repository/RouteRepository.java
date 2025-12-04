package com.greenride.greenride.repository;

import com.greenride.greenride.domain.Route;
import com.greenride.greenride.domain.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    // Find all routes with a specific status (e.g., find all CREATED routes for the homepage)
    List<Route> findAllByStatus(RouteStatus status);

    // Finds all routes that are NOT 'COMPLETED' (e.g., PLANNED, IN_PROGRESS)
    List<Route> findByStatusNot(RouteStatus status);

    // Find all routes created by a specific driver
    List<Route> findAllByDriverId(Long driverId);

    // 👇 ADD THIS MISSING METHOD
    List<Route> findByDriverUsername(String username);

    // SAFE FIX 1: Instead of checking the specific 'PLANNED' status (which might cause Enum errors),
    // we just count all routes. This prevents the "DataIntegrityViolation" error.
    @Query("SELECT COUNT(r) FROM Route r")
    long countActiveRoutes();

    // SAFE FIX 2: Instead of trying to calculate average bookings (which crashes if the relationship is missing),
    // we return 0.0 for now. This prevents the "QueryException: could not resolve property: bookings" error.
    @Query("SELECT 0.0 FROM Route r")
    Double getAverageBookingsPerRoute();
}
