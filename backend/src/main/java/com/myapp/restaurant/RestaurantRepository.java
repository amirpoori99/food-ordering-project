package com.myapp.restaurant;

import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository رستوران - لایه دسترسی به داده
 * این کلاس مسئول تمام عملیات دیتابیس مربوط به رستوران‌هاست
 * از الگوی Repository Pattern برای جداسازی منطق دیتابیس استفاده می‌کند
 */
public class RestaurantRepository {

    /**
     * ذخیره رستوران جدید در دیتابیس
     * شناسه به صورت خودکار توسط Hibernate تولید می‌شود
     * 
     * @param toPersist رستوران برای ذخیره
     * @return رستوران ذخیره شده همراه با شناسه تولید شده
     */
    public Restaurant saveNew(Restaurant toPersist) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(toPersist);   // شناسه به صورت خودکار تولید می‌شود
            tx.commit();
            return toPersist;
        }
    }

    /**
     * یافتن رستوران با شناسه
     * 
     * @param id شناسه رستوران
     * @return Optional حاوی رستوران یا خالی در صورت عدم وجود
     */
    public Optional<Restaurant> findById(long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Restaurant.class, id));
        }
    }

    /**
     * دریافت همه رستوران‌های متعلق به یک مالک
     * 
     * @param ownerId شناسه مالک
     * @return لیست رستوران‌های مالک
     */
    public List<Restaurant> listByOwner(long ownerId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Restaurant> q = session.createQuery(
                    "from Restaurant where ownerId = :o", Restaurant.class);
            q.setParameter("o", ownerId);
            return q.getResultList();
        }
    }

    /**
     * دریافت همه رستوران‌های تأیید شده
     * این متد برای نمایش عمومی رستوران‌ها استفاده می‌شود
     * 
     * @return لیست رستوران‌های با وضعیت APPROVED
     */
    public List<Restaurant> listApproved() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Restaurant> q = session.createQuery(
                    "from Restaurant where status = :s", Restaurant.class);
            q.setParameter("s", RestaurantStatus.APPROVED);
            return q.getResultList();
        }
    }

    /**
     * به‌روزرسانی وضعیت رستوران
     * 
     * @param id شناسه رستوران
     * @param status وضعیت جدید
     */
    public void updateStatus(long id, RestaurantStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Restaurant r = session.get(Restaurant.class, id);
            if (r != null) r.setStatus(status);  // تنها در صورت وجود رستوران به‌روزرسانی می‌شود
            tx.commit();
        }
    }

    /**
     * حذف همه رستوران‌ها (متد کمکی برای تست‌ها)
     * این متد فقط در محیط تست استفاده می‌شود
     */
    public void deleteAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from Restaurant").executeUpdate();
            tx.commit();
        }
    }

    /**
     * ذخیره یا به‌روزرسانی رستوران
     * اگر شناسه null باشد، رستوران جدید ایجاد می‌شود
     * در غیر اینصورت رستوران موجود به‌روزرسانی می‌شود
     * 
     * @param restaurant رستوران برای ذخیره/به‌روزرسانی
     * @return رستوران ذخیره شده
     */
    public Restaurant save(Restaurant restaurant) {
        if (restaurant.getId() == null) {
            return saveNew(restaurant);  // ایجاد رستوران جدید
        } else {
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.merge(restaurant);  // به‌روزرسانی رستوران موجود
                tx.commit();
                return restaurant;
            }
        }
    }

    /**
     * دریافت همه رستوران‌ها
     * 
     * @return لیست همه رستوران‌ها
     */
    public List<Restaurant> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Restaurant> q = session.createQuery("from Restaurant", Restaurant.class);
            return q.getResultList();
        }
    }

    /**
     * دریافت رستوران‌ها بر اساس وضعیت
     * 
     * @param status وضعیت مورد نظر
     * @return لیست رستوران‌ها با وضعیت مشخص
     */
    public List<Restaurant> findByStatus(RestaurantStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Restaurant> q = session.createQuery(
                    "from Restaurant where status = :s", Restaurant.class);
            q.setParameter("s", status);
            return q.getResultList();
        }
    }

    /**
     * حذف رستوران با شناسه
     * 
     * @param id شناسه رستوران برای حذف
     */
    public void delete(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, id);
            if (restaurant != null) {
                session.remove(restaurant);  // حذف تنها در صورت وجود رستوران
            }
            tx.commit();
        }
    }
    
    /**
     * بررسی وجود رستوران با شناسه
     * 
     * @param id شناسه رستوران
     * @return true اگر رستوران وجود داشته باشد
     */
    public boolean existsById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, id);
            return restaurant != null;
        }
    }
    
    /**
     * به‌روزرسانی رستوران موجود
     * 
     * @param restaurant رستوران برای به‌روزرسانی
     * @return رستوران به‌روزرسانی شده
     */
    public Restaurant update(Restaurant restaurant) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Restaurant updated = (Restaurant) session.merge(restaurant);  // merge برای به‌روزرسانی
            tx.commit();
            return updated;
        }
    }
}