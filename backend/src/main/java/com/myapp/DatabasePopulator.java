package com.myapp;

import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Arrays;
import java.util.List;

/**
 * کلاس پر کردن دیتابیس با داده‌های نمونه
 * این کلاس برای تست و نمایش سیستم استفاده می‌شود
 */
public class DatabasePopulator {
    
    public static void main(String[] args) {
        DatabasePopulator populator = new DatabasePopulator();
        populator.populateDatabase();
    }
    
    public void populateDatabase() {
        System.out.println("🔄 در حال پر کردن دیتابیس با داده‌های نمونه...");
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            try {
                // ایجاد کاربران نمونه
                createSampleUsers(session);
                
                // ایجاد رستوران‌های نمونه
                createSampleRestaurants(session);
                
                // ایجاد غذاهای نمونه
                createSampleFoodItems(session);
                
                transaction.commit();
                System.out.println("✅ دیتابیس با موفقیت پر شد!");
                
            } catch (Exception e) {
                transaction.rollback();
                System.err.println("❌ خطا در پر کردن دیتابیس: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void createSampleUsers(Session session) {
        System.out.println("👥 در حال ایجاد کاربران نمونه...");
        
        // بررسی تعداد کاربران موجود
        Long userCount = (Long) session.createQuery("SELECT COUNT(u) FROM User u").uniqueResult();
        if (userCount > 0) {
            System.out.println("⚠️ کاربران قبلاً وجود دارند. رد کردن ایجاد کاربران.");
            return;
        }
        
        List<User> users = Arrays.asList(
            new User("علی احمدی", "09120000001", "ali@example.com", "password123", User.Role.BUYER, "تهران، خیابان ولیعصر"),
            new User("فاطمه محمدی", "09120000002", "fateme@example.com", "password123", User.Role.BUYER, "تهران، خیابان انقلاب"),
            new User("محمد رضایی", "09120000003", "mohammad@example.com", "password123", User.Role.BUYER, "تهران، خیابان آزادی"),
            new User("زهرا کریمی", "09120000004", "zahra@example.com", "password123", User.Role.BUYER, "تهران، خیابان شریعتی"),
            new User("احمد نوری", "09120000005", "ahmad@example.com", "password123", User.Role.SELLER, "تهران، خیابان سعادت آباد"),
            new User("مریم صادقی", "09120000006", "maryam@example.com", "password123", User.Role.SELLER, "تهران، خیابان ونک"),
            new User("حسن رحیمی", "09120000007", "hasan@example.com", "password123", User.Role.ADMIN, "تهران، خیابان پاسداران")
        );
        
        for (User user : users) {
            session.persist(user);
            System.out.println("✅ کاربر ایجاد شد: " + user.getFullName() + " (" + user.getPhone() + ")");
        }
    }
    
    private void createSampleRestaurants(Session session) {
        System.out.println("🏪 در حال ایجاد رستوران‌های نمونه...");
        
        // بررسی تعداد رستوران‌های موجود
        Long restaurantCount = (Long) session.createQuery("SELECT COUNT(r) FROM Restaurant r").uniqueResult();
        if (restaurantCount > 0) {
            System.out.println("⚠️ رستوران‌ها قبلاً وجود دارند. رد کردن ایجاد رستوران‌ها.");
            return;
        }
        
        // دریافت کاربران vendor
        List<User> vendors = session.createQuery("FROM User u WHERE u.role = :role", User.class)
                                   .setParameter("role", User.Role.SELLER)
                                   .list();
        
        if (vendors.isEmpty()) {
            System.out.println("⚠️ هیچ کاربر vendor یافت نشد. رد کردن ایجاد رستوران‌ها.");
            return;
        }
        
        List<Restaurant> restaurants = Arrays.asList(
            new Restaurant(vendors.get(0).getId(), "رستوران سنتی ایران", "تهران، خیابان ولیعصر، پلاک 123", "021-12345678"),
            new Restaurant(vendors.get(1).getId(), "فست فود برگر هاوس", "تهران، خیابان انقلاب، پلاک 456", "021-87654321")
        );
        
        for (Restaurant restaurant : restaurants) {
            restaurant.setStatus(RestaurantStatus.APPROVED);
            session.persist(restaurant);
            System.out.println("✅ رستوران ایجاد شد: " + restaurant.getName() + " (" + restaurant.getPhone() + ")");
        }
    }
    
    private void createSampleFoodItems(Session session) {
        System.out.println("🍽️ در حال ایجاد غذاهای نمونه...");
        
        // بررسی تعداد غذاهای موجود
        Long foodCount = (Long) session.createQuery("SELECT COUNT(f) FROM FoodItem f").uniqueResult();
        if (foodCount > 0) {
            System.out.println("⚠️ غذاها قبلاً وجود دارند. رد کردن ایجاد غذاها.");
            return;
        }
        
        // دریافت رستوران‌ها
        List<Restaurant> restaurants = session.createQuery("FROM Restaurant", Restaurant.class).list();
        
        if (restaurants.isEmpty()) {
            System.out.println("⚠️ هیچ رستورانی یافت نشد. رد کردن ایجاد غذاها.");
            return;
        }
        
        // غذاهای رستوران سنتی
        List<FoodItem> traditionalFoods = Arrays.asList(
            new FoodItem("کباب کوبیده", "کباب سنتی ایرانی با گوشت گوسفند", 85000.0, "کباب", null, 50, "کباب", restaurants.get(0)),
            new FoodItem("چلو ماهی", "ماهی سرخ شده با برنج ایرانی", 95000.0, "غذای دریایی", null, 30, "ماهی", restaurants.get(0)),
            new FoodItem("دلمه برگ مو", "دلمه سنتی با گوشت و برنج", 65000.0, "خوراک", null, 25, "دلمه", restaurants.get(0)),
            new FoodItem("آبگوشت", "آبگوشت سنتی با گوشت و حبوبات", 75000.0, "سوپ", null, 40, "آبگوشت", restaurants.get(0))
        );
        
        // غذاهای فست فود
        List<FoodItem> fastFoods = Arrays.asList(
            new FoodItem("برگر کلاسیک", "برگر گوشت با نان و سبزیجات", 45000.0, "برگر", null, 100, "برگر", restaurants.get(1)),
            new FoodItem("پیتزا مارگاریتا", "پیتزا با پنیر و سس گوجه", 55000.0, "پیتزا", null, 80, "پیتزا", restaurants.get(1)),
            new FoodItem("سیب زمینی سرخ شده", "سیب زمینی سرخ شده ترد", 25000.0, "ساید", null, 150, "سیب زمینی", restaurants.get(1)),
            new FoodItem("نوشابه", "نوشابه گازدار", 15000.0, "نوشیدنی", null, 200, "نوشابه", restaurants.get(1))
        );
        
        // ذخیره غذاهای سنتی
        for (FoodItem food : traditionalFoods) {
            session.persist(food);
            System.out.println("✅ غذای سنتی ایجاد شد: " + food.getName() + " - " + food.getPrice() + " تومان");
        }
        
        // ذخیره غذاهای فست فود
        for (FoodItem food : fastFoods) {
            session.persist(food);
            System.out.println("✅ غذای فست فود ایجاد شد: " + food.getName() + " - " + food.getPrice() + " تومان");
        }
    }
} 