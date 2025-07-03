package com.myapp.common.utils;

import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Advanced validation utilities for input validation and sanitization
 */
public class ValidationUtil {
    private static final Logger logger = Logger.getLogger(ValidationUtil.class.getName());
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // Phone validation patterns (Iranian and international)
    private static final Pattern IRAN_PHONE_PATTERN = Pattern.compile(
        "^(\\+98|0)?9[0-9]{9}$"
    );
    
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile(
        "^\\+[1-9]\\d{1,14}$"
    );
    
    // Password strength patterns
    private static final Pattern PASSWORD_LENGTH_PATTERN = Pattern.compile(".{8,}");
    private static final Pattern PASSWORD_UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern PASSWORD_SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    
    // Name validation patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\u0600-\\u06FF\\s]{2,50}$");
    
    // Address validation patterns
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9\\u0600-\\u06FF\\s,.-]{10,200}$");
    
    // Price validation patterns
    private static final Pattern PRICE_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    
    // URL validation patterns
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$"
    );
    
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
     * Validate phone number (Iranian or international)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        String cleanPhone = phone.trim();
        
        // Check Iranian phone format
        if (IRAN_PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return true;
        }
        
        // Check international phone format
        if (INTERNATIONAL_PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Normalize Iranian phone number to standard format
     */
    public static String normalizeIranPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        String cleanPhone = phone.trim().replaceAll("\\s+", "");
        
        // Remove +98 prefix and add it back
        if (cleanPhone.startsWith("+98")) {
            cleanPhone = cleanPhone.substring(3);
        }
        
        // Remove leading 0
        if (cleanPhone.startsWith("0")) {
            cleanPhone = cleanPhone.substring(1);
        }
        
        // Add +98 prefix
        return "+98" + cleanPhone;
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        String pwd = password.trim();
        
        // Check minimum length
        if (!PASSWORD_LENGTH_PATTERN.matcher(pwd).matches()) {
            return false;
        }
        
        // Check for at least one uppercase letter
        if (!PASSWORD_UPPERCASE_PATTERN.matcher(pwd).find()) {
            return false;
        }
        
        // Check for at least one lowercase letter
        if (!PASSWORD_LOWERCASE_PATTERN.matcher(pwd).find()) {
            return false;
        }
        
        // Check for at least one digit
        if (!PASSWORD_DIGIT_PATTERN.matcher(pwd).find()) {
            return false;
        }
        
        // Check for at least one special character
        if (!PASSWORD_SPECIAL_PATTERN.matcher(pwd).find()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get password requirements as a string
     */
    public static String getPasswordRequirements() {
        return "Password must contain:\n" +
               "- At least 8 characters\n" +
               "- At least one uppercase letter (A-Z)\n" +
               "- At least one lowercase letter (a-z)\n" +
               "- At least one digit (0-9)\n" +
               "- At least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)";
    }
    
    /**
     * Get password strength score (0-100)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return 0;
        }
        
        String pwd = password.trim();
        int score = 0;
        
        // Length score (up to 25 points)
        if (pwd.length() >= 8) score += 10;
        if (pwd.length() >= 12) score += 10;
        if (pwd.length() >= 16) score += 5;
        
        // Character variety score (up to 50 points)
        if (PASSWORD_UPPERCASE_PATTERN.matcher(pwd).find()) score += 10;
        if (PASSWORD_LOWERCASE_PATTERN.matcher(pwd).find()) score += 10;
        if (PASSWORD_DIGIT_PATTERN.matcher(pwd).find()) score += 10;
        if (PASSWORD_SPECIAL_PATTERN.matcher(pwd).find()) score += 10;
        
        // Complexity score (up to 25 points)
        if (pwd.length() > 8 && PASSWORD_UPPERCASE_PATTERN.matcher(pwd).find() && 
            PASSWORD_LOWERCASE_PATTERN.matcher(pwd).find() && 
            PASSWORD_DIGIT_PATTERN.matcher(pwd).find() && 
            PASSWORD_SPECIAL_PATTERN.matcher(pwd).find()) {
            score += 25;
        }
        
        return Math.min(score, 100);
    }
    
    /**
     * Get password strength description
     */
    public static String getPasswordStrengthDescription(String password) {
        int strength = getPasswordStrength(password);
        
        if (strength >= 80) {
            return "Very Strong";
        } else if (strength >= 60) {
            return "Strong";
        } else if (strength >= 40) {
            return "Medium";
        } else if (strength >= 20) {
            return "Weak";
        } else {
            return "Very Weak";
        }
    }
    
    /**
     * Validate full name (supports Persian and English)
     */
    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        
        return NAME_PATTERN.matcher(fullName.trim()).matches();
    }
    
    /**
     * Validate address
     */
    public static boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        
        return ADDRESS_PATTERN.matcher(address.trim()).matches();
    }
    
    /**
     * Validate price format
     */
    public static boolean isValidPrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            return false;
        }
        
        return PRICE_PATTERN.matcher(price.trim()).matches();
    }
    
    /**
     * Validate price range
     */
    public static boolean isValidPriceRange(double minPrice, double maxPrice) {
        return minPrice >= 0 && maxPrice > minPrice && maxPrice <= 1000000;
    }
    
    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        return URL_PATTERN.matcher(url.trim()).matches();
    }
    
    /**
     * Validate restaurant name
     */
    public static boolean isValidRestaurantName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String cleanName = name.trim();
        return cleanName.length() >= 2 && cleanName.length() <= 100;
    }
    
    /**
     * Validate item name
     */
    public static boolean isValidItemName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String cleanName = name.trim();
        return cleanName.length() >= 2 && cleanName.length() <= 100;
    }
    
    /**
     * Validate item description
     */
    public static boolean isValidItemDescription(String description) {
        if (description == null) {
            return false;
        }
        
        return description.length() <= 500;
    }
    
    /**
     * Validate rating (1-5 stars)
     */
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }
    
    /**
     * Validate rating (1-5 stars) as double
     */
    public static boolean isValidRating(double rating) {
        return rating >= 1.0 && rating <= 5.0;
    }
    
    /**
     * Sanitize input to prevent XSS and injection attacks
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                   .replaceAll("(?i)<script[^>]*>.*?</script>", "")
                   .replaceAll("(?i)javascript:", "")
                   .replaceAll("(?i)on\\w+\\s*=", "")
                   .replaceAll("(?i)<iframe[^>]*>", "")
                   .replaceAll("(?i)<object[^>]*>", "")
                   .replaceAll("(?i)<embed[^>]*>", "")
                   .replaceAll("(?i)<link[^>]*>", "")
                   .replaceAll("(?i)<meta[^>]*>", "")
                   .replaceAll("(?i)(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION)", "")
                   .replaceAll("(?i)('|;|--|/\\*|\\*/)", "");
    }
    
    /**
     * Validate and sanitize email
     */
    public static String validateAndSanitizeEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        return email.trim().toLowerCase();
    }
    
    /**
     * Validate and normalize phone
     */
    public static String validateAndNormalizePhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone format");
        }
        
        // If it's an Iranian phone, normalize it
        if (IRAN_PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return normalizeIranPhone(phone);
        }
        
        return phone.trim();
    }
    
    /**
     * Validate and sanitize full name
     */
    public static String validateAndSanitizeFullName(String fullName) {
        if (!isValidFullName(fullName)) {
            throw new IllegalArgumentException("Invalid full name format");
        }
        
        return sanitizeInput(fullName);
    }
    
    /**
     * Validate and sanitize address
     */
    public static String validateAndSanitizeAddress(String address) {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid address format");
        }
        
        return sanitizeInput(address);
    }
    
    /**
     * Validate and parse price
     */
    public static double validateAndParsePrice(String price) {
        if (!isValidPrice(price)) {
            throw new IllegalArgumentException("Invalid price format");
        }
        
        return Double.parseDouble(price.trim());
    }
    
    /**
     * Validate and sanitize restaurant name
     */
    public static String validateAndSanitizeRestaurantName(String name) {
        if (!isValidRestaurantName(name)) {
            throw new IllegalArgumentException("Invalid restaurant name");
        }
        
        return sanitizeInput(name);
    }
    
    /**
     * Validate and sanitize item name
     */
    public static String validateAndSanitizeItemName(String name) {
        if (!isValidItemName(name)) {
            throw new IllegalArgumentException("Invalid item name");
        }
        
        return sanitizeInput(name);
    }
    
    /**
     * Validate and sanitize item description
     */
    public static String validateAndSanitizeItemDescription(String description) {
        if (!isValidItemDescription(description)) {
            throw new IllegalArgumentException("Invalid item description");
        }
        
        return sanitizeInput(description);
    }
    
    /**
     * Get validation statistics
     */
    public static String getValidationStats() {
        return "=== Validation Statistics ===\n" +
               "Email validation: " + EMAIL_PATTERN.pattern() + "\n" +
               "Phone validation: Iranian and International formats supported\n" +
               "Password requirements: 8+ chars, uppercase, lowercase, digit, special\n" +
               "Name validation: 2-50 chars, Persian and English supported\n" +
               "Address validation: 10-200 chars, Persian and English supported\n" +
               "Price validation: Positive numbers with up to 2 decimal places\n" +
               "Rating validation: 1-5 stars\n";
    }
} 