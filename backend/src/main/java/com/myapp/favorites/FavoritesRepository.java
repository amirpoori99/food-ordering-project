package com.myapp.favorites;

import com.myapp.common.models.Favorite;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for favorites. A composite key of (userId, restaurantId) is used.
 * Thread-safe for tests; replace with JPA/SQL implementation in later phases.
 */
public class FavoritesRepository {

    private final Map<Key, Favorite> store = new ConcurrentHashMap<>();

    public Favorite save(long userId, long restaurantId) {
        Key k = new Key(userId, restaurantId);
        Favorite fav = new Favorite(userId, restaurantId, Instant.now());
        store.put(k, fav);
        return fav;
    }

    public Optional<Favorite> find(long userId, long restaurantId) {
        return Optional.ofNullable(store.get(new Key(userId, restaurantId)));
    }

    public List<Favorite> listByUser(long userId) {
        List<Favorite> result = new ArrayList<>();
        for (Favorite f : store.values()) {
            if (f.getUserId() == userId) result.add(f);
        }
        result.sort(Comparator.comparing(Favorite::getCreatedAt).reversed());
        return result;
    }

    public void delete(long userId, long restaurantId) {
        store.remove(new Key(userId, restaurantId));
    }

    public void clear() {
        store.clear();
    }

    private static class Key {
        private final long userId;
        private final long restaurantId;

        private Key(long userId, long restaurantId) {
            this.userId = userId;
            this.restaurantId = restaurantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return userId == key.userId && restaurantId == key.restaurantId;
        }

        @Override
        public int hashCode() {
            int result = Long.hashCode(userId);
            result = 31 * result + Long.hashCode(restaurantId);
            return result;
        }
    }
} 