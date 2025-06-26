package com.myapp.common.exceptions;

/**
 * استثنا برای یافت نشدن موجودیت (Entity)
 * این استثنا هنگام جستجو برای entity ای که وجود ندارد پرتاب می‌شود
 * از RuntimeException ارث‌بری می‌کند تا نیازی به try-catch اجباری نباشد
 */
public class NotFoundException extends RuntimeException {
    
    /**
     * سازنده استثنا با نام entity و شناسه آن
     * 
     * @param entity نام موجودیت (مثل "User", "Restaurant", "Order")
     * @param id شناسه موجودیت که یافت نشد
     */
    public NotFoundException(String entity, Object id) {
        super(entity + " not found with id=" + id); // پیام خطا شامل entity و id
    }
} 