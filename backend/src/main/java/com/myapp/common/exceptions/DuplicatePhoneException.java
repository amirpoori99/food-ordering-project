package com.myapp.common.exceptions;

/**
 * استثنا برای تکراری بودن شماره تلفن
 * این استثنا هنگام ثبت نام یا به‌روزرسانی کاربر با شماره تلفن تکراری پرتاب می‌شود
 * از RuntimeException ارث‌بری می‌کند تا نیازی به try-catch اجباری نباشد
 * 
 * مثال استفاده:
 * <pre>
 * if (userRepository.existsByPhone(phoneNumber)) {
 *     throw new DuplicatePhoneException(phoneNumber);
 * }
 * </pre>
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class DuplicatePhoneException extends RuntimeException {
    
    /**
     * شناسه منحصر به فرد برای serialization
     * این مقدار برای حفظ سازگاری در زمان serialize/deserialize استفاده می‌شود
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * سازنده استثنا با شماره تلفن تکراری
     * پیام خطا شامل شماره تلفن مشکل‌دار می‌شود
     * 
     * @param phone شماره تلفن تکراری که باعث ایجاد خطا شده
     */
    public DuplicatePhoneException(String phone) {
        super("Phone number already exists: " + phone); // پیام خطا شامل شماره تلفن
    }
    
    /**
     * سازنده استثنا با پیام سفارشی
     * برای موارد خاص که نیاز به پیام سفارشی داریم
     * 
     * @param message پیام خطای سفارشی
     */
    public DuplicatePhoneException(String message, boolean isCustomMessage) {
        super(message);
    }
    
    /**
     * سازنده استثنا با پیام و علت
     * این سازنده برای حفظ stack trace اصلی خطا استفاده می‌شود
     * 
     * @param message پیام خطا
     * @param cause علت اصلی خطا
     */
    public DuplicatePhoneException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * روش کمکی برای ایجاد استثنا بر اساس شماره تلفن
     * این روش static برای راحتی استفاده طراحی شده
     * 
     * @param phone شماره تلفن تکراری
     * @return نمونه جدید از DuplicatePhoneException
     */
    public static DuplicatePhoneException forPhone(String phone) {
        return new DuplicatePhoneException(phone);
    }
} 