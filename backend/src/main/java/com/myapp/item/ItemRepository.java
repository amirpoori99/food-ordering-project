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

    /**
     * ذخیره آیتم غذایی جدید در دیتابیس
     * 
     * این متد برای ایجاد آیتم‌های جدید استفاده می‌شود که ID آن‌ها توسط دیتابیس
     * به صورت خودکار تولید می‌شود.
     * 
     * @param foodItem آیتم غذایی جدید برای ذخیره
     * @return آیتم ذخیره شده همراه با ID تولید شده
     */
    public FoodItem saveNew(FoodItem foodItem) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(foodItem);   // شناسه به صورت خودکار تولید می‌شود
            tx.commit();
            return foodItem;
        }
    }

    /**
     * ذخیره یا به‌روزرسانی آیتم غذایی
     * 
     * این متد بسته به وجود یا عدم وجود ID، آیتم را ایجاد یا به‌روزرسانی می‌کند:
     * - اگر ID وجود نداشته باشد: آیتم جدید ایجاد می‌شود
     * - اگر ID موجود باشد: آیتم موجود به‌روزرسانی می‌شود
     * 
     * @param foodItem آیتم غذایی برای ذخیره یا به‌روزرسانی
     * @return آیتم ذخیره شده
     */
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

    /**
     * جستجوی آیتم غذایی بر اساس شناسه
     * 
     * @param id شناسه آیتم غذایی
     * @return Optional حاوی آیتم یافت شده یا خالی اگر وجود نداشته باشد
     */
    public Optional<FoodItem> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(FoodItem.class, id));
        }
    }

    /**
     * دریافت تمام آیتم‌های غذایی یک رستوران
     * 
     * این متد تمام آیتم‌های یک رستوران را برمی‌گرداند بدون در نظر گیری
     * وضعیت available یا موجودی آن‌ها.
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست تمام آیتم‌های رستوران
     */
    public List<FoodItem> findByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery(
                    "from FoodItem where restaurant.id = :restaurantId", FoodItem.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getResultList();
        }
    }

    /**
     * دریافت آیتم‌های در دسترس یک رستوران
     * 
     * این متد فقط آیتم‌هایی را برمی‌گرداند که:
     * - متعلق به رستوران مشخص شده باشند
     * - وضعیت available آن‌ها true باشد
     * - موجودی آن‌ها بیشتر از صفر باشد
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست آیتم‌های قابل سفارش رستوران
     */
    public List<FoodItem> findAvailableByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery(
                    "from FoodItem where restaurant.id = :restaurantId and available = true and quantity > 0", 
                    FoodItem.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getResultList();
        }
    }

    /**
     * جستجوی آیتم‌ها بر اساس دسته‌بندی
     * 
     * این متد فقط آیتم‌های در دسترس (available = true) را برمی‌گرداند
     * که در دسته‌بندی مشخص شده قرار دارند.
     * 
     * @param category نام دسته‌بندی مورد نظر
     * @return لیست آیتم‌های در دسترس در آن دسته‌بندی
     */
    public List<FoodItem> findByCategory(String category) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery(
                    "from FoodItem where category = :category and available = true", FoodItem.class);
            q.setParameter("category", category);
            return q.getResultList();
        }
    }

    /**
     * جستجوی آیتم‌ها بر اساس کلیدواژه
     * 
     * این متد در نام و کلیدواژه‌های آیتم‌ها جستجو می‌کند:
     * - جستجو case-insensitive است
     * - هم در نام و هم در فیلد keywords جستجو می‌شود
     * - فقط آیتم‌های در دسترس (available = true) برگردانده می‌شوند
     * - اگر کلیدواژه خالی باشد، لیست خالی برگردانده می‌شود
     * 
     * @param keyword کلیدواژه جستجو
     * @return لیست آیتم‌های یافت شده
     */
    public List<FoodItem> searchByKeyword(String keyword) {
        // برگرداندن لیست خالی اگر کلیدواژه null یا خالی باشد
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

    /**
     * دریافت تمام آیتم‌های غذایی موجود در سیستم
     * 
     * این متد بدون هیچ فیلتری تمام آیتم‌ها را برمی‌گرداند.
     * معمولاً برای مدیریت سیستم و گزارش‌گیری استفاده می‌شود.
     * 
     * @return لیست تمام آیتم‌های غذایی
     */
    public List<FoodItem> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<FoodItem> q = session.createQuery("from FoodItem", FoodItem.class);
            return q.getResultList();
        }
    }

    /**
     * حذف آیتم غذایی بر اساس شناسه
     * 
     * این متد آیتم را از دیتابیس حذف می‌کند. اگر آیتم با شناسه مشخص شده
     * وجود نداشته باشد، هیچ عملی انجام نمی‌شود.
     * 
     * @param id شناسه آیتم برای حذف
     */
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

    /**
     * به‌روزرسانی وضعیت در دسترس بودن آیتم
     * 
     * این متد وضعیت available یک آیتم را تغییر می‌دهد.
     * اگر آیتم با شناسه مشخص شده وجود نداشته باشد، هیچ عملی انجام نمی‌شود.
     * 
     * @param id شناسه آیتم
     * @param available وضعیت جدید در دسترس بودن
     */
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

    /**
     * به‌روزرسانی موجودی آیتم
     * 
     * این متد مقدار موجودی یک آیتم را تغییر می‌دهد.
     * اگر آیتم با شناسه مشخص شده وجود نداشته باشد، هیچ عملی انجام نمی‌شود.
     * 
     * @param id شناسه آیتم
     * @param quantity مقدار جدید موجودی
     */
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

    /**
     * حذف تمام آیتم‌های غذایی از دیتابیس
     * 
     * این متد utility برای پاک‌سازی دیتابیس در طول تست‌ها استفاده می‌شود.
     * تمام آیتم‌ها را بدون در نظر گیری رستوران یا سایر شرایط حذف می‌کند.
     * 
     * ⚠️ توجه: این متد تمام داده‌ها را پاک می‌کند
     */
    public void deleteAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from FoodItem").executeUpdate();
            tx.commit();
        }
    }
    
    /**
     * بررسی وجود آیتم بر اساس شناسه
     * 
     * این متد چک می‌کند که آیا آیتمی با شناسه مشخص شده در دیتابیس وجود دارد یا خیر.
     * 
     * @param id شناسه آیتم برای بررسی
     * @return true اگر آیتم وجود داشته باشد، در غیر این صورت false
     */
    public boolean existsById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            FoodItem foodItem = session.get(FoodItem.class, id);
            return foodItem != null;
        }
    }
    
    /**
     * جستجوی آیتم‌ها بر اساس رستوران و دسته‌بندی
     * 
     * این متد آیتم‌هایی را برمی‌گرداند که هم متعلق به رستوران مشخص شده
     * و هم در دسته‌بندی مورد نظر قرار دارند.
     * 
     * @param restaurantId شناسه رستوران
     * @param category نام دسته‌بندی
     * @return لیست آیتم‌های یافت شده
     */
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
    
    /**
     * دریافت لیست دسته‌بندی‌های موجود در رستوران
     * 
     * این متد تمام دسته‌بندی‌های منحصر به فردی که در رستوران مشخص شده
     * استفاده می‌شوند را برمی‌گرداند.
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست نام دسته‌بندی‌های منحصر به فرد
     */
    public List<String> getCategoriesByRestaurant(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<String> q = session.createQuery(
                    "select distinct category from FoodItem where restaurant.id = :restaurantId", 
                    String.class);
            q.setParameter("restaurantId", restaurantId);
            return q.getResultList();
        }
    }
    
    /**
     * جستجوی آیتم‌های کم موجودی در رستوران
     * 
     * این متد آیتم‌هایی را برمی‌گرداند که موجودی آن‌ها کمتر یا مساوی
     * حد آستانه مشخص شده باشد.
     * 
     * @param restaurantId شناسه رستوران
     * @param threshold حد آستانه موجودی
     * @return لیست آیتم‌های کم موجودی
     */
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
     * شمارش کل آیتم‌های غذایی رستوران
     * 
     * این متد تعداد کل آیتم‌های یک رستوران را بدون در نظر گیری
     * وضعیت availability یا موجودی برمی‌گرداند.
     * 
     * @param restaurantId شناسه رستوران
     * @return تعداد کل آیتم‌های رستوران
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
     * شمارش آیتم‌های در دسترس رستوران
     * 
     * این متد تعداد آیتم‌هایی را برمی‌گرداند که:
     * - متعلق به رستوران مشخص شده باشند
     * - وضعیت available آن‌ها true باشد
     * - موجودی آن‌ها بیشتر از صفر باشد
     * 
     * @param restaurantId شناسه رستوران
     * @return تعداد آیتم‌های قابل سفارش رستوران
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
