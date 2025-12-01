package com.greenride.greenride.domain;

public enum RouteStatus {
    CREATED,      // Driver created it, waiting for passengers
    BOOKED,       // (Optional) Full or confirmed
    IN_PROGRESS,  // Driver started the ride
    COMPLETED,    // Ride finished
    CANCELLED     // Driver cancelled
}