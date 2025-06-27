package com.myapp.payment;

import com.myapp.common.models.Transaction;
import com.myapp.common.models.TransactionType;
import com.myapp.common.exceptions.InsufficientFundsException;
import com.myapp.common.exceptions.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * تست‌های جامع WalletController
 * این کلاس تمام عملیات مربوط به کنترلر کیف پول را آزمایش می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Wallet Controller Tests")
class WalletControllerTest {

    private WalletController walletController;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = Mockito.mock(WalletService.class);
        walletController = new WalletController(walletService);
    }

    @Nested
    @DisplayName("Wallet Balance Tests")
    class WalletBalanceTests {

        @Test
        @DisplayName("Get wallet balance should delegate to service")
        void getWalletBalance_shouldDelegateToService() {
            // Given
            Long userId = 1L;
            BigDecimal expectedBalance = new BigDecimal("150.00");
            when(walletService.getBalance(userId)).thenReturn(expectedBalance);

            // When
            BigDecimal actualBalance = walletController.getBalance(userId);

            // Then
            verify(walletService).getBalance(userId);
            assertThat(actualBalance).isEqualTo(expectedBalance);
        }

        @Test
        @DisplayName("Get balance for non-existent user should propagate exception")
        void getBalance_nonExistentUser_shouldPropagateException() {
            // Given
            Long userId = 999L;
            when(walletService.getBalance(userId)).thenThrow(new NotFoundException("User", userId));

            // When & Then
            assertThatThrownBy(() -> walletController.getBalance(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");

            verify(walletService).getBalance(userId);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.00", "25.50", "100.00", "999.99"})
        @DisplayName("Various balance amounts should be handled correctly")
        void variousBalanceAmounts_shouldBeHandledCorrectly(String balanceStr) {
            // Given
            Long userId = 2L;
            BigDecimal balance = new BigDecimal(balanceStr);
            when(walletService.getBalance(userId)).thenReturn(balance);

            // When
            BigDecimal result = walletController.getBalance(userId);

            // Then
            assertThat(result).isEqualTo(balance);
            verify(walletService).getBalance(userId);
        }
    }

    @Nested
    @DisplayName("Wallet Credit Tests")
    class WalletCreditTests {

        @Test
        @DisplayName("Credit wallet should delegate to service")
        void creditWallet_shouldDelegateToService() {
            // Given
            Long userId = 3L;
            BigDecimal amount = new BigDecimal("50.00");
            String description = "شارژ کیف پول";
            Transaction expectedTransaction = createMockTransaction(userId, amount, TransactionType.CREDIT, description);
            when(walletService.creditWallet(userId, amount, description)).thenReturn(expectedTransaction);

            // When
            Transaction result = walletController.creditWallet(userId, amount, description);

            // Then
            verify(walletService).creditWallet(userId, amount, description);
            assertThat(result).isEqualTo(expectedTransaction);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getAmount()).isEqualTo(amount.doubleValue());
            assertThat(result.getType()).isEqualTo(TransactionType.CREDIT);
        }

        @Test
        @DisplayName("Credit with negative amount should propagate exception")
        void credit_negativeAmount_shouldPropagateException() {
            // Given
            Long userId = 4L;
            BigDecimal negativeAmount = new BigDecimal("-10.00");
            String description = "شارژ منفی";
            when(walletService.creditWallet(userId, negativeAmount, description))
                .thenThrow(new IllegalArgumentException("Amount must be positive"));

            // When & Then
            assertThatThrownBy(() -> walletController.creditWallet(userId, negativeAmount, description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

            verify(walletService).creditWallet(userId, negativeAmount, description);
        }

        @ParameterizedTest
        @ValueSource(strings = {"1.00", "10.50", "100.00", "500.00"})
        @DisplayName("Credit with various amounts should work")
        void creditWithVariousAmounts_shouldWork(String amountStr) {
            // Given
            Long userId = 5L;
            BigDecimal amount = new BigDecimal(amountStr);
            String description = "شارژ " + amountStr;
            Transaction mockTransaction = createMockTransaction(userId, amount, TransactionType.CREDIT, description);
            when(walletService.creditWallet(userId, amount, description)).thenReturn(mockTransaction);

            // When
            Transaction result = walletController.creditWallet(userId, amount, description);

            // Then
            assertThat(result.getAmount()).isEqualTo(amount.doubleValue());
            verify(walletService).creditWallet(userId, amount, description);
        }
    }

    @Nested
    @DisplayName("Wallet Debit Tests")
    class WalletDebitTests {

        @Test
        @DisplayName("Debit wallet should delegate to service")
        void debitWallet_shouldDelegateToService() {
            // Given
            Long userId = 6L;
            BigDecimal amount = new BigDecimal("30.00");
            String description = "پرداخت سفارش";
            Transaction expectedTransaction = createMockTransaction(userId, amount, TransactionType.DEBIT, description);
            when(walletService.debitWallet(userId, amount, description)).thenReturn(expectedTransaction);

            // When
            Transaction result = walletController.debitWallet(userId, amount, description);

            // Then
            verify(walletService).debitWallet(userId, amount, description);
            assertThat(result).isEqualTo(expectedTransaction);
            assertThat(result.getType()).isEqualTo(TransactionType.DEBIT);
        }

        @Test
        @DisplayName("Debit with insufficient funds should propagate exception")
        void debit_insufficientFunds_shouldPropagateException() {
            // Given
            Long userId = 7L;
            BigDecimal amount = new BigDecimal("100.00");
            String description = "پرداخت بیش از حد";
            when(walletService.debitWallet(userId, amount, description))
                .thenThrow(new InsufficientFundsException("Insufficient funds in wallet"));

            // When & Then
            assertThatThrownBy(() -> walletController.debitWallet(userId, amount, description))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");

            verify(walletService).debitWallet(userId, amount, description);
        }
    }

    @Nested
    @DisplayName("Transaction History Tests")
    class TransactionHistoryTests {

        @Test
        @DisplayName("Get transaction history should delegate to service")
        void getTransactionHistory_shouldDelegateToService() {
            // Given
            Long userId = 8L;
            List<Transaction> expectedTransactions = Arrays.asList(
                createMockTransaction(userId, new BigDecimal("50.00"), TransactionType.CREDIT, "شارژ"),
                createMockTransaction(userId, new BigDecimal("20.00"), TransactionType.DEBIT, "خرید")
            );
            when(walletService.getTransactionHistory(userId)).thenReturn(expectedTransactions);

            // When
            List<Transaction> result = walletController.getTransactionHistory(userId);

            // Then
            verify(walletService).getTransactionHistory(userId);
            assertThat(result).isEqualTo(expectedTransactions);
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getType()).isEqualTo(TransactionType.CREDIT);
            assertThat(result.get(1).getType()).isEqualTo(TransactionType.DEBIT);
        }

        @Test
        @DisplayName("Get empty transaction history should work")
        void getEmptyTransactionHistory_shouldWork() {
            // Given
            Long userId = 9L;
            List<Transaction> emptyList = Arrays.asList();
            when(walletService.getTransactionHistory(userId)).thenReturn(emptyList);

            // When
            List<Transaction> result = walletController.getTransactionHistory(userId);

            // Then
            verify(walletService).getTransactionHistory(userId);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Transfer Operations Tests")
    class TransferOperationsTests {

        @Test
        @DisplayName("Transfer between wallets should delegate to service")
        void transferBetweenWallets_shouldDelegateToService() {
            // Given
            Long fromUserId = 10L;
            Long toUserId = 11L;
            BigDecimal amount = new BigDecimal("25.00");
            String description = "انتقال وجه";
            Transaction expectedTransaction = createMockTransaction(fromUserId, amount, TransactionType.TRANSFER, description);
            when(walletService.transfer(fromUserId, toUserId, amount, description)).thenReturn(expectedTransaction);

            // When
            Transaction result = walletController.transfer(fromUserId, toUserId, amount, description);

            // Then
            verify(walletService).transfer(fromUserId, toUserId, amount, description);
            assertThat(result).isEqualTo(expectedTransaction);
            assertThat(result.getType()).isEqualTo(TransactionType.TRANSFER);
        }

        @Test
        @DisplayName("Transfer with insufficient funds should propagate exception")
        void transfer_insufficientFunds_shouldPropagateException() {
            // Given
            Long fromUserId = 12L;
            Long toUserId = 13L;
            BigDecimal amount = new BigDecimal("1000.00");
            String description = "انتقال بیش از حد";
            when(walletService.transfer(fromUserId, toUserId, amount, description))
                .thenThrow(new InsufficientFundsException("Insufficient funds for transfer"));

            // When & Then
            assertThatThrownBy(() -> walletController.transfer(fromUserId, toUserId, amount, description))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("transfer");

            verify(walletService).transfer(fromUserId, toUserId, amount, description);
        }

        @Test
        @DisplayName("Transfer to same user should propagate exception")
        void transferToSameUser_shouldPropagateException() {
            // Given
            Long userId = 14L;
            BigDecimal amount = new BigDecimal("50.00");
            String description = "انتقال به خود";
            when(walletService.transfer(userId, userId, amount, description))
                .thenThrow(new IllegalArgumentException("Cannot transfer to same user"));

            // When & Then
            assertThatThrownBy(() -> walletController.transfer(userId, userId, amount, description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same user");

            verify(walletService).transfer(userId, userId, amount, description);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Null user ID should be handled gracefully")
        void nullUserId_shouldBeHandledGracefully() {
            // When & Then
            assertThatThrownBy(() -> walletController.getBalance(null))
                .isInstanceOf(IllegalArgumentException.class);

            // Service should not be called with null
            verify(walletService, never()).getBalance(any());
        }

        @Test
        @DisplayName("Service exceptions should be properly propagated")
        void serviceExceptions_shouldBeProperlyPropagated() {
            // Given
            Long userId = 15L;
            when(walletService.getBalance(userId))
                .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> walletController.getBalance(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");

            verify(walletService).getBalance(userId);
        }
    }

    // Helper method برای ایجاد mock transaction
    private Transaction createMockTransaction(Long userId, BigDecimal amount, TransactionType type, String description) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUserId(userId);
        transaction.setAmount(amount.doubleValue());  // Convert to Double
        transaction.setType(type);
        transaction.setDescription(description);
        return transaction;
    }
} 