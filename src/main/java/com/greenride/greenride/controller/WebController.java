package com.greenride.greenride.controller;

import com.greenride.greenride.domain.*;
import com.greenride.greenride.repository.RatingRepository;
import com.greenride.greenride.repository.UserRepository;
import com.greenride.greenride.service.BookingService;
import com.greenride.greenride.service.NotificationService;
import com.greenride.greenride.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;     // <--- IMPORT
import java.nio.file.*;         // <--- IMPORT
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final RouteService routeService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final RatingRepository ratingRepository;

    // Define where to save images
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public WebController(RouteService routeService,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         BookingService bookingService,
                         NotificationService notificationService,
                         RatingRepository ratingRepository) {
        this.routeService = routeService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
        this.ratingRepository = ratingRepository;
    }

    //  HOME PAGE
    @GetMapping("/")
    public String home(@RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       Model model) {
        List<Route> routes;
        if ((from != null && !from.isEmpty()) || (to != null && !to.isEmpty())) {
            routes = routeService.searchRoutes(from, to);
        } else {
            routes = routeService.getAllAvailableRoutes();
        }

        model.addAttribute("routes", routes);
        model.addAttribute("searchFrom", from);
        model.addAttribute("searchTo", to);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<Long> bookedRouteIds = new HashSet<>();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String username = auth.getName();
            List<Booking> userBookings = bookingService.getBookingsForUser(username);
            bookedRouteIds = userBookings.stream().map(b -> b.getRoute().getId()).collect(Collectors.toSet());
        }
        model.addAttribute("bookedRouteIds", bookedRouteIds);

        return "home";
    }

    //API: CHECK UNREAD NOTIFICATIONS
    @GetMapping("/api/notifications/unread")
    @ResponseBody
    public List<Notification> getUnreadNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Notification> all = notificationService.getUserNotifications(username);
        return all.stream()
                .filter(n -> n.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(4)))
                .collect(Collectors.toList());
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("notifications", notificationService.getUserNotifications(username));
        return "notifications";
    }

    //  AUTHENTICATION
    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String showRegisterForm() { return "register"; }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String fullName,
                               @RequestParam String email,
                               Model model) {
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already taken!");
            return "register";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return "redirect:/login";
    }

    //  CREATE ROUTe
    @GetMapping("/create-route")
    public String showCreateRouteForm() { return "create-route"; }

    @PostMapping("/create-route")
    public String createRoute(@RequestParam String startAddress,
                              @RequestParam String destinationAddress,
                              @RequestParam String departureTime,
                              @RequestParam Integer availableSeats,
                              @RequestParam Double price,
                              @RequestParam String carModel,
                              @RequestParam Integer carYear,
                              @RequestParam Integer carHp,
                              @RequestParam String fuelType,
                              Model model) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDateTime localDateTime = LocalDateTime.parse(departureTime);

        if (localDateTime.isBefore(LocalDateTime.now())) {
            model.addAttribute("error", " You cannot travel back in time!");
            return "create-route";
        }

        // --- SIMULATED FILE NAMES ---
        // We just pretend we saved them.
        String fakeLicense = "simulated_license.jpg";
        String fakeId = "simulated_id.jpg";

        // Call Service
        routeService.createRoute(
                username,
                startAddress,
                destinationAddress,
                localDateTime,
                availableSeats,
                price,
                carModel,
                carYear,
                carHp,
                fuelType,
                fakeLicense,
                fakeId
        );

        return "redirect:/";
    }

    // BOOKING
    @PostMapping("/book/{id}")
    public String bookRoute(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            bookingService.bookRoute(id, username);
            redirectAttributes.addFlashAttribute("success", "✅ Booking Confirmed!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        }
        return "redirect:/route/" + id;
    }

    // CANCELLATION
    @PostMapping("/booking/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                                @RequestParam String reason,
                                RedirectAttributes redirectAttributes) {
        String msg = bookingService.cancelBooking(id, reason);
        redirectAttributes.addFlashAttribute("info", msg);
        return "redirect:/my-routes";
    }

    //  ROUTE DETAILS
    @GetMapping("/route/{id}")
    public String routeDetails(@PathVariable Long id, Model model) {
        Route route = routeService.getRouteById(id);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isBooked = bookingService.isPassengerOnRoute(id, currentUsername);

        int progress = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = route.getDepartureTime();
        long durationSeconds = route.getEstimatedDurationSeconds();
        LocalDateTime end = start.plusSeconds(durationSeconds);

        String rideStatus = "UPCOMING";
        if (now.isAfter(start) && now.isBefore(end)) {
            rideStatus = "IN_PROGRESS";
            long secondsElapsed = Duration.between(start, now).getSeconds();
            progress = (int) ((secondsElapsed * 100.0) / durationSeconds);
        } else if (now.isAfter(end)) {
            rideStatus = "FINISHED";
            progress = 100;
        }

        model.addAttribute("route", route);
        model.addAttribute("currentUser", currentUsername);
        model.addAttribute("isBooked", isBooked);
        model.addAttribute("progress", progress);
        model.addAttribute("rideStatus", rideStatus);

        return "route-details";
    }

    // --- LIVE RIDE ---
    @GetMapping("/live-ride/{id}")
    public String liveRide(@PathVariable Long id, Model model) {
        Route route = routeService.getRouteById(id);
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = route.getDepartureTime();
        long durationSeconds = route.getEstimatedDurationSeconds();
        LocalDateTime end = start.plusSeconds(durationSeconds);

        int progress = 0;
        if (now.isAfter(start) && now.isBefore(end)) {
            long secondsElapsed = Duration.between(start, now).getSeconds();
            progress = (int) ((secondsElapsed * 100.0) / durationSeconds);
        } else if (now.isAfter(end)) progress = 100;

        // Participants and Filtering
        Set<User> participants = new HashSet<>();
        participants.add(route.getDriver());
        if (route.getBookings() != null) {
            for (Booking b : route.getBookings()) participants.add(b.getPassenger());
        }
        participants.removeIf(u -> u.getUsername().equals(currentUsername));

        List<Rating> myGivenRatings = ratingRepository.findByReviewerUsernameAndRouteId(currentUsername, id);
        Set<String> ratedUsernames = myGivenRatings.stream()
                .map(r -> r.getReviewee().getUsername())
                .collect(Collectors.toSet());
        participants.removeIf(u -> ratedUsernames.contains(u.getUsername()));

        boolean allRated = participants.isEmpty();

        model.addAttribute("route", route);
        model.addAttribute("progress", progress);
        model.addAttribute("arrivalTime", end);
        model.addAttribute("participants", participants);
        model.addAttribute("allRated", allRated);

        return "live-ride";
    }

    // --- DRIVER ACTIONS ---
    @PostMapping("/ride/{id}/finish")
    public String finishRide(@PathVariable Long id) {
        routeService.finishRoute(id);
        return "redirect:/live-ride/" + id;
    }

    @GetMapping("/api/ride/{id}/status")
    @ResponseBody
    public Map<String, String> checkRideStatus(@PathVariable Long id) {
        Route route = routeService.getRouteById(id);
        return Map.of("status", route.getStatus().name());
    }

    // --- SUBMIT RATING ---
    @PostMapping("/ride/{id}/rate")
    public String submitRating(@PathVariable Long id,
                               @RequestParam String targetUsername,
                               @RequestParam Integer rating,
                               @RequestParam String comment) {

        String reviewerName = SecurityContextHolder.getContext().getAuthentication().getName();
        User reviewer = userRepository.findByUsername(reviewerName).orElseThrow();
        User target = userRepository.findByUsername(targetUsername).orElseThrow();
        Route route = routeService.getRouteById(id);

        Rating newRating = new Rating(reviewer, target, route, rating, comment);
        ratingRepository.save(newRating);

        return "redirect:/live-ride/" + id;
    }

    // --- DEV TOOLS ---
    @PostMapping("/dev/start-now/{id}")
    public String devStartRideNow(@PathVariable Long id) {
        Route route = routeService.getRouteById(id);
        route.setDepartureTime(LocalDateTime.now().minusMinutes(1));
        routeService.updateRouteTime(route);
        return "redirect:/route/" + id;
    }

    @PostMapping("/dev/finish-now/{id}")
    public String devFinishRideNow(@PathVariable Long id) {
        Route route = routeService.getRouteById(id);
        long duration = route.getEstimatedDurationSeconds();
        route.setDepartureTime(LocalDateTime.now().minusSeconds(duration).minusMinutes(1));
        routeService.finishRoute(id);
        return "redirect:/route/" + id;
    }

    // --- OTHER PAGES ---
    @GetMapping("/my-routes")
    public String myRoutes(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("drivingRoutes", routeService.getRoutesByDriver(username));
        model.addAttribute("myBookings", bookingService.getBookingsForUser(username));
        return "my-routes";
    }

    @GetMapping("/my-account")
    public String myAccount(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("ridesDriven", routeService.getRoutesByDriver(username).size());
        model.addAttribute("ridesBooked", bookingService.getBookingsForUser(username).size());
        return "my-account";
    }

    @GetMapping("/user/{username}")
    public String userProfile(@PathVariable String username, Model model) {
        User user = userRepository.findByUsername(username).orElseThrow();

        Double avg = ratingRepository.getAverageRating(username);
        String formattedRating = (avg == null) ? "New" : String.format("%.1f", avg);
        List<Rating> reviews = ratingRepository.findByRevieweeUsername(username);

        model.addAttribute("profileUser", user);
        model.addAttribute("rating", formattedRating);
        model.addAttribute("reviews", reviews);
        model.addAttribute("ridesDriven", routeService.getRoutesByDriver(username).size());
        model.addAttribute("ridesBooked", bookingService.getBookingsForUser(username).size());

        return "public-profile";
    }

    @Autowired
    private com.greenride.greenride.service.AdminService adminService; // Inject the service

    @GetMapping("/admin")
    public String adminDashboard(org.springframework.ui.Model model) {
        // 1. Get Statistics
        model.addAttribute("stats", adminService.getSystemStatistics());

        // 2. Get User List (for the table)
        model.addAttribute("users", adminService.getAllUsers());

        return "admin-dashboard"; // This looks for admin-dashboard.html
    }

    // Endpoint to delete a user
    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return "redirect:/admin"; // Refresh page after delete
    }





}