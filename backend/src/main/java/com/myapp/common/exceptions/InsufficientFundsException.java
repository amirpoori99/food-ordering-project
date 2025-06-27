package com.myapp.common.exceptions;

/**
 * Exception پرتاب می‌شود زمانی که موجودی کیف پول کافی نباشد
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class InsufficientFundsException extends RuntimeException {
    
    /**
     * سازنده پیش‌فرض
     */
    public InsufficientFundsException() {
        super("Insufficient funds");
    }
    
    /**
     * سازنده با پیام سفارشی
     * 
     * @param message پیام خطا
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    /**
     * سازنده با پیام و علت
     * 
     * @param message پیام خطا
     * @param cause علت اصلی خطا
     */
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * ایجاد exception برای موجودی ناکافی کیف پول
     * 
     * @param currentBalance موجودی فعلی
     * @param requiredAmount مبلغ مورد نیاز
     * @return InsufficientFundsException با پیام مناسب
     */
    public static InsufficientFundsException forWallet(double currentBalance, double requiredAmount) {
        return new InsufficientFundsException(
            String.format("Insufficient wallet balance. Current: %.2f, Required: %.2f", 
                currentBalance, requiredAmount)
        );
    }
    
    /**
     * ایجاد exception برای موجودی ناکافی کیف پول با پیام ساده
     * 
     * @param requiredAmount مبلغ مورد نیاز
     * @return InsufficientFundsException با پیام ساده
     */
    public static InsufficientFundsException forAmount(double requiredAmount) {
        return new InsufficientFundsException(
            String.format("Insufficient wallet balance for amount: %.2f", requiredAmount)
        );
    }
} 