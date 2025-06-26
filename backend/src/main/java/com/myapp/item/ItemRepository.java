package com.myapp.item;

import com.myapp.common.models.FoodItem;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository لایه دسترسی به داده‌های آیتم‌های غذایی
 * 
 * این کلاس مسئول تمام عملیات دیتابیس مربوط به آیتم‌های غذایی است:
 * 
 * === عملیات CRUD ===
 * - ایجاد آیتم جدید
 * - خواندن آیتم‌ها با معیارهای مختلف
 * - به‌روزرسانی آیتم‌های موجود
 * - حذف آیتم‌ها
 * 
 * === جستجو و فیلتر ===
 * - جستجو بر اساس کلیدواژه
 * - فیلتر بر اساس رستوران
 * - فیلتر بر اساس دسته‌بندی
 * - یافتن آیتم‌های در دسترس
 * - یافتن آیتم‌های کم موجودی
 * 
 * === مدیریت موجودی ===
 * - به‌روزرسانی وضعیت در دسترس بودن
 * - به‌روزرسانی مقدار موجودی
 * - یافتن آیتم‌های کم موجودی
 * 
 * === گزارش و آمار ===
 * - شمارش آیتم‌ها
 * - دسته‌بندی‌های رستوران
 * - آمار موجودی
 * 
 * ویژگی‌های کلیدی:
 * - HQL Queries: استفاده از Hibernate Query Language
 * - Transaction Management: مدیریت تراکنش‌ها
 * - Resource Management: مدیریت Session ها
 * - Query Optimization: بهینه‌سازی پرس و جوها
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class ItemRepository {

    public FoodItem saveNew(FoodItem foodItem) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(foodItem);   // ID will be generated
            tx.commit();
            return foodItem;
        }
    }

    public FoodItem save(FoodItem foodItem) {
        if (foodItem.getId() == null) {
            return saveNew(foodItem);
        } else {
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.merge(foodItem);
                tx.commit();
                return foodItem;
            }
        }
    }

    public Optional<FoodItem> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(FoodItem.class, id));
        }
    }

    public List<FoodItem> findByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery(
                    "from FoodItem where restaurant.id = :restaurantId", FoodItem.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getResultList();
        }
    }

    public List<FoodItem> findAvailableByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery(
                    "from FoodItem where restaurant.id = :restaurantId and available = true and quantity > 0", 
                    FoodItem.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getResultList();
        }
    }

    public List<FoodItem> findByCategory(String category) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery(
                    "from FoodItem where category = :category and available = true", FoodItem.class);
            q.setParameter("category", category);
            return q.getResultList();
        }
    }

    public List<FoodItem> searchByKeyword(String keyword) {
        // Return empty list if keyword is null or empty
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery(
                    "from FoodItem where (lower(name) like :keyword or lower(keywords) like :keyword) and available = true", 
                    FoodItem.class);
            q.setParameter("keyword", "%" + keyword.toLowerCase().trim() + "%");
            return q.getResultList();
        }
    }

    public List<FoodItem> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery("from FoodItem", FoodItem.class);
            return q.getResultList();
        }
    }

    public void delete(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            FoodItem foodItem = session.get(FoodItem.class, id);
            if (foodItem != null) {
                session.remove(foodItem);
            }
            tx.commit();
        }
    }

    public void updateAvailability(Long id, boolean available) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            FoodItem foodItem = session.get(FoodItem.class, id);
            if (foodItem != null) {
                foodItem.setAvailable(available);
            }
            tx.commit();
        }
    }

    public void updateQuantity(Long id, Integer quantity) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            FoodItem foodItem = session.get(FoodItem.class, id);
            if (foodItem != null) {
                foodItem.setQuantity(quantity);
            }
            tx.commit();
        }
    }

    // Utility method for cleaning database during tests
    public void deleteAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from FoodItem").executeUpdate();
            tx.commit();
        }
    }
    
    public boolean existsById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            FoodItem foodItem = session.get(FoodItem.class, id);
            return foodItem != null;
        }
    }
    
    public List<FoodItem> findByRestaurantAndCategory(Long restaurantId, String category) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery(
                    "from FoodItem where restaurant.id = :restaurantId and category = :category", 
                    FoodItem.class);
            q.setParameter("restaurantId", restaurantId);
            q.setParameter("category", category);
            return q.getResultList();
        }
    }
    
    public List<String> getCategoriesByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<String> q = session.createQuery(
                    "select distinct category from FoodItem where restaurant.id = :restaurantId", 
                    String.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getResultList();
        }
    }
    
    public List<FoodItem> findLowStockByRestaurant(Long restaurantId, int threshold) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery(
                    "from FoodItem where restaurant.id = :restaurantId and quantity <= :threshold", 
                    FoodItem.class);
            q.setParameter("restaurantId", restaurantId);
            q.setParameter("threshold", threshold);
            return q.getResultList();
        }
    }
    
    /**
     * Count total number of food items for a restaurant
     */
    public int countByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> q = session.createQuery(
                    "select count(*) from FoodItem where restaurant.id = :restaurantId", 
                    Long.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getSingleResult().intValue();
        }
    }
    
    /**
     * Count available food items for a restaurant
     */
    public int countAvailableByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> q = session.createQuery(
                    "select count(*) from FoodItem where restaurant.id = :restaurantId and available = true and quantity > 0", 
                    Long.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getSingleResult().intValue();
        }
    }
}
