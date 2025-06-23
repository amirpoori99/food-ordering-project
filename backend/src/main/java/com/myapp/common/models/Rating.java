package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Rating entity representing user ratings and reviews for restaurants
 * Supports ratings from 1-5 stars with optional text reviews
 */
@Entity
@Table(name = "ratings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "restaurant_id"}),
       indexes = {
           @Index(name = "idx_rating_restaurant", columnList = "restaurant_id"),
           @Index(name = "idx_rating_user", columnList = "user_id"),
           @Index(name = "idx_rating_score", columnList = "rating_score")
       })
public class Rating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(name = "rating_score", nullable = false)
    private Integer ratingScore; // 1-5 stars
    
    @Column(name = "review_text", length = 1000)
    private String reviewText;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false; // Admin can verify reviews
    
    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0;
    
    // Constructors
    public Rating() {
        this.createdAt = LocalDateTime.now();
        this.isVerified = false;
        this.helpfulCount = 0;
    }
    
    public Rating(User user, Restaurant restaurant, Integer ratingScore) {
        this();
        this.user = user;
        this.restaurant = restaurant;
        setRatingScore(ratingScore);
    }
    
    public Rating(User user, Restaurant restaurant, Integer ratingScore, String reviewText) {
        this(user, restaurant, ratingScore);
        this.reviewText = reviewText;
    }
    
    // Business Logic Methods
    
    /**
     * Updates the rating score with validation
     */
    public void updateRating(Integer newScore, String newReviewText) {
        setRatingScore(newScore);
        this.reviewText = newReviewText;
        this.updatedAt = LocalDateTime.now();
        this.isVerified = false; // Reset verification on update
    }
    
    /**
     * Validates and sets rating score (1-5 stars)
     */
    public void setRatingScore(Integer ratingScore) {
        if (ratingScore == null || ratingScore < 1 || ratingScore > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5 stars");
        }
        this.ratingScore = ratingScore;
    }
    
    /**
     * Validates and sets review text
     */
    public void setReviewText(String reviewText) {
        if (reviewText != null && reviewText.trim().length() > 1000) {
            throw new IllegalArgumentException("Review text cannot exceed 1000 characters");
        }
        this.reviewText = reviewText != null ? reviewText.trim() : null;
    }
    
    /**
     * Increments helpful count
     */
    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }
    
    /**
     * Decrements helpful count (cannot go below 0)
     */
    public void decrementHelpfulCount() {
        if (this.helpfulCount > 0) {
            this.helpfulCount--;
        }
    }
    
    /**
     * Verifies the review (admin action)
     */
    public void verify() {
        this.isVerified = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Unverifies the review (admin action)
     */
    public void unverify() {
        this.isVerified = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if rating has review text
     */
    public boolean hasReview() {
        return reviewText != null && !reviewText.trim().isEmpty();
    }
    
    /**
     * Checks if rating is recent (within last 30 days)
     */
    public boolean isRecent() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * Gets display text for rating score
     */
    public String getRatingDisplay() {
        return "★".repeat(ratingScore) + "☆".repeat(5 - ratingScore);
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isVerified == null) {
            isVerified = false;
        }
        if (helpfulCount == null) {
            helpfulCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Restaurant getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    
    public Integer getRatingScore() {
        return ratingScore;
    }
    
    public String getReviewText() {
        return reviewText;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
    
    public Integer getHelpfulCount() {
        return helpfulCount;
    }
    
    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = Math.max(0, helpfulCount != null ? helpfulCount : 0);
    }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return Objects.equals(id, rating.id) &&
               Objects.equals(user, rating.user) &&
               Objects.equals(restaurant, rating.restaurant);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, user != null ? user.getId() : null, 
                           restaurant != null ? restaurant.getId() : null);
    }
    
    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", restaurantId=" + (restaurant != null ? restaurant.getId() : null) +
                ", ratingScore=" + ratingScore +
                ", hasReview=" + hasReview() +
                ", isVerified=" + isVerified +
                ", helpfulCount=" + helpfulCount +
                ", createdAt=" + createdAt +
                '}';
    }
}
