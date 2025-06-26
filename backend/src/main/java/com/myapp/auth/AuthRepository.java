package com.myapp.auth;

import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * مخزن داده‌های احراز هویت - لایه دسترسی به داده برای عملیات کاربران
 * این کلاس تمام عملیات CRUD مربوط به کاربران را مدیریت می‌کند
 * از الگوی Repository Pattern استفاده می‌کند
 */
public class AuthRepository {

    /**
     * ذخیره کاربر جدید در دیتابیس
     * بررسی تکراری نبودن شماره تلفن قبل از ذخیره
     * 
     * @param user کاربر جدید برای ذخیره
     * @return کاربر ذخیره شده همراه با ID تولید شده
     * @throws DuplicatePhoneException در صورت تکراری بودن شماره تلفن
     */
    public User saveNew(User user) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            // بررسی تکراری بودن شماره تلفن
            Query<User> q = session.createQuery("from User where phone = :p", User.class);
            q.setParameter("p", user.getPhone());
            if (!q.getResultList().isEmpty()) {
                throw new DuplicatePhoneException(user.getPhone());
            }
            
            // ذخیره کاربر جدید
            Transaction tx = session.beginTransaction();
            session.persist(user);           // ذخیره در دیتابیس
            tx.commit();                     // تأیید تراکنش
            return user;                     // بازگشت کاربر با ID تولید شده
        }
    }

    /**
     * یافتن کاربر بر اساس شناسه
     * 
     * @param id شناسه کاربر
     * @return Optional حاوی کاربر یا empty در صورت عدم وجود
     */
    public Optional<User> findById(long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        }
    }

    /**
     * یافتن کاربر بر اساس شماره تلفن
     * این متد برای فرآیند ورود استفاده می‌شود
     * 
     * @param phone شماره تلفن کاربر
     * @return Optional حاوی کاربر یا empty در صورت عدم وجود
     */
    public Optional<User> findByPhone(String phone) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<User> q = session.createQuery("from User where phone = :p", User.class);
            q.setParameter("p", phone);
            return q.uniqueResultOptional();  // برگرداندن یک نتیجه یکتا یا empty
        }
    }

    /**
     * به‌روزرسانی اطلاعات کاربر
     * مدیریت تعارض‌های ممکن در شماره تلفن
     * 
     * @param updated کاربر با اطلاعات به‌روز شده
     * @return کاربر به‌روز شده
     * @throws DuplicatePhoneException در صورت تعارض شماره تلفن
     */
    public User update(User updated) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(updated);          // به‌روزرسانی در دیتابیس
            tx.commit();                     // تأیید تراکنش
            return updated;
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            // تبدیل خطای constraint violation به استثنای سفارشی
            if (e.getConstraintName() != null && e.getConstraintName().toLowerCase().contains("phone")) {
                throw new DuplicatePhoneException(updated.getPhone());
            }
            throw e;  // در غیر اینصورت، خطای اصلی را پرتاب کن
        } catch (org.hibernate.exception.GenericJDBCException e) {
            // مدیریت خطاهای unique constraint در SQLite
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique constraint failed: users.phone")) {
                throw new DuplicatePhoneException(updated.getPhone());
            }
            throw e;  // در غیر اینصورت، خطای اصلی را پرتاب کن
        }
    }

    /**
     * حذف کاربر بر اساس شناسه
     * 
     * @param id شناسه کاربر برای حذف
     */
    public void delete(long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User u = session.get(User.class, id);  // یافتن کاربر
            if (u != null) session.remove(u);      // حذف در صورت وجود
            tx.commit();                            // تأیید تراکنش
        }
    }

    /**
     * بررسی وجود کاربر بر اساس شناسه
     * این متد برای validation استفاده می‌شود
     * 
     * @param id شناسه کاربر
     * @return true اگر کاربر وجود داشته باشد، در غیر اینصورت false
     */
    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            return false;  // شناسه نامعتبر
        }
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.id = :id", Long.class);
            query.setParameter("id", id);
            Long count = query.uniqueResult();
            return count != null && count > 0;  // بررسی وجود کاربر
        }
    }

    /**
     * یافتن شناسه تمام کاربران فعال
     * این متد برای عملیات bulk استفاده می‌شود
     * 
     * @return لیست شناسه‌های تمام کاربران فعال
     */
    public List<Long> findAllActiveUserIds() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT u.id FROM User u", Long.class);
            return query.getResultList();
        }
    }

    /**
     * حذف تمام کاربران - متد کمکی برای تست‌ها
     * ⚠️ هشدار: این متد فقط برای محیط تست استفاده شود
     */
    public void deleteAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from User").executeUpdate();  // حذف تمام کاربران
            tx.commit();                                               // تأیید تراکنش
        }
    }
}
