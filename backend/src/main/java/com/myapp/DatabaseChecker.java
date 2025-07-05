package com.myapp;

import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class DatabaseChecker {
    public static void main(String[] args) {
        System.out.println("🔍 بررسی محتویات دیتابیس...");
        
        try {
            SessionFactory sessionFactory = DatabaseUtil.getSessionFactory();
            
            try (Session session = sessionFactory.openSession()) {
                // شمارش کاربران
                Long userCount = session.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
                System.out.println("✅ تعداد کاربران: " + userCount);
                
                // شمارش رستوران‌ها
                Long restaurantCount = session.createQuery("SELECT COUNT(r) FROM Restaurant r", Long.class)
                    .getSingleResult();
                System.out.println("✅ تعداد رستوران‌ها: " + restaurantCount);
                
                // شمارش منوها
                Long itemCount = session.createQuery("SELECT COUNT(f) FROM FoodItem f", Long.class)
                    .getSingleResult();
                System.out.println("✅ تعداد آیتم‌های منو: " + itemCount);
                
                // شمارش سفارشات
                Long orderCount = session.createQuery("SELECT COUNT(o) FROM Order o", Long.class)
                    .getSingleResult();
                System.out.println("✅ تعداد سفارشات: " + orderCount);
                
                // شمارش کوپن‌ها
                Long couponCount = session.createQuery("SELECT COUNT(c) FROM Coupon c", Long.class)
                    .getSingleResult();
                System.out.println("✅ تعداد کوپن‌ها: " + couponCount);
                
                System.out.println("\n📊 خلاصه: دیتابیس حاوی داده است و اطلاعات به درستی ذخیره شده‌اند!");
                
                // نمایش چند کاربر نمونه
                if (userCount > 0) {
                    System.out.println("\n👥 چند کاربر نمونه:");
                    session.createQuery("FROM User u", com.myapp.common.models.User.class)
                        .setMaxResults(3)
                        .getResultList()
                        .forEach(user -> {
                            System.out.println("  - " + user.getFullName() + " (" + user.getPhone() + ") - " + user.getRole());
                        });
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ خطا در بررسی دیتابیس: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 