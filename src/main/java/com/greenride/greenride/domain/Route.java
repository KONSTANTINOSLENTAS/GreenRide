package com.greenride.greenride.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the Driver (User)
    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    private String startAddress;
    private String destinationAddress;

    // We will populate these using the External Service!
    private Long distanceMeters;
    private Long estimatedDurationSeconds;

    private LocalDateTime departureTime;
    private Integer availableSeats;
    private Double price;

    @Enumerated(EnumType.STRING)
    private RouteStatus status;

    // --- CONSTRUCTORS ---
    public Route() {}

    // --- GETTERS & SETTERS (Manual) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public Long getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Long distanceMeters) { this.distanceMeters = distanceMeters; }

    public Long getEstimatedDurationSeconds() { return estimatedDurationSeconds; }
    public void setEstimatedDurationSeconds(Long estimatedDurationSeconds) { this.estimatedDurationSeconds = estimatedDurationSeconds; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public RouteStatus getStatus() { return status; }
    public void setStatus(RouteStatus status) { this.status = status; }


    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private java.util.List<Booking> bookings;

    //  NEW CAR DETAILS
    private String carModel;
    private Integer carYear;
    private Integer carHp;
    private String fuelType; // Petrol, Diesel, Electric, Hybrid

    //  NEW VERIFICATION IMAGES (Store filenames only)
    private String licenseImage; // e.g., "driver1_license.jpg"
    private String idCardImage;  // e.g., "driver1_id.jpg"

    //  GETTERS & SETTERS
    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public Integer getCarYear() { return carYear; }
    public void setCarYear(Integer carYear) { this.carYear = carYear; }

    public Integer getCarHp() { return carHp; }
    public void setCarHp(Integer carHp) { this.carHp = carHp; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getLicenseImage() { return licenseImage; }
    public void setLicenseImage(String licenseImage) { this.licenseImage = licenseImage; }

    public String getIdCardImage() { return idCardImage; }
    public void setIdCardImage(String idCardImage) { this.idCardImage = idCardImage; }

    public java.util.List<Booking> getBookings() {
        return bookings;
    }
}