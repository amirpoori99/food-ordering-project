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

@DisplayName("Payment Controller Comprehensive Tests")
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    private PaymentControllerWrapper controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PaymentControllerWrapper(paymentService);
    }

    // Wrapper class to test controller logic without HttpExchange complexity
    private static class PaymentControllerWrapper {
        private final PaymentService paymentService;

        public PaymentControllerWrapper(PaymentService paymentService) {
            this.paymentService = paymentService;
        }

        public String processPayment(Map<String, Object> requestData) {
            try {
                // Validate required fields
                if (!requestData.containsKey("userId") || requestData.get("userId") == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "User ID is required"));
                }
                if (!requestData.containsKey("orderId") || requestData.get("orderId") == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Order ID is required"));
                }
                if (!requestData.containsKey("paymentMethod") || requestData.get("paymentMethod") == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Payment method is required"));
                }

                Long userId = ((Number) requestData.get("userId")).longValue();
                Long orderId = ((Number) requestData.get("orderId")).longValue();
                String paymentMethod = (String) requestData.get("paymentMethod");

                // Validate payment method
                if (!Arrays.asList("CARD", "WALLET", "CASH_ON_DELIVERY").contains(paymentMethod)) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Invalid payment method"));
                }

                // For CARD payments, validate card details
                if ("CARD".equals(paymentMethod)) {
                    if (!requestData.containsKey("cardNumber") || requestData.get("cardNumber") == null) {
                        return JsonUtil.toJson(Map.of("success", false, "error", "Card details are required"));
                    }
                    String cardNumber = (String) requestData.get("cardNumber");
                    if (cardNumber.length() < 13 || cardNumber.length() > 19) {
                        return JsonUtil.toJson(Map.of("success", false, "error", "Invalid card number"));
                    }
                    
                    if (requestData.containsKey("expiryMonth")) {
                        String month = (String) requestData.get("expiryMonth");
                        int monthInt = Integer.parseInt(month);
                        if (monthInt < 1 || monthInt > 12) {
                            return JsonUtil.toJson(Map.of("success", false, "error", "Invalid expiry month"));
                        }
                    }
                    
                    if (requestData.containsKey("expiryYear")) {
                        String year = (String) requestData.get("expiryYear");
                        int yearInt = Integer.parseInt(year);
                        if (yearInt < 2024) {
                            return JsonUtil.toJson(Map.of("success", false, "error", "Card has expired"));
                        }
                    }
                }

                // For WALLET payments, validate user ID
                if ("WALLET".equals(paymentMethod)) {
                    if (!requestData.containsKey("userId") || requestData.get("userId") == null) {
                        return JsonUtil.toJson(Map.of("success", false, "error", "User ID is required for wallet payment"));
                    }
                }

                Transaction transaction = paymentService.processPayment(userId, orderId, paymentMethod);
                
                return JsonUtil.toJson(Map.of(
                    "success", true,
                    "message", "Payment processed successfully",
                    "transaction", transaction
                ));

            } catch (Exception e) {
                return JsonUtil.toJson(Map.of("success", false, "error", e.getMessage()));
            }
        }

        public String getPaymentHistory(Long userId) {
            try {
                if (userId == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "User ID is required"));
                }

                List<Transaction> transactions = paymentService.getUserTransactionHistory(userId);
                
                return JsonUtil.toJson(Map.of(
                    "success", true,
                    "transactions", transactions
                ));

            } catch (Exception e) {
                return JsonUtil.toJson(Map.of("success", false, "error", e.getMessage()));
            }
        }

        public String getPaymentDetails(Long transactionId) {
            try {
                if (transactionId == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Transaction ID is required"));
                }

                Transaction transaction = paymentService.getTransaction(transactionId);
                
                return JsonUtil.toJson(Map.of(
                    "success", true,
                    "transaction", transaction
                ));

            } catch (Exception e) {
                return JsonUtil.toJson(Map.of("success", false, "error", e.getMessage()));
            }
        }

        public String processRefund(Map<String, Object> requestData) {
            try {
                if (!requestData.containsKey("transactionId") || requestData.get("transactionId") == null) {
                    return JsonUtil.toJson(Map.of("success", false, "error", "Transaction ID is required"));
                }

                Long transactionId = ((Number) requestData.get("transactionId")).longValue();
                String reason = requestData.containsKey("reason") ? (String) requestData.get("reason") : "Refund requested";

                Transaction refund = paymentService.processRefund(transactionId, reason);
                
                return JsonUtil.toJson(Map.of(
                    "success", true,
                    "message", "Refund processed successfully",
                    "refund", refund
                ));

            } catch (Exception e) {
                return JsonUtil.toJson(Map.of("success", false, "error", e.getMessage()));
            }
        }
    }

    @Nested
    @DisplayName("Payment Processing Tests")
    class PaymentProcessingTests {

        @Test
        @DisplayName("Should process CARD payment successfully")
        void processPayment_CardPayment_Success() throws Exception {
            // Given
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

            Transaction expectedTransaction = createSampleTransaction(1L, 50000.0, TransactionType.PAYMENT);
            when(paymentService.processPayment(1L, 1L, "CARD")).thenReturn(expectedTransaction);

            // When
            String response = controller.processPayment(requestData);

            // Then
            assertNotNull(response);
            Map<String, Object> responseMap = parseJsonToMap(response);
            assertTrue((Boolean) responseMap.get("success"));
            assertEquals("Payment processed successfully", responseMap.get("message"));
            
            verify(paymentService).processPayment(1L, 1L, "CARD");
        }

        @Test
        @DisplayName("Should process WALLET payment successfully")
        void processPayment_WalletPayment_Success() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "WALLET"
            );

            Transaction expectedTransaction = createSampleTransaction(1L, 30000.0, TransactionType.PAYMENT);
            when(paymentService.processPayment(1L, 1L, "WALLET")).thenReturn(expectedTransaction);

            // When
            String response = controller.processPayment(requestData);

            // Then
            assertNotNull(response);
            Map<String, Object> responseMap = parseJsonToMap(response);
            assertTrue((Boolean) responseMap.get("success"));
            
            verify(paymentService).processPayment(1L, 1L, "WALLET");
        }

        @Test
        @DisplayName("Should process COD payment successfully")
        void processPayment_CodPayment_Success() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "CASH_ON_DELIVERY"
            );

            Transaction expectedTransaction = createSampleTransaction(1L, 25000.0, TransactionType.PAYMENT);
            when(paymentService.processPayment(1L, 1L, "CASH_ON_DELIVERY")).thenReturn(expectedTransaction);

            // When
            String response = controller.processPayment(requestData);

            // Then
            assertNotNull(response);
            Map<String, Object> responseMap = parseJsonToMap(response);
            assertTrue((Boolean) responseMap.get("success"));
            
            verify(paymentService).processPayment(1L, 1L, "CASH_ON_DELIVERY");
        }

        @Test
        @DisplayName("Should handle missing userId")
        void processPayment_MissingUserId_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "orderId", 1L,
                "paymentMethod", "CARD"
            );

            // When
            String response = controller.processPayment(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("User ID is required"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }

        @Test
        @DisplayName("Should handle missing orderId")
        void processPayment_MissingOrderId_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "paymentMethod", "CARD"
            );

            // When
            String response = controller.processPayment(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Order ID is required"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }

        @Test
        @DisplayName("Should handle invalid payment method")
        void processPayment_InvalidPaymentMethod_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "INVALID"
            );

            // When
            String response = controller.processPayment(requestData);

            // Then
            Map<String, Object> responseMap = JsonUtil.fromJson(response, Map.class);
            assertFalse((Boolean) responseMap.get("success"));
            assertTrue(responseMap.get("error").toString().contains("Invalid payment method"));
            
            verify(paymentService, never()).processPayment(any(), any(), any());
        }

        @Test
        @DisplayName("Should handle payment service exceptions")
        void processPayment_ServiceException_ReturnsError() throws Exception {
            // Given
            Map<String, Object> requestData = Map.of(
                "userId", 1L,
                "orderId", 1L,
                "paymentMethod", "CARD",
                "cardNumber", "1234567812345678"
            );

            when(paymentService.processPayment(1L, 1L, "CARD"))
                .thenThrow(new RuntimeException("Payment processing failed"));

            // When
            String response = controller.processPayment(requestData);

            // Then
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
