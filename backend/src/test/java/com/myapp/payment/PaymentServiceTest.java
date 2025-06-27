package com.myapp.payment;

import com.myapp.common.models.Transaction;
import com.myapp.common.models.TransactionType;
import com.myapp.common.models.TransactionStatus;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.auth.AuthRepository;
import com.myapp.order.OrderRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * تست‌های جامع PaymentService
 * این کلاس تمام عملیات مربوط به پرداخت و تراکنش‌ها را آزمایش می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Payment Service Tests")
class PaymentServiceTest {

    private PaymentService paymentService;
    private PaymentRepository paymentRepository;
    private AuthRepository authRepository;
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepository();
        authRepository = new AuthRepository();
        orderRepository = new OrderRepository();
        paymentService = new PaymentService(paymentRepository, authRepository, orderRepository);
        
        // پاک‌سازی تراکنش‌های قبلی
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Transaction retrieval should work")
    void transactionRetrieval_shouldWork() {
        // Given - ایجاد تراکنش مستقیماً در repository
        Transaction created = new Transaction();
        created.setUserId(1L);
        created.setOrderId(100L);
        created.setAmount(50.0);
        created.setType(TransactionType.PAYMENT);
        created.setStatus(TransactionStatus.COMPLETED);
        created.setDescription("تست تراکنش");
        created = paymentRepository.save(created);
        
        // When - دریافت تراکنش
        Transaction found = paymentService.getTransaction(created.getId());
        
        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getAmount()).isEqualTo(created.getAmount());
    }

    @Test
    @DisplayName("User transaction history should work")
    void userTransactionHistory_shouldWork() {
        // Given - ایجاد کاربر در authRepository با شماره تلفن منحصر به فرد
        String uniquePhone = "091234567" + System.currentTimeMillis() % 100; // شماره منحصر به فرد
        com.myapp.common.models.User user = new com.myapp.common.models.User();
        user.setPhone(uniquePhone);
        user.setFullName("کاربر تست");
        user.setPasswordHash("test_password_hash_123");
        user.setRole(com.myapp.common.models.User.Role.BUYER);
        final com.myapp.common.models.User savedUser = authRepository.saveNew(user);
        
        // ایجاد چند تراکنش
        Transaction t1 = new Transaction();
        t1.setUserId(savedUser.getId());
        t1.setOrderId(101L);
        t1.setAmount(20.0);
        t1.setType(TransactionType.PAYMENT);
        t1.setStatus(TransactionStatus.COMPLETED);
        paymentRepository.save(t1);
        
        Transaction t2 = new Transaction();
        t2.setUserId(savedUser.getId());
        t2.setOrderId(102L);
        t2.setAmount(30.0);
        t2.setType(TransactionType.PAYMENT);
        t2.setStatus(TransactionStatus.COMPLETED);
        paymentRepository.save(t2);
        
        // When - دریافت تاریخچه تراکنش‌های کاربر
        List<Transaction> transactions = paymentService.getUserTransactionHistory(savedUser.getId());
        
        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions).allMatch(t -> t.getUserId().equals(savedUser.getId()));
    }

    @Test
    @DisplayName("Invalid transaction ID should throw exception")
    void invalidTransactionId_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> paymentService.getTransaction(99999L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Null parameters should be handled safely")
    void nullParameters_shouldBeHandledSafely() {
        // When & Then
        assertThatThrownBy(() -> paymentService.getTransaction(null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> paymentService.getUserTransactionHistory(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Transaction statistics should work")
    void transactionStatistics_shouldWork() {
        // Given - ایجاد کاربر با شماره تلفن منحصر به فرد
        String uniquePhone = "091234568" + System.currentTimeMillis() % 100; // شماره متفاوت
        com.myapp.common.models.User user = new com.myapp.common.models.User();
        user.setPhone(uniquePhone);
        user.setFullName("کاربر آمار");
        user.setPasswordHash("test_password_hash_456");
        user.setRole(com.myapp.common.models.User.Role.BUYER);
        final com.myapp.common.models.User savedUser = authRepository.saveNew(user);
        
        // ایجاد چند تراکنش
        for (int i = 1; i <= 5; i++) {
            Transaction t = new Transaction();
            t.setUserId(savedUser.getId());
            t.setOrderId((long)(200 + i));
            t.setAmount(i * 10.0);
            t.setType(TransactionType.PAYMENT);
            t.setStatus(TransactionStatus.COMPLETED);
            paymentRepository.save(t);
        }
        
        // When - دریافت آمار
        PaymentRepository.TransactionStatistics stats = paymentService.getUserTransactionStatistics(savedUser.getId());
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalTransactions()).isEqualTo(5);
        assertThat(stats.getTotalSpent()).isEqualTo(150.0); // 10+20+30+40+50
    }

    @ParameterizedTest
    @ValueSource(strings = {"WALLET", "CARD", "CASH_ON_DELIVERY"})
    @DisplayName("Payment method validation should work")
    void paymentMethodValidation_shouldWork(String paymentMethod) {
        // When & Then
        boolean isValid = paymentService.isValidPaymentMethod(paymentMethod);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Transaction status filtering should work")
    void transactionStatusFiltering_shouldWork() {
        // Given - تراکنش‌های با وضعیت‌های مختلف
        Transaction completed = new Transaction();
        completed.setUserId(10L);
        completed.setOrderId(300L);
        completed.setAmount(100.0);
        completed.setType(TransactionType.PAYMENT);
        completed.setStatus(TransactionStatus.COMPLETED);
        paymentRepository.save(completed);
        
        Transaction pending = new Transaction();
        pending.setUserId(11L);
        pending.setOrderId(301L);
        pending.setAmount(50.0);
        pending.setType(TransactionType.PAYMENT);
        pending.setStatus(TransactionStatus.PENDING);
        paymentRepository.save(pending);
        
        // When - فیلتر کردن بر اساس وضعیت
        List<Transaction> completedTransactions = paymentService.getTransactionsByStatus(TransactionStatus.COMPLETED);
        List<Transaction> pendingTransactions = paymentService.getTransactionsByStatus(TransactionStatus.PENDING);
        
        // Then
        assertThat(completedTransactions).isNotEmpty();
        assertThat(pendingTransactions).isNotEmpty();
        assertThat(completedTransactions).allMatch(t -> t.getStatus() == TransactionStatus.COMPLETED);
        assertThat(pendingTransactions).allMatch(t -> t.getStatus() == TransactionStatus.PENDING);
    }
} 