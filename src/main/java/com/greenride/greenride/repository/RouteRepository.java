package com.greenride.greenride.repository;

import com.greenride.greenride.domain.Route;
import com.greenride.greenride.domain.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    // Find all routes with a specific status (e.g., find all CREATED routes for the homepage)
    List<Route> findAllByStatus(RouteStatus status);

    // Find all routes created by a specific driver
    List<Route> findAllByDriverId(Long driverId);

    // 👇 ADD THIS MISSING METHOD
    List<Route> findByDriverUsername(String username);
}