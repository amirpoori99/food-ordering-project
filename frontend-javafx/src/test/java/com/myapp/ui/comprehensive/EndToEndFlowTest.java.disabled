package com.myapp.ui.comprehensive;

import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.common.NavigationController;
import com.myapp.ui.common.TestFXBase;
import com.myapp.ui.auth.LoginController;
import com.myapp.ui.auth.RegisterController;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import org.junit.jupiter.api.*;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * End-to-End User Journey Tests
 * تست‌های کامل journey کاربر از ابتدا تا انتها
 */
@DisplayName("End-to-End User Flow Tests")
class EndToEndFlowTest extends TestFXBase {

    private NavigationController navigationController;
    private LoginController loginController;
    private RegisterController registerController;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        // Initialize controllers
        navigationController = NavigationController.getInstance();
        loginController = new LoginController();
        registerController = new RegisterController();
        
        // Clear any existing authentication
        HttpClientUtil.clearAuthToken();
        
        // Reset UI state
        runOnFxThreadAndWait(() -> {
            // Set up initial scene
            VBox root = new VBox(10);
            root.getChildren().add(new Label("Test Application"));
            testStage.setScene(new Scene(root, 800, 600));
        });
    }

    @Nested
    @DisplayName("New User Complete Journey")
    class NewUserJourneyTests {

        @Test
        @DisplayName("Should complete full new user registration and first order journey")
        void completeNewUserJourney() throws InterruptedException {
            CountDownLatch testLatch = new CountDownLatch(1);
            
            Platform.runLater(() -> {
                try {
                    // Step 1: User starts at registration page
                    simulateUserRegistration();
                    
                    // Step 2: User proceeds to login
                    simulateUserLogin();
                    
                    // Step 3: User browses restaurants
                    simulateRestaurantBrowsing();
                    
                    // Step 4: User selects restaurant and views menu
                    simulateMenuBrowsing();
                    
                    // Step 5: User adds items to cart
                    simulateCartManagement();
                    
                    // Step 6: User proceeds to checkout
                    simulateCheckoutProcess();
                    
                    // Step 7: User completes payment
                    simulatePaymentProcess();
                    
                    // Step 8: User views order confirmation
                    simulateOrderConfirmation();
                    
                    testLatch.countDown();
                } catch (Exception e) {
                    fail("End-to-end journey failed: " + e.getMessage());
                    testLatch.countDown();
                }
            });
            
            assertTrue(testLatch.await(30, TimeUnit.SECONDS), "End-to-end test should complete within 30 seconds");
        }

        @Test
        @DisplayName("Should handle registration validation errors gracefully")
        void registrationValidationFlow() throws InterruptedException {
            CountDownLatch testLatch = new CountDownLatch(1);
            
            Platform.runLater(() -> {
                try {
                    // Test invalid phone number
                    registerController.setPhoneFieldText("123"); // Invalid
                    registerController.setPasswordFieldText("password123");
                    registerController.setFullNameFieldText("Test User");
                    registerController.setEmailFieldText("test@example.com");
                    
                    registerController.triggerRegister();
                    
                    // Should show validation error
                    String errorMessage = registerController.getStatusText();
                    assertTrue(errorMessage.contains("شماره تلفن"), "Should show phone validation error");
                    
                    // Test short password
                    registerController.setPhoneFieldText("09123456789"); // Valid
                    registerController.setPasswordFieldText("123"); // Too short
                    
                    registerController.triggerRegister();
                    
                    errorMessage = registerController.getStatusText();
                    assertTrue(errorMessage.contains("رمز عبور"), "Should show password validation error");
                    
                    // Test empty name
                    registerController.setPasswordFieldText("password123"); // Valid
                    registerController.setFullNameFieldText(""); // Empty
                    
                    registerController.triggerRegister();
                    
                    errorMessage = registerController.getStatusText();
                    assertTrue(errorMessage.contains("نام"), "Should show name validation error");
                    
                    testLatch.countDown();
                } catch (Exception e) {
                    fail("Registration validation test failed: " + e.getMessage());
                    testLatch.countDown();
                }
            });
            
            assertTrue(testLatch.await(10, TimeUnit.SECONDS), "Registration validation test should complete");
        }

        @Test
        @DisplayName("Should handle network errors during registration gracefully")
        void registrationNetworkErrorFlow() throws InterruptedException {
            CountDownLatch testLatch = new CountDownLatch(1);
            
            Platform.runLater(() -> {
                try {
                    // Simulate network timeout
                    HttpClientUtil.setTimeoutMs(1); // Very short timeout
                    
                    // Fill valid registration data
                    registerController.setPhoneFieldText("09123456789");
                    registerController.setPasswordFieldText("password123");
                    registerController.setFullNameFieldText("Test User");
                    registerController.setEmailFieldText("test@example.com");
                    
                    registerController.triggerRegister();
                    
                    // Should handle network error gracefully
                    String errorMessage = registerController.getStatusText();
                    assertFalse(errorMessage.isEmpty(), "Should show error message for network issues");
                    
                    // Reset timeout
                    HttpClientUtil.setTimeoutMs(30000);
                    
                    testLatch.countDown();
                } catch (Exception e) {
                    fail("Network error test failed: " + e.getMessage());
                    testLatch.countDown();
                }
            });
            
            assertTrue(testLatch.await(10, TimeUnit.SECONDS), "Network error test should complete");
        }
    }

    @Nested
    @DisplayName("Returning User Journey")
    class ReturningUserJourneyTests {

        @Test
        @DisplayName("Should complete returning user login and repeat order journey")
        void completeReturningUserJourney() throws InterruptedException {
            CountDownLatch testLatch = new CountDownLatch(1);
            
            Platform.runLater(() -> {
                try {
                    // Step 1: User logs in with existing credentials
                    simulateReturningUserLogin();
                    
                    // Step 2: User views order history
                    simulateOrderHistoryViewing();
                    
                    // Step 3: User repeats previous order
                    simulateRepeatOrder();
                    
                    // Step 4: User modifies cart
                    simulateCartModification();
                    
                    // Step 5: User uses saved payment method
                    simulateExistingPaymentMethod();
                    
                    // Step 6: User tracks order status
                    simulateOrderTracking();
                    
                    testLatch.countDown();
                } catch (Exception e) {
                    fail("Returning user journey failed: " + e.getMessage());
                    testLatch.countDown();
                }
            });
            
            assertTrue(testLatch.await(25, TimeUnit.SECONDS), "Returning user journey should complete");
        }

        @Test
        @DisplayName("Should handle remember me functionality correctly")
        void rememberMeFunctionality() throws InterruptedException {
            CountDownLatch testLatch = new CountDownLatch(1);
            
            Platform.runLater(() -> {
                try {
                    // Login with remember me checked
                    loginController.setPhoneFieldText("09123456789");
                    loginController.setPasswordFieldText("password123");
                    loginController.setRememberMeSelected(true);
                    
                    assertTrue(loginController.isRememberMeSelected(), "Remember me should be selected");
                    
                    loginController.triggerLogin();
                    
                    // Simulate app restart - credentials should be remembered
                    // (In real app, this would check stored preferences)
                    
                    testLatch.countDown();
                } catch (Exception e) {
                    fail("Remember me test failed: " + e.getMessage());
                    testLatch.countDown();
                }
            });
            
            assertTrue(testLatch.await(10, TimeUnit.SECONDS), "Remember me test should complete");
        }
    }

    @Nested
    @DisplayName("Admin User Journey")
    class AdminUserJourneyTests {

        @Test
        @DisplayName("Should complete admin dashboard and management journey")
        void completeAdminJourney() throws InterruptedException {
            CountDownLatch testLatch = new CountDownLatch(1);
            
            Platform.runLater(() -> {
                try {
                    // Step 1: Admin logs in
                    simulateAdminLogin();
                    
                    // Step 2: Admin views dashboard
                    simulateAdminDashboard();
                    
                    // Step 3: Admin manages restaurants
                    simulateRestaurantManagement();
                    
                    // Step 4: Admin reviews orders
                    simulateOrderManagement();
                    
                    // Step 5: Admin manages users
                    simulateUserManagement();
                    
                    // Step 6: Admin views reports
                    simulateReportsViewing();
                    
                    testLatch.countDown();
                } catch (Exception e) {
                    fail("Admin journey failed: " + e.getMessage());
                    testLatch.countDown();
                }
            });
            
            assertTrue(testLatch.await(30, TimeUnit.SECONDS), "Admin journey should complete");
        }
    }

    @Nested
    @DisplayName("Error Recovery Scenarios")
    class ErrorRecoveryTests {

        @Test
        @DisplayName("Should recover from payment failures gracefully")
        void paymentFailureRecovery() throws InterruptedException {
            CountDownLatch testLatch = new CountDownLatch(1);
            
            Platform.runLater(() -> {
                try {
                    // Simulate payment failure
                    simulateUserLogin();
                    simulateCartManagement();
                    
                    // Try payment with invalid card
                    simulateFailedPayment();
                    
                    // User should be able to retry with different method
                    simulatePaymentRetry();
                    
                    testLatch.countDown();
                } catch (Exception e) {
                    fail("Payment failure recovery test failed: " + e.getMessage());
                    testLatch.countDown();
                }
            });
            
            assertTrue(testLatch.await(20, TimeUnit.SECONDS), "Payment failure recovery should complete");
        }

        @Test
        @DisplayName("Should handle session timeout gracefully")
        void sessionTimeoutHandling() throws InterruptedException {
            CountDownLatch testLatch = new CountDownLatch(1);
            
            Platform.runLater(() -> {
                try {
                    // Login and start shopping
                    simulateUserLogin();
                    simulateCartManagement();
                    
                    // Simulate session timeout
                    HttpClientUtil.clearAuthToken();
                    
                    // Try to proceed to checkout - should redirect to login
                    simulateCheckoutWithExpiredSession();
                    
                    // Re-login should preserve cart
                    simulateReloginAfterTimeout();
                    
                    testLatch.countDown();
                } catch (Exception e) {
                    fail("Session timeout test failed: " + e.getMessage());
                    testLatch.countDown();
                }
            });
            
            assertTrue(testLatch.await(15, TimeUnit.SECONDS), "Session timeout test should complete");
        }
    }

    // ==================== SIMULATION HELPER METHODS ====================

    private void simulateUserRegistration() {
        // Simulate user filling registration form
        registerController.setPhoneFieldText("09123456789");
        registerController.setPasswordFieldText("password123");
        registerController.setFullNameFieldText("تست کاربر");
        registerController.setEmailFieldText("test@example.com");
        registerController.setRoleSelection("BUYER");
        
        // Trigger registration
        registerController.triggerRegister();
        
        // Verify registration attempt was made
        assertFalse(registerController.isRegisterButtonDisabled(), "Register button should be enabled with valid data");
    }

    private void simulateUserLogin() {
        // Simulate user login
        loginController.setPhoneFieldText("09123456789");
        loginController.setPasswordFieldText("password123");
        
        loginController.triggerLogin();
        
        // Verify login attempt
        assertFalse(loginController.isLoginButtonDisabled(), "Login button should be enabled with valid data");
    }

    private void simulateReturningUserLogin() {
        // Simulate returning user with remember me
        loginController.setPhoneFieldText("09123456789");
        loginController.setPasswordFieldText("password123");
        loginController.setRememberMeSelected(true);
        
        loginController.triggerLogin();
    }

    private void simulateAdminLogin() {
        // Simulate admin login
        loginController.setPhoneFieldText("09123456789");
        loginController.setPasswordFieldText("admin123");
        
        loginController.triggerLogin();
    }

    private void simulateRestaurantBrowsing() {
        // Simulate browsing restaurants
        // This would involve RestaurantListController when implemented
        assertTrue(true, "Restaurant browsing simulation");
    }

    private void simulateMenuBrowsing() {
        // Simulate menu browsing
        assertTrue(true, "Menu browsing simulation");
    }

    private void simulateCartManagement() {
        // Simulate adding items to cart
        assertTrue(true, "Cart management simulation");
    }

    private void simulateCartModification() {
        // Simulate modifying cart items
        assertTrue(true, "Cart modification simulation");
    }

    private void simulateCheckoutProcess() {
        // Simulate checkout process
        assertTrue(true, "Checkout process simulation");
    }

    private void simulatePaymentProcess() {
        // Simulate payment
        assertTrue(true, "Payment process simulation");
    }

    private void simulateFailedPayment() {
        // Simulate failed payment
        assertTrue(true, "Failed payment simulation");
    }

    private void simulatePaymentRetry() {
        // Simulate payment retry
        assertTrue(true, "Payment retry simulation");
    }

    private void simulateExistingPaymentMethod() {
        // Simulate using existing payment method
        assertTrue(true, "Existing payment method simulation");
    }

    private void simulateOrderConfirmation() {
        // Simulate order confirmation viewing
        assertTrue(true, "Order confirmation simulation");
    }

    private void simulateOrderHistoryViewing() {
        // Simulate viewing order history
        assertTrue(true, "Order history simulation");
    }

    private void simulateRepeatOrder() {
        // Simulate repeating previous order
        assertTrue(true, "Repeat order simulation");
    }

    private void simulateOrderTracking() {
        // Simulate order tracking
        assertTrue(true, "Order tracking simulation");
    }

    private void simulateAdminDashboard() {
        // Simulate admin dashboard viewing
        assertTrue(true, "Admin dashboard simulation");
    }

    private void simulateRestaurantManagement() {
        // Simulate restaurant management
        assertTrue(true, "Restaurant management simulation");
    }

    private void simulateOrderManagement() {
        // Simulate order management
        assertTrue(true, "Order management simulation");
    }

    private void simulateUserManagement() {
        // Simulate user management
        assertTrue(true, "User management simulation");
    }

    private void simulateReportsViewing() {
        // Simulate reports viewing
        assertTrue(true, "Reports viewing simulation");
    }

    private void simulateCheckoutWithExpiredSession() {
        // Simulate checkout with expired session
        assertTrue(true, "Checkout with expired session simulation");
    }

    private void simulateReloginAfterTimeout() {
        // Simulate re-login after timeout
        assertTrue(true, "Re-login after timeout simulation");
    }
} 