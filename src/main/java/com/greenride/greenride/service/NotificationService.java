package com.greenride.greenride.service;

import com.greenride.greenride.domain.Booking;
import com.greenride.greenride.domain.Notification;
import com.greenride.greenride.repository.BookingRepository;
import com.greenride.greenride.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;

    public NotificationService(BookingRepository bookingRepository, NotificationRepository notificationRepository) {
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Runs every 30 seconds to check if any rides are starting or finishing.
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void checkRideStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> allBookings = bookingRepository.findAll();

        for (Booking booking : allBookings) {
            LocalDateTime departure = booking.getRoute().getDepartureTime();

            // Calculate Arrival based on duration
            long durationMinutes = booking.getRoute().getEstimatedDurationSeconds() / 60;
            LocalDateTime arrival = departure.plusMinutes(durationMinutes);

            Long routeId = booking.getRoute().getId();
            String destination = booking.getRoute().getDestinationAddress();

            // 1. PRE-RIDE CHECK (Between 4 and 6 minutes before departure)
            if (now.isAfter(departure.minusMinutes(6)) && now.isBefore(departure.minusMinutes(4))) {
                // Alert Passenger
                String msgPass = "🚗 Your ride to " + destination + " leaves in 5 minutes! Hurry up!";
                sendAlert(booking.getPassenger(), msgPass, routeId);

                // Alert Driver
                String msgDriver = "🔑 Time to drive! Your trip to " + destination + " starts in 5 minutes.";
                sendAlert(booking.getRoute().getDriver(), msgDriver, routeId);

                System.out.println("📧 SENDING ALERTS for Route " + routeId);
            }

            // 2. DEPARTURE CHECK (Between 0 and 2 minutes after departure)
            if (now.isAfter(departure) && now.isBefore(departure.plusMinutes(2))) {
                // Alert Passenger
                sendAlert(booking.getPassenger(), "🚦 It's time! Did you get into the car?", routeId);

                // Alert Driver
                sendAlert(booking.getRoute().getDriver(), "🟢 Start your engine! The ride has officially started.", routeId);
            }

            // 3. ARRIVAL CHECK (Between 0 and 2 minutes after arrival)
            if (now.isAfter(arrival) && now.isBefore(arrival.plusMinutes(2))) {
                // Alert Passenger
                sendAlert(booking.getPassenger(), "🏁 You should have arrived at " + destination + ". Did you get out safely?", routeId);

                // Alert Driver
                sendAlert(booking.getRoute().getDriver(), "🏁 Destination reached. Please mark the ride as Finished.", routeId);
            }
        }
    }


    private void sendAlert(com.greenride.greenride.domain.User user, String msg, Long routeId) {
        // Get recent notifications for this user
        List<Notification> recent = notificationRepository.findByUserUsernameOrderByTimestampDesc(user.getUsername());

        // Check if this specific message was sent in the last 5 minutes to avoid duplicates
        boolean alreadyExists = recent.stream()
                .anyMatch(n -> n.getMessage().equals(msg) && n.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(5)));

        if (!alreadyExists) {
            notificationRepository.save(new Notification(user, msg, routeId));
            System.out.println("🔔 Notification created for " + user.getUsername() + ": " + msg);
        }
    }

    public List<Notification> getUserNotifications(String username) {
        return notificationRepository.findByUserUsernameOrderByTimestampDesc(username);
    }

    // --- NEW PUBLIC METHOD FOR INSTANT ALERTS ---
    public void sendImmediateAlert(com.greenride.greenride.domain.User user, String msg, Long routeId) {
        // Save directly without checking for duplicates (Urgent messages)
        notificationRepository.save(new Notification(user, msg, routeId));
        System.out.println("🔔 INSTANT Notification for " + user.getUsername() + ": " + msg);
    }
}