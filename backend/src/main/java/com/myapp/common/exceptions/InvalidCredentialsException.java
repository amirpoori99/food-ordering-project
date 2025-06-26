package com.myapp.common.exceptions;

/**
 * استثنا برای اطلاعات ورود نامعتبر
 * این استثنا هنگام ورود با شماره تلفن یا رمز عبور اشتباه پرتاب می‌شود
 * از RuntimeException ارث‌بری می‌کند تا نیازی به try-catch اجباری نباشد
 */
public class InvalidCredentialsException extends RuntimeException {
    
    /**
     * سازنده استثنا با پیام خطای ثابت
     * از روی امنیت، جزئیات دقیق خطا را نمایش نمی‌دهد
     */
    public InvalidCredentialsException() {
        super("Invalid phone or password"); // پیام عمومی برای امنیت
    }
} 