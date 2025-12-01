package com.greenride.greenride.repository;

import com.greenride.greenride.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // This defines the method so the Controller can find it
    Optional<User> findByUsername(String username);
}