package com.myapp.order;

import com.myapp.common.models.Order;
import com.myapp.common.models.OrderStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository سفارشات - لایه دسترسی به داده
 * این کلاس مسئول تمام عملیات دیتابیس مربوط به سفارشات است
 * از الگوی Repository Pattern برای جداسازی منطق دیتابیس استفاده می‌کند
 * شامل eager loading برای بارگذاری کامل اطلاعات سفارش و آیتم‌ها
 */
public class OrderRepository {

    /**
     * ذخیره سفارش جدید در دیتابیس
     * شناسه به صورت خودکار توسط Hibernate تولید می‌شود
     * 
     * @param order سفارش برای ذخیره
     * @return سفارش ذخیره شده همراه با شناسه تولید شده
     */
    public Order saveNew(Order order) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(order);   // شناسه به صورت خودکار تولید می‌شود
            tx.commit();
            return order;
        }
    }

    /**
     * ذخیره یا به‌روزرسانی سفارش
     * اگر شناسه null باشد، سفارش جدید ایجاد می‌شود
     * در غیر اینصورت سفارش موجود به‌روزرسانی می‌شود
     * 
     * @param order سفارش برای ذخیره/به‌روزرسانی
     * @return سفارش ذخیره شده
     */
    public Order save(Order order) {
        if (order.getId() == null) {
            return saveNew(order);  // ایجاد سفارش جدید
        } else {
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.merge(order);  // به‌روزرسانی سفارش موجود
                tx.commit();
                return order;
            }
        }
    }

    /**
     * یافتن سفارش با شناسه همراه با بارگذاری کامل آیتم‌ها
     * از left join fetch برای بارگذاری eager استفاده می‌کند
     * 
     * @param id شناسه سفارش
     * @return Optional حاوی سفارش کامل یا خالی در صورت عدم وجود
     */
    public Optional<Order> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "select o from Order o left join fetch o.orderItems oi left join fetch oi.foodItem where o.id = :id", Order.class);
            q.setParameter("id", id);
            return q.uniqueResultOptional();
        }
    }

    /**
     * دریافت همه سفارشات یک مشتری همراه با آیتم‌ها
     * مرتب شده بر اساس تاریخ سفارش (جدیدترین اول)
     * 
     * @param customerId شناسه مشتری
     * @return لیست سفارشات مشتری
     */
    public List<Order> findByCustomer(Long customerId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "select distinct o from Order o left join fetch o.orderItems oi left join fetch oi.foodItem where o.customer.id = :customerId order by o.orderDate desc", Order.class);
            q.setParameter("customerId", customerId);
            return q.getResultList();
        }
    }

    /**
     * دریافت همه سفارشات یک رستوران همراه با آیتم‌ها
     * مرتب شده بر اساس تاریخ سفارش (جدیدترین اول)
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست سفارشات رستوران
     */
    public List<Order> findByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "select distinct o from Order o left join fetch o.orderItems oi left join fetch oi.foodItem where o.restaurant.id = :restaurantId order by o.orderDate desc", Order.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getResultList();
        }
    }

    /**
     * دریافت سفارشات بر اساس وضعیت
     * مرتب شده بر اساس تاریخ سفارش (جدیدترین اول)
     * 
     * @param status وضعیت مورد نظر
     * @return لیست سفارشات با وضعیت مشخص
     */
    public List<Order> findByStatus(OrderStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "from Order where status = :status order by orderDate desc", Order.class);
            q.setParameter("status", status);
            return q.getResultList();
        }
    }

    /**
     * دریافت سفارشات مشتری با وضعیت خاص
     * 
     * @param customerId شناسه مشتری
     * @param status وضعیت مورد نظر
     * @return لیست سفارشات مشتری با وضعیت مشخص
     */
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

    /**
     * دریافت سفارشات رستوران با وضعیت خاص
     * 
     * @param restaurantId شناسه رستوران
     * @param status وضعیت مورد نظر
     * @return لیست سفارشات رستوران با وضعیت مشخص
     */
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

    /**
     * دریافت سفارشات در انتظار تأیید
     * مرتب شده بر اساس تاریخ سفارش (قدیمی‌ترین اول برای پردازش)
     * 
     * @return لیست سفارشات PENDING
     */
    public List<Order> findPendingOrders() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "from Order where status = :pending order by orderDate asc", Order.class);
            q.setParameter("pending", OrderStatus.PENDING);
            return q.getResultList();
        }
    }

    /**
     * دریافت سفارشات فعال (غیر از تحویل شده و لغو شده)
     * مرتب شده بر اساس تاریخ سفارش (جدیدترین اول)
     * 
     * @return لیست سفارشات فعال
     */
    public List<Order> findActiveOrders() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery(
                    "from Order where status in (:statuses) order by orderDate desc", Order.class);
            q.setParameterList("statuses", List.of(
                OrderStatus.PENDING,         // در انتظار
                OrderStatus.CONFIRMED,       // تأیید شده
                OrderStatus.PREPARING,       // در حال آماده‌سازی
                OrderStatus.READY,           // آماده
                OrderStatus.OUT_FOR_DELIVERY // در حال تحویل
            ));
            return q.getResultList();
        }
    }

    /**
     * دریافت همه سفارشات
     * مرتب شده بر اساس تاریخ سفارش (جدیدترین اول)
     * 
     * @return لیست همه سفارشات
     */
    public List<Order> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Order> q = session.createQuery("from Order order by orderDate desc", Order.class);
            return q.getResultList();
        }
    }

    /**
     * به‌روزرسانی وضعیت سفارش
     * 
     * @param id شناسه سفارش
     * @param status وضعیت جدید
     */
    public void updateStatus(Long id, OrderStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Order order = session.get(Order.class, id);
            if (order != null) {
                order.setStatus(status);  // تنها در صورت وجود سفارش به‌روزرسانی می‌شود
            }
            tx.commit();
        }
    }

    /**
     * حذف سفارش با شناسه
     * 
     * @param id شناسه سفارش برای حذف
     */
    public void delete(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Order order = session.get(Order.class, id);
            if (order != null) {
                session.remove(order);  // حذف تنها در صورت وجود سفارش
            }
            tx.commit();
        }
    }

    /**
     * بررسی وجود سفارش با شناسه
     * 
     * @param id شناسه سفارش
     * @return true اگر سفارش وجود داشته باشد
     */
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

    /**
     * به‌روزرسانی سفارش موجود
     * 
     * @param order سفارش برای به‌روزرسانی
     * @return سفارش به‌روزرسانی شده
     */
    public Order update(Order order) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Order updated = (Order) session.merge(order);  // merge برای به‌روزرسانی
            tx.commit();
            return updated;
        }
    }

    /**
     * حذف همه سفارشات (متد کمکی برای تست‌ها)
     * این متد فقط در محیط تست استفاده می‌شود
     */
    public void deleteAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from Order").executeUpdate();
            tx.commit();
        }
    }
}
