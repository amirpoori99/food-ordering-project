package com.myapp.payment;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for WalletService
 * Tests all wallet operations, balance management, charging, withdrawal, and validation scenarios
 */
class WalletServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private AuthRepository authRepository;
    
    private WalletService walletService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        walletService = new WalletService(paymentRepository, authRepository);
    }

    // ==================== WALLET BALANCE TESTS ====================

    @Nested
    @DisplayName("Wallet Balance Tests")
    class WalletBalanceTests {

        @Test
        @DisplayName("Should get wallet balance successfully")
        void getWalletBalance_Success() {
            // Arrange
            Long userId = 1L;
            Double expectedBalance = 150.0;
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(paymentRepository.calculateWalletBalance(userId)).thenReturn(expectedBalance);
            
            // Act
            Double result = walletService.getWalletBalance(userId);
            
            // Assert
            assertEquals(expectedBalance, result);
            verify(authRepository).existsById(userId);
            verify(paymentRepository).calculateWalletBalance(userId);
        }

        @Test
        @DisplayName("Should throw exception for invalid user ID")
        void getWalletBalance_InvalidUserId() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> walletService.getWalletBalance(0L));
            
            assertEquals("User ID must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for non-existent user")
        void getWalletBalance_UserNotFound() {
            // Arrange
            Long userId = 999L;
            when(authRepository.existsById(userId)).thenReturn(false);
            
            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, 
                () -> walletService.getWalletBalance(userId));
            
            assertTrue(exception.getMessage().contains("User"));
        }

        @Test
        @DisplayName("Should check sufficient balance correctly")
        void hasSufficientBalance_Tests() {
            // Arrange
            Long userId = 1L;
            Double currentBalance = 100.0;
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(paymentRepository.calculateWalletBalance(userId)).thenReturn(currentBalance);
            
            // Valid scenarios
            assertTrue(walletService.hasSufficientBalance(userId, 50.0));
            assertTrue(walletService.hasSufficientBalance(userId, 100.0)); // Exact amount
            assertFalse(walletService.hasSufficientBalance(userId, 150.0)); // Insufficient
            
            // Invalid scenarios
            assertFalse(walletService.hasSufficientBalance(null, 50.0));
            assertFalse(walletService.hasSufficientBalance(userId, null));
            assertFalse(walletService.hasSufficientBalance(userId, 0.0));
            assertFalse(walletService.hasSufficientBalance(userId, -10.0));
        }
    }

    // ==================== WALLET CHARGING TESTS ====================

    @Nested
    @DisplayName("Wallet Charging Tests")
    class WalletChargingTests {

        @Test
        @DisplayName("Should charge wallet with card successfully")
        void chargeWalletWithCard_Success() {
            // Arrange
            Long userId = 1L;
            Double amount = 100.0;
            
            User user = createTestUser(userId);
            Transaction chargeTransaction = createTestTransaction(1L, userId, null, amount, TransactionType.WALLET_CHARGE, TransactionStatus.PENDING);
            
            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUserIdAndDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList()); // No previous charges today
            when(paymentRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });
            when(paymentRepository.update(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            Transaction result = walletService.chargeWalletWithCard(userId, amount);
            
            // Assert
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(amount, result.getAmount());
            assertEquals(TransactionType.WALLET_CHARGE, result.getType());
            assertEquals("CARD", result.getPaymentMethod());
            
            verify(paymentRepository).save(any(Transaction.class));
            verify(paymentRepository).update(any(Transaction.class));
        }

        @Test
        @DisplayName("Should charge wallet with bank transfer successfully")
        void chargeWalletWithBankTransfer_Success() {
            // Arrange
            Long userId = 1L;
            Double amount = 200.0;
            String transferReference = "BANK123456";
            
            User user = createTestUser(userId);
            Transaction chargeTransaction = createTestTransaction(1L, userId, null, amount, TransactionType.WALLET_CHARGE, TransactionStatus.PENDING);
            
            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUserIdAndDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
            when(paymentRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });
            when(paymentRepository.update(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            Transaction result = walletService.chargeWalletWithBankTransfer(userId, amount, transferReference);
            
            // Assert
            assertNotNull(result);
            assertEquals(TransactionType.WALLET_CHARGE, result.getType());
            assertEquals("BANK_TRANSFER", result.getPaymentMethod());
            assertTrue(result.getDescription().contains(transferReference));
        }

        @Test
        @DisplayName("Should throw exception for invalid charge amount")
        void chargeWallet_InvalidAmount() {
            // Arrange
            Long userId = 1L;
            
            // Act & Assert
            IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.chargeWallet(userId, 0.0, "CARD", "Test"));
            assertEquals("Charge amount must be positive", exception1.getMessage());
            
            IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.chargeWallet(userId, -10.0, "CARD", "Test"));
            assertEquals("Charge amount must be positive", exception2.getMessage());
            
            IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.chargeWallet(userId, 15000.0, "CARD", "Test"));
            assertEquals("Maximum charge amount is 10,000 per transaction", exception3.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when daily charge limit exceeded")
        void chargeWallet_DailyLimitExceeded() {
            // Arrange
            Long userId = 1L;
            Double amount = 1000.0;
            
            User user = createTestUser(userId);
            
            // Mock today's charges that exceed limit
            List<Transaction> todayCharges = Arrays.asList(
                createTestTransaction(1L, userId, null, 25000.0, TransactionType.WALLET_CHARGE, TransactionStatus.COMPLETED),
                createTestTransaction(2L, userId, null, 25000.0, TransactionType.WALLET_CHARGE, TransactionStatus.COMPLETED)
            );
            
            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUserIdAndDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(todayCharges);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> walletService.chargeWallet(userId, amount, "CARD", "Test"));
            
            assertTrue(exception.getMessage().contains("Daily charge limit exceeded"));
        }

        @Test
        @DisplayName("Should throw exception for unsupported payment method")
        void chargeWallet_UnsupportedPaymentMethod() {
            // Arrange
            Long userId = 1L;
            Double amount = 100.0;
            
            User user = createTestUser(userId);
            Transaction chargeTransaction = createTestTransaction(1L, userId, null, amount, TransactionType.WALLET_CHARGE, TransactionStatus.PENDING);
            
            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUserIdAndDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
            when(paymentRepository.save(any(Transaction.class))).thenReturn(chargeTransaction);
            when(paymentRepository.update(any(Transaction.class))).thenReturn(chargeTransaction);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> walletService.chargeWallet(userId, amount, "CRYPTO", "Test"));
            
            assertTrue(exception.getMessage().contains("Unsupported payment method for wallet charge"));
        }
    }

    // ==================== WALLET WITHDRAWAL TESTS ====================

    @Nested
    @DisplayName("Wallet Withdrawal Tests")
    class WalletWithdrawalTests {

        @Test
        @DisplayName("Should withdraw to bank successfully")
        void withdrawToBank_Success() {
            // Arrange
            Long userId = 1L;
            Double amount = 100.0;
            String bankAccount = "1234567890";
            String reason = "Emergency fund";
            
            User user = createTestUser(userId);
            Transaction withdrawalTransaction = createTestTransaction(1L, userId, null, amount, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.PENDING);
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(paymentRepository.calculateWalletBalance(userId)).thenReturn(200.0); // Sufficient balance
            when(paymentRepository.findByUserIdAndDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList()); // No previous withdrawals today
            when(paymentRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });
            when(paymentRepository.update(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            Transaction result = walletService.withdrawToBank(userId, amount, bankAccount, reason);
            
            // Assert
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(amount, result.getAmount());
            assertEquals(TransactionType.WALLET_WITHDRAWAL, result.getType());
            assertTrue(result.getDescription().contains(bankAccount));
            assertTrue(result.getDescription().contains(reason));
            
            verify(paymentRepository).save(any(Transaction.class));
            verify(paymentRepository).update(any(Transaction.class));
        }

        @Test
        @DisplayName("Should withdraw for payment successfully")
        void withdrawForPayment_Success() {
            // Arrange
            Long userId = 1L;
            Double amount = 50.0;
            String orderReference = "ORDER123";
            
            Transaction withdrawalTransaction = createTestTransaction(1L, userId, null, amount, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED);
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(paymentRepository.calculateWalletBalance(userId)).thenReturn(100.0); // Sufficient balance
            when(paymentRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });
            
            // Act
            Transaction result = walletService.withdrawForPayment(userId, amount, orderReference);
            
            // Assert
            assertNotNull(result);
            assertEquals(TransactionStatus.COMPLETED, result.getStatus());
            assertTrue(result.getDescription().contains(orderReference));
            
            verify(paymentRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw exception for insufficient balance")
        void withdrawToBank_InsufficientBalance() {
            // Arrange
            Long userId = 1L;
            Double amount = 200.0;
            String bankAccount = "1234567890";
            
            User user = createTestUser(userId);
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(paymentRepository.calculateWalletBalance(userId)).thenReturn(100.0); // Insufficient
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> walletService.withdrawToBank(userId, amount, bankAccount, "Test"));
            
            assertTrue(exception.getMessage().contains("Insufficient wallet balance"));
        }

        @Test
        @DisplayName("Should throw exception for invalid withdrawal amount")
        void withdrawToBank_InvalidAmount() {
            // Arrange
            Long userId = 1L;
            String bankAccount = "1234567890";
            
            // Act & Assert
            IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.withdrawToBank(userId, 5.0, bankAccount, "Test"));
            assertEquals("Minimum withdrawal amount is 10", exception1.getMessage());
            
            IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.withdrawToBank(userId, 6000.0, bankAccount, "Test"));
            assertEquals("Maximum withdrawal amount is 5,000 per transaction", exception2.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when daily withdrawal limit exceeded")
        void withdrawToBank_DailyLimitExceeded() {
            // Arrange
            Long userId = 1L;
            Double amount = 1000.0;
            String bankAccount = "1234567890";
            
            User user = createTestUser(userId);
            
            // Mock today's withdrawals that exceed limit
            List<Transaction> todayWithdrawals = Arrays.asList(
                createTestTransaction(1L, userId, null, 10000.0, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED),
                createTestTransaction(2L, userId, null, 10000.0, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED)
            );
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(paymentRepository.calculateWalletBalance(userId)).thenReturn(25000.0); // Sufficient balance
            when(paymentRepository.findByUserIdAndDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(todayWithdrawals);
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> walletService.withdrawToBank(userId, amount, bankAccount, "Test"));
            
            assertTrue(exception.getMessage().contains("Daily withdrawal limit exceeded"));
        }
    }

    // ==================== WALLET TRANSACTION HISTORY TESTS ====================

    @Nested
    @DisplayName("Wallet Transaction History Tests")
    class WalletTransactionHistoryTests {

        @Test
        @DisplayName("Should get wallet transaction history successfully")
        void getWalletTransactionHistory_Success() {
            // Arrange
            Long userId = 1L;
            List<Transaction> walletTransactions = Arrays.asList(
                createTestTransaction(1L, userId, null, 100.0, TransactionType.WALLET_CHARGE, TransactionStatus.COMPLETED),
                createTestTransaction(2L, userId, null, 50.0, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED)
            );
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(paymentRepository.findWalletTransactions(userId)).thenReturn(walletTransactions);
            
            // Act
            List<Transaction> result = walletService.getWalletTransactionHistory(userId);
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(paymentRepository).findWalletTransactions(userId);
        }

        @Test
        @DisplayName("Should get wallet transaction history with date range successfully")
        void getWalletTransactionHistoryWithDateRange_Success() {
            // Arrange
            Long userId = 1L;
            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();
            
            List<Transaction> allTransactions = Arrays.asList(
                createTestTransaction(1L, userId, null, 100.0, TransactionType.WALLET_CHARGE, TransactionStatus.COMPLETED),
                createTestTransaction(2L, userId, 1L, 50.0, TransactionType.PAYMENT, TransactionStatus.COMPLETED),
                createTestTransaction(3L, userId, null, 25.0, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED)
            );
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(paymentRepository.findByUserIdAndDateRange(userId, startDate, endDate)).thenReturn(allTransactions);
            
            // Act
            List<Transaction> result = walletService.getWalletTransactionHistory(userId, startDate, endDate);
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.size()); // Only wallet transactions
            assertTrue(result.stream().allMatch(t -> 
                t.getType() == TransactionType.WALLET_CHARGE || t.getType() == TransactionType.WALLET_WITHDRAWAL));
        }

        @Test
        @DisplayName("Should get wallet charge history successfully")
        void getWalletChargeHistory_Success() {
            // Arrange
            Long userId = 1L;
            List<Transaction> chargeTransactions = Arrays.asList(
                createTestTransaction(1L, userId, null, 100.0, TransactionType.WALLET_CHARGE, TransactionStatus.COMPLETED),
                createTestTransaction(2L, userId, null, 200.0, TransactionType.WALLET_CHARGE, TransactionStatus.COMPLETED)
            );
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(paymentRepository.findByUserIdAndType(userId, TransactionType.WALLET_CHARGE)).thenReturn(chargeTransactions);
            
            // Act
            List<Transaction> result = walletService.getWalletChargeHistory(userId);
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(t -> t.getType() == TransactionType.WALLET_CHARGE));
        }

        @Test
        @DisplayName("Should get wallet withdrawal history successfully")
        void getWalletWithdrawalHistory_Success() {
            // Arrange
            Long userId = 1L;
            List<Transaction> withdrawalTransactions = Arrays.asList(
                createTestTransaction(1L, userId, null, 50.0, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED),
                createTestTransaction(2L, userId, null, 75.0, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED)
            );
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(paymentRepository.findByUserIdAndType(userId, TransactionType.WALLET_WITHDRAWAL)).thenReturn(withdrawalTransactions);
            
            // Act
            List<Transaction> result = walletService.getWalletWithdrawalHistory(userId);
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(t -> t.getType() == TransactionType.WALLET_WITHDRAWAL));
        }
    }

    // ==================== WALLET STATISTICS TESTS ====================

    @Nested
    @DisplayName("Wallet Statistics Tests")
    class WalletStatisticsTests {

        @Test
        @DisplayName("Should get wallet statistics successfully")
        void getWalletStatistics_Success() {
            // Arrange
            Long userId = 1L;
            List<Transaction> walletTransactions = Arrays.asList(
                createTestTransaction(1L, userId, null, 100.0, TransactionType.WALLET_CHARGE, TransactionStatus.COMPLETED),
                createTestTransaction(2L, userId, null, 200.0, TransactionType.WALLET_CHARGE, TransactionStatus.COMPLETED),
                createTestTransaction(3L, userId, null, 50.0, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED),
                createTestTransaction(4L, userId, null, 25.0, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED),
                createTestTransaction(5L, userId, null, 150.0, TransactionType.WALLET_CHARGE, TransactionStatus.PENDING) // Should not count
            );
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(paymentRepository.findWalletTransactions(userId)).thenReturn(walletTransactions);
            when(paymentRepository.calculateWalletBalance(userId)).thenReturn(225.0); // 300 charged - 75 withdrawn
            
            // Act
            WalletService.WalletStatistics result = walletService.getWalletStatistics(userId);
            
            // Assert
            assertNotNull(result);
            assertEquals(225.0, result.getCurrentBalance());
            assertEquals(300.0, result.getTotalCharged()); // Only completed charges
            assertEquals(75.0, result.getTotalWithdrawn()); // Only completed withdrawals
            assertEquals(3L, result.getTotalChargeTransactions()); // All charge transactions (including pending)
            assertEquals(2L, result.getTotalWithdrawalTransactions());
            assertEquals(225.0, result.getNetFlow()); // 300 - 75
            assertEquals(5L, result.getTotalTransactions()); // All transactions
        }
    }

    // ==================== ADMIN OPERATIONS TESTS ====================

    @Nested
    @DisplayName("Admin Operations Tests")
    class AdminOperationsTests {

        @Test
        @DisplayName("Should admin credit wallet successfully")
        void adminCreditWallet_Success() {
            // Arrange
            Long userId = 1L;
            Long adminId = 2L;
            Double amount = 100.0;
            String reason = "Compensation";
            
            User user = createTestUser(userId);
            User admin = createTestUser(adminId);
            Transaction creditTransaction = createTestTransaction(1L, userId, null, amount, TransactionType.WALLET_CHARGE, TransactionStatus.PENDING);
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(authRepository.existsById(adminId)).thenReturn(true);
            when(authRepository.findById(userId)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUserIdAndDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
            when(paymentRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });
            when(paymentRepository.update(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // Act
            Transaction result = walletService.adminCreditWallet(userId, amount, reason, adminId);
            
            // Assert
            assertNotNull(result);
            assertEquals(TransactionType.WALLET_CHARGE, result.getType());
            assertEquals("ADMIN_CREDIT", result.getPaymentMethod());
            assertTrue(result.getDescription().contains(String.valueOf(adminId)));
            assertTrue(result.getDescription().contains(reason));
        }

        @Test
        @DisplayName("Should admin debit wallet successfully")
        void adminDebitWallet_Success() {
            // Arrange
            Long userId = 1L;
            Long adminId = 2L;
            Double amount = 50.0;
            String reason = "Penalty";
            
            Transaction debitTransaction = createTestTransaction(1L, userId, null, amount, TransactionType.WALLET_WITHDRAWAL, TransactionStatus.COMPLETED);
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(authRepository.existsById(adminId)).thenReturn(true);
            when(paymentRepository.calculateWalletBalance(userId)).thenReturn(100.0); // Sufficient balance
            when(paymentRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });
            
            // Act
            Transaction result = walletService.adminDebitWallet(userId, amount, reason, adminId);
            
            // Assert
            assertNotNull(result);
            assertEquals(TransactionType.WALLET_WITHDRAWAL, result.getType());
            assertTrue(result.getDescription().contains(String.valueOf(adminId)));
            assertTrue(result.getDescription().contains(reason));
            assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when admin debit exceeds balance")
        void adminDebitWallet_InsufficientBalance() {
            // Arrange
            Long userId = 1L;
            Long adminId = 2L;
            Double amount = 150.0;
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(authRepository.existsById(adminId)).thenReturn(true);
            when(paymentRepository.calculateWalletBalance(userId)).thenReturn(100.0); // Insufficient
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> walletService.adminDebitWallet(userId, amount, "Test", adminId));
            
            assertTrue(exception.getMessage().contains("Insufficient wallet balance for debit"));
        }

        @Test
        @DisplayName("Should throw exception for non-existent admin")
        void adminOperations_AdminNotFound() {
            // Arrange
            Long userId = 1L;
            Long adminId = 999L;
            Double amount = 100.0;
            
            when(authRepository.existsById(userId)).thenReturn(true);
            when(authRepository.existsById(adminId)).thenReturn(false);
            
            // Act & Assert
            NotFoundException exception1 = assertThrows(NotFoundException.class, 
                () -> walletService.adminCreditWallet(userId, amount, "Test", adminId));
            assertTrue(exception1.getMessage().contains("Admin"));
            
            NotFoundException exception2 = assertThrows(NotFoundException.class, 
                () -> walletService.adminDebitWallet(userId, amount, "Test", adminId));
            assertTrue(exception2.getMessage().contains("Admin"));
        }
    }

    // ==================== VALIDATION TESTS ====================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate input parameters correctly")
        void validateInputParameters() {
            // Test null and invalid user IDs
            IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.getWalletBalance(null));
            assertEquals("User ID must be positive", exception1.getMessage());
            
            IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.getWalletBalance(-1L));
            assertEquals("User ID must be positive", exception2.getMessage());
            
            // Test null amounts
            IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.chargeWallet(1L, null, "CARD", "Test"));
            assertEquals("Charge amount must be positive", exception3.getMessage());
            
            // Test empty payment method
            IllegalArgumentException exception4 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.chargeWallet(1L, 100.0, "", "Test"));
            assertEquals("Payment method cannot be empty", exception4.getMessage());
            
            // Test empty bank account
            IllegalArgumentException exception5 = assertThrows(IllegalArgumentException.class, 
                () -> walletService.withdrawToBank(1L, 100.0, "", "Test"));
            assertEquals("Bank account cannot be empty", exception5.getMessage());
        }

        @Test
        @DisplayName("Should validate date range correctly")
        void validateDateRange() {
            // Arrange
            Long userId = 1L;
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = LocalDateTime.now().minusDays(1); // End before start
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> walletService.getWalletTransactionHistory(userId, startDate, endDate));
            
            assertEquals("Start date cannot be after end date", exception.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setFullName("Test User " + id);
        user.setPhone("123456789" + id);
        user.setPasswordHash("password");
        return user;
    }

    private Transaction createTestTransaction(Long id, Long userId, Long orderId, Double amount, 
                                           TransactionType type, TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setUserId(userId);
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(status);
        transaction.setCreatedAt(LocalDateTime.now());
        
        if (type == TransactionType.WALLET_CHARGE || type == TransactionType.WALLET_WITHDRAWAL) {
            transaction.setPaymentMethod("WALLET");
        } else {
            transaction.setPaymentMethod("CARD");
        }
        
        // Set description based on transaction type
        if (type == TransactionType.WALLET_CHARGE) {
            transaction.setDescription("Test wallet charge");
        } else if (type == TransactionType.WALLET_WITHDRAWAL) {
            transaction.setDescription("Test wallet withdrawal");
        } else {
            transaction.setDescription("Test transaction");
        }
        
        return transaction;
    }
} 