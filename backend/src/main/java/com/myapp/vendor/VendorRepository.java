package com.myapp.vendor;

import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Repository لایه دسترسی داده برای عملیات فروشندگان (Vendor Operations)
 * 
 * این کلاس تمام عملیات پایگاه داده مربوط به فروشندگان از دیدگاه مشتری را ارائه می‌دهد:
 * 
 * === جستجو و مرور فروشندگان ===
 * - findById(): یافتن فروشنده با شناسه
 * - searchVendors(): جستجو در نام و موقعیت فروشندگان
 * - findByLocation(): یافتن فروشندگان در منطقه خاص
 * - getFeaturedVendors(): فروشندگان برجسته/محبوب
 * 
 * === فیلتر و دسته‌بندی ===
 * - findByFoodCategory(): فروشندگان بر اساس دسته غذا
 * - findByFilters(): جستجوی پیشرفته با چندین فیلتر
 * - getVendorsWithItemCounts(): فروشندگان همراه تعداد آیتم‌های منو
 * 
 * === ویژگی‌های کلیدی ===
 * - Customer-Focused Queries: queries مختص دیدگاه مشتری
 * - Status Filtering: فقط فروشندگان تایید شده
 * - Search Optimization: جستجوی بهینه شده
 * - Category-Based Discovery: کشف بر اساس دسته غذا
 * - Location-Based Search: جستجوی موقعیت‌محور
 * - Featured Content: محتوای برجسته
 * - Multi-Filter Support: پشتیبانی از چندین فیلتر همزمان
 * - Performance Optimization: بهینه‌سازی کارایی
 * 
 * === Inner Classes ===
 * - VendorWithItemCount: کلاس داده فروشنده با تعداد آیتم‌ها
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class VendorRepository {
    
    /**
     * یافتن فروشنده با شناسه
     * 
     * @param id شناسه فروشنده
     * @return Optional حاوی فروشنده یا خالی
     */
    public Optional<Restaurant> findById(long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Restaurant.class, id));
        } catch (Exception e) {
            System.err.println("Error finding vendor by ID: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * جستجوی فروشندگان بر اساس نام یا موقعیت
     * 
     * این متد در نام رستوران و آدرس آن جستجو می‌کند
     * فقط رستوران‌های تایید شده را برمی‌گرداند
     * 
     * @param searchTerm عبارت جستجو
     * @return لیست فروشندگان یافت شده
     */
    public List<Restaurant> searchVendors(String searchTerm) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Restaurant r WHERE r.status = :status AND " +
                        "(LOWER(r.name) LIKE LOWER(:searchTerm) OR " +
                        "LOWER(r.address) LIKE LOWER(:searchTerm))";
            
            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error searching vendors: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * یافتن فروشندگان در منطقه/موقعیت مشخص
     * 
     * جستجو در آدرس رستوران‌ها برای یافتن فروشندگان در منطقه خاص
     * 
     * @param location نام منطقه یا موقعیت
     * @return لیست فروشندگان در آن منطقه
     */
    public List<Restaurant> findByLocation(String location) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Restaurant r WHERE r.status = :status AND " +
                        "LOWER(r.address) LIKE LOWER(:location)";
            
            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            query.setParameter("location", "%" + location + "%");
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error finding vendors by location: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * دریافت فروشندگان برجسته/محبوب
     * 
     * در حال حاضر، فروشندگان تایید شده را بر اساس تاریخ ثبت (جدیدترین اول) برمی‌گرداند
     * در آینده، می‌تواند بر اساس امتیاز، تعداد سفارش و غیره باشد
     * 
     * @return لیست فروشندگان برجسته (حداکثر 10 مورد)
     */
    public List<Restaurant> getFeaturedVendors() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Restaurant r WHERE r.status = :status " +
                        "ORDER BY r.id DESC";
            
            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            query.setMaxResults(10); // محدود به 10 مورد برتر
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting featured vendors: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * یافتن فروشندگانی که دسته غذای مشخصی ارائه می‌دهند
     * 
     * از join با جدول FoodItem استفاده می‌کند تا فروشندگانی که
     * حداقل یک آیتم در دسته مورد نظر دارند را پیدا کند
     * 
     * @param category دسته غذایی مورد نظر
     * @return لیست فروشندگان ارائه‌دهنده آن دسته غذا
     */
    public List<Restaurant> findByFoodCategory(String category) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT r FROM Restaurant r " +
                        "JOIN FoodItem f ON f.restaurant.id = r.id " +
                        "WHERE r.status = :status AND " +
                        "LOWER(f.category) = LOWER(:category) AND " +
                        "f.available = true";
            
            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            query.setParameter("category", category);
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error finding vendors by category: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * دریافت فروشندگان همراه تعداد آیتم‌های منوی آنها
     * 
     * query آماری که تعداد آیتم‌های موجود هر فروشنده را محاسبه می‌کند
     * بر اساس تعداد آیتم‌ها مرتب می‌شود (بیشترین اول)
     * 
     * @return لیست فروشندگان با تعداد آیتم‌هایشان
     */
    public List<VendorWithItemCount> getVendorsWithItemCounts() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT r.id, r.name, r.address, " +
                        "COUNT(f.id) as itemCount " +
                        "FROM Restaurant r " +
                        "LEFT JOIN FoodItem f ON f.restaurant.id = r.id AND f.available = true " +
                        "WHERE r.status = :status " +
                        "GROUP BY r.id, r.name, r.address " +
                        "ORDER BY itemCount DESC";
            
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            
            List<Object[]> results = query.getResultList();
            List<VendorWithItemCount> vendors = new ArrayList<>();
            
            // تبدیل نتایج خام به آبجکت‌های structured
            for (Object[] row : results) {
                Long id = (Long) row[0];
                String name = (String) row[1];
                String address = (String) row[2];
                Long itemCount = (Long) row[3];
                
                vendors.add(new VendorWithItemCount(id, name, address, null, itemCount.intValue()));
            }
            
            return vendors;
        } catch (Exception e) {
            System.err.println("Error getting vendors with item counts: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * جستجوی فروشندگان با چندین فیلتر همزمان
     * 
     * این متد امکان فیلتر کردن فروشندگان بر اساس ترکیبی از:
     * - موقعیت جغرافیایی
     * - دسته غذایی
     * - عبارت جستجو در نام
     * 
     * query به صورت dynamic ساخته می‌شود تا فقط فیلترهای ارائه شده اعمال شوند
     * 
     * @param location موقعیت جغرافیایی (اختیاری)
     * @param category دسته غذایی (اختیاری)  
     * @param searchTerm عبارت جستجو در نام (اختیاری)
     * @return لیست فروشندگان فیلتر شده
     */
    public List<Restaurant> findByFilters(String location, String category, String searchTerm) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hqlBuilder = new StringBuilder();
            hqlBuilder.append("SELECT DISTINCT r FROM Restaurant r ");
            
            // اگر فیلتر دسته‌بندی مشخص شده، join با FoodItem ضروری است
            if (category != null && !category.trim().isEmpty()) {
                hqlBuilder.append("JOIN FoodItem f ON f.restaurant.id = r.id ");
            }
            
            hqlBuilder.append("WHERE r.status = :status ");
            
            // اضافه کردن شرط موقعیت
            if (location != null && !location.trim().isEmpty()) {
                hqlBuilder.append("AND LOWER(r.address) LIKE LOWER(:location) ");
            }
            
            // اضافه کردن شرط دسته‌بندی
            if (category != null && !category.trim().isEmpty()) {
                hqlBuilder.append("AND LOWER(f.category) = LOWER(:category) AND f.available = true ");
            }
            
            // اضافه کردن شرط جستجوی نام
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hqlBuilder.append("AND LOWER(r.name) LIKE LOWER(:searchTerm) ");
            }
            
            Query<Restaurant> query = session.createQuery(hqlBuilder.toString(), Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            
            // تنظیم پارامترهای مشروط
            if (location != null && !location.trim().isEmpty()) {
                query.setParameter("location", "%" + location + "%");
            }
            
            if (category != null && !category.trim().isEmpty()) {
                query.setParameter("category", category);
            }
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                query.setParameter("searchTerm", "%" + searchTerm + "%");
            }
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error finding vendors by filters: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * کلاس داده برای فروشنده همراه تعداد آیتم‌ها
     * 
     * این کلاس اطلاعات اساسی فروشنده به همراه تعداد آیتم‌های منوی او را نگه می‌دارد
     * برای نمایش آمار و مقایسه فروشندگان مفید است
     */
    public static class VendorWithItemCount {
        /** شناسه فروشنده */
        private final Long id;
        /** نام فروشنده */
        private final String name;
        /** آدرس فروشنده */
        private final String address;
        /** توضیحات فروشنده (اختیاری) */
        private final String description;
        /** تعداد آیتم‌های موجود در منو */
        private final int itemCount;
        
        /**
         * سازنده کلاس VendorWithItemCount
         * 
         * @param id شناسه فروشنده
         * @param name نام فروشنده
         * @param address آدرس فروشنده
         * @param description توضیحات فروشنده
         * @param itemCount تعداد آیتم‌های منو
         */
        public VendorWithItemCount(Long id, String name, String address, String description, int itemCount) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.description = description;
            this.itemCount = itemCount;
        }
        
        // Getters با کامنت فارسی
        /** @return شناسه فروشنده */
        public Long getId() { return id; }
        /** @return نام فروشنده */
        public String getName() { return name; }
        /** @return آدرس فروشنده */
        public String getAddress() { return address; }
        /** @return توضیحات فروشنده */
        public String getDescription() { return description; }
        /** @return تعداد آیتم‌های منو */
        public int getItemCount() { return itemCount; }
    }
}
