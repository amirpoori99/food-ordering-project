package com.myapp.common.models;

/**
 * انواع تراکنش‌های پشتیبانی شده توسط سیستم
 * 
 * این enum تمام انواع تراکنش‌های مالی موجود در سیستم را تعریف می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public enum TransactionType {
    /** پرداخت برای سفارش */
    PAYMENT,          // Payment for an order
    
    /** استرداد وجه برای سفارش لغو شده/برگشتی */
    REFUND,           // Refund for a cancelled/returned order
    
    /** شارژ کیف پول - افزودن پول به کیف پول */
    WALLET_CHARGE,    // Adding money to wallet
    
    /** برداشت از کیف پول - کاهش موجودی کیف پول */
    WALLET_WITHDRAWAL, // Withdrawing money from wallet
    
    /** تراکنش اعتباری (شارژ) - برای سازگاری با تست‌ها */
    CREDIT,
    
    /** تراکنش بدهی (برداشت) - برای سازگاری با تست‌ها */
    DEBIT,
    
    /** انتقال وجه بین کیف پول‌ها */
    TRANSFER
} 
