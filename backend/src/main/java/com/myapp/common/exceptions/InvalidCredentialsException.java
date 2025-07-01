package com.myapp.common.exceptions;

/**
 * استثنا برای اطلاعات ورود نامعتبر
 * این استثنا هنگام ورود با شماره تلفن یا رمز عبور اشتباه پرتاب می‌شود
 * از RuntimeException ارث‌بری می‌کند تا نیازی به try-catch اجباری نباشد
 * 
 * مثال استفاده:
 * <pre>
 * if (!passwordMatches) {
 *     throw new InvalidCredentialsException();
 * }
 * </pre>
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class InvalidCredentialsException extends RuntimeException {
    
    /**
     * شناسه منحصر به فرد برای serialization
     * این مقدار برای حفظ سازگاری در زمان serialize/deserialize استفاده می‌شود
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * سازنده استثنا با پیام خطای ثابت
     * از روی امنیت، جزئیات دقیق خطا را نمایش نمی‌دهد
     * این کار مانع از information leakage می‌شود
     */
    public InvalidCredentialsException() {
        super("Invalid phone or password"); // پیام عمومی برای امنیت
    }
    
    /**
     * سازنده استثنا با پیام سفارشی
     * این سازنده برای موارد خاص استفاده می‌شود که نیاز به پیام سفارشی داریم
     * 
     * @param message پیام خطای سفارشی
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
    
    /**
     * سازنده استثنا با پیام و علت
     * این سازنده برای حفظ stack trace اصلی خطا استفاده می‌شود
     * 
     * @param message پیام خطا
     * @param cause علت اصلی خطا
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
} 
