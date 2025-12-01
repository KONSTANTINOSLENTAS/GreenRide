package com.greenride.greenride.dto;

import java.time.LocalDateTime;

public class CreateRouteRequest {
    private String startAddress;
    private String destinationAddress;
    private LocalDateTime departureTime;
    private Integer availableSeats;
    private Double price;

    //  Getters and Setters
    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}