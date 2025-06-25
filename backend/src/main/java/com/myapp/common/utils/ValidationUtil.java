package com.myapp.common.utils;

import com.myapp.common.constants.ApplicationConstants;

import java.util.regex.Pattern;

/**
 * Centralized validation utility for consistent validation across the application
 */
public class ValidationUtil {
    
    // Pre-compiled regex patterns for better performance
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^09\\d{9}$"  // Iranian mobile phone format
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF\\s\\w.-]+$"  // Persian + English characters
    );
    
    // Private constructor to prevent instantiation
    private ValidationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // ==================== String Validation ====================
    
    /**
     * Validate if string is not null or empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate string length within range
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) return false;
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Validate required string field
     */
    public static void validateRequiredString(String value, String fieldName) {
        if (!isNotEmpty(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    
    /**
     * Validate string length with custom error message
     */
    public static void validateStringLength(String value, String fieldName, int minLength, int maxLength) {
        if (value != null && !isValidLength(value, minLength, maxLength)) {
            throw new IllegalArgumentException(String.format(
                "%s must be between %d and %d characters long", fieldName, minLength, maxLength
            ));
        }
    }
    
    // ==================== Email Validation ====================
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate email with exception throwing
     */
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(ApplicationConstants.ERROR_MESSAGES.INVALID_EMAIL_FORMAT);
        }
    }
    
    // ==================== Phone Validation ====================
    
    /**
     * Validate Iranian phone number format
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Validate phone with exception throwing
     */
    public static void validatePhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException(ApplicationConstants.ERROR_MESSAGES.INVALID_PHONE_FORMAT);
        }
    }
    
    // ==================== Password Validation ====================
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validate password with exception throwing
     */
    public static void validatePassword(String password) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(ApplicationConstants.ERROR_MESSAGES.WEAK_PASSWORD);
        }
    }
    
    /**
     * Get password requirements description
     */
    public static String getPasswordRequirements() {
        return "Password must contain:\n" +
               "- At least 8 characters\n" +
               "- At least one uppercase letter (A-Z)\n" +
               "- At least one lowercase letter (a-z)\n" +
               "- At least one digit (0-9)\n" +
               "- At least one special character (@#$%^&+=)\n" +
               "- No whitespace characters";
    }
    
    // ==================== Name Validation ====================
    
    /**
     * Validate name (supports Persian and English characters)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches() && 
               isValidLength(name, 2, ApplicationConstants.VALIDATION.MAX_NAME_LENGTH);
    }
    
    /**
     * Validate name with exception throwing
     */
    public static void validateName(String name, String fieldName) {
        validateRequiredString(name, fieldName);
        if (!isValidName(name)) {
            throw new IllegalArgumentException(fieldName + " contains invalid characters or length");
        }
    }
    
    // ==================== Numeric Validation ====================
    
    /**
     * Validate number is positive
     */
    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }
    
    /**
     * Validate number is non-negative
     */
    public static boolean isNonNegative(Number number) {
        return number != null && number.doubleValue() >= 0;
    }
    
    /**
     * Validate number is within range
     */
    public static boolean isInRange(Number number, double min, double max) {
        if (number == null) return false;
        double value = number.doubleValue();
        return value >= min && value <= max;
    }
    
    /**
     * Validate positive number with exception
     */
    public static void validatePositiveNumber(Number number, String fieldName) {
        if (!isPositive(number)) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
    
    /**
     * Validate price
     */
    public static void validatePrice(Double price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (!isInRange(price, ApplicationConstants.VALIDATION.MIN_PRICE, ApplicationConstants.VALIDATION.MAX_PRICE)) {
            throw new IllegalArgumentException(String.format(
                "Price must be between %.2f and %.2f", 
                ApplicationConstants.VALIDATION.MIN_PRICE, 
                ApplicationConstants.VALIDATION.MAX_PRICE
            ));
        }
    }
    
    /**
     * Validate quantity
     */
    public static void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (!isInRange(quantity, ApplicationConstants.VALIDATION.MIN_QUANTITY, ApplicationConstants.VALIDATION.MAX_QUANTITY)) {
            throw new IllegalArgumentException(String.format(
                "Quantity must be between %d and %d", 
                ApplicationConstants.VALIDATION.MIN_QUANTITY, 
                ApplicationConstants.VALIDATION.MAX_QUANTITY
            ));
        }
    }
    
    /**
     * Validate rating
     */
    public static void validateRating(Integer rating) {
        if (rating == null) {
            throw new IllegalArgumentException("Rating cannot be null");
        }
        if (!isInRange(rating, ApplicationConstants.VALIDATION.MIN_RATING, ApplicationConstants.VALIDATION.MAX_RATING)) {
            throw new IllegalArgumentException(String.format(
                "Rating must be between %d and %d", 
                ApplicationConstants.VALIDATION.MIN_RATING, 
                ApplicationConstants.VALIDATION.MAX_RATING
            ));
        }
    }
    
    // ==================== ID Validation ====================
    
    /**
     * Validate entity ID
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }
    
    /**
     * Validate ID with exception throwing
     */
    public static void validateId(Long id, String entityName) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException(entityName + " ID must be positive");
        }
    }
    
    // ==================== Business Logic Validation ====================
    
    /**
     * Validate user role
     */
    public static boolean isValidUserRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return false;
        }
        String upperRole = role.trim().toUpperCase();
        return upperRole.equals(ApplicationConstants.BUSINESS.ROLE_BUYER) ||
               upperRole.equals(ApplicationConstants.BUSINESS.ROLE_SELLER) ||
               upperRole.equals(ApplicationConstants.BUSINESS.ROLE_COURIER) ||
               upperRole.equals(ApplicationConstants.BUSINESS.ROLE_ADMIN);
    }
    
    /**
     * Validate payment method
     */
    public static boolean isValidPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return false;
        }
        String upperMethod = paymentMethod.trim().toUpperCase();
        return upperMethod.equals(ApplicationConstants.BUSINESS.PAYMENT_CARD) ||
               upperMethod.equals(ApplicationConstants.BUSINESS.PAYMENT_WALLET) ||
               upperMethod.equals(ApplicationConstants.BUSINESS.PAYMENT_COD);
    }
    
    /**
     * Validate order status
     */
    public static boolean isValidOrderStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String upperStatus = status.trim().toUpperCase();
        return upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_PENDING) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_CONFIRMED) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_PREPARING) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_READY) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_PICKED_UP) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_DELIVERED) ||
               upperStatus.equals(ApplicationConstants.BUSINESS.ORDER_STATUS_CANCELLED);
    }
    
    // ==================== Composite Validation ====================
    
    /**
     * Validate user registration data
     */
    public static void validateUserRegistration(String fullName, String phone, String password, String email, String role) {
        validateName(fullName, "Full name");
        validatePhone(phone);
        validatePassword(password);
        
        if (isNotEmpty(email)) {
            validateEmail(email);
        }
        
        if (!isValidUserRole(role)) {
            throw new IllegalArgumentException("Invalid user role: " + role);
        }
    }
    
    /**
     * Validate food item data
     */
    public static void validateFoodItem(String name, String description, Double price, String category, Integer quantity) {
        validateName(name, "Item name");
        validatePrice(price);
        validateRequiredString(category, "Category");
        
        if (description != null) {
            validateStringLength(description, "Description", 0, ApplicationConstants.VALIDATION.MAX_DESCRIPTION_LENGTH);
        }
        
        if (quantity != null) {
            validateQuantity(quantity);
        }
    }
} 