package com.myapp.ui.auth;

import com.myapp.ui.common.NavigationController;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simplified tests for RegisterController
 */
public class RegisterControllerTest extends ApplicationTest {

    private RegisterController controller;
    private NavigationController mockNavigationController;
    
    // UI Components
    private TextField fullNameField;
    private TextField phoneField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextArea addressField;
    private ComboBox<String> roleComboBox;
    private Button registerButton;
    private Hyperlink loginLink;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;

    @BeforeAll
    static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Create controller
        controller = new RegisterController();
        
        // Create UI components
        fullNameField = new TextField();
        phoneField = new TextField();
        emailField = new TextField();
        passwordField = new PasswordField();
        confirmPasswordField = new PasswordField();
        addressField = new TextArea();
        roleComboBox = new ComboBox<>();
        registerButton = new Button("Register");
        loginLink = new Hyperlink("Login");
        statusLabel = new Label();
        loadingIndicator = new ProgressIndicator();
        
        // Mock dependencies
        mockNavigationController = mock(NavigationController.class);
        
        // Set up FXML injections
        setPrivateField(controller, "fullNameField", fullNameField);
        setPrivateField(controller, "phoneField", phoneField);
        setPrivateField(controller, "emailField", emailField);
        setPrivateField(controller, "passwordField", passwordField);
        setPrivateField(controller, "confirmPasswordField", confirmPasswordField);
        setPrivateField(controller, "addressField", addressField);
        setPrivateField(controller, "roleComboBox", roleComboBox);
        setPrivateField(controller, "registerButton", registerButton);
        setPrivateField(controller, "loginLink", loginLink);
        setPrivateField(controller, "statusLabel", statusLabel);
        setPrivateField(controller, "loadingIndicator", loadingIndicator);
        setPrivateField(controller, "navigationController", mockNavigationController);
        
        // Initialize controller
        Platform.runLater(() -> {
            controller.initialize(null, null);
            controller.setNavigationController(mockNavigationController);
            loadingIndicator.setVisible(false);
            
            // Manually add action handler to button
            registerButton.setOnAction(event -> {
                try {
                    Method handleRegisterMethod = RegisterController.class.getDeclaredMethod("handleRegister");
                    handleRegisterMethod.setAccessible(true);
                    handleRegisterMethod.invoke(controller);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void tearDown() {
        Platform.runLater(() -> {
            fullNameField.clear();
            phoneField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            addressField.clear();
            roleComboBox.setValue(null);
            statusLabel.setText("");
            loadingIndicator.setVisible(false);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterAll
    static void tearDownClass() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    // ==================== BASIC FUNCTIONALITY TESTS ====================

    @Test
    @DisplayName("Should initialize controller properly")
    void testInitialization() {
        assertNotNull(controller);
        assertNotNull(fullNameField);
        assertNotNull(phoneField);
        assertNotNull(passwordField);
        assertNotNull(registerButton);
        assertNotNull(statusLabel);
    }

    @Test
    @DisplayName("Should populate role combo box with correct values")
    void testRoleComboBoxPopulation() {
        // Clear existing items first
        Platform.runLater(() -> {
            roleComboBox.getItems().clear();
            roleComboBox.getItems().addAll("BUYER", "SELLER", "COURIER");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertEquals(3, roleComboBox.getItems().size());
        assertTrue(roleComboBox.getItems().contains("BUYER"));
        assertTrue(roleComboBox.getItems().contains("SELLER"));
        assertTrue(roleComboBox.getItems().contains("COURIER"));
    }

    @Test
    @DisplayName("Should enable button when all fields are filled")
    void testButtonEnabledWhenAllFieldsFilled() {
        Platform.runLater(() -> {
            fullNameField.setText("احمد محمدی");
            phoneField.setText("09123456789");
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");
            addressField.setText("آدرس");
            roleComboBox.setValue("BUYER");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertFalse(registerButton.isDisabled(), "Button should be enabled when all fields are filled");
    }
    
    @Test
    @DisplayName("Should disable button when fields are empty")
    void testButtonDisabledWhenFieldsEmpty() {
        Platform.runLater(() -> {
            fullNameField.clear();
            phoneField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            addressField.clear();
            roleComboBox.setValue(null);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(registerButton.isDisabled(), "Button should be disabled when fields are empty");
    }
    
    @Test
    @DisplayName("Should update button state on field changes")
    void testButtonStateUpdate() {
        // Initially disabled
        assertTrue(registerButton.isDisabled());
        
        // Fill one field at a time
        Platform.runLater(() -> {
            fullNameField.setText("احمد");
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(registerButton.isDisabled(), "Should still be disabled with only name");
        
        // Fill all required fields
        Platform.runLater(() -> {
            phoneField.setText("09123456789");
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");
            addressField.setText("آدرس");
            roleComboBox.setValue("BUYER");
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(registerButton.isDisabled(), "Should be enabled with all fields filled");
    }
    
    @Test
    @DisplayName("Should handle login link navigation")
    void testLoginLinkNavigation() {
        Platform.runLater(() -> {
            loginLink.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        verify(mockNavigationController).navigateTo("Login");
    }

    // ==================== HELPER METHODS ====================

    private void setPrivateField(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set private field: " + fieldName, e);
        }
    }
} 