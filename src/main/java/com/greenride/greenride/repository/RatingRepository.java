package com.greenride.greenride.repository;

import com.greenride.greenride.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Calculate average rating for a specific user
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.reviewee.username = :username")
    Double getAverageRating(String username);

    // Get all reviews received by a user
    List<Rating> findByRevieweeUsername(String username);
    // Find ratings given by a specific user for a specific route
    List<Rating> findByReviewerUsernameAndRouteId(String reviewerUsername, Long routeId);
}

