package com.myapp.common;

import com.myapp.common.exceptions.*;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * تست‌های جامع برای کلاس‌های Exception
 * بررسی صحت عملکرد و پیام‌های خطا
 */
@DisplayName("Exception Classes Tests")
class ExceptionClassesTest {

    @Nested
    @DisplayName("InvalidCredentialsException Tests")
    class InvalidCredentialsExceptionTests {

        @Test
        @DisplayName("Should create exception with default message")
        void shouldCreateException_withDefaultMessage() {
            // Given & When
            InvalidCredentialsException exception = new InvalidCredentialsException();

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Invalid phone or password");
        }

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            assertThatThrownBy(() -> {
                throw new InvalidCredentialsException();
            }).isInstanceOf(InvalidCredentialsException.class)
              .hasMessage("Invalid phone or password");
        }

        @Test
        @DisplayName("Should have consistent message for security")
        void shouldHaveConsistentMessage_forSecurity() {
            InvalidCredentialsException exception1 = new InvalidCredentialsException();
            InvalidCredentialsException exception2 = new InvalidCredentialsException();

            assertThat(exception1.getMessage()).isEqualTo(exception2.getMessage());
        }
    }

    @Nested
    @DisplayName("NotFoundException Tests")
    class NotFoundExceptionTests {

        @Test
        @DisplayName("Should create exception with entity and id")
        void shouldCreateException_withEntityAndId() {
            // Given
            String entity = "User";
            Object id = 123;

            // When
            NotFoundException exception = new NotFoundException(entity, id);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("User not found with id=123");
        }

        @Test
        @DisplayName("Should handle different id types")
        void shouldHandleDifferentIdTypes() {
            // Test with String ID
            NotFoundException stringIdException = new NotFoundException("Restaurant", "rest_001");
            assertThat(stringIdException.getMessage()).isEqualTo("Restaurant not found with id=rest_001");

            // Test with Long ID
            NotFoundException longIdException = new NotFoundException("Order", 456L);
            assertThat(longIdException.getMessage()).isEqualTo("Order not found with id=456");

            // Test with null ID
            NotFoundException nullIdException = new NotFoundException("Item", null);
            assertThat(nullIdException.getMessage()).isEqualTo("Item");
        }

        @Test
        @DisplayName("Should be throwable with proper message")
        void shouldBeThrowable_withProperMessage() {
            assertThatThrownBy(() -> {
                throw new NotFoundException("Payment", 789);
            }).isInstanceOf(NotFoundException.class)
              .hasMessage("Payment not found with id=789");
        }

        @Test
        @DisplayName("Should handle empty entity name")
        void shouldHandleEmptyEntityName() {
            NotFoundException exception = new NotFoundException("", 123);
            assertThat(exception.getMessage()).isEqualTo(" not found with id=123");
        }
    }

    @Nested
    @DisplayName("InsufficientFundsException Tests")
    class InsufficientFundsExceptionTests {

        @Test
        @DisplayName("Should create exception with default constructor")
        void shouldCreateException_withDefaultConstructor() {
            // When
            InsufficientFundsException exception = new InsufficientFundsException();

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Insufficient funds");
        }

        @Test
        @DisplayName("Should create exception with custom message")
        void shouldCreateException_withCustomMessage() {
            // Given
            String customMessage = "Not enough money in wallet";

            // When
            InsufficientFundsException exception = new InsufficientFundsException(customMessage);

            // Then
            assertThat(exception.getMessage()).isEqualTo(customMessage);
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateException_withMessageAndCause() {
            // Given
            String message = "Payment failed due to insufficient funds";
            RuntimeException cause = new RuntimeException("Database connection failed");

            // When
            InsufficientFundsException exception = new InsufficientFundsException(message, cause);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create exception for wallet with balance info")
        void shouldCreateException_forWalletWithBalanceInfo() {
            // Given
            double currentBalance = 50.75;
            double requiredAmount = 100.00;

            // When
            InsufficientFundsException exception = InsufficientFundsException.forWallet(currentBalance, requiredAmount);

            // Then
            assertThat(exception.getMessage())
                .isEqualTo("Insufficient wallet balance. Current: 50.75, Required: 100.00");
        }

        @Test
        @DisplayName("Should create exception for amount only")
        void shouldCreateException_forAmountOnly() {
            // Given
            double requiredAmount = 75.50;

            // When
            InsufficientFundsException exception = InsufficientFundsException.forAmount(requiredAmount);

            // Then
            assertThat(exception.getMessage())
                .isEqualTo("Insufficient wallet balance for amount: 75.50");
        }

        @Test
        @DisplayName("Should handle zero and negative amounts")
        void shouldHandleZeroAndNegativeAmounts() {
            // Test with zero amounts
            InsufficientFundsException zeroException = InsufficientFundsException.forWallet(0.0, 0.0);
            assertThat(zeroException.getMessage())
                .isEqualTo("Insufficient wallet balance. Current: 0.00, Required: 0.00");

            // Test with negative amounts (edge case)
            InsufficientFundsException negativeException = InsufficientFundsException.forAmount(-10.0);
            assertThat(negativeException.getMessage())
                .isEqualTo("Insufficient wallet balance for amount: -10.00");
        }

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            assertThatThrownBy(() -> {
                throw InsufficientFundsException.forWallet(10.0, 20.0);
            }).isInstanceOf(InsufficientFundsException.class)
              .hasMessageContaining("Current: 10.00")
              .hasMessageContaining("Required: 20.00");
        }
    }

    @Nested
    @DisplayName("DuplicatePhoneException Tests")
    class DuplicatePhoneExceptionTests {

        @Test
        @DisplayName("Should create exception with phone number")
        void shouldCreateException_withPhoneNumber() {
            // Given
            String phone = "09123456789";

            // When
            DuplicatePhoneException exception = new DuplicatePhoneException(phone);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception.getMessage()).isEqualTo("Phone number already exists: 09123456789");
        }

        @Test
        @DisplayName("Should handle different phone formats")
        void shouldHandleDifferentPhoneFormats() {
            // Test with different phone formats
            DuplicatePhoneException iranPhone = new DuplicatePhoneException("09123456789");
            assertThat(iranPhone.getMessage()).isEqualTo("Phone number already exists: 09123456789");

            DuplicatePhoneException intlPhone = new DuplicatePhoneException("+989123456789");
            assertThat(intlPhone.getMessage()).isEqualTo("Phone number already exists: +989123456789");

            DuplicatePhoneException shortPhone = new DuplicatePhoneException("1234567890");
            assertThat(shortPhone.getMessage()).isEqualTo("Phone number already exists: 1234567890");
        }

        @Test
        @DisplayName("Should handle null phone number")
        void shouldHandleNullPhoneNumber() {
            DuplicatePhoneException exception = new DuplicatePhoneException(null);
            assertThat(exception.getMessage()).isEqualTo("Phone number already exists: null");
        }

        @Test
        @DisplayName("Should handle empty phone number")
        void shouldHandleEmptyPhoneNumber() {
            DuplicatePhoneException exception = new DuplicatePhoneException("");
            assertThat(exception.getMessage()).isEqualTo("Phone number already exists: ");
        }

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            assertThatThrownBy(() -> {
                throw new DuplicatePhoneException("09876543210");
            }).isInstanceOf(DuplicatePhoneException.class)
              .hasMessage("Phone number already exists: 09876543210");
        }
    }

    @Nested
    @DisplayName("Exception Inheritance Tests")
    class ExceptionInheritanceTests {

        @Test
        @DisplayName("All custom exceptions should extend RuntimeException")
        void allCustomExceptions_shouldExtendRuntimeException() {
            assertThat(InvalidCredentialsException.class.getSuperclass()).isEqualTo(RuntimeException.class);
            assertThat(NotFoundException.class.getSuperclass()).isEqualTo(RuntimeException.class);
            assertThat(InsufficientFundsException.class.getSuperclass()).isEqualTo(RuntimeException.class);
            assertThat(DuplicatePhoneException.class.getSuperclass()).isEqualTo(RuntimeException.class);
        }

        @Test
        @DisplayName("All exceptions should be in correct package")
        void allExceptions_shouldBeInCorrectPackage() {
            assertThat(InvalidCredentialsException.class.getPackageName())
                .isEqualTo("com.myapp.common.exceptions");
            assertThat(NotFoundException.class.getPackageName())
                .isEqualTo("com.myapp.common.exceptions");
            assertThat(InsufficientFundsException.class.getPackageName())
                .isEqualTo("com.myapp.common.exceptions");
            assertThat(DuplicatePhoneException.class.getPackageName())
                .isEqualTo("com.myapp.common.exceptions");
        }
    }

    @Nested
    @DisplayName("Exception Usage Scenarios Tests")
    class ExceptionUsageScenariosTests {

        @Test
        @DisplayName("Should simulate login failure scenario")
        void shouldSimulateLoginFailureScenario() {
            assertThatThrownBy(() -> {
                // Simulate authentication service
                String inputPhone = "09123456789";
                String inputPassword = "wrongpassword";
                
                // Simulate credential validation failure
                if (!"correctpassword".equals(inputPassword)) {
                    throw new InvalidCredentialsException();
                }
            }).isInstanceOf(InvalidCredentialsException.class)
              .hasMessage("Invalid phone or password");
        }

        @Test
        @DisplayName("Should simulate entity not found scenario")
        void shouldSimulateEntityNotFoundScenario() {
            assertThatThrownBy(() -> {
                // Simulate repository lookup
                Long orderId = 999L;
                
                // Simulate database lookup failure
                if (orderId > 100) {  // Simulate condition where order doesn't exist
                    throw new NotFoundException("Order", orderId);
                }
            }).isInstanceOf(NotFoundException.class)
              .hasMessage("Order not found with id=999");
        }

        @Test
        @DisplayName("Should simulate payment failure scenario")
        void shouldSimulatePaymentFailureScenario() {
            assertThatThrownBy(() -> {
                // Simulate payment processing
                double walletBalance = 25.50;
                double orderAmount = 50.00;
                
                // Simulate insufficient funds check
                if (walletBalance < orderAmount) {
                    throw InsufficientFundsException.forWallet(walletBalance, orderAmount);
                }
            }).isInstanceOf(InsufficientFundsException.class)
              .hasMessage("Insufficient wallet balance. Current: 25.50, Required: 50.00");
        }

        @Test
        @DisplayName("Should simulate duplicate registration scenario")
        void shouldSimulateDuplicateRegistrationScenario() {
            assertThatThrownBy(() -> {
                // Simulate user registration
                String phoneNumber = "09123456789";
                
                // Simulate checking if phone already exists
                boolean phoneExists = true; // Simulate phone already in database
                if (phoneExists) {
                    throw new DuplicatePhoneException(phoneNumber);
                }
            }).isInstanceOf(DuplicatePhoneException.class)
              .hasMessage("Phone number already exists: 09123456789");
        }
    }
} 