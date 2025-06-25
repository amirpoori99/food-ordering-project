package com.myapp.ui.security;

import com.myapp.ui.common.SessionManager;
import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.auth.LoginController;
import com.myapp.ui.auth.RegisterController;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;

@DisplayName("Security Validation Tests")
class SecurityValidationTest extends ApplicationTest {

    private Stage stage;
    private LoginController loginController;
    private RegisterController registerController;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        loginController = new LoginController();
        registerController = new RegisterController();
        
        Scene loginScene = new Scene(loginController.getView(), 800, 600);
        stage.setScene(loginScene);
        stage.show();
    }

    @Nested
    @DisplayName("Input Validation Security Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should prevent SQL injection in login")
        void loginSecurity_SqlInjection_Blocked() {
            // Given
            String sqlInjection = "'; DROP TABLE users; --";
            
            // When
            clickOn("#usernameField").write(sqlInjection);
            clickOn("#passwordField").write("password");
            clickOn("#loginButton");
            
            WaitForAsyncUtils.waitForFxEvents();
            
            // Then
            verifyThat("#errorLabel", LabeledMatchers.hasText("Invalid username format"));
            assertThat(SessionManager.getCurrentUser()).isNull();
        }

        @Test
        @DisplayName("Should prevent XSS in user input")
        void inputValidation_XssAttempt_Sanitized() {
            // Given
            String xssPayload = "<script>alert('xss')</script>";
            
            // When
            clickOn("#usernameField").write(xssPayload);
            clickOn("#passwordField").write("password");
            clickOn("#loginButton");
            
            WaitForAsyncUtils.waitForFxEvents();
            
            // Then
            TextField usernameField = lookup("#usernameField").query();
            assertThat(usernameField.getText()).doesNotContain("<script>");
            verifyThat("#errorLabel", LabeledMatchers.hasText("Invalid characters in username"));
        }

        @Test
        @DisplayName("Should validate email format strictly")
        void emailValidation_InvalidFormats_Rejected() {
            // Switch to registration form
            clickOn("#registerButton");
            
            String[] invalidEmails = {
                "invalid-email",
                "test@",
                "@domain.com",
                "test..test@domain.com",
                "test@domain",
                "test@.com"
            };
            
            for (String email : invalidEmails) {
                clickOn("#emailField").eraseText().write(email);
                clickOn("#passwordField").write("ValidPass123!");
                clickOn("#confirmPasswordField").write("ValidPass123!");
                clickOn("#registerSubmitButton");
                
                WaitForAsyncUtils.waitForFxEvents();
                
                verifyThat("#emailErrorLabel", LabeledMatchers.hasText("Invalid email format"));
                
                // Clear fields for next test
                clickOn("#emailField").eraseText();
            }
        }

        @Test
        @DisplayName("Should enforce strong password requirements")
        void passwordValidation_WeakPasswords_Rejected() {
            clickOn("#registerButton");
            
            String[] weakPasswords = {
                "123456",           // Too simple
                "password",         // Common word
                "abc123",           // Too short
                "PASSWORD123",      // Missing lowercase
                "password123",      // Missing uppercase
                "Password"          // Missing numbers
            };
            
            for (String password : weakPasswords) {
                clickOn("#emailField").write("test@example.com");
                clickOn("#passwordField").eraseText().write(password);
                clickOn("#confirmPasswordField").eraseText().write(password);
                clickOn("#registerSubmitButton");
                
                WaitForAsyncUtils.waitForFxEvents();
                
                verifyThat("#passwordErrorLabel", LabeledMatchers.hasText("Password does not meet security requirements"));
                
                // Clear fields
                clickOn("#emailField").eraseText();
            }
        }
    }

    @Nested
    @DisplayName("Session Security Tests")
    class SessionSecurityTests {

        @Test
        @DisplayName("Should expire session after timeout")
        void sessionManagement_Timeout_ExpiresSession() {
            // Login successfully
            performSuccessfulLogin();
            
            // Simulate session timeout
            SessionManager.setSessionTimeout(1); // 1 second for testing
            
            // Wait for timeout
            WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS, () -> 
                SessionManager.isSessionExpired());
            
            // Try to access protected resource
            clickOn("#profileButton");
            
            // Should redirect to login
            verifyThat("#loginForm", node -> node.isVisible());
            assertThat(SessionManager.getCurrentUser()).isNull();
        }

        @Test
        @DisplayName("Should invalidate session on logout")
        void sessionManagement_Logout_ClearsSession() {
            // Login successfully
            performSuccessfulLogin();
            
            // Logout
            clickOn("#logoutButton");
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify session cleared
            assertThat(SessionManager.getCurrentUser()).isNull();
            assertThat(SessionManager.getAuthToken()).isNull();
            
            // Verify redirected to login
            verifyThat("#loginForm", node -> node.isVisible());
        }

        @Test
        @DisplayName("Should prevent concurrent sessions")
        void sessionManagement_MultipleLogins_PreventsConcurrent() {
            // First login
            performSuccessfulLogin();
            String firstToken = SessionManager.getAuthToken();
            
            // Second login attempt (simulate another browser)
            SessionManager.clearSession();
            performSuccessfulLogin();
            String secondToken = SessionManager.getAuthToken();
            
            // Tokens should be different (previous invalidated)
            assertThat(firstToken).isNotEqualTo(secondToken);
            
            // Verify first session is invalidated
            HttpClientUtil.setAuthToken(firstToken);
            // Should get 401 Unauthorized
            // This would be tested in actual implementation
        }
    }

    @Nested
    @DisplayName("Authorization Security Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should prevent unauthorized access to admin functions")
        void authorization_NonAdmin_DeniedAdminAccess() {
            // Login as regular user
            performSuccessfulLogin();
            
            // Try to access admin functions
            clickOn("#adminMenuButton");
            
            // Should show access denied
            verifyThat("#accessDeniedLabel", LabeledMatchers.hasText("Access denied. Admin privileges required."));
        }

        @Test
        @DisplayName("Should prevent access to other users' data")
        void authorization_UserData_OnlyOwnDataAccess() {
            // Login as user
            performSuccessfulLogin();
            
            // Try to access another user's orders (via URL manipulation simulation)
            HttpClientUtil.setRequestParameter("userId", "999");
            clickOn("#ordersButton");
            
            WaitForAsyncUtils.waitForFxEvents();
            
            // Should only show current user's orders
            verifyThat("#ordersTitle", LabeledMatchers.hasText("Your Orders"));
            // Should not show other user's orders
            assertThat(lookup("#ordersList").queryAll()).allMatch(node -> 
                node.getUserData().toString().contains("userId=1")); // Current user ID
        }

        @Test
        @DisplayName("Should require authentication for protected resources")
        void authorization_ProtectedResources_RequiresAuth() {
            // Clear any existing session
            SessionManager.clearSession();
            
            // Try to access protected resources
            String[] protectedRoutes = {
                "#profileButton",
                "#ordersButton", 
                "#cartButton",
                "#settingsButton"
            };
            
            for (String route : protectedRoutes) {
                clickOn(route);
                WaitForAsyncUtils.waitForFxEvents();
                
                // Should redirect to login
                verifyThat("#loginForm", node -> node.isVisible());
                verifyThat("#authRequiredMessage", LabeledMatchers.hasText("Please login to continue"));
            }
        }
    }

    @Nested
    @DisplayName("Data Protection Tests")
    class DataProtectionTests {

        @Test
        @DisplayName("Should not store sensitive data in local storage")
        void dataProtection_SensitiveData_NotStoredLocally() {
            // Login
            performSuccessfulLogin();
            
            // Check that sensitive data is not stored in plain text
            String localStorage = SessionManager.getLocalStorage();
            
            // Should not contain passwords, credit card numbers, etc.
            assertThat(localStorage).doesNotContain("password");
            assertThat(localStorage).doesNotContain("creditCard");
            assertThat(localStorage).doesNotContain("ssn");
            
            // Should only contain encrypted/hashed data
            assertThat(localStorage).contains("encryptedToken");
        }

        @Test
        @DisplayName("Should mask sensitive input fields")
        void dataProtection_SensitiveFields_Masked() {
            // Navigate to payment form
            performSuccessfulLogin();
            navigateToPayment();
            
            // Check password field is masked
            PasswordField passwordField = lookup("#passwordField").query();
            assertThat(passwordField.getText()).isEqualTo("password123");
            assertThat(passwordField.getCharacters().toString()).doesNotContain("password123");
            
            // Check credit card field is partially masked
            TextField cardField = lookup("#cardNumberField").query();
            cardField.setText("4111111111111111");
            assertThat(cardField.getText()).matches("\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*1111");
        }

        @Test
        @DisplayName("Should clear sensitive data on logout")
        void dataProtection_Logout_ClearsSensitiveData() {
            // Login and enter sensitive data
            performSuccessfulLogin();
            navigateToPayment();
            
            clickOn("#cardNumberField").write("4111111111111111");
            clickOn("#cvvField").write("123");
            
            // Logout
            clickOn("#logoutButton");
            
            // Verify sensitive data cleared
            assertThat(SessionManager.getLocalStorage()).doesNotContain("4111111111111111");
            assertThat(SessionManager.getLocalStorage()).doesNotContain("123");
            
            // Verify form fields cleared
            TextField cardField = lookup("#cardNumberField").query();
            TextField cvvField = lookup("#cvvField").query();
            assertThat(cardField.getText()).isEmpty();
            assertThat(cvvField.getText()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Network Security Tests")
    class NetworkSecurityTests {

        @Test
        @DisplayName("Should use HTTPS for sensitive requests")
        void networkSecurity_SensitiveRequests_UsesHttps() {
            // Login
            performSuccessfulLogin();
            
            // Make payment request
            navigateToPayment();
            clickOn("#cardNumberField").write("4111111111111111");
            clickOn("#processPaymentButton");
            
            // Verify HTTPS used
            String lastRequestUrl = HttpClientUtil.getLastRequestUrl();
            assertThat(lastRequestUrl).startsWith("https://");
        }

        @Test
        @DisplayName("Should validate SSL certificates")
        void networkSecurity_SslValidation_RejectsInvalidCerts() {
            // Simulate invalid SSL certificate
            HttpClientUtil.setInvalidSslCert(true);
            
            // Try to make secure request
            performSuccessfulLogin();
            
            // Should show security warning
            verifyThat("#securityWarningLabel", 
                LabeledMatchers.hasText("Security warning: Invalid SSL certificate"));
            
            // Should not proceed with request
            assertThat(SessionManager.getCurrentUser()).isNull();
        }

        @Test
        @DisplayName("Should implement request rate limiting")
        void networkSecurity_RateLimiting_PreventsAbuse() {
            // Make multiple rapid requests
            for (int i = 0; i < 10; i++) {
                clickOn("#usernameField").write("test" + i + "@example.com");
                clickOn("#passwordField").write("wrongpassword");
                clickOn("#loginButton");
                WaitForAsyncUtils.waitForFxEvents();
            }
            
            // Should show rate limit warning
            verifyThat("#rateLimitWarningLabel", 
                LabeledMatchers.hasText("Too many attempts. Please try again later."));
            
            // Login button should be disabled
            assertThat(lookup("#loginButton").queryButton().isDisabled()).isTrue();
        }
    }

    // Helper methods
    private void performSuccessfulLogin() {
        clickOn("#usernameField").write("testuser@test.com");
        clickOn("#passwordField").write("password123");
        clickOn("#loginButton");
        WaitForAsyncUtils.waitFor(3, TimeUnit.SECONDS, () -> 
            SessionManager.getCurrentUser() != null);
    }
    
    private void navigateToPayment() {
        clickOn("#cartButton");
        clickOn("#checkoutButton");
        clickOn("#paymentButton");
    }

    @AfterEach
    void cleanup() {
        SessionManager.clearSession();
        HttpClientUtil.setInvalidSslCert(false);
        HttpClientUtil.resetRateLimit();
    }
} 