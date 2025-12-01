package com.greenride.greenride.service;

import com.greenride.greenride.domain.Booking;
import com.greenride.greenride.domain.BookingStatus;
import com.greenride.greenride.domain.Route;
import com.greenride.greenride.domain.User;
import com.greenride.greenride.dto.PaymentRequest;
import com.greenride.greenride.dto.PaymentResponse;
import com.greenride.greenride.repository.BookingRepository;
import com.greenride.greenride.repository.RouteRepository;
import com.greenride.greenride.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // <--- Notification Support

    private final RestTemplate restTemplate = new RestTemplate();

    public BookingService(BookingRepository bookingRepository,
                          RouteRepository routeRepository,
                          UserRepository userRepository,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void bookRoute(Long routeId, String passengerUsername) {

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        User passenger = userRepository.findByUsername(passengerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validation Checks
        if (route.getDriver().getUsername().equals(passengerUsername)) {
            throw new RuntimeException("You cannot book your own route!");
        }
        if (route.getAvailableSeats() <= 0) {
            throw new RuntimeException("This route is fully booked.");
        }
        if (bookingRepository.existsByRouteIdAndPassengerUsername(routeId, passengerUsername)) {
            throw new RuntimeException("You have already booked this route.");
        }

        // ============================================================
        // 💸 PAYMENT STEP (Mock)
        // ============================================================
        String bankUrl = "http://localhost:8080/api/external/bank/process";
        PaymentRequest payReq = new PaymentRequest(passengerUsername, route.getPrice());

        try {
            PaymentResponse response = restTemplate.postForObject(bankUrl, payReq, PaymentResponse.class);
            if (response == null || "FAILED".equals(response.getStatus())) {
                throw new RuntimeException("Payment Declined by Bank! (Insufficient Funds)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Transaction Failed: " + e.getMessage());
        }
        // ============================================================

        // 1. SAVE BOOKING
        route.setAvailableSeats(route.getAvailableSeats() - 1);
        routeRepository.save(route);

        Booking booking = new Booking(route, passenger, BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // 2. ALERT DRIVER
        String msg = "🎟️ New Booking! " + passenger.getFullName() + " joined your ride to " + route.getDestinationAddress();
        notificationService.sendImmediateAlert(route.getDriver(), msg, route.getId());
    }

    // --- CANCELLATION LOGIC ---
    @Transactional
    public String cancelBooking(Long bookingId, String reasonCategory) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Route route = booking.getRoute();
        User passenger = booking.getPassenger();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departure = route.getDepartureTime();

        long minutesUntilDeparture = java.time.Duration.between(now, departure).toMinutes();
        String resultMessage;

        // 1. CHECK 10 MINUTE RULE
        // If departing in past or < 10 mins future
        if (minutesUntilDeparture < 10) {
            // Bad Mark on Profile
            passenger.setLateCancellations(passenger.getLateCancellations() + 1);
            userRepository.save(passenger);
            resultMessage = "⚠️ Late Cancellation (<10 mins). No Refund issued. Marked on profile.";
        } else {
            // Good Cancel
            resultMessage = "✅ Booking cancelled. Refund processed.";
        }

        // 2. RESTORE SEAT & DELETE BOOKING
        route.setAvailableSeats(route.getAvailableSeats() + 1);
        routeRepository.save(route);
        bookingRepository.delete(booking);

        // 3. ALERT DRIVER
        String driverMsg = "❌ Passenger " + passenger.getFullName() + " cancelled. Reason: " + reasonCategory;
        notificationService.sendImmediateAlert(route.getDriver(), driverMsg, route.getId());

        return resultMessage;
    }

    public List<Booking> getBookingsForUser(String username) {
        return bookingRepository.findByPassengerUsername(username);
    }

    public boolean isPassengerOnRoute(Long routeId, String username) {
        return bookingRepository.existsByRouteIdAndPassengerUsername(routeId, username);
    }
}