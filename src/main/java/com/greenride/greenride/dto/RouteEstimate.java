package com.greenride.greenride.dto;

public class RouteEstimate {
    private Long distanceMeters;
    private Long travelTimeSeconds;


    public RouteEstimate() {
    }


    public RouteEstimate(Long distanceMeters, Long travelTimeSeconds) {
        this.distanceMeters = distanceMeters;
        this.travelTimeSeconds = travelTimeSeconds;
    }

    public Long getDistanceMeters() {
        return distanceMeters;
    }

    public Long getTravelTimeSeconds() {
        return travelTimeSeconds;
    }

    public void setDistanceMeters(Long distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public void setTravelTimeSeconds(Long travelTimeSeconds) {
        this.travelTimeSeconds = travelTimeSeconds;
    }
}