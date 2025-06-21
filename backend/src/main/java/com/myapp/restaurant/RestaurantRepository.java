package com.myapp.restaurant;

import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class RestaurantRepository {

    public Restaurant saveNew(Restaurant toPersist) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(toPersist);   // ID will be generated
            tx.commit();
            return toPersist;
        }
    }

    public Optional<Restaurant> findById(long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Restaurant.class, id));
        }
    }

    public List<Restaurant> listByOwner(long ownerId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Restaurant> q = session.createQuery(
                    "from Restaurant where ownerId = :o", Restaurant.class);
            q.setParameter("o", ownerId);
            return q.list();
        }
    }

    public List<Restaurant> listApproved() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Restaurant> q = session.createQuery(
                    "from Restaurant where status = :s", Restaurant.class);
            q.setParameter("s", RestaurantStatus.APPROVED);
            return q.list();
        }
    }

    public void updateStatus(long id, RestaurantStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Restaurant r = session.get(Restaurant.class, id);
            if (r != null) r.setStatus(status);
            tx.commit();
        }
    }

    // Utility method for cleaning database during tests
    public void deleteAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from Restaurant").executeUpdate();
            tx.commit();
        }
    }

    // Additional methods for testing compatibility
    public Restaurant save(Restaurant restaurant) {
        if (restaurant.getId() == null) {
            return saveNew(restaurant);
        } else {
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.merge(restaurant);
                tx.commit();
                return restaurant;
            }
        }
    }

    public List<Restaurant> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Restaurant> q = session.createQuery("from Restaurant", Restaurant.class);
            return q.list();
        }
    }

    public List<Restaurant> findByStatus(RestaurantStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Restaurant> q = session.createQuery(
                    "from Restaurant where status = :s", Restaurant.class);
            q.setParameter("s", status);
            return q.list();
        }
    }

    public void delete(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, id);
            if (restaurant != null) {
                session.remove(restaurant);
            }
            tx.commit();
        }
    }
}