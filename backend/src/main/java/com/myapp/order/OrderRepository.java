package com.myapp.order;

import com.myapp.common.models.Order;
import com.myapp.common.models.OrderStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class OrderRepository {

    public Order saveNew(Order order) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(order);   // ID will be generated
            tx.commit();
            return order;
        }
    }

    public Order save(Order order) {
        if (order.getId() == null) {
            return saveNew(order);
        } else {
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.merge(order);
                tx.commit();
                return order;
            }
        }
    }

    public Optional<Order> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "select o from Order o left join fetch o.orderItems oi left join fetch oi.foodItem where o.id = :id", Order.class);
            q.setParameter("id", id);
            return q.uniqueResultOptional();
        }
    }

    public List<Order> findByCustomer(Long customerId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "select distinct o from Order o left join fetch o.orderItems oi left join fetch oi.foodItem where o.customer.id = :customerId order by o.orderDate desc", Order.class);
            q.setParameter("customerId", customerId);
            return q.getResultList();
        }
    }

    public List<Order> findByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "select distinct o from Order o left join fetch o.orderItems oi left join fetch oi.foodItem where o.restaurant.id = :restaurantId order by o.orderDate desc", Order.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getResultList();
        }
    }

    public List<Order> findByStatus(OrderStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "from Order where status = :status order by orderDate desc", Order.class);
            q.setParameter("status", status);
            return q.getResultList();
        }
    }

    public List<Order> findByCustomerAndStatus(Long customerId, OrderStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "from Order where customer.id = :customerId and status = :status order by orderDate desc", 
                    Order.class);
            q.setParameter("customerId", customerId);
            q.setParameter("status", status);
            return q.getResultList();
        }
    }

    public List<Order> findByRestaurantAndStatus(Long restaurantId, OrderStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "from Order where restaurant.id = :restaurantId and status = :status order by orderDate desc", 
                    Order.class);
            q.setParameter("restaurantId", restaurantId);
            q.setParameter("status", status);
            return q.getResultList();
        }
    }

    public List<Order> findPendingOrders() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "from Order where status = :pending order by orderDate asc", Order.class);
            q.setParameter("pending", OrderStatus.PENDING);
            return q.getResultList();
        }
    }

    public List<Order> findActiveOrders() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "from Order where status in (:statuses) order by orderDate desc", Order.class);
            q.setParameterList("statuses", List.of(
                OrderStatus.PENDING, 
                OrderStatus.CONFIRMED, 
                OrderStatus.PREPARING, 
                OrderStatus.READY, 
                OrderStatus.OUT_FOR_DELIVERY
            ));
            return q.getResultList();
        }
    }

    public List<Order> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery("from Order order by orderDate desc", Order.class);
            return q.getResultList();
        }
    }

    public void updateStatus(Long id, OrderStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Order order = session.get(Order.class, id);
            if (order != null) {
                order.setStatus(status);
            }
            tx.commit();
        }
    }

    public void delete(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Order order = session.get(Order.class, id);
            if (order != null) {
                session.remove(order);
            }
            tx.commit();
        }
    }

    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(o) FROM Order o WHERE o.id = :id", Long.class);
            query.setParameter("id", id);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        }
    }

    public Order update(Order order) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Order updated = (Order) session.merge(order);
            tx.commit();
            return updated;
        }
    }

    // Utility method for cleaning database during tests
    public void deleteAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from Order").executeUpdate();
            tx.commit();
        }
    }
}
