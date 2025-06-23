package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity representing a user's favorite restaurant
 * Tracks when users add restaurants to their favorites list
 */
@Entity
@Table(name = "favorites", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "restaurant_id"}),
       indexes = {
           @Index(name = "idx_favorite_user", columnList = "user_id"),
           @Index(name = "idx_favorite_restaurant", columnList = "restaurant_id"),
           @Index(name = "idx_favorite_created", columnList = "created_at")
       })
public class Favorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "notes", length = 500)
    private String notes; // Optional notes about why they liked it
    
    // Constructors
    public Favorite() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Favorite(User user, Restaurant restaurant) {
        this();
        this.user = user;
        this.restaurant = restaurant;
    }
    
    public Favorite(User user, Restaurant restaurant, String notes) {
        this(user, restaurant);
        this.notes = notes;
    }
    
    // Legacy constructor for backward compatibility
    public Favorite(long userId, long restaurantId, java.time.Instant createdAt) {
        this.createdAt = LocalDateTime.ofInstant(createdAt, java.time.ZoneOffset.UTC);
        // Note: User and Restaurant objects need to be set separately when using this constructor
    }
    
    // Business Logic Methods
    
    /**
     * Updates the notes for this favorite
     */
    public void updateNotes(String notes) {
        this.notes = notes != null && notes.trim().length() > 500 
            ? notes.trim().substring(0, 500) 
            : (notes != null ? notes.trim() : null);
    }
    
    /**
     * Checks if this favorite has notes
     */
    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
    
    /**
     * Checks if this favorite is recent (within last 30 days)
     */
    public boolean isRecent() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * Gets a display-friendly creation date
     */
    public String getCreatedAtFormatted() {
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        updateNotes(notes);
    }
    
    // Legacy getters for backward compatibility
    public long getUserId() {
        return user != null ? user.getId() : 0L;
    }
    
    public long getRestaurantId() {
        return restaurant != null ? restaurant.getId() : 0L;
    }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Favorite favorite = (Favorite) o;
        return Objects.equals(id, favorite.id) &&
               Objects.equals(user, favorite.user) &&
               Objects.equals(restaurant, favorite.restaurant);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, user != null ? user.getId() : null, 
                           restaurant != null ? restaurant.getId() : null);
    }
    
    @Override
    public String toString() {
        return "Favorite{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", restaurantId=" + (restaurant != null ? restaurant.getId() : null) +
                ", hasNotes=" + hasNotes() +
                ", createdAt=" + createdAt +
                '}';
    }
} 