package com.myapp.common;

import com.myapp.common.constants.ApplicationConstants;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

/**
 * تست‌های کامل برای ApplicationConstants
 * بررسی اینکه تمام ثابت‌ها به درستی تعریف شده‌اند
 */
@DisplayName("ApplicationConstants Tests")
class ApplicationConstantsTest {

    @Test
    @DisplayName("تست عدم امکان نمونه‌سازی کلاس")
    void constructor_shouldThrowException() {
        assertThatThrownBy(() -> {
            // استفاده از reflection برای دسترسی به سازنده خصوصی
            var constructor = ApplicationConstants.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Nested
    @DisplayName("API Constants Tests")
    class ApiConstantsTests {

        @Test
        @DisplayName("API paths should be properly defined")
        void apiPaths_shouldBeProperlyDefined() {
            assertThat(ApplicationConstants.API.BASE_PATH).isEqualTo("/api");
            assertThat(ApplicationConstants.API.AUTH_PATH).isEqualTo("/api/auth");
            assertThat(ApplicationConstants.API.RESTAURANTS_PATH).isEqualTo("/api/restaurants");
            assertThat(ApplicationConstants.API.ORDERS_PATH).isEqualTo("/api/orders");
            assertThat(ApplicationConstants.API.ITEMS_PATH).isEqualTo("/api/items");
            assertThat(ApplicationConstants.API.PAYMENTS_PATH).isEqualTo("/api/payments");
            assertThat(ApplicationConstants.API.DELIVERY_PATH).isEqualTo("/api/delivery");
            assertThat(ApplicationConstants.API.COUPONS_PATH).isEqualTo("/api/coupons");
            assertThat(ApplicationConstants.API.FAVORITES_PATH).isEqualTo("/api/favorites");
            assertThat(ApplicationConstants.API.RATINGS_PATH).isEqualTo("/api/ratings");
            assertThat(ApplicationConstants.API.ADMIN_PATH).isEqualTo("/api/admin");
            assertThat(ApplicationConstants.API.VENDORS_PATH).isEqualTo("/api/vendors");
            assertThat(ApplicationConstants.API.NOTIFICATIONS_PATH).isEqualTo("/api/notifications");
        }

        @Test
        @DisplayName("All API paths should be non-empty")
        void allApiPaths_shouldBeNonEmpty() {
            assertThat(ApplicationConstants.API.BASE_PATH).isNotEmpty();
            assertThat(ApplicationConstants.API.AUTH_PATH).isNotEmpty();
            assertThat(ApplicationConstants.API.RESTAURANTS_PATH).isNotEmpty();
            assertThat(ApplicationConstants.API.ORDERS_PATH).isNotEmpty();
            assertThat(ApplicationConstants.API.ITEMS_PATH).isNotEmpty();
            assertThat(ApplicationConstants.API.PAYMENTS_PATH).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Validation Constants Tests")
    class ValidationConstantsTests {

        @Test
        @DisplayName("Text length limits should be positive")
        void textLengthLimits_shouldBePositive() {
            assertThat(ApplicationConstants.VALIDATION.MAX_NAME_LENGTH).isPositive();
            assertThat(ApplicationConstants.VALIDATION.MAX_DESCRIPTION_LENGTH).isPositive();
            assertThat(ApplicationConstants.VALIDATION.MAX_ADDRESS_LENGTH).isPositive();
            assertThat(ApplicationConstants.VALIDATION.MAX_EMAIL_LENGTH).isPositive();
        }

        @Test
        @DisplayName("Price limits should be reasonable")
        void priceLimits_shouldBeReasonable() {
            assertThat(ApplicationConstants.VALIDATION.MIN_PRICE).isNotNegative();
            assertThat(ApplicationConstants.VALIDATION.MAX_PRICE).isPositive();
            assertThat(ApplicationConstants.VALIDATION.MIN_PRICE)
                .isLessThan(ApplicationConstants.VALIDATION.MAX_PRICE);
        }

        @Test
        @DisplayName("Quantity limits should be logical")
        void quantityLimits_shouldBeLogical() {
            assertThat(ApplicationConstants.VALIDATION.MIN_QUANTITY).isPositive();
            assertThat(ApplicationConstants.VALIDATION.MAX_QUANTITY).isPositive();
            assertThat(ApplicationConstants.VALIDATION.MIN_QUANTITY)
                .isLessThan(ApplicationConstants.VALIDATION.MAX_QUANTITY);
        }

        @Test
        @DisplayName("Rating limits should be valid")
        void ratingLimits_shouldBeValid() {
            assertThat(ApplicationConstants.VALIDATION.MIN_RATING).isEqualTo(1);
            assertThat(ApplicationConstants.VALIDATION.MAX_RATING).isEqualTo(5);
            assertThat(ApplicationConstants.VALIDATION.MIN_RATING)
                .isLessThan(ApplicationConstants.VALIDATION.MAX_RATING);
        }
    }

    @Nested
    @DisplayName("Business Logic Constants Tests")
    class BusinessLogicConstantsTests {

        @Test
        @DisplayName("User roles should be properly defined")
        void userRoles_shouldBeProperlyDefined() {
            assertThat(ApplicationConstants.BUSINESS.ROLE_ADMIN).isEqualTo("ADMIN");
            assertThat(ApplicationConstants.BUSINESS.ROLE_SELLER).isEqualTo("SELLER");
            assertThat(ApplicationConstants.BUSINESS.ROLE_BUYER).isEqualTo("BUYER");
            assertThat(ApplicationConstants.BUSINESS.ROLE_COURIER).isEqualTo("COURIER");
        }

        @Test
        @DisplayName("Order statuses should be comprehensive")
        void orderStatuses_shouldBeComprehensive() {
            assertThat(ApplicationConstants.BUSINESS.ORDER_STATUS_PENDING).isEqualTo("PENDING");
            assertThat(ApplicationConstants.BUSINESS.ORDER_STATUS_CONFIRMED).isEqualTo("CONFIRMED");
            assertThat(ApplicationConstants.BUSINESS.ORDER_STATUS_PREPARING).isEqualTo("PREPARING");
            assertThat(ApplicationConstants.BUSINESS.ORDER_STATUS_READY).isEqualTo("READY");
            assertThat(ApplicationConstants.BUSINESS.ORDER_STATUS_PICKED_UP).isEqualTo("PICKED_UP");
            assertThat(ApplicationConstants.BUSINESS.ORDER_STATUS_DELIVERED).isEqualTo("DELIVERED");
            assertThat(ApplicationConstants.BUSINESS.ORDER_STATUS_CANCELLED).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("Payment methods should be complete")
        void paymentMethods_shouldBeComplete() {
            assertThat(ApplicationConstants.BUSINESS.PAYMENT_CARD).isEqualTo("CARD");
            assertThat(ApplicationConstants.BUSINESS.PAYMENT_WALLET).isEqualTo("WALLET");
            assertThat(ApplicationConstants.BUSINESS.PAYMENT_COD).isEqualTo("COD");
        }

        @Test
        @DisplayName("Business rules should be properly configured")
        void businessRules_shouldBeProperlyConfigured() {
            assertThat(ApplicationConstants.BUSINESS.MAX_WALLET_CHARGE).isPositive();
            assertThat(ApplicationConstants.BUSINESS.MIN_ORDER_AMOUNT).isPositive();
            assertThat(ApplicationConstants.BUSINESS.DELIVERY_FEE).isPositive();
            assertThat(ApplicationConstants.BUSINESS.MAX_COUPON_USAGE_PER_USER).isPositive();
            assertThat(ApplicationConstants.BUSINESS.PASSWORD_MIN_LENGTH).isPositive();
            assertThat(ApplicationConstants.BUSINESS.PHONE_NUMBER_LENGTH).isEqualTo(11);
            assertThat(ApplicationConstants.BUSINESS.PHONE_PREFIX).isEqualTo("09");
        }
    }

    @Nested
    @DisplayName("Message Constants Tests")
    class MessageConstantsTests {

        @Test
        @DisplayName("Success messages should be informative")
        void successMessages_shouldBeInformative() {
            assertThat(ApplicationConstants.SUCCESS_MESSAGES.USER_REGISTERED).contains("success");
            assertThat(ApplicationConstants.SUCCESS_MESSAGES.LOGIN_SUCCESS).contains("success");
            assertThat(ApplicationConstants.SUCCESS_MESSAGES.ORDER_PLACED).contains("success");
            assertThat(ApplicationConstants.SUCCESS_MESSAGES.PAYMENT_PROCESSED).contains("success");
            assertThat(ApplicationConstants.SUCCESS_MESSAGES.RESTAURANT_CREATED).contains("success");
            assertThat(ApplicationConstants.SUCCESS_MESSAGES.ITEM_ADDED).contains("success");
        }

        @Test
        @DisplayName("Error messages should be clear")
        void errorMessages_shouldBeClear() {
            assertThat(ApplicationConstants.ERROR_MESSAGES.INVALID_CREDENTIALS).isNotEmpty();
            assertThat(ApplicationConstants.ERROR_MESSAGES.USER_NOT_FOUND).isNotEmpty();
            assertThat(ApplicationConstants.ERROR_MESSAGES.RESTAURANT_NOT_FOUND).isNotEmpty();
            assertThat(ApplicationConstants.ERROR_MESSAGES.ORDER_NOT_FOUND).isNotEmpty();
            assertThat(ApplicationConstants.ERROR_MESSAGES.UNAUTHORIZED_ACCESS).isNotEmpty();
            assertThat(ApplicationConstants.ERROR_MESSAGES.DUPLICATE_PHONE).isNotEmpty();
        }

        @Test
        @DisplayName("Error messages should contain specific keywords")
        void errorMessages_shouldContainSpecificKeywords() {
            assertThat(ApplicationConstants.ERROR_MESSAGES.INVALID_PHONE_FORMAT).contains("phone");
            assertThat(ApplicationConstants.ERROR_MESSAGES.INVALID_EMAIL_FORMAT).contains("email");
            assertThat(ApplicationConstants.ERROR_MESSAGES.WEAK_PASSWORD.toLowerCase()).contains("password");
            assertThat(ApplicationConstants.ERROR_MESSAGES.WALLET_INSUFFICIENT_BALANCE).contains("balance");
        }
    }

    @Nested
    @DisplayName("Database Constants Tests")
    class DatabaseConstantsTests {

        @Test
        @DisplayName("Database configuration should be present")
        void databaseConfiguration_shouldBePresent() {
            assertThat(ApplicationConstants.DATABASE.CONNECTION_POOL_SIZE).isPositive();
            assertThat(ApplicationConstants.DATABASE.JDBC_BATCH_SIZE).isPositive();
            assertThat(ApplicationConstants.DATABASE.MAX_RETRY_ATTEMPTS).isPositive();
            assertThat(ApplicationConstants.DATABASE.RETRY_DELAY_MS).isPositive();
            assertThat(ApplicationConstants.DATABASE.BUSY_TIMEOUT_MS).isPositive();
        }

        @Test
        @DisplayName("SQLite URL should be properly configured")
        void sqliteUrl_shouldBeProperlyConfigured() {
            String url = ApplicationConstants.DATABASE.SQLITE_URL;
            assertThat(url).isNotEmpty();
            assertThat(url).startsWith("jdbc:sqlite:");
            assertThat(url).contains("journal_mode=WAL");
            assertThat(url).contains("synchronous=NORMAL");
            assertThat(url).contains("busy_timeout=30000");
        }
    }

    @Nested
    @DisplayName("Security Constants Tests")
    class SecurityConstantsTests {

        @Test
        @DisplayName("JWT configuration should be secure")
        void jwtConfiguration_shouldBeSecure() {
            assertThat(ApplicationConstants.SECURITY.JWT_SECRET_KEY).isNotEmpty();
            assertThat(ApplicationConstants.SECURITY.ACCESS_TOKEN_VALIDITY).isPositive();
            assertThat(ApplicationConstants.SECURITY.REFRESH_TOKEN_VALIDITY).isPositive();
            assertThat(ApplicationConstants.SECURITY.AUTHORIZATION_HEADER).isEqualTo("Authorization");
            assertThat(ApplicationConstants.SECURITY.BEARER_PREFIX).isEqualTo("Bearer ");
            assertThat(ApplicationConstants.SECURITY.BCRYPT_ROUNDS).isPositive();
        }

        @Test
        @DisplayName("Security timeouts should be reasonable")
        void securityTimeouts_shouldBeReasonable() {
            // Access token should expire before refresh token
            long accessValidity = ApplicationConstants.SECURITY.ACCESS_TOKEN_VALIDITY;
            long refreshValidity = ApplicationConstants.SECURITY.REFRESH_TOKEN_VALIDITY;
            
            assertThat(accessValidity).isLessThan(refreshValidity);
            
            // Check reasonable durations (access: 1 hour, refresh: 7 days)
            assertThat(accessValidity).isEqualTo(3600000L); // 1 hour in milliseconds
            assertThat(refreshValidity).isEqualTo(604800000L); // 7 days in milliseconds
        }

        @Test
        @DisplayName("BCrypt rounds should be secure")
        void bcryptRounds_shouldBeSecure() {
            int rounds = ApplicationConstants.SECURITY.BCRYPT_ROUNDS;
            assertThat(rounds).isGreaterThanOrEqualTo(10);
                         assertThat(rounds).isLessThanOrEqualTo(15);
         }
     }

    @Nested
    @DisplayName("HTTP Status Constants Tests")
    class HttpStatusConstantsTests {

        @Test
        @DisplayName("HTTP status codes should be valid")
        void httpStatusCodes_shouldBeValid() {
            assertThat(ApplicationConstants.HTTP_STATUS.OK).isEqualTo(200);
            assertThat(ApplicationConstants.HTTP_STATUS.CREATED).isEqualTo(201);
            assertThat(ApplicationConstants.HTTP_STATUS.NO_CONTENT).isEqualTo(204);
            assertThat(ApplicationConstants.HTTP_STATUS.BAD_REQUEST).isEqualTo(400);
            assertThat(ApplicationConstants.HTTP_STATUS.UNAUTHORIZED).isEqualTo(401);
            assertThat(ApplicationConstants.HTTP_STATUS.FORBIDDEN).isEqualTo(403);
            assertThat(ApplicationConstants.HTTP_STATUS.NOT_FOUND).isEqualTo(404);
            assertThat(ApplicationConstants.HTTP_STATUS.METHOD_NOT_ALLOWED).isEqualTo(405);
            assertThat(ApplicationConstants.HTTP_STATUS.CONFLICT).isEqualTo(409);
            assertThat(ApplicationConstants.HTTP_STATUS.INTERNAL_SERVER_ERROR).isEqualTo(500);
            assertThat(ApplicationConstants.HTTP_STATUS.SERVICE_UNAVAILABLE).isEqualTo(503);
        }
    }

    @Nested
    @DisplayName("Content Type Constants Tests")
    class ContentTypeConstantsTests {

        @Test
        @DisplayName("Content types should be properly defined")
        void contentTypes_shouldBeProperlyDefined() {
            assertThat(ApplicationConstants.CONTENT_TYPE.APPLICATION_JSON).isEqualTo("application/json");
            assertThat(ApplicationConstants.CONTENT_TYPE.TEXT_PLAIN).isEqualTo("text/plain");
            assertThat(ApplicationConstants.CONTENT_TYPE.TEXT_HTML).isEqualTo("text/html");
        }
    }

    @Nested
    @DisplayName("Date Format Constants Tests")
    class DateFormatConstantsTests {

        @Test
        @DisplayName("Date formats should be properly defined")
        void dateFormats_shouldBeProperlyDefined() {
            assertThat(ApplicationConstants.DATE_FORMAT.ISO_DATE_TIME).isEqualTo("yyyy-MM-dd'T'HH:mm:ss");
            assertThat(ApplicationConstants.DATE_FORMAT.SIMPLE_DATE).isEqualTo("yyyy-MM-dd");
            assertThat(ApplicationConstants.DATE_FORMAT.TIMESTAMP).isEqualTo("yyyy-MM-dd HH:mm:ss");
            assertThat(ApplicationConstants.DATE_FORMAT.PERSIAN_DATE).isEqualTo("yyyy/MM/dd");
        }
    }
} 