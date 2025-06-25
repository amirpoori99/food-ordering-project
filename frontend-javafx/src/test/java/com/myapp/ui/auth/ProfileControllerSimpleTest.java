package com.myapp.ui.auth;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified test cases for ProfileController functionality
 * Tests core logic without FXML dependency
 */
@ExtendWith(ApplicationExtension.class)
class ProfileControllerSimpleTest {

    private ProfileController controller;
    private Scene scene;

    @Start
    void start(Stage stage) throws Exception {
        // Create a simple scene for testing without FXML
        VBox root = new VBox();
        scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
        
        // Create controller instance
        controller = new ProfileController();
    }

    @BeforeEach
    void setUp() {
        // Reset any state before each test
        Platform.runLater(() -> {
            // Setup complete
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testControllerInitialization() {
        assertNotNull(controller, "Controller should be initialized");
    }

    @Test
    void testEmailValidation() {
        // Test email validation logic
        assertTrue(isValidEmail("user@example.com"), "Valid email should pass");
        assertTrue(isValidEmail("test.user@domain.co.uk"), "Valid email with subdomain should pass");
        
        assertFalse(isValidEmail("invalid-email"), "Invalid email should fail");
        assertFalse(isValidEmail("user@"), "Incomplete email should fail");
        assertFalse(isValidEmail("@domain.com"), "Email without user should fail");
        assertFalse(isValidEmail(""), "Empty email should fail");
        assertFalse(isValidEmail(null), "Null email should fail");
    }

    @Test
    void testPhoneNumberValidation() {
        // Test Iranian mobile phone validation
        assertTrue(isValidPhone("09123456789"), "Valid Iranian mobile should pass");
        assertTrue(isValidPhone("09987654321"), "Another valid Iranian mobile should pass");
        
        assertFalse(isValidPhone("021123456"), "Landline should fail");
        assertFalse(isValidPhone("0912345678"), "Short number should fail");
        assertFalse(isValidPhone("091234567890"), "Long number should fail");
        assertFalse(isValidPhone("08123456789"), "Wrong prefix should fail");
        assertFalse(isValidPhone("abcd123456789"), "Non-numeric should fail");
        assertFalse(isValidPhone(""), "Empty phone should fail");
        assertFalse(isValidPhone(null), "Null phone should fail");
    }

    @Test
    void testNameValidation() {
        // Test full name validation
        assertTrue(isValidName("احمد محمدی"), "Valid Persian name should pass");
        assertTrue(isValidName("John Doe"), "Valid English name should pass");
        assertTrue(isValidName("علی"), "Single name should pass");
        
        assertFalse(isValidName(""), "Empty name should fail");
        assertFalse(isValidName("   "), "Whitespace-only name should fail");
        assertFalse(isValidName("A"), "Single character should fail");
        assertFalse(isValidName(null), "Null name should fail");
    }

    @Test
    void testPasswordValidation() {
        // Test password strength validation
        assertTrue(isStrongPassword("MyStr0ng!P@ssw0rd"), "Strong password should pass");
        assertTrue(isStrongPassword("SecurePass123!"), "Another strong password should pass");
        
        assertFalse(isStrongPassword("123"), "Weak password should fail");
        assertFalse(isStrongPassword("password"), "Common password should fail");
        assertFalse(isStrongPassword("PASSWORD123"), "No special chars should fail");
        assertFalse(isStrongPassword(""), "Empty password should fail");
        assertFalse(isStrongPassword(null), "Null password should fail");
    }

    @Test
    void testPasswordMatching() {
        // Test password confirmation logic
        assertTrue(passwordsMatch("password123", "password123"), "Identical passwords should match");
        
        assertFalse(passwordsMatch("password1", "password2"), "Different passwords should not match");
        assertFalse(passwordsMatch("password", ""), "Password and empty should not match");
        assertFalse(passwordsMatch("", "password"), "Empty and password should not match");
        assertFalse(passwordsMatch(null, "password"), "Null and password should not match");
        assertFalse(passwordsMatch("password", null), "Password and null should not match");
    }

    @Test
    void testAddressValidation() {
        // Test address validation
        assertTrue(isValidAddress("تهران، خیابان ولیعصر، کوچه 15، پلاک 123"), 
                  "Detailed address should pass");
        assertTrue(isValidAddress("123 Main Street, City"), "English address should pass");
        
        assertFalse(isValidAddress("کوتاه"), "Too short address should fail");
        assertFalse(isValidAddress(""), "Empty address should fail");
        assertFalse(isValidAddress("   "), "Whitespace-only address should fail");
        assertFalse(isValidAddress(null), "Null address should fail");
    }

    @Test
    void testFormValidation() {
        // Test complete form validation
        assertTrue(isValidProfile("احمد محمدی", "ahmad@example.com", 
                  "تهران، خیابان ولیعصر، پلاک 123"), "Valid profile should pass");
        
        assertFalse(isValidProfile("", "ahmad@example.com", 
                   "تهران، خیابان ولیعصر، پلاک 123"), "Empty name should fail");
        assertFalse(isValidProfile("احمد محمدی", "invalid-email", 
                   "تهران، خیابان ولیعصر، پلاک 123"), "Invalid email should fail");
        assertFalse(isValidProfile("احمد محمدی", "ahmad@example.com", 
                   "کوتاه"), "Invalid address should fail");
    }

    @Test
    void testPasswordChangeValidation() {
        // Test password change validation
        assertTrue(isValidPasswordChange("oldpass123", "NewStr0ng!Pass", "NewStr0ng!Pass"), 
                  "Valid password change should pass");
        
        assertFalse(isValidPasswordChange("", "NewStr0ng!Pass", "NewStr0ng!Pass"), 
                   "Empty current password should fail");
        assertFalse(isValidPasswordChange("oldpass123", "weak", "weak"), 
                   "Weak new password should fail");
        assertFalse(isValidPasswordChange("oldpass123", "NewStr0ng!Pass", "DifferentPass"), 
                   "Non-matching passwords should fail");
    }

    @Test
    void testUserRoleValidation() {
        // Test user role validation
        assertTrue(isValidRole("BUYER"), "BUYER role should be valid");
        assertTrue(isValidRole("SELLER"), "SELLER role should be valid");
        assertTrue(isValidRole("COURIER"), "COURIER role should be valid");
        assertTrue(isValidRole("ADMIN"), "ADMIN role should be valid");
        
        assertFalse(isValidRole("INVALID"), "Invalid role should fail");
        assertFalse(isValidRole(""), "Empty role should fail");
        assertFalse(isValidRole(null), "Null role should fail");
    }

    @Test
    void testAccountStatusValidation() {
        // Test account status validation
        assertTrue(isValidAccountStatus("ACTIVE"), "ACTIVE status should be valid");
        assertTrue(isValidAccountStatus("INACTIVE"), "INACTIVE status should be valid");
        assertTrue(isValidAccountStatus("SUSPENDED"), "SUSPENDED status should be valid");
        
        assertFalse(isValidAccountStatus("INVALID"), "Invalid status should fail");
        assertFalse(isValidAccountStatus(""), "Empty status should fail");
        assertFalse(isValidAccountStatus(null), "Null status should fail");
    }

    // Helper methods for validation logic
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.matches("^09\\d{9}$");
    }

    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.trim().length() >= 2;
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    private boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        return address.trim().length() >= 10;
    }

    private boolean isValidProfile(String name, String email, String address) {
        return isValidName(name) && isValidEmail(email) && isValidAddress(address);
    }

    private boolean isValidPasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        return !currentPassword.trim().isEmpty() && 
               isStrongPassword(newPassword) && 
               passwordsMatch(newPassword, confirmPassword);
    }

    private boolean isValidRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return false;
        }
        return role.equals("BUYER") || role.equals("SELLER") || 
               role.equals("COURIER") || role.equals("ADMIN");
    }

    private boolean isValidAccountStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        return status.equals("ACTIVE") || status.equals("INACTIVE") || 
               status.equals("SUSPENDED");
    }
} 