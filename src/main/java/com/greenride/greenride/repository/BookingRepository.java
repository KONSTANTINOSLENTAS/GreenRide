package com.greenride.greenride.repository;

import com.greenride.greenride.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find all bookings for a specific passenger (for "My Account" page)
    List<Booking> findByPassengerUsername(String username);

    // Find if a specific user is already on a specific route (prevent double booking)
    boolean existsByRouteIdAndPassengerUsername(Long routeId, String username);
}