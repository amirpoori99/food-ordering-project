package com.myapp.ui.auth;

import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.common.NavigationController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Controller for Login screen
 * Handles user authentication, validation, and navigation
 */
public class LoginController implements Initializable {

    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private NavigationController navigationController;
    private Preferences preferences;
    private boolean isLoginInProgress = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        this.preferences = Preferences.userNodeForPackage(LoginController.class);
        
        setupUI();
        loadSavedCredentials();
    }

    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Enable/disable login button based on input
        phoneField.textProperty().addListener((obs, oldText, newText) -> updateLoginButtonState());
        passwordField.textProperty().addListener((obs, oldText, newText) -> updateLoginButtonState());
        
        // Enter key handlers
        phoneField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        
        // Initial state
        updateLoginButtonState();
        clearStatus();
    }

    /**
     * Load saved credentials if remember me was checked
     */
    public void loadSavedCredentials() {
        String savedPhone = preferences.get("saved_phone", "");
        boolean rememberMe = preferences.getBoolean("remember_me", false);
        
        if (rememberMe && !savedPhone.isEmpty()) {
            phoneField.setText(savedPhone);
            rememberMeCheckbox.setSelected(true);
        }
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        // Prevent multiple login attempts
        if (isLoginInProgress) {
            return;
        }
        
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (!validateInput(phone, password)) {
            return;
        }
        
        // Start login process
        isLoginInProgress = true;
        setLoading(true);
        clearStatus();
        
        // Create background task for login
        Task<HttpClientUtil.ApiResponse> loginTask = new Task<HttpClientUtil.ApiResponse>() {
            @Override
            protected HttpClientUtil.ApiResponse call() throws Exception {
                return HttpClientUtil.login(phone, password);
            }
        };
        
        loginTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                isLoginInProgress = false;
                setLoading(false);
                HttpClientUtil.ApiResponse response = loginTask.getValue();
                handleLoginResponse(response, phone);
            });
        });
        
        loginTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                isLoginInProgress = false;
                setLoading(false);
                Throwable exception = loginTask.getException();
                handleLoginError(exception);
            });
        });
        
        // Run task in background thread
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }

    /**
     * Validate user input
     */
    private boolean validateInput(String phone, String password) {
        if (phone.isEmpty()) {
            showError("لطفاً شماره تلفن را وارد کنید");
            phoneField.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            showError("لطفاً رمز عبور را وارد کنید");
            passwordField.requestFocus();
            return false;
        }
        
        // Basic phone number validation
        if (!phone.matches("^09\\d{9}$")) {
            showError("شماره تلفن باید با 09 شروع شود و 11 رقم باشد");
            phoneField.requestFocus();
            return false;
        }
        
        if (password.length() < 4) {
            showError("رمز عبور باید حداقل 4 کاراکتر باشد");
            passwordField.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Handle login response
     */
    public void handleLoginResponse(HttpClientUtil.ApiResponse response, String phone) {
        if (response.isSuccess()) {
            showSuccess("ورود موفقیت‌آمیز بود!");
            
            // Save credentials if remember me is checked
            if (rememberMeCheckbox.isSelected()) {
                preferences.put("saved_phone", phone);
                preferences.putBoolean("remember_me", true);
            } else {
                preferences.remove("saved_phone");
                preferences.putBoolean("remember_me", false);
            }
            
            // Navigate to main screen
            navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
            
        } else {
            String errorMessage = response.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "خطا در ورود به سیستم";
            }
            showError(errorMessage);
        }
    }

    /**
     * Handle login error
     */
    public void handleLoginError(Throwable exception) {
        String errorMessage = "خطا در اتصال به سرور";
        
        if (exception == null) {
            errorMessage = "خطا در اتصال به سرور";
        } else if (exception instanceof java.net.SocketTimeoutException || 
                   exception.getMessage() != null && exception.getMessage().contains("timeout")) {
            errorMessage = "خطا در اتصال به سرور. لطفاً اتصال اینترنت خود را بررسی کنید";
        } else if (exception instanceof java.net.UnknownHostException ||
                   exception.getMessage() != null && exception.getMessage().contains("connection")) {
            errorMessage = "خطا در اتصال به سرور. لطفاً اتصال اینترنت خود را بررسی کنید";
        } else if (exception instanceof IOException) {
            errorMessage = "خطا در اتصال به سرور. لطفاً اتصال اینترنت خود را بررسی کنید";
        } else if (exception.getMessage() != null) {
            errorMessage = exception.getMessage();
        }
        
        showError(errorMessage);
    }

    /**
     * Handle register link click
     */
    @FXML
    private void handleRegisterLink() {
        navigationController.navigateTo(NavigationController.REGISTER_SCENE);
    }

    /**
     * Update login button state based on input fields
     */
    private void updateLoginButtonState() {
        boolean hasInput = !phoneField.getText().trim().isEmpty() && 
                          !passwordField.getText().isEmpty();
        loginButton.setDisable(!hasInput);
    }

    /**
     * Set loading state
     */
    public void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        phoneField.setDisable(loading);
        passwordField.setDisable(loading);
        rememberMeCheckbox.setDisable(loading);
        registerLink.setDisable(loading);
        
        if (loading) {
            statusLabel.setText("در حال ورود...");
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    /**
     * Clear status message
     */
    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setStyle("");
    }

    // ==================== PUBLIC METHODS FOR TESTING ====================

    /**
     * Get phone field text (for testing)
     */
    public String getPhoneFieldText() {
        return phoneField != null ? phoneField.getText() : "";
    }

    /**
     * Set phone field text (for testing)
     */
    public void setPhoneFieldText(String text) {
        if (phoneField != null) {
            phoneField.setText(text);
        }
    }

    /**
     * Get password field text (for testing)
     */
    public String getPasswordFieldText() {
        return passwordField != null ? passwordField.getText() : "";
    }

    /**
     * Set password field text (for testing)
     */
    public void setPasswordFieldText(String text) {
        if (passwordField != null) {
            passwordField.setText(text);
        }
    }

    /**
     * Get remember me checkbox state (for testing)
     */
    public boolean isRememberMeSelected() {
        return rememberMeCheckbox != null && rememberMeCheckbox.isSelected();
    }

    /**
     * Set remember me checkbox state (for testing)
     */
    public void setRememberMeSelected(boolean selected) {
        if (rememberMeCheckbox != null) {
            rememberMeCheckbox.setSelected(selected);
        }
    }

    /**
     * Get status label text (for testing)
     */
    public String getStatusText() {
        return statusLabel != null ? statusLabel.getText() : "";
    }

    /**
     * Check if login button is disabled (for testing)
     */
    public boolean isLoginButtonDisabled() {
        return loginButton != null && loginButton.isDisabled();
    }

    /**
     * Check if loading indicator is visible (for testing)
     */
    public boolean isLoadingVisible() {
        return loadingIndicator != null && loadingIndicator.isVisible();
    }

    /**
     * Trigger login programmatically (for testing)
     */
    public void triggerLogin() {
        handleLogin();
    }

    /**
     * Trigger register link programmatically (for testing)
     */
    public void triggerRegisterLink() {
        handleRegisterLink();
    }
    
    
}
