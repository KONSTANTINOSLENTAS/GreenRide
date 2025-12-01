package com.greenride.greenride.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which Route is this?
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    // Who is the Passenger?
    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    //  CONSTRUCTORS
    public Booking() {}

    public Booking(Route route, User passenger, BookingStatus status) {
        this.route = route;
        this.passenger = passenger;
        this.status = status;
        this.bookingTime = LocalDateTime.now();
    }

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}