package com.greenride.greenride.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer; // Who gave the rating

    @ManyToOne
    @JoinColumn(name = "reviewee_id")
    private User reviewee; // Who received the rating

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    private Integer score; // 1 to 5
    private String comment;
    private LocalDateTime timestamp = LocalDateTime.now();

    public Rating() {}

    public Rating(User reviewer, User reviewee, Route route, Integer score, String comment) {
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.route = route;
        this.score = score;
        this.comment = comment;
    }

    // GETTERS

    public Long getId() { return id; }

    public User getReviewer() { return reviewer; }

    public User getReviewee() { return reviewee; }

    public Route getRoute() { return route; }

    public Integer getScore() { return score; }

    public String getComment() { return comment; }

    public LocalDateTime getTimestamp() { return timestamp; }
}