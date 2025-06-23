package com.myapp.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtil class
 */
public class PasswordUtilTest {
    
    @Test
    @DisplayName("Hash password should create valid hash")
    public void testHashPassword() {
        String password = "TestPassword123@";
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.contains(":"));
        
        String[] parts = hashedPassword.split(":");
        assertEquals(2, parts.length);
        assertFalse(parts[0].isEmpty()); // salt
        assertFalse(parts[1].isEmpty()); // hash
    }
    
    @Test
    @DisplayName("Hash password should throw exception for null password")
    public void testHashPasswordNullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword(null);
        });
    }
    
    @Test
    @DisplayName("Hash password should throw exception for empty password")
    public void testHashPasswordEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword("");
        });
    }
    
    @Test
    @DisplayName("Verify password should return true for correct password")
    public void testVerifyPasswordCorrect() {
        String password = "TestPassword123@";
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        assertTrue(PasswordUtil.verifyPassword(password, hashedPassword));
    }
    
    @Test
    @DisplayName("Verify password should return false for incorrect password")
    public void testVerifyPasswordIncorrect() {
        String password = "TestPassword123@";
        String wrongPassword = "WrongPassword123@";
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        assertFalse(PasswordUtil.verifyPassword(wrongPassword, hashedPassword));
    }
    
    @Test
    @DisplayName("Verify password should return false for null inputs")
    public void testVerifyPasswordNullInputs() {
        assertFalse(PasswordUtil.verifyPassword(null, "hash"));
        assertFalse(PasswordUtil.verifyPassword("password", null));
        assertFalse(PasswordUtil.verifyPassword(null, null));
    }
    
    @Test
    @DisplayName("Verify password should return false for invalid hash format")
    public void testVerifyPasswordInvalidHashFormat() {
        assertFalse(PasswordUtil.verifyPassword("password", "invalidhash"));
        assertFalse(PasswordUtil.verifyPassword("password", "invalid:hash:format"));
    }
    
    @Test
    @DisplayName("Same password should produce different hashes (due to salt)")
    public void testSamePasswordDifferentHashes() {
        String password = "TestPassword123@";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);
        
        assertNotEquals(hash1, hash2);
        
        // But both should verify correctly
        assertTrue(PasswordUtil.verifyPassword(password, hash1));
        assertTrue(PasswordUtil.verifyPassword(password, hash2));
    }
    
    @Test
    @DisplayName("Valid password should pass validation")
    public void testIsValidPasswordValid() {
        assertTrue(PasswordUtil.isValidPassword("ValidPass123@"));
        assertTrue(PasswordUtil.isValidPassword("AnotherGood1#"));
        assertTrue(PasswordUtil.isValidPassword("Complex123$"));
    }
    
    @Test
    @DisplayName("Invalid passwords should fail validation")
    public void testIsValidPasswordInvalid() {
        // Too short
        assertFalse(PasswordUtil.isValidPassword("Short1@"));
        
        // No uppercase
        assertFalse(PasswordUtil.isValidPassword("lowercase123@"));
        
        // No lowercase
        assertFalse(PasswordUtil.isValidPassword("UPPERCASE123@"));
        
        // No digits
        assertFalse(PasswordUtil.isValidPassword("NoDigits@"));
        
        // No special characters
        assertFalse(PasswordUtil.isValidPassword("NoSpecial123"));
        
        // Contains whitespace
        assertFalse(PasswordUtil.isValidPassword("Has Space123@"));
        
        // Null password
        assertFalse(PasswordUtil.isValidPassword(null));
    }
    
    @Test
    @DisplayName("Generate secure password should create valid password")
    public void testGenerateSecurePassword() {
        String password = PasswordUtil.generateSecurePassword(12);
        
        assertNotNull(password);
        assertEquals(12, password.length());
        assertTrue(PasswordUtil.isValidPassword(password));
    }
    
    @Test
    @DisplayName("Generate secure password should enforce minimum length")
    public void testGenerateSecurePasswordMinimumLength() {
        String password = PasswordUtil.generateSecurePassword(5); // Less than 8
        
        assertNotNull(password);
        assertTrue(password.length() >= 8);
        assertTrue(PasswordUtil.isValidPassword(password));
    }
    
    @Test
    @DisplayName("Generate secure password should create different passwords")
    public void testGenerateSecurePasswordUnique() {
        String password1 = PasswordUtil.generateSecurePassword(10);
        String password2 = PasswordUtil.generateSecurePassword(10);
        
        assertNotEquals(password1, password2);
    }
    
    @Test
    @DisplayName("Password strength should return correct scores")
    public void testGetPasswordStrength() {
        // Very weak
        assertEquals(0, PasswordUtil.getPasswordStrength(""));
        assertEquals(0, PasswordUtil.getPasswordStrength(null));
        assertEquals(0, PasswordUtil.getPasswordStrength("abc")); // Too short
        
        // Weak
        assertEquals(1, PasswordUtil.getPasswordStrength("password")); // 8+ chars, lowercase only
        
        // Fair
        assertEquals(2, PasswordUtil.getPasswordStrength("Password")); // 8+ chars, lowercase, uppercase
        
        // Good
        assertEquals(3, PasswordUtil.getPasswordStrength("Password1")); // 8+ chars, lowercase, uppercase, digits
        
        // Strong
        assertEquals(4, PasswordUtil.getPasswordStrength("Password1@")); // all requirements
        
        // Very Strong
        assertEquals(5, PasswordUtil.getPasswordStrength("VeryStrongPassword1@")); // all requirements + 12+ chars
    }
    
    @Test
    @DisplayName("Password strength description should return correct strings")
    public void testGetPasswordStrengthDescription() {
        assertEquals("Very Weak", PasswordUtil.getPasswordStrengthDescription(0));
        assertEquals("Weak", PasswordUtil.getPasswordStrengthDescription(1));
        assertEquals("Fair", PasswordUtil.getPasswordStrengthDescription(2));
        assertEquals("Good", PasswordUtil.getPasswordStrengthDescription(3));
        assertEquals("Strong", PasswordUtil.getPasswordStrengthDescription(4));
        assertEquals("Very Strong", PasswordUtil.getPasswordStrengthDescription(5));
        assertEquals("Unknown", PasswordUtil.getPasswordStrengthDescription(-1));
        assertEquals("Unknown", PasswordUtil.getPasswordStrengthDescription(10));
    }
    
    @Test
    @DisplayName("Password requirements should return non-empty string")
    public void testGetPasswordRequirements() {
        String requirements = PasswordUtil.getPasswordRequirements();
        
        assertNotNull(requirements);
        assertFalse(requirements.isEmpty());
        assertTrue(requirements.contains("8 characters"));
        assertTrue(requirements.contains("uppercase"));
        assertTrue(requirements.contains("lowercase"));
        assertTrue(requirements.contains("digit"));
        assertTrue(requirements.contains("special"));
    }
    
    @Test
    @DisplayName("Needs rehash should return false for all inputs")
    public void testNeedsRehash() {
        assertFalse(PasswordUtil.needsRehash("any_hash"));
        assertFalse(PasswordUtil.needsRehash(""));
        assertFalse(PasswordUtil.needsRehash(null));
    }
    
    @Test
    @DisplayName("Password strength should penalize common patterns")
    public void testPasswordStrengthPenalty() {
        // Passwords with common patterns should have lower scores
        int normalScore = PasswordUtil.getPasswordStrength("StrongPass1@");
        int patternScore1 = PasswordUtil.getPasswordStrength("StrongPass123@");
        int patternScore2 = PasswordUtil.getPasswordStrength("password123@ABC");
        
        assertTrue(normalScore >= patternScore1);
        assertTrue(normalScore >= patternScore2);
    }
    
    @Test
    @DisplayName("Hash verification should be case sensitive")
    public void testHashVerificationCaseSensitive() {
        String password = "TestPassword123@";
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        assertTrue(PasswordUtil.verifyPassword(password, hashedPassword));
        assertFalse(PasswordUtil.verifyPassword(password.toLowerCase(), hashedPassword));
        assertFalse(PasswordUtil.verifyPassword(password.toUpperCase(), hashedPassword));
    }
    
    @Test
    @DisplayName("Performance test - hash and verify operations")
    public void testPerformance() {
        String password = "TestPassword123@";
        
        // Test hashing performance
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            PasswordUtil.hashPassword(password);
        }
        long hashTime = System.currentTimeMillis() - startTime;
        
        // Hashing should be reasonably fast (less than 5 seconds for 100 operations)
        assertTrue(hashTime < 5000, "Hashing took too long: " + hashTime + "ms");
        
        // Test verification performance
        String hashedPassword = PasswordUtil.hashPassword(password);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            PasswordUtil.verifyPassword(password, hashedPassword);
        }
        long verifyTime = System.currentTimeMillis() - startTime;
        
        // Verification should be reasonably fast (less than 2 seconds for 100 operations)
        assertTrue(verifyTime < 2000, "Verification took too long: " + verifyTime + "ms");
    }
} 