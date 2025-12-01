package com.greenride.greenride;

import com.greenride.greenride.domain.Route;
import com.greenride.greenride.domain.RouteStatus;
import com.greenride.greenride.domain.User;
import com.greenride.greenride.repository.RouteRepository;
import com.greenride.greenride.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class GreenrideApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenrideApplication.class, args);
    }


    @Bean
    public CommandLineRunner dataLoader(UserRepository userRepo,
                                        RouteRepository routeRepo,
                                        PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepo.findByUsername("driver1").isEmpty()) {
                User driver = new User();
                driver.setUsername("driver1");
                // Username: driver1, Password: password
                driver.setPassword(passwordEncoder.encode("password"));
                driver.setRole("ROLE_USER");
                driver.setFullName("Alice Driver");
                driver.setEmail("alice@greenride.com");
                userRepo.save(driver);
                User passenger = new User();
                passenger.setUsername("test");
                passenger.setPassword(passwordEncoder.encode("password"));
                passenger.setRole("ROLE_USER");
                passenger.setFullName("Alice Passenger");
                passenger.setEmail("random@lekkes.com");
                userRepo.save(passenger);

                // 2. Create a  Route (Athens -> Thessaloniki)
                Route r1 = new Route();
                r1.setDriver(driver);
                r1.setStartAddress("Syntagma, Athens");
                r1.setDestinationAddress("White Tower, Thessaloniki");
                r1.setAvailableSeats(3);
                r1.setPrice(45.50);
                r1.setCarModel("BMW X1");
                r1.setCarHp(140);
                r1.setCarYear(2021);
                r1.setFuelType("Gasoline");
                r1.setStatus(RouteStatus.CREATED);


                r1.setDistanceMeters(500000L); // 500 km
                r1.setEstimatedDurationSeconds(18000L); // 5 hours
                r1.setDepartureTime(java.time.LocalDateTime.now().plusDays(1)); // Tomorrow

                routeRepo.save(r1);

                System.out.println("✅ INJECTED DUMMY DATA: User 'driver1' and 1 Route.");
            }
        };
    }
}