package com.myapp.common.models;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a restaurant that a user has marked as favourite.
 * At this stage we only store the userId and restaurantId to keep the model simple.
 */
public class Favorite {
    private final long userId;
    private final long restaurantId;
    private final Instant createdAt;

    public Favorite(long userId, long restaurantId, Instant createdAt) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.createdAt = createdAt;
    }

    public long getUserId() {
        return userId;
    }

    public long getRestaurantId() {
        return restaurantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Favorite)) return false;
        Favorite favorite = (Favorite) o;
        return userId == favorite.userId && restaurantId == favorite.restaurantId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, restaurantId);
    }
} 