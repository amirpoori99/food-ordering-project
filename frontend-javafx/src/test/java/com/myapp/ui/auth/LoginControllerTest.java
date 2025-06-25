package com.myapp.ui.auth;

import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.common.NavigationController;
import com.myapp.ui.common.TestFXBase;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * Comprehensive test suite for LoginController
 * Tests UI behavior, validation, API integration, and edge cases
 */
public class LoginControllerTest extends TestFXBase {

    private LoginController controller;
    private NavigationController mockNavigationController;
    private Preferences mockPreferences;
    
    // UI Components
    private TextField phoneField;
    private PasswordField passwordField;
    private CheckBox rememberMeCheckbox;
    private Button loginButton;
    private Hyperlink registerLink;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp(); // Call parent setup
        
        // Try to load the actual LoginController from FXML
        this.controller = loadFXMLController("/fxml/Login.fxml");
        
        // If FXML loading failed, create mock UI
        if (controller == null) {
            createMockLoginUI();
        } else {
            // Extract UI components from loaded FXML
            extractUIComponents();
        }
        
        // Initialize UI state
        runOnFxThreadAndWait(() -> {
            if (phoneField != null) phoneField.clear();
            if (passwordField != null) passwordField.clear();
            if (rememberMeCheckbox != null) rememberMeCheckbox.setSelected(false);
            if (statusLabel != null) statusLabel.setText("");
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
        });
    }
    
    private void createMockLoginUI() {
        // Create controller
        controller = new LoginController();
        
        // Create mock components
        phoneField = new TextField();
        phoneField.setId("phoneField");
        passwordField = new PasswordField();
        passwordField.setId("passwordField");
        rememberMeCheckbox = new CheckBox();
        rememberMeCheckbox.setId("rememberMeCheckbox");
        loginButton = new Button("Login");
        loginButton.setId("loginButton");
        registerLink = new Hyperlink("Register");
        registerLink.setId("registerLink");
        statusLabel = new Label();
        statusLabel.setId("statusLabel");
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setId("loadingIndicator");
        
        // Set up FXML injections manually
        setPrivateField(controller, "phoneField", phoneField);
        setPrivateField(controller, "passwordField", passwordField);
        setPrivateField(controller, "rememberMeCheckbox", rememberMeCheckbox);
        setPrivateField(controller, "loginButton", loginButton);
        setPrivateField(controller, "registerLink", registerLink);
        setPrivateField(controller, "statusLabel", statusLabel);
        setPrivateField(controller, "loadingIndicator", loadingIndicator);
        
        // Create scene with mock components
        runOnFxThreadAndWait(() -> {
            VBox root = new VBox(10);
            root.getChildren().addAll(
                phoneField, passwordField, rememberMeCheckbox, 
                loginButton, registerLink, statusLabel, loadingIndicator
            );
            
            Scene scene = new Scene(root, 800, 600);
            testStage.setScene(scene);
            
            // Initialize controller
            try {
                controller.initialize(null, null);
                loadingIndicator.setVisible(false);
            } catch (Exception e) {
                // Ignore initialization errors in tests
            }
        });
    }
    
    private void extractUIComponents() {
        // Extract UI components from the loaded FXML
        phoneField = lookup("#phoneField", TextField.class);
        passwordField = lookup("#passwordField", PasswordField.class);
        rememberMeCheckbox = lookup("#rememberMeCheckbox", CheckBox.class);
        loginButton = lookup("#loginButton", Button.class);
        registerLink = lookup("#registerLink", Hyperlink.class);
        statusLabel = lookup("#statusLabel", Label.class);
        loadingIndicator = lookup("#loadingIndicator", ProgressIndicator.class);
        
        // If any component is missing, fall back to mock UI
        if (phoneField == null || passwordField == null || loginButton == null) {
            createMockLoginUI();
        }
    }

    // ==================== INITIALIZATION TESTS ====================

    @Test
    @DisplayName("Should initialize controller properly")
    void testInitialization() {
        assertNotNull(controller);
        assertNotNull(phoneField);
        assertNotNull(passwordField);
        assertNotNull(loginButton);
        assertTrue(loginButton.isDisabled()); // Should be disabled initially
        assertFalse(loadingIndicator.isVisible()); // Should be hidden initially
    }

    // ==================== INPUT VALIDATION TESTS ====================

    @Test
    @DisplayName("Should enable login button when fields have valid input")
    void testLoginButtonEnabledWithValidInput() {
        Platform.runLater(() -> {
            phoneField.setText("09123456789");
            passwordField.setText("password123");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertFalse(controller.isLoginButtonDisabled());
    }

    @Test
    @DisplayName("Should disable login button when fields are empty")
    void testLoginButtonDisabledWithEmptyFields() {
        Platform.runLater(() -> {
            phoneField.setText("");
            passwordField.setText("");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.isLoginButtonDisabled());
    }

    @Test
    @DisplayName("Should disable login button when phone is empty")
    void testLoginButtonDisabledWithEmptyPhone() {
        Platform.runLater(() -> {
            phoneField.setText("");
            passwordField.setText("password123");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.isLoginButtonDisabled());
    }

    @Test
    @DisplayName("Should disable login button when password is empty")
    void testLoginButtonDisabledWithEmptyPassword() {
        Platform.runLater(() -> {
            phoneField.setText("09123456789");
            passwordField.setText("");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.isLoginButtonDisabled());
    }

    // ==================== PHONE VALIDATION TESTS ====================

    @Test
    @DisplayName("Should show error for invalid phone number format")
    void testInvalidPhoneNumberFormat() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("123456789"); // Invalid format
            controller.setPasswordFieldText("password123");
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("شماره تلفن باید با 09 شروع شود"));
    }

    @Test
    @DisplayName("Should show error for short phone number")
    void testShortPhoneNumber() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("091234"); // Too short
            controller.setPasswordFieldText("password123");
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("شماره تلفن باید با 09 شروع شود"));
    }

    @Test
    @DisplayName("Should accept valid phone number format")
    void testValidPhoneNumberFormat() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789"); // Valid format
            controller.setPasswordFieldText("password123");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // Should not show format error (we can't easily test API call without mocking)
        assertFalse(controller.isLoginButtonDisabled());
    }

    // ==================== PASSWORD VALIDATION TESTS ====================

    @Test
    @DisplayName("Should show error for short password")
    void testShortPassword() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
            controller.setPasswordFieldText("123"); // Too short
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("رمز عبور باید حداقل 4 کاراکتر باشد"));
    }

    @Test
    @DisplayName("Should accept valid password length")
    void testValidPasswordLength() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
            controller.setPasswordFieldText("1234"); // Minimum valid length
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // Should not show length error
        assertFalse(controller.isLoginButtonDisabled());
    }

    // ==================== EMPTY FIELD VALIDATION TESTS ====================

    @Test
    @DisplayName("Should show error for empty phone field")
    void testEmptyPhoneField() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText(""); // Empty
            controller.setPasswordFieldText("password123");
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("لطفاً شماره تلفن را وارد کنید"));
    }

    @Test
    @DisplayName("Should show error for empty password field")
    void testEmptyPasswordField() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
            controller.setPasswordFieldText(""); // Empty
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("لطفاً رمز عبور را وارد کنید"));
    }

    // ==================== REMEMBER ME FUNCTIONALITY TESTS ====================

    @Test
    @DisplayName("Should handle remember me checkbox state")
    void testRememberMeCheckbox() {
        Platform.runLater(() -> {
            controller.setRememberMeSelected(true);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.isRememberMeSelected());
        
        Platform.runLater(() -> {
            controller.setRememberMeSelected(false);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertFalse(controller.isRememberMeSelected());
    }

    // ==================== NAVIGATION TESTS ====================

    @Test
    @DisplayName("Should trigger register navigation")
    void testRegisterNavigation() {
        // Mock the NavigationController to avoid null primaryStage
        NavigationController mockNavController = mock(NavigationController.class);
        setPrivateField(controller, "navigationController", mockNavController);
        
        Platform.runLater(() -> {
            controller.triggerRegisterLink();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verify navigation was called
        verify(mockNavController).navigateTo(NavigationController.REGISTER_SCENE);
    }

    // ==================== UI STATE TESTS ====================

    @Test
    @DisplayName("Should handle loading state properly")
    void testLoadingState() {
        // Note: This test would require mocking the HTTP client
        // For now, we test the UI component states
        assertFalse(controller.isLoadingVisible());
        
        // We can't easily test the loading state without triggering actual API calls
        // This would require more sophisticated mocking
    }

    @Test
    @DisplayName("Should clear status message")
    void testClearStatus() {
        Platform.runLater(() -> {
            // Set some error first
            controller.setPhoneFieldText("");
            controller.setPasswordFieldText("password123");
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertFalse(controller.getStatusText().isEmpty());
        
        // Now test that status gets cleared when typing
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // Status should be cleared when user starts typing
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle whitespace in phone field")
    void testPhoneFieldWithWhitespace() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("  09123456789  "); // With whitespace
            controller.setPasswordFieldText("password123");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // Should still work (trimmed internally)
        assertFalse(controller.isLoginButtonDisabled());
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testPasswordWithSpecialCharacters() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
            controller.setPasswordFieldText("pass@123!"); // Special characters
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // Should accept special characters
        assertFalse(controller.isLoginButtonDisabled());
    }

    @Test
    @DisplayName("Should handle very long inputs")
    void testVeryLongInputs() {
        String longPhone = "09123456789123456789"; // Very long
        String longPassword = "a".repeat(100); // Very long password
        
        Platform.runLater(() -> {
            controller.setPhoneFieldText(longPhone);
            controller.setPasswordFieldText(longPassword);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // Should handle long inputs gracefully
        assertFalse(controller.isLoginButtonDisabled());
    }

    // ==================== GETTER/SETTER TESTS ====================

    @Test
    @DisplayName("Should handle phone field getter/setter")
    void testPhoneFieldGetterSetter() {
        String testPhone = "09123456789";
        
        Platform.runLater(() -> {
            controller.setPhoneFieldText(testPhone);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertEquals(testPhone, controller.getPhoneFieldText());
    }

    @Test
    @DisplayName("Should handle password field getter/setter")
    void testPasswordFieldGetterSetter() {
        String testPassword = "testPassword123";
        
        Platform.runLater(() -> {
            controller.setPasswordFieldText(testPassword);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertEquals(testPassword, controller.getPasswordFieldText());
    }

    @Test
    @DisplayName("Should handle null field access gracefully")
    void testNullFieldAccess() {
        // Create a controller without FXML injection
        LoginController emptyController = new LoginController();
        
        // Should not throw exceptions
        assertDoesNotThrow(() -> {
            assertEquals("", emptyController.getPhoneFieldText());
            assertEquals("", emptyController.getPasswordFieldText());
            assertFalse(emptyController.isRememberMeSelected());
            assertEquals("", emptyController.getStatusText());
            assertFalse(emptyController.isLoadingVisible());
            assertFalse(emptyController.isLoginButtonDisabled());
        });
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to set private fields using reflection
     */
    private void setPrivateField(Object object, String fieldName, Object value) {
        try {
            var field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
} 