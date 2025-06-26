package com.myapp.common.exceptions;

/**
 * استثنا برای تکراری بودن شماره تلفن
 * این استثنا هنگام ثبت نام یا به‌روزرسانی کاربر با شماره تلفن تکراری پرتاب می‌شود
 * از RuntimeException ارث‌بری می‌کند تا نیازی به try-catch اجباری نباشد
 */
public class DuplicatePhoneException extends RuntimeException {
    
    /**
     * سازنده استثنا با شماره تلفن تکراری
     * 
     * @param phone شماره تلفن تکراری که باعث ایجاد خطا شده
     */
    public DuplicatePhoneException(String phone) {
        super("Phone number already exists: " + phone); // پیام خطا شامل شماره تلفن
    }
} 