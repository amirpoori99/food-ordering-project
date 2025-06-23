package com.myapp.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Utility class for password operations
 * Provides secure password hashing, verification, and validation
 */
public class PasswordUtil {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 32;
    private static final SecureRandom random = new SecureRandom();
    
    // Password validation patterns
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );
    
    /**
     * Hash a password with a randomly generated salt
     * 
     * @param password The plain text password
     * @return A string containing the salt and hashed password separated by ':'
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            // Generate a random salt
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash the password with the salt
            String hashedPassword = hashPasswordWithSalt(password, salt);
            
            // Return salt:hashedPassword format
            return Base64.getEncoder().encodeToString(salt) + ":" + hashedPassword;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * Verify a password against a stored hash
     * 
     * @param password The plain text password to verify
     * @param storedHash The stored hash in format "salt:hashedPassword"
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        
        try {
            // Split the stored hash to get salt and hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            // Decode the salt
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String expectedHash = parts[1];
            
            // Hash the provided password with the same salt
            String actualHash = hashPasswordWithSalt(password, salt);
            
            // Compare hashes using constant-time comparison
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            // Log the exception in a real application
            return false;
        }
    }
    
    /**
     * Hash password with a specific salt
     */
    private static String hashPasswordWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        
        // Add salt to the digest
        md.update(salt);
        
        // Hash the password
        byte[] hashedPassword = md.digest(password.getBytes());
        
        // Return base64 encoded hash
        return Base64.getEncoder().encodeToString(hashedPassword);
    }
    
    /**
     * Constant-time string comparison to prevent timing attacks
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
    
    /**
     * Validate password strength
     * 
     * @param password The password to validate
     * @return true if password meets strength requirements
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Get password strength requirements as a readable string
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
    
    /**
     * Generate a secure random password
     * 
     * @param length The desired password length (minimum 8)
     * @return A randomly generated password that meets strength requirements
     */
    public static String generateSecurePassword(int length) {
        if (length < 8) {
            length = 8;
        }
        
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "@#$%^&+=";
        String allChars = upperCase + lowerCase + digits + specialChars;
        
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each required category
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // Fill the rest with random characters
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password to avoid predictable patterns
        return shuffleString(password.toString());
    }
    
    /**
     * Shuffle characters in a string
     */
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();
        
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            
            // Swap chars[i] and chars[j]
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }
    
    /**
     * Check if a password needs to be rehashed (for security upgrades)
     * Currently always returns false, but can be extended for algorithm changes
     */
    public static boolean needsRehash(String hashedPassword) {
        // In future versions, this could check if the hash was created with old algorithms
        return false;
    }
    
    /**
     * Get password strength score (0-5)
     * 
     * @param password The password to score
     * @return Strength score: 0=Very Weak, 1=Weak, 2=Fair, 3=Good, 4=Strong, 5=Very Strong
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety checks
        if (password.matches(".*[a-z].*")) score++; // lowercase
        if (password.matches(".*[A-Z].*")) score++; // uppercase
        if (password.matches(".*[0-9].*")) score++; // digits
        if (password.matches(".*[@#$%^&+=!*].*")) score++; // special chars
        
        // Penalty for common patterns
        if (password.matches(".*123.*") || password.matches(".*abc.*") || 
            password.toLowerCase().contains("password")) {
            score = Math.max(0, score - 1);
        }
        
        return Math.min(5, score);
    }
    
    /**
     * Get password strength description
     */
    public static String getPasswordStrengthDescription(int strength) {
        switch (strength) {
            case 0: return "Very Weak";
            case 1: return "Weak";
            case 2: return "Fair";
            case 3: return "Good";
            case 4: return "Strong";
            case 5: return "Very Strong";
            default: return "Unknown";
        }
    }
}