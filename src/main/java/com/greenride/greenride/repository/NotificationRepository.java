package com.greenride.greenride.repository;

import com.greenride.greenride.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Get newest notifications first
    List<Notification> findByUserUsernameOrderByTimestampDesc(String username);
}