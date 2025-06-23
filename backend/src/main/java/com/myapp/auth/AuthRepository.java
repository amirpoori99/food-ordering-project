package com.myapp.auth;

import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Optional;

public class AuthRepository {

    public User saveNew(User user) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            // Check duplicate phone
            Query<User> q = session.createQuery("from User where phone = :p", User.class);
            q.setParameter("p", user.getPhone());
            if (!q.getResultList().isEmpty()) {
                throw new DuplicatePhoneException(user.getPhone());
            }
            Transaction tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            return user;
        }
    }

    public Optional<User> findById(long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        }
    }

    public Optional<User> findByPhone(String phone) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<User> q = session.createQuery("from User where phone = :p", User.class);
            q.setParameter("p", phone);
            return q.uniqueResultOptional();
        }
    }

    public User update(User updated) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(updated);
            tx.commit();
            return updated;
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            // Convert constraint violation to our custom exception
            if (e.getConstraintName() != null && e.getConstraintName().toLowerCase().contains("phone")) {
                throw new DuplicatePhoneException(updated.getPhone());
            }
            throw e;
        } catch (org.hibernate.exception.GenericJDBCException e) {
            // Handle SQLite unique constraint violations
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique constraint failed: users.phone")) {
                throw new DuplicatePhoneException(updated.getPhone());
            }
            throw e;
        }
    }

    public void delete(long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User u = session.get(User.class, id);
            if (u != null) session.remove(u);
            tx.commit();
        }
    }

    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.id = :id", Long.class);
            query.setParameter("id", id);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        }
    }

    // Utility method for cleaning database during tests
    public void deleteAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from User").executeUpdate();
            tx.commit();
        }
    }
}
