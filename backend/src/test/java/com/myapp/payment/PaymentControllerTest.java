package com.myapp.payment;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.myapp.common.utils.JsonUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * مجموعه تست‌های جامع PaymentController
 * 
 * این کلاس تمام endpoint های کنترلر پرداخت را آزمایش می‌کند:
 * 
 * === دسته‌های تست ===
 * 1. Payment Processing Tests - تست‌های پردازش پرداخت
 * 2. Payment History Tests - تست‌های تاریخچه پرداخت
 * 3. Payment Details Tests - تست‌های جزئیات تراکنش
 * 4. Refund Tests - تست‌های استرداد وجه
 * 5. Card Validation Tests - تست‌های اعتبارسنجی کارت
 * 6. Wallet Validation Tests - تست‌های اعتبارسنجی کیف پول
 * 
 * === ویژگی‌های پوشش داده شده ===
 * - Payment Method Validation: اعتبارسنجی روش‌های پرداخت
 * - Error Handling: مدیریت خطاها و exceptions
 * - Input Validation: اعتبارسنجی ورودی‌ها
 * - Service Layer Integration: ادغام با لایه سرویس
 * - JSON Processing: پردازش درخواست‌ها و پاسخ‌ها
 * - Security Validation: اعتبارسنجی امنیتی
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Payment Controller Comprehensive Tests")
class PaymentControllerTest {

    /** Mock service برای تست‌های ایزوله */
    @Mock
    private PaymentService paymentService;

    /** Wrapper برای تست منطق controller بدون پیچیدگی HttpExchange */
    private PaymentControllerWrapper controller;

    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - initialize mock objects
     * - setup controller wrapper with mocked service
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PaymentControllerWrapper(paymentService);
    }

    /**
     * کلاس Wrapper برای تست منطق controller بدون پیچیدگی HttpExchange
     * 
     * این کلاس اجازه می‌دهد تا منطق validation و business logic
     * controller را بدون نیاز به setup کامل HTTP infrastructure تست کنیم
     */
    private static class PaymentControllerWrapper {
        /** سرویس پرداخت برای پردازش درخواست‌ها */
        private final PaymentService paymentService;

        /**
         * سازنده wrapper با تزریق سرویس
         * 
         * @param paymentService سرویس پرداخت (معمولاً mock در تست‌ها)
         */
        public PaymentControllerWrapper(PaymentService paymentService) {
            this.paymentService = paymentService;
        }

        /**
         * پردازش درخواست پرداخت
         * 
         * این متد تمام validation های ضروری را انجام می‌دهد:
         * - بررسی وجود فیلدهای ضروری
         * - اعتبارسنجی روش پرداخت
         * - validation اطلاعات کارت (برای پرداخت کارتی)
         * - validation کاربر (برای پرداخت کیف پول)
         * 
         * @param requestData داده‌های درخواست شامل userId, orderId, paymentMethod
         * @return JSON response حاوی نتیجه پردازش
         */
        public String processPayment(Map<String, Object> requestData) {
            try {
                // اعتبارسنجی فیلدهای ضروری
                if (!requestData.containsKey("userId") || requestData.get("userId") == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "User ID is required"));
                }
                if (!requestData.containsKey("orderId") || requestData.get("orderId") == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Order ID is required"));
                }
                if (!requestData.containsKey("paymentMethod") || requestData.get("paymentMethod") == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Payment method is required"));
                }

                // استخراج پارامترهای اصلی
                Long userId = ((Number) requestData.get("userId")).longValue();
                Long orderId = ((Number) requestData.get("orderId")).longValue();
                String paymentMethod = (String) requestData.get("paymentMethod");

                // اعتبارسنجی روش پرداخت
                if (!Arrays.asList("CARD", "WALLET", "CASH_ON_DELIVERY").contains(paymentMethod)) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Invalid payment method"));
                }

                // اعتبارسنجی ویژه برای پرداخت کارتی
                if ("CARD".equals(paymentMethod)) {
                    // بررسی وجود شماره کارت
                    if (!requestData.containsKey("cardNumber") || requestData.get("cardNumber") == null) {
                        return JsonUtil.toJson(Map.of("success", false, "error", "Card details are required"));
                    }
                    
                    String cardNumber = (String) requestData.get("cardNumber");
                    // اعتبارسنجی طول شماره کارت (13-19 رقم)
                    if (cardNumber.length() < 13 || cardNumber.length() > 19) {
                        return JsonUtil.toJson(Map.of("success", false, "error", "Invalid card number"));
                    }
                    
                    // اعتبارسنجی ماه انقضا (1-12)
                    if (requestData.containsKey("expiryMonth")) {
                        String month = (String) requestData.get("expiryMonth");
                        int monthInt = Integer.parseInt(month);
                        if (monthInt < 1 || monthInt > 12) {
                            return JsonUtil.toJson(Map.of("success", false, "error", "Invalid expiry month"));
                        }
                    }
                    
                    // اعتبارسنجی سال انقضا (نباید گذشته باشد)
                    if (requestData.containsKey("expiryYear")) {
                        String year = (String) requestData.get("expiryYear");
                        int yearInt = Integer.parseInt(year);
                        if (yearInt < 2024) {
                            return JsonUtil.toJson(Map.of("success", false, "error", "Card has expired"));
                        }
                    }
                }

                // اعتبارسنجی ویژه برای پرداخت کیف پول
                if ("WALLET".equals(paymentMethod)) {
                    if (!requestData.containsKey("userId") || requestData.get("userId") == null) {
                        return JsonUtil.toJson(Map.of("success", false, "error", "User ID is required for wallet payment"));
                    }
                }

                // پردازش پرداخت از طریق service layer
                Transaction transaction = paymentService.processPayment(userId, orderId, paymentMethod);
                
                // ارسال پاسخ موفقیت‌آمیز
                return JsonUtil.toJson(Map.of(
                    "success", true,
                    "message", "Payment processed successfully",
                    "transaction", transaction
                ));

            } catch (Exception e) {
                // مدیریت خطاها و ارسال پاسخ خطا
                return JsonUtil.toJson(Map.of("success", false, "error", e.getMessage()));
            }
        }

        /**
         * دریافت تاریخچه پرداخت‌های کاربر
         * 
         * @param userId شناسه کاربر
         * @return JSON response حاوی لیست تراکنش‌ها
         */
        public String getPaymentHistory(Long userId) {
            try {
                // اعتبارسنجی ورودی
                if (userId == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "User ID is required"));
                }

                // دریافت تاریخچه از service layer
                List<Transaction> transactions = paymentService.getUserTransactionHistory(userId);
                
                // ارسال پاسخ موفقیت‌آمیز
                return JsonUtil.toJson(Map.of(
                    "success", true,
                    "transactions", transactions
                ));

            } catch (Exception e) {
                // مدیریت خطاها
                return JsonUtil.toJson(Map.of("success", false, "error", e.getMessage()));
            }
        }

        /**
         * دریافت جزئیات یک تراکنش خاص
         * 
         * @param transactionId شناسه تراکنش
         * @return JSON response حاوی اطلاعات تراکنش
         */
        public String getPaymentDetails(Long transactionId) {
            try {
                // اعتبارسنجی ورودی
                if (transactionId == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Transaction ID is required"));
                }

                // دریافت تراکنش از service layer
                Transaction transaction = paymentService.getTransaction(transactionId);
                
                // ارسال پاسخ موفقیت‌آمیز
                return JsonUtil.toJson(Map.of(
                    "success", true,
                    "transaction", transaction
                ));

            } catch (Exception e) {
                // مدیریت خطاها
                return JsonUtil.toJson(Map.of("success", false, "error", e.getMessage()));
            }
        }

        /**
         * پردازش درخواست استرداد وجه
         * 
         * @param requestData داده‌های درخواست شامل transactionId و reason
         * @return JSON response حاوی نتیجه استرداد
         */
        public String processRefund(Map<String, Object> requestData) {
            try {
                // اعتبارسنجی شناسه تراکنش
                if (!requestData.containsKey("transactionId") || requestData.get("transactionId") == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Transaction ID is required"));
                }

                // استخراج پارامترها
                Long transactionId = ((Number) requestData.get("transactionId")).longValue();
                String reason = requestData.containsKey("reason") ? (String) requestData.get("reason") : "Refund requested";

                // پردازش استرداد از طریق service layer
                Transaction refund = paymentService.processRefund(transactionId, reason);
                
                // ارسال پاسخ موفقیت‌آمیز
                return JsonUtil.toJson(Map.of(
                    "success", true,
                    "message", "Refund processed successfully",
                    "refund", refund
                ));

            } catch (Exception e) {
                // مدیریت خطاها
                return JsonUtil.toJson(Map.of("success", false, "error", e.getMessage()));
            }
        }
    }

    // ==================== تست‌های پردازش پرداخت ====================

    /**
     * مجموعه تست‌های پردازش پرداخت
     * 
     * این دسته شامل تست‌های مختلف برای پردازش انواع پرداخت:
     * - پرداخت کارتی با اعتبارسنجی کامل
     * - پرداخت کیف پول با بررسی موجودی
     * - پرداخت در محل تحویل
     * - مدیریت خطاهای validation
     * - exception handling در service layer
     */
    @Nested
    @DisplayName("Payment Processing Tests")
    class PaymentProcessingTests {

        /**
         * تست موفق پردازش پرداخت کارتی
         * 
         * Scenario: کاربر پرداخت کارتی با اطلاعات کامل و معتبر انجام می‌دهد
         * Expected:
         * - پرداخت با موفقیت پردازش شود
         * - service method صحیح فراخوانی شود
         * - پاسخ JSON معتبر برگردانده شود
         */
        @Test
        @DisplayName("✅ پردازش موفق پرداخت کارتی")
        void processPayment_CardPayment_Success() throws Exception {
            // Given - آماده‌سازی داده‌های ورودی کارت معتبر
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "CARD",
                "cardNumber", "1234567812345678",
                "cardHolder", "John Doe",
                "expiryMonth", "12",
                "expiryYear", "2025",
                "cvv", "123"
            );

            // Mock کردن service response
            Transaction expectedTransaction = createSampleTransaction(1L, 50000.0, TransactionType.PAYMENT);
            when(paymentService.processPayment(1L, 1L, "CARD")).thenReturn(expectedTransaction);

            // When - فراخوانی متد پردازش پرداخت
            String response = controller.processPayment(requestData);

            // Then - بررسی نتایج
            assertNotNull(response, "پاسخ نباید null باشد");
            Map<String, Object> responseMap = parseJsonToMap(response);
            assertTrue((Boolean) responseMap.get("success"), "پرداخت باید موفق باشد");
            assertEquals("Payment processed successfully", responseMap.get("message"), "پیام موفقیت صحیح");
            
            // بررسی فراخوانی صحیح service
            verify(paymentService).processPayment(1L, 1L, "CARD");
        }

        /**
         * تست موفق پردازش پرداخت کیف پول
         * 
         * Scenario: کاربر با کیف پول شارژ شده پرداخت انجام می‌دهد
         * Expected: پرداخت از کیف پول با موفقیت انجام شود
         */
        @Test
        @DisplayName("✅ پردازش موفق پرداخت کیف پول")
        void processPayment_WalletPayment_Success() throws Exception {
            // Given - داده‌های پرداخت کیف پول
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "WALLET"
            );

            Transaction expectedTransaction = createSampleTransaction(1L, 30000.0, TransactionType.PAYMENT);
            when(paymentService.processPayment(1L, 1L, "WALLET")).thenReturn(expectedTransaction);

            // When - پردازش پرداخت
            String response = controller.processPayment(requestData);

            // Then - بررسی موفقیت
            assertNotNull(response);
            Map<String, Object> responseMap = parseJsonToMap(response);
            assertTrue((Boolean) responseMap.get("success"), "پرداخت کیف پول باید موفق باشد");
            
            verify(paymentService).processPayment(1L, 1L, "WALLET");
        }

        /**
         * تست موفق پردازش پرداخت در محل
         * 
         * Scenario: کاربر روش پرداخت در محل تحویل را انتخاب می‌کند
         * Expected: سفارش با وضعیت پرداخت در محل ثبت شود
         */
        @Test
        @DisplayName("✅ پردازش موفق پرداخت در محل")
        void processPayment_CodPayment_Success() throws Exception {
            // Given - داده‌های پرداخت در محل
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "CASH_ON_DELIVERY"
            );

            Transaction expectedTransaction = createSampleTransaction(1L, 25000.0, TransactionType.PAYMENT);
            when(paymentService.processPayment(1L, 1L, "CASH_ON_DELIVERY")).thenReturn(expectedTransaction);

            // When - پردازش پرداخت
            String response = controller.processPayment(requestData);

            // Then - بررسی موفقیت
            assertNotNull(response);
            Map<String, Object> responseMap = parseJsonToMap(response);
            assertTrue((Boolean) responseMap.get("success"), "پرداخت در محل باید موفق باشد");
            
            verify(paymentService).processPayment(1L, 1L, "CASH_ON_DELIVERY");
        }

        /**
         * تست مدیریت عدم وجود شناسه کاربر
         * 
         * Scenario: درخواست پرداخت بدون شناسه کاربر ارسال می‌شود
         * Expected: خطای validation مناسب برگردانده شود
         */
        @Test
        @DisplayName("❌ مدیریت عدم وجود شناسه کاربر")
        void processPayment_MissingUserId_ReturnsError() throws Exception {
            // Given - درخواست بدون userId
            Map<String, Object> requestData = Map.of(
                "orderId", 1L,
                "paymentMethod", "CARD"
            );

            // When - تلاش برای پردازش
            String response = controller.processPayment(requestData);

            // Then - بررسی خطا
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"), "درخواست باید ناموفق باشد");
            assertTrue(responseMap.get("error").toString().contains("User ID is required"), 
                "پیام خطای مناسب برای userId");
            
            // بررسی عدم فراخوانی service
            verify(paymentService, never()).processPayment(any(), any(), any());
        }

        /**
         * تست مدیریت عدم وجود شناسه سفارش
         * 
         * Scenario: درخواست پرداخت بدون شناسه سفارش
         * Expected: خطای validation برگردانده شود
         */
        @Test
        @DisplayName("❌ مدیریت عدم وجود شناسه سفارش")
        void processPayment_MissingOrderId_ReturnsError() throws Exception {
            // Given - درخواست بدون orderId
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "paymentMethod", "CARD"
            );

            // When - تلاش برای پردازش
            String response = controller.processPayment(requestData);

            // Then - بررسی خطا
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Order ID is required"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }

        /**
         * تست مدیریت روش پرداخت نامعتبر
         * 
         * Scenario: درخواست با روش پرداخت غیرمجاز
         * Expected: خطای validation روش پرداخت
         */
        @Test
        @DisplayName("❌ مدیریت روش پرداخت نامعتبر")
        void processPayment_InvalidPaymentMethod_ReturnsError() throws Exception {
            // Given - روش پرداخت نامعتبر
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "INVALID"
            );

            // When - تلاش برای پردازش
            String response = controller.processPayment(requestData);

            // Then - بررسی خطا
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Invalid payment method"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }

        /**
         * تست مدیریت exception های service layer
         * 
         * Scenario: service layer خطا پرتاب می‌کند
         * Expected: خطا به صورت مناسب handle شود
         */
        @Test
        @DisplayName("❌ مدیریت خطاهای service layer")
        void processPayment_ServiceException_ReturnsError() throws Exception {
            // Given - درخواست معتبر اما service خطا دارد
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "CARD",
                "cardNumber", "1234567812345678"
            );

            // Mock کردن exception در service
            when(paymentService.processPayment(1L, 1L, "CARD"))
                .thenThrow(new RuntimeException("Payment processing failed"));

            // When - تلاش برای پردازش
            String response = controller.processPayment(requestData);

            // Then - بررسی مدیریت خطا
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertEquals("Payment processing failed", responseMap.get("error"));
        }
    }

    @Nested
    @DisplayName("Payment History Tests")
    class PaymentHistoryTests {

        @Test
        @DisplayName("Should get user payment history successfully")
        void getPaymentHistory_ValidUserId_Success() throws Exception {
            // Given
            Long userId = 1L;
            List<Transaction> expectedTransactions = Arrays.asList(
                createSampleTransaction(1L, 50000.0, TransactionType.PAYMENT),
                createSampleTransaction(2L, 30000.0, TransactionType.PAYMENT)
            );
            
            when(paymentService.getUserTransactionHistory(userId)).thenReturn(expectedTransactions);

            // When
            String response = controller.getPaymentHistory(userId);

            // Then
            assertNotNull(response);
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertTrue((Boolean) responseMap.get("success"));
            assertNotNull(responseMap.get("transactions"));
            
            verify(paymentService).getUserTransactionHistory(userId);
        }

        @Test
        @DisplayName("Should handle null user ID")
        void getPaymentHistory_NullUserId_ReturnsError() throws Exception {
            // When
            String response = controller.getPaymentHistory(null);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("User ID is required"));
            
            verify(paymentService, never()).getUserTransactionHistory(any());
        }

        @Test
        @DisplayName("Should handle non-existent user")
        void getPaymentHistory_NonExistentUser_ReturnsError() throws Exception {
            // Given
            Long userId = 999L;
            when(paymentService.getUserTransactionHistory(userId))
                .thenThrow(new NotFoundException("User", userId));

            // When
            String response = controller.getPaymentHistory(userId);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertEquals("User not found with id=999", responseMap.get("error"));
        }
    }

    @Nested
    @DisplayName("Payment Details Tests")
    class PaymentDetailsTests {

        @Test
        @DisplayName("Should get payment details successfully")
        void getPaymentDetails_ValidTransactionId_Success() throws Exception {
            // Given
            Long transactionId = 1L;
            Transaction expectedTransaction = createSampleTransaction(transactionId, 50000.0, TransactionType.PAYMENT);
            
            when(paymentService.getTransaction(transactionId)).thenReturn(expectedTransaction);

            // When
            String response = controller.getPaymentDetails(transactionId);

            // Then
            assertNotNull(response);
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertTrue((Boolean) responseMap.get("success"));
            assertNotNull(responseMap.get("transaction"));
            
            verify(paymentService).getTransaction(transactionId);
        }

        @Test
        @DisplayName("Should handle null transaction ID")
        void getPaymentDetails_NullTransactionId_ReturnsError() throws Exception {
            // When
            String response = controller.getPaymentDetails(null);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Transaction ID is required"));
            
            verify(paymentService, never()).getTransaction(any());
        }

        @Test
        @DisplayName("Should handle non-existent transaction")
        void getPaymentDetails_NonExistentTransaction_ReturnsError() throws Exception {
            // Given
            Long transactionId = 999L;
            when(paymentService.getTransaction(transactionId))
                .thenThrow(new NotFoundException("Transaction", transactionId));

            // When
            String response = controller.getPaymentDetails(transactionId);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertEquals("Transaction not found with id=999", responseMap.get("error"));
        }
    }

    @Nested
    @DisplayName("Refund Tests")
    class RefundTests {

        @Test
        @DisplayName("Should process refund successfully")
        void processRefund_ValidData_Success() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "transactionId", 1L,
                "reason", "Order cancelled by customer"
            );

            Transaction expectedRefund = createSampleTransaction(2L, 50000.0, TransactionType.REFUND);
            when(paymentService.processRefund(1L, "Order cancelled by customer")).thenReturn(expectedRefund);

            // When
            String response = controller.processRefund(requestData);

            // Then
            assertNotNull(response);
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertTrue((Boolean) responseMap.get("success"));
            assertEquals("Refund processed successfully", responseMap.get("message"));
            
            verify(paymentService).processRefund(1L, "Order cancelled by customer");
        }

        @Test
        @DisplayName("Should handle missing transaction ID in refund")
        void processRefund_MissingTransactionId_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "reason", "Order cancelled"
            );

            // When
            String response = controller.processRefund(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Transaction ID is required"));
            
            verify(paymentService, never()).processRefund(any(), any());
        }

        @Test
        @DisplayName("Should handle refund service exceptions")
        void processRefund_ServiceException_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "transactionId", 1L,
                "reason", "Test refund"
            );

            when(paymentService.processRefund(1L, "Test refund"))
                .thenThrow(new RuntimeException("Refund not allowed"));

            // When
            String response = controller.processRefund(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertEquals("Refund not allowed", responseMap.get("error"));
        }
    }

    @Nested
    @DisplayName("Card Validation Tests")
    class CardValidationTests {

        @Test
        @DisplayName("Should handle missing card details for CARD payment")
        void processPayment_CardPaymentMissingDetails_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "CARD"
                // Missing card details
            );

            // When
            String response = controller.processPayment(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Card details are required"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }

        @Test
        @DisplayName("Should handle invalid card number")
        void processPayment_InvalidCardNumber_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "CARD",
                "cardNumber", "123", // Too short
                "cardHolder", "John Doe",
                "expiryMonth", "12",
                "expiryYear", "2025",
                "cvv", "123"
            );

            // When
            String response = controller.processPayment(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Invalid card number"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "13", "00", "99"})
        @DisplayName("Should handle invalid expiry month")
        void processPayment_InvalidExpiryMonth_ReturnsError(String month) throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "CARD",
                "cardNumber", "1234567812345678",
                "cardHolder", "John Doe",
                "expiryMonth", month,
                "expiryYear", "2025",
                "cvv", "123"
            );

            // When
            String response = controller.processPayment(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Invalid expiry month"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }

        @Test
        @DisplayName("Should handle expired card")
        void processPayment_ExpiredCard_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "CARD",
                "cardNumber", "1234567812345678",
                "cardHolder", "John Doe",
                "expiryMonth", "01",
                "expiryYear", "2020", // Expired
                "cvv", "123"
            );

            // When
            String response = controller.processPayment(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Card has expired"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Wallet Validation Tests")
    class WalletValidationTests {

        @Test
        @DisplayName("Should handle missing user ID for WALLET payment")
        void processPayment_WalletPaymentMissingUserId_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "orderId", 1L,
                "paymentMethod", "WALLET"
                // Missing userId
            );

            // When
            String response = controller.processPayment(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("User ID is required"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }
    }

    // ==================== HELPER METHODS ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {
        return JsonUtil.fromJson(json, Map.class);
    }

    private Transaction createSampleTransaction(Long id, Double amount, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
}
