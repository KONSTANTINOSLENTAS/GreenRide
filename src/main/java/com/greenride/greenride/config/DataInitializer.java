package com.greenride.greenride.config;

import com.greenride.greenride.domain.User;
import com.greenride.greenride.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    // This method runs automatically every time the server starts
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if the "admin" user already exists
            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@greenride.com");
                admin.setFullName("System Administrator");

                // CRITICAL: We must encrypt the password exactly like AuthController does
                admin.setPassword(passwordEncoder.encode("admin123"));

                // CRITICAL: This must match what you check in home.html
                // Spring Security 'hasRole("ADMIN")' looks for "ROLE_ADMIN" in the database
                admin.setRole("ROLE_ADMIN");

                userRepository.save(admin);

                System.out.println("---------------------------------------------");
                System.out.println(" ADMIN ACCOUNT CREATED SUCCESSFULLY");
                System.out.println("   Username: admin");
                System.out.println("   Password: admin123");
                System.out.println("---------------------------------------------");
            }
        };
    }
}