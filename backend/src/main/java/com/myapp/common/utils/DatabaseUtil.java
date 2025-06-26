package com.myapp.common.utils;

// وارد کردن کلاس‌های Hibernate برای مدیریت دیتابیس
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * کلاس ابزاری دیتابیس - مدیریت اتصال به پایگاه داده با Hibernate
 * این کلاس مسئول ایجاد و مدیریت SessionFactory برای کل برنامه است
 * از الگوی Singleton Pattern برای یک نمونه SessionFactory استفاده می‌کند
 */
public class DatabaseUtil {
    // نمونه یکتای SessionFactory برای کل برنامه
    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * ساخت SessionFactory با استفاده از تنظیمات hibernate.cfg.xml
     * این متد در ابتدای اجرای برنامه یک بار فراخوانی می‌شود
     * 
     * @return SessionFactory تنظیم شده برای اتصال به SQLite
     * @throws ExceptionInInitializerError در صورت خطا در ایجاد SessionFactory
     */
    private static SessionFactory buildSessionFactory() {
        try {
            // استفاده از تنظیمات SQLite از فایل hibernate.cfg.xml
            StandardServiceRegistryBuilder registryBuilder =
                    new StandardServiceRegistryBuilder().configure(); // خواندن hibernate.cfg.xml
    
            // ایجاد service registry برای Hibernate
            StandardServiceRegistry registry = registryBuilder.build();
            
            // ایجاد metadata از منابع موجود (entity classes)
            Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
            
            // ساخت و برگرداندن SessionFactory
            return metadata.getSessionFactoryBuilder().build();
    
        } catch (Exception e) {
            // در صورت خطا، پرتاب ExceptionInInitializerError
            throw new ExceptionInInitializerError(
                    "Initial SessionFactory failed " + e.getMessage());
        }
    }

    /**
     * دریافت نمونه SessionFactory برای استفاده در سایر کلاس‌ها
     * این متد برای باز کردن Session و اجرای عملیات دیتابیس استفاده می‌شود
     * 
     * @return SessionFactory یکتای برنامه
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * بستن SessionFactory هنگام خاتمه برنامه
     * این متد برای آزادسازی منابع و بستن اتصالات دیتابیس استفاده می‌شود
     * معمولاً در shutdown hook یا در پایان برنامه فراخوانی می‌شود
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close(); // بستن SessionFactory و آزادسازی منابع
        }
    }
}

