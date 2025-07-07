package com.myapp.common;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

/**
 * کلاس پایه برای تمام تست‌های backend
 * 
 * این کلاس وظایف زیر را انجام می‌دهد:
 * - راه‌اندازی و پاک‌سازی دیتابیس قبل و بعد از هر تست
 * - فراهم‌کردن متدهای کمکی برای تست‌ها
 * - مدیریت session و transaction های Hibernate
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public abstract class TestBase {
    
    protected SessionFactory sessionFactory;
    protected Session session;
    protected Transaction transaction;
    
    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - ایجاد session factory
     * - باز کردن session جدید
     * - شروع transaction
     * - پاک‌سازی دیتابیس
     */
    @BeforeEach
    public void setUp() {
        try {
            // استفاده از پیکربندی اصلی
            Configuration config = new Configuration().configure("hibernate.cfg.xml");
            sessionFactory = config.buildSessionFactory();
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            
            // پاک‌سازی دیتابیس
            cleanDatabaseSafely();
            
        } catch (Exception e) {
            System.err.println("Error in setUp: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * تمیزکاری بعد از هر تست
     * 
     * Operations:
     * - rollback transaction
     * - بستن session
     * - بستن session factory
     */
    @AfterEach
    public void tearDown() {
        try {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            
            if (session != null && session.isOpen()) {
                session.close();
            }
            
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
            
        } catch (Exception e) {
            System.err.println("Error in tearDown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * پاک‌سازی امن دیتابیس
     * 
     * این متد تمام جداول دیتابیس را به ترتیب صحیح پاک‌سازی می‌کند
     * تا از نقض constraint های foreign key جلوگیری شود.
     * 
     * ترتیب پاک‌سازی:
     * 1. جداول وابسته (child tables)
     * 2. جداول اصلی (parent tables)
     */
    protected void cleanDatabaseSafely() {
        try {
            if (session == null || !session.isOpen()) {
                return;
            }
            
            // ترتیب پاک‌سازی بر اساس وابستگی‌ها
            String[] tablesToClean = {
                "coupon_usage",
                "notifications", 
                "ratings",
                "favorites",
                "order_items",
                "transactions",
                "orders",
                "food_items",
                "coupons",
                "restaurants",
                "users"
            };
            
            for (String tableName : tablesToClean) {
                try {
                    session.createNativeQuery("DELETE FROM " + tableName).executeUpdate();
                } catch (Exception e) {
                    // اگر جدول وجود نداشت، نادیده بگیر
                    System.out.println("Table " + tableName + " not found or already empty: " + e.getMessage());
                }
            }
            
            // commit تغییرات
            if (transaction != null && transaction.isActive()) {
                transaction.commit();
                transaction = session.beginTransaction();
            }
            
        } catch (Exception e) {
            System.err.println("Error in cleanDatabaseSafely: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * متد کمکی برای ذخیره entity در دیتابیس
     * 
     * @param entity entity برای ذخیره
     * @return entity ذخیره شده با ID
     */
    protected <T> T saveEntity(T entity) {
        if (session != null && session.isOpen()) {
            session.save(entity);
            session.flush();
            return entity;
        }
        return null;
    }
    
    /**
     * متد کمکی برای بازیابی entity از دیتابیس
     * 
     * @param entityClass کلاس entity
     * @param id شناسه entity
     * @return entity یا null اگر پیدا نشد
     */
    protected <T> T findEntity(Class<T> entityClass, Long id) {
        if (session != null && session.isOpen()) {
            return session.get(entityClass, id);
        }
        return null;
    }
    
    /**
     * متد کمکی برای بازیابی تمام entity های یک کلاس
     * 
     * @param entityClass کلاس entity
     * @return لیست تمام entity ها
     */
    protected <T> List<T> findAllEntities(Class<T> entityClass) {
        if (session != null && session.isOpen()) {
            return session.createQuery("FROM " + entityClass.getSimpleName(), entityClass).list();
        }
        return List.of();
    }
} 