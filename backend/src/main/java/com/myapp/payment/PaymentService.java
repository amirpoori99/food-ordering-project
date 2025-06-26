package com.myapp.payment;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Transaction;
import com.myapp.common.models.TransactionStatus;
import com.myapp.common.models.TransactionType;
import com.myapp.common.models.Order;
import com.myapp.common.models.User;
import com.myapp.auth.AuthRepository;
import com.myapp.order.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * سرویس مدیریت پرداخت و پردازش تراکنش‌های مالی
 * 
 * این کلاس مسئول پیاده‌سازی منطق کسب‌وکار مربوط به:
 * - پردازش انواع مختلف پرداخت (کیف پول، کارت، پرداخت در محل)
 * - مدیریت استرداد وجه
 * - اعتبارسنجی تراکنش‌ها
 * - محاسبه موجودی کیف پول
 * - تولید گزارش‌های مالی و آمار
 * - validation و security checks
 * 
 * Pattern های استفاده شده:
 * - Service Layer Pattern: جداسازی منطق کسب‌وکار از controller
 * - Repository Pattern: دسترسی انتزاعی به لایه داده
 * - Strategy Pattern: روش‌های مختلف پرداخت
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class PaymentService {
    
    /** Repository برای مدیریت تراکنش‌ها و پرداخت‌ها */
    private final PaymentRepository paymentRepository;
    
    /** Repository برای اعتبارسنجی کاربران */
    private final AuthRepository authRepository;
    
    /** Repository برای اعتبارسنجی سفارشات */
    private final OrderRepository orderRepository;
    
    /**
     * سازنده پیش‌فرض - ایجاد instance های جدید از repository ها
     */
    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
        this.authRepository = new AuthRepository();
        this.orderRepository = new OrderRepository();
    }
    
    /**
     * سازنده برای تزریق وابستگی (Dependency Injection)
     * برای تست‌ها و configuration سفارشی استفاده می‌شود
     * 
     * @param paymentRepository repository تراکنش‌ها
     * @param authRepository repository کاربران
     * @param orderRepository repository سفارشات
     */
    public PaymentService(PaymentRepository paymentRepository, AuthRepository authRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.authRepository = authRepository;
        this.orderRepository = orderRepository;
    }
    
    // ==================== PAYMENT PROCESSING ====================
    
    /**
     * پردازش پرداخت برای سفارش
     * 
     * این متد کلیدی‌ترین functionality سیستم پرداخت است که:
     * 1. تمام ورودی‌ها را validate می‌کند
     * 2. وجود کاربر و سفارش را بررسی می‌کند
     * 3. وضعیت سفارش را برای قابلیت پرداخت چک می‌کند
     * 4. تراکنش پرداخت را ایجاد می‌کند
     * 5. بر اساس نوع پرداخت، فرآیند مناسب را اجرا می‌کند
     * 
     * @param userId شناسه کاربر پرداخت‌کننده
     * @param orderId شناسه سفارش مورد پرداخت
     * @param paymentMethod روش پرداخت (WALLET, CARD, CASH_ON_DELIVERY)
     * @return تراکنش ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     * @throws NotFoundException در صورت عدم وجود کاربر یا سفارش
     */
    public Transaction processPayment(Long userId, Long orderId, String paymentMethod) {
        // 1. اعتبارسنجی ورودی‌ها
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be empty");
        }
        
        // 2. اعتبارسنجی وجود کاربر
        Optional<User> userOpt = authRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("User", userId);
        }
        
        // 3. اعتبارسنجی وجود سفارش و تعلق آن به کاربر
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order", orderId);
        }
        
        Order order = orderOpt.get();
        if (!order.getCustomer().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to the specified user");
        }
        
        // 4. بررسی وضعیت سفارش برای قابلیت پرداخت
        if (order.getStatus() != com.myapp.common.models.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order is not in a payable state");
        }
        
        // 5. بررسی عدم وجود پرداخت قبلی موفق
        List<Transaction> existingPayments = paymentRepository.findByOrderId(orderId);
        Optional<Transaction> completedPayment = existingPayments.stream()
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED && t.getType() == TransactionType.PAYMENT)
            .findFirst();
        
        if (completedPayment.isPresent()) {
            throw new IllegalArgumentException("Payment already completed for this order");
        }
        
        // 6. ایجاد تراکنش پرداخت اولیه
        Transaction payment = Transaction.forPayment(userId, orderId, order.getTotalAmount(), paymentMethod);
        payment = paymentRepository.save(payment);
        
        // 7. پردازش پرداخت بر اساس روش انتخابی
        try {
            switch (paymentMethod.toUpperCase()) {
                case "WALLET":
                    processWalletPayment(payment);
                    break;
                case "CARD":
                    processCardPayment(payment);
                    break;
                case "CASH_ON_DELIVERY":
                    processCashOnDeliveryPayment(payment);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
            }
        } catch (Exception e) {
            // در صورت خطا، تراکنش را failed علامت‌گذاری کن
            payment.markAsFailed(e.getMessage());
            paymentRepository.update(payment);
            throw e;
        }
        
        return payment;
    }
    
    /**
     * پردازش پرداخت از طریق کیف پول
     * 
     * مراحل:
     * 1. بررسی موجودی کافی در کیف پول
     * 2. ایجاد تراکنش برداشت از کیف پول
     * 3. تکمیل تراکنش پرداخت
     * 
     * @param payment تراکنش پرداخت اولیه
     * @throws IllegalArgumentException در صورت کمبود موجودی
     */
    private void processWalletPayment(Transaction payment) {
        // 1. محاسبه موجودی فعلی کیف پول
        Double walletBalance = paymentRepository.calculateWalletBalance(payment.getUserId());
        if (walletBalance < payment.getAmount()) {
            throw new IllegalArgumentException("Insufficient wallet balance. Balance: " + walletBalance + ", Required: " + payment.getAmount());
        }
        
        // 2. ایجاد تراکنش برداشت از کیف پول
        Transaction withdrawal = Transaction.forWalletWithdrawal(
            payment.getUserId(), 
            payment.getAmount(), 
            "Payment for order #" + payment.getOrderId()
        );
        withdrawal.markAsCompleted("WALLET_" + UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(withdrawal);
        
        // 3. تکمیل موفقیت‌آمیز تراکنش پرداخت
        payment.markAsCompleted("WALLET_PAYMENT_" + payment.getId());
        paymentRepository.update(payment);
    }
    
    /**
     * پردازش پرداخت از طریق کارت (شبیه‌سازی)
     * 
     * در محیط واقعی، این متد با payment gateway های خارجی ارتباط برقرار می‌کند
     * برای demo، 90% احتمال موفقیت دارد
     * 
     * @param payment تراکنش پرداخت اولیه
     */
    private void processCardPayment(Transaction payment) {
        // تولید شناسه مرجع برای تراکنش
        String referenceId = "CARD_" + UUID.randomUUID().toString().substring(0, 8);
        
        // شبیه‌سازی پاسخ payment gateway (90% نرخ موفقیت)
        if (Math.random() < 0.9) {
            payment.markAsCompleted(referenceId);
        } else {
            payment.markAsFailed("Card payment declined by bank");
        }
        
        paymentRepository.update(payment);
    }
    
    /**
     * پردازش پرداخت در محل تحویل
     * 
     * این نوع پرداخت تا زمان تحویل در وضعیت pending باقی می‌ماند
     * 
     * @param payment تراکنش پرداخت اولیه
     */
    private void processCashOnDeliveryPayment(Transaction payment) {
        // COD payments تا زمان تحویل pending هستند
        payment.setDescription("Cash on delivery - payment pending until delivery");
        paymentRepository.update(payment);
    }
    
    // ==================== REFUND PROCESSING ====================
    
    /**
     * پردازش استرداد وجه برای پرداخت قبلی
     * 
     * مراحل:
     * 1. یافتن تراکنش پرداخت اصلی
     * 2. اعتبارسنجی قابلیت استرداد
     * 3. ایجاد تراکنش استرداد
     * 4. پردازش استرداد بر اساس روش پرداخت اصلی
     * 
     * @param paymentId شناسه تراکنش پرداخت اصلی
     * @param reason دلیل استرداد
     * @return تراکنش استرداد ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     * @throws NotFoundException در صورت عدم وجود تراکنش
     */
    public Transaction processRefund(Long paymentId, String reason) {
        // 1. اعتبارسنجی ورودی‌ها
        if (paymentId == null || paymentId <= 0) {
            throw new IllegalArgumentException("Payment ID must be positive");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Refund reason cannot be empty");
        }
        
        // 2. یافتن تراکنش پرداخت اصلی
        Optional<Transaction> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new NotFoundException("Payment transaction", paymentId);
        }
        
        Transaction originalPayment = paymentOpt.get();
        
        // 3. اعتبارسنجی قابلیت استرداد
        if (originalPayment.getType() != TransactionType.PAYMENT) {
            throw new IllegalArgumentException("Only payment transactions can be refunded");
        }
        if (originalPayment.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed payments can be refunded");
        }
        
        // 4. بررسی عدم وجود استرداد قبلی
        List<Transaction> existingRefunds = paymentRepository.findByOrderId(originalPayment.getOrderId());
        Optional<Transaction> completedRefund = existingRefunds.stream()
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED && t.getType() == TransactionType.REFUND)
            .findFirst();
        
        if (completedRefund.isPresent()) {
            throw new IllegalArgumentException("Payment already refunded");
        }
        
        // 5. ایجاد تراکنش استرداد
        Transaction refund = Transaction.forRefund(
            originalPayment.getUserId(), 
            originalPayment.getOrderId(), 
            originalPayment.getAmount(), 
            reason
        );
        refund = paymentRepository.save(refund);
        
        // 6. پردازش استرداد بر اساس روش پرداخت اصلی
        try {
            switch (originalPayment.getPaymentMethod().toUpperCase()) {
                case "WALLET":
                    processWalletRefund(refund);
                    break;
                case "CARD":
                    processCardRefund(refund);
                    break;
                case "CASH_ON_DELIVERY":
                    processCashRefund(refund);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot process refund for payment method: " + originalPayment.getPaymentMethod());
            }
        } catch (Exception e) {
            // در صورت خطا، استرداد را failed علامت‌گذاری کن
            refund.markAsFailed(e.getMessage());
            paymentRepository.update(refund);
            throw e;
        }
        
        return refund;
    }
    
    /**
     * پردازش استرداد به کیف پول
     * 
     * مبلغ استرداد شده به کیف پول کاربر اضافه می‌شود
     * 
     * @param refund تراکنش استرداد
     */
    private void processWalletRefund(Transaction refund) {
        // ایجاد تراکنش شارژ کیف پول برای استرداد
        Transaction walletCharge = Transaction.forWalletCharge(
            refund.getUserId(), 
            refund.getAmount(), 
            "REFUND"
        );
        walletCharge.setDescription("Refund for order #" + refund.getOrderId());
        walletCharge.markAsCompleted("REFUND_" + UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(walletCharge);
        
        // تکمیل تراکنش استرداد
        refund.markAsCompleted("WALLET_REFUND_" + refund.getId());
        paymentRepository.update(refund);
    }
    
    /**
     * پردازش استرداد به کارت (شبیه‌سازی)
     * 
     * در محیط واقعی، با payment gateway برای استرداد به کارت ارتباط برقرار می‌شود
     * 
     * @param refund تراکنش استرداد
     */
    private void processCardRefund(Transaction refund) {
        // شبیه‌سازی پردازش استرداد کارت
        String referenceId = "REFUND_" + UUID.randomUUID().toString().substring(0, 8);
        refund.markAsCompleted(referenceId);
        paymentRepository.update(refund);
    }
    
    /**
     * پردازش استرداد پرداخت نقدی
     * 
     * استرداد نقدی نیاز به پردازش دستی دارد
     * 
     * @param refund تراکنش استرداد
     */
    private void processCashRefund(Transaction refund) {
        // استرداد نقدی نیاز به پردازش دستی دارد
        refund.setDescription(refund.getDescription() + " - Manual cash refund required");
        refund.markAsCompleted("CASH_REFUND_MANUAL");
        paymentRepository.update(refund);
    }
    
    // ==================== TRANSACTION MANAGEMENT ====================
    
    /**
     * دریافت تراکنش بر اساس شناسه
     * 
     * @param transactionId شناسه تراکنش
     * @return تراکنش مورد نظر
     * @throws IllegalArgumentException در صورت نامعتبر بودن ID
     * @throws NotFoundException در صورت عدم وجود تراکنش
     */
    public Transaction getTransaction(Long transactionId) {
        if (transactionId == null || transactionId <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive");
        }
        
        Optional<Transaction> transactionOpt = paymentRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new NotFoundException("Transaction", transactionId);
        }
        
        return transactionOpt.get();
    }
    
    /**
     * دریافت تاریخچه کامل تراکنش‌های کاربر
     * 
     * شامل تمام انواع تراکنش: پرداخت، استرداد، شارژ و برداشت کیف پول
     * 
     * @param userId شناسه کاربر
     * @return لیست تراکنش‌ها مرتب شده بر اساس تاریخ (جدیدترین ابتدا)
     * @throws IllegalArgumentException در صورت نامعتبر بودن userId
     * @throws NotFoundException در صورت عدم وجود کاربر
     */
    public List<Transaction> getUserTransactionHistory(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // بررسی وجود کاربر
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findByUserId(userId);
    }
    
    /**
     * دریافت تاریخچه تراکنش‌های سفارش
     * 
     * تمام تراکنش‌های مربوط به یک سفارش شامل پرداخت و استرداد
     * 
     * @param orderId شناسه سفارش
     * @return لیست تراکنش‌های مربوط به سفارش
     * @throws IllegalArgumentException در صورت نامعتبر بودن orderId
     * @throws NotFoundException در صورت عدم وجود سفارش
     */
    public List<Transaction> getOrderTransactionHistory(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        
        // بررسی وجود سفارش
        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Order", orderId);
        }
        
        return paymentRepository.findByOrderId(orderId);
    }
    
    /**
     * دریافت تراکنش‌های کیف پول کاربر
     * 
     * فقط تراکنش‌های شارژ و برداشت کیف پول
     * 
     * @param userId شناسه کاربر
     * @return لیست تراکنش‌های کیف پول
     */
    public List<Transaction> getUserWalletTransactions(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // بررسی وجود کاربر
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findWalletTransactions(userId);
    }
    
    /**
     * دریافت تراکنش‌های پرداخت کاربر
     * 
     * فقط تراکنش‌های پرداخت و استرداد سفارشات
     * 
     * @param userId شناسه کاربر
     * @return لیست تراکنش‌های پرداخت
     */
    public List<Transaction> getUserPaymentTransactions(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // بررسی وجود کاربر
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findPaymentTransactions(userId);
    }
    
    /**
     * دریافت تراکنش‌ها بر اساس وضعیت
     * 
     * فیلتر کردن تراکنش‌ها بر اساس وضعیت: PENDING, COMPLETED, FAILED, CANCELLED
     * 
     * @param status وضعیت مورد نظر
     * @return لیست تراکنش‌های با وضعیت مشخص
     * @throws IllegalArgumentException در صورت null بودن status
     */
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Transaction status cannot be null");
        }
        
        return paymentRepository.findByStatus(status);
    }
    
    /**
     * دریافت تراکنش‌ها بر اساس نوع
     * 
     * فیلتر کردن تراکنش‌ها بر اساس نوع: PAYMENT, REFUND, WALLET_CHARGE, WALLET_WITHDRAWAL
     * 
     * @param type نوع تراکنش مورد نظر
     * @return لیست تراکنش‌های با نوع مشخص
     * @throws IllegalArgumentException در صورت null بودن type
     */
    public List<Transaction> getTransactionsByType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        
        return paymentRepository.findByType(type);
    }
    
    /**
     * دریافت تراکنش‌ها در بازه زمانی مشخص
     * 
     * @param startDate تاریخ شروع
     * @param endDate تاریخ پایان
     * @return لیست تراکنش‌ها در بازه زمانی
     * @throws IllegalArgumentException در صورت نامعتبر بودن تاریخ‌ها
     */
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return paymentRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * دریافت تراکنش‌های کاربر در بازه زمانی مشخص
     * 
     * @param userId شناسه کاربر
     * @param startDate تاریخ شروع
     * @param endDate تاریخ پایان
     * @return لیست تراکنش‌های کاربر در بازه زمانی
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     * @throws NotFoundException در صورت عدم وجود کاربر
     */
    public List<Transaction> getUserTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // بررسی وجود کاربر
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    // ==================== TRANSACTION STATISTICS ====================
    
    /**
     * دریافت آمار تراکنش‌های کاربر
     * 
     * شامل اطلاعات جامعی از فعالیت‌های مالی کاربر:
     * - تعداد کل تراکنش‌ها
     * - تعداد تراکنش‌های موفق/ناموفق/در انتظار
     * - مجموع مبلغ خرج شده
     * - مجموع مبلغ استرداد شده
     * - مبلغ خالص خرج شده
     * - درصد موفقیت تراکنش‌ها
     * 
     * @param userId شناسه کاربر
     * @return آمار کامل تراکنش‌های کاربر
     * @throws IllegalArgumentException در صورت نامعتبر بودن userId
     * @throws NotFoundException در صورت عدم وجود کاربر
     */
    public PaymentRepository.TransactionStatistics getUserTransactionStatistics(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // بررسی وجود کاربر
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.getUserTransactionStatistics(userId);
    }
    
    // ==================== PAYMENT VALIDATION ====================
    
    /**
     * اعتبارسنجی روش پرداخت
     * 
     * روش‌های پرداخت پشتیبانی شده:
     * - WALLET: پرداخت از کیف پول
     * - CARD: پرداخت با کارت
     * - CASH_ON_DELIVERY: پرداخت در محل تحویل
     * 
     * @param paymentMethod روش پرداخت
     * @return true اگر معتبر باشد، در غیر این صورت false
     */
    public boolean isValidPaymentMethod(String paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        
        String method = paymentMethod.toUpperCase();
        return method.equals("WALLET") || method.equals("CARD") || method.equals("CASH_ON_DELIVERY");
    }
    
    /**
     * بررسی قابلیت پرداخت کاربر
     * 
     * چک می‌کند که آیا کاربر قادر به پرداخت مبلغ مشخص با روش انتخابی است
     * برای پرداخت کیف پول، موجودی کافی بررسی می‌شود
     * 
     * @param userId شناسه کاربر
     * @param amount مبلغ پرداخت
     * @param paymentMethod روش پرداخت
     * @return true اگر قادر به پرداخت باشد، در غیر این صورت false
     */
    public boolean canUserMakePayment(Long userId, Double amount, String paymentMethod) {
        if (userId == null || userId <= 0 || amount == null || amount <= 0 || paymentMethod == null) {
            return false;
        }
        
        // بررسی وجود کاربر
        if (!authRepository.existsById(userId)) {
            return false;
        }
        
        // بررسی معتبر بودن روش پرداخت
        if (!isValidPaymentMethod(paymentMethod)) {
            return false;
        }
        
        // برای پرداخت کیف پول، بررسی موجودی
        if ("WALLET".equalsIgnoreCase(paymentMethod)) {
            Double walletBalance = paymentRepository.calculateWalletBalance(userId);
            return walletBalance >= amount;
        }
        
        // برای سایر روش‌های پرداخت، فرض بر قابلیت پردازش است
        return true;
    }
    
    /**
     * بررسی قابلیت استرداد تراکنش
     * 
     * شرایط استرداد:
     * 1. تراکنش باید از نوع PAYMENT باشد
     * 2. وضعیت تراکنش باید COMPLETED باشد
     * 3. قبلاً استرداد نشده باشد
     * 
     * @param transactionId شناسه تراکنش
     * @return true اگر قابل استرداد باشد، در غیر این صورت false
     */
    public boolean canRefundTransaction(Long transactionId) {
        if (transactionId == null || transactionId <= 0) {
            return false;
        }
        
        Optional<Transaction> transactionOpt = paymentRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            return false;
        }
        
        Transaction transaction = transactionOpt.get();
        
        // فقط تراکنش‌های پرداخت موفق قابل استرداد هستند
        if (transaction.getType() != TransactionType.PAYMENT || transaction.getStatus() != TransactionStatus.COMPLETED) {
            return false;
        }
        
        // بررسی عدم وجود استرداد قبلی
        if (transaction.getOrderId() != null) {
            List<Transaction> orderTransactions = paymentRepository.findByOrderId(transaction.getOrderId());
            return orderTransactions.stream()
                .noneMatch(t -> t.getType() == TransactionType.REFUND && t.getStatus() == TransactionStatus.COMPLETED);
        }
        
        return true;
    }
    
    /**
     * به‌روزرسانی وضعیت تراکنش (برای callback های خارجی)
     * 
     * این متد توسط payment gateway ها برای اطلاع‌رسانی نتیجه پرداخت استفاده می‌شود
     * 
     * @param transactionId شناسه تراکنش
     * @param status وضعیت جدید
     * @param referenceId شناسه مرجع از payment gateway
     * @param notes یادداشت‌های اضافی
     * @return تراکنش به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     * @throws NotFoundException در صورت عدم وجود تراکنش
     */
    public Transaction updateTransactionStatus(Long transactionId, TransactionStatus status, String referenceId, String notes) {
        if (transactionId == null || transactionId <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Transaction status cannot be null");
        }
        
        Optional<Transaction> transactionOpt = paymentRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new NotFoundException("Transaction", transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        
        // به‌روزرسانی تراکنش بر اساس وضعیت جدید
        switch (status) {
            case COMPLETED:
                transaction.markAsCompleted(referenceId);
                break;
            case FAILED:
                transaction.markAsFailed(notes != null ? notes : "Payment failed");
                break;
            case CANCELLED:
                transaction.markAsCancelled(notes != null ? notes : "Payment cancelled");
                break;
            default:
                transaction.setStatus(status);
                if (notes != null) {
                    transaction.setDescription(transaction.getDescription() + ". " + notes);
                }
                break;
        }
        
        return paymentRepository.update(transaction);
    }
} 