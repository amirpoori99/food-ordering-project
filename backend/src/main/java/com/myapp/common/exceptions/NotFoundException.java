package com.myapp.common.exceptions;

/**
 * استثنا برای یافت نشدن موجودیت (Entity)
 * این استثنا هنگام جستجو برای entity ای که وجود ندارد پرتاب می‌شود
 * از RuntimeException ارث‌بری می‌کند تا نیازی به try-catch اجباری نباشد
 * 
 * مثال استفاده:
 * <pre>
 * User user = userRepository.findById(userId);
 * if (user == null) {
 *     throw new NotFoundException("User", userId);
 * }
 * </pre>
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class NotFoundException extends RuntimeException {
    
    /**
     * شناسه منحصر به فرد برای serialization
     * این مقدار برای حفظ سازگاری در زمان serialize/deserialize استفاده می‌شود
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * سازنده استثنا با نام entity و شناسه آن
     * پیام خطا به صورت خودکار تولید می‌شود
     * 
     * @param entity نام موجودیت (مثل "User", "Restaurant", "Order")
     * @param id شناسه موجودیت که یافت نشد
     */
    public NotFoundException(String entity, Object id) {
        super(buildMessage(entity, id)); // پیام خطا شامل entity و id
    }
    
    /**
     * متد کمکی برای ساخت پیام خطا
     */
    private static String buildMessage(String entity, Object id) {
        String entityName = (entity != null) ? entity : "Entity";
        String idValue = (id != null) ? id.toString() : "null";
        return entityName + " not found with ID: " + idValue;
    }
    
    /**
     * سازنده استثنا با پیام سفارشی
     * برای موارد خاص که نیاز به پیام سفارشی داریم
     * 
     * @param message پیام خطای سفارشی
     */
    public NotFoundException(String message) {
        super(message);
    }
    
    /**
     * سازنده استثنا با پیام و علت
     * این سازنده برای حفظ stack trace اصلی خطا استفاده می‌شود
     * 
     * @param message پیام خطا
     * @param cause علت اصلی خطا
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * سازنده کمکی برای entity های با نام فیلد مشخص
     * برای جستجو بر اساس فیلدهای غیر از ID
     * 
     * @param entity نام موجودیت
     * @param fieldName نام فیلد جستجو شده
     * @param fieldValue مقدار فیلد
     */
    public NotFoundException(String entity, String fieldName, Object fieldValue) {
        super(entity + " not found with " + fieldName + "=" + fieldValue);
    }
} 
