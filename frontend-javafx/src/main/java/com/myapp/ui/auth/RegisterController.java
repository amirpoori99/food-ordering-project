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

/**
 * Controller for Register screen
 * Handles user registration, validation, and navigation
 */
public class RegisterController implements Initializable {

    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextArea addressField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private NavigationController navigationController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        setupUI();
        populateRoles();
    }

    /**
     * Set navigation controller (for testing)
     */
    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Enable/disable register button based on input
        fullNameField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        phoneField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        passwordField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        addressField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        roleComboBox.valueProperty().addListener((obs, oldValue, newValue) -> updateRegisterButtonState());
        
        // --- Added: wire up action handlers for unit tests (FXML covers runtime) ---
        registerButton.setOnAction(e -> handleRegister());
        loginLink.setOnAction(e -> handleLoginLink());
        // -----------------------------------------------------------------------
        
        // Initial state
        updateRegisterButtonState();
        clearStatus();
    }

    /**
     * Populate role combo box
     */
    private void populateRoles() {
        roleComboBox.getItems().addAll("BUYER", "SELLER", "COURIER");
        roleComboBox.setValue("BUYER"); // Default selection
    }

    /**
     * Handle register button click
     */
    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String address = addressField.getText().trim();
        String role = roleComboBox.getValue();
        
        // Validation
        if (!validateInput(fullName, phone, email, password, confirmPassword, address, role)) {
            return;
        }
        
        // Start registration process
        setLoading(true);
        clearStatus();
        
        // Create background task for registration
        Task<HttpClientUtil.ApiResponse> registerTask = new Task<HttpClientUtil.ApiResponse>() {
            @Override
            protected HttpClientUtil.ApiResponse call() throws Exception {
                return HttpClientUtil.register(fullName, phone, email, password, address);
            }
        };
        
        registerTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                HttpClientUtil.ApiResponse response = registerTask.getValue();
                handleRegisterResponse(response);
            });
        });
        
        registerTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                Throwable exception = registerTask.getException();
                handleRegisterError(exception);
            });
        });
        
        // Run task in background thread
        Thread registerThread = new Thread(registerTask);
        registerThread.setDaemon(true);
        registerThread.start();
    }

    /**
     * Validate user input
     */
    private boolean validateInput(String fullName, String phone, String email, 
                                 String password, String confirmPassword, String address, String role) {
        if (fullName == null || fullName.trim().isEmpty()) {
            showError("لطفاً نام کامل را وارد کنید");
            fullNameField.requestFocus();
            return false;
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            showError("لطفاً شماره تلفن را وارد کنید");
            phoneField.requestFocus();
            return false;
        }
        
        // Basic phone number validation
        if (!phone.trim().matches("^09\\d{9}$")) {
            showError("شماره تلفن باید با 09 شروع شود و 11 رقم باشد");
            phoneField.requestFocus();
            return false;
        }
        
        if (password == null || password.isEmpty()) {
            showError("لطفاً رمز عبور را وارد کنید");
            passwordField.requestFocus();
            return false;
        }
        
        if (password.length() < 4) {
            showError("رمز عبور باید حداقل 4 کاراکتر باشد");
            passwordField.requestFocus();
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("رمز عبور و تکرار آن یکسان نیستند");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        if (address == null || address.trim().isEmpty()) {
            showError("لطفاً آدرس را وارد کنید");
            addressField.requestFocus();
            return false;
        }
        
        if (role == null || role.isEmpty()) {
            showError("لطفاً نقش را انتخاب کنید");
            roleComboBox.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Handle register response
     */
    private void handleRegisterResponse(HttpClientUtil.ApiResponse response) {
        if (response.isSuccess()) {
            showSuccess("ثبت‌نام موفقیت‌آمیز بود! اکنون وارد شوید");
            
            // Navigate to login screen after short delay
            Platform.runLater(() -> {
                Task<Void> delayTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Thread.sleep(2000);
                        return null;
                    }
                };
                delayTask.setOnSucceeded(event -> {
                    navigationController.navigateTo(NavigationController.LOGIN_SCENE);
                });
                new Thread(delayTask).start();
            });
            
        } else {
            String errorMessage = response.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "خطا در ثبت‌نام";
            }
            showError(errorMessage);
        }
    }

    /**
     * Handle register error
     */
    private void handleRegisterError(Throwable exception) {
        String errorMessage = "خطا در اتصال به سرور";
        
        if (exception instanceof IOException) {
            errorMessage = "خطا در اتصال به سرور. لطفاً اتصال اینترنت خود را بررسی کنید";
        } else if (exception != null && exception.getMessage() != null) {
            errorMessage = exception.getMessage();
        }
        
        showError(errorMessage);
    }

    /**
     * Handle login link click
     */
    @FXML
    public void handleLoginLink() {
        navigationController.navigateTo(NavigationController.LOGIN_SCENE);
    }

    /**
     * Update register button state based on input fields
     */
    private void updateRegisterButtonState() {
        boolean hasInput = !fullNameField.getText().trim().isEmpty() && 
                          !phoneField.getText().trim().isEmpty() &&
                          !passwordField.getText().isEmpty() &&
                          !confirmPasswordField.getText().isEmpty() &&
                          !addressField.getText().trim().isEmpty() &&
                          roleComboBox.getValue() != null;
        registerButton.setDisable(!hasInput);
    }

    /**
     * Set loading state
     */
    public void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        registerButton.setDisable(loading);
        fullNameField.setDisable(loading);
        phoneField.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
        addressField.setDisable(loading);
        roleComboBox.setDisable(loading);
        loginLink.setDisable(loading);
        
        if (loading) {
            statusLabel.setText("در حال ثبت‌نام...");
        }
    }

    /**
     * Show error message
     */
    public void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    /**
     * Show success message
     */
    public void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    /**
     * Clear status message
     */
    public void clearStatus() {
        statusLabel.setText("");
        statusLabel.setStyle("");
    }

    public String getStatusText() {return statusLabel.getText();}
    public boolean isLoadingVisible() {return loadingIndicator.isVisible();}
}
