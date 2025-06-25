package com.myapp.ui.auth;

import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.common.NavigationController;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for User Profile management screen
 * Handles profile viewing, editing, and password change
 */
public class ProfileController implements Initializable {

    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressField;
    @FXML private Label roleLabel;
    @FXML private Label accountStatusLabel;
    @FXML private Label memberSinceLabel;
    
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button refreshButton;
    @FXML private Button changePasswordButton;
    @FXML private Button clearPasswordButton;
    
    @FXML private MenuItem backMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem cartMenuItem;
    @FXML private MenuItem logoutMenuItem;
    
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private NavigationController navigationController;
    private UserProfile originalProfile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        
        setupUI();
        loadProfile();
    }

    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Make phone field read-only (cannot change phone number)
        phoneField.setEditable(false);
        phoneField.setStyle("-fx-background-color: #e9ecef;");
        
        // Setup validation listeners
        setupValidationListeners();
        
        // Initial status
        setStatus("در حال بارگذاری پروفایل...");
    }

    /**
     * Setup validation listeners for form fields
     */
    private void setupValidationListeners() {
        // Enable save button only when changes are made
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        addressField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        
        // Password validation
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswordFields());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswordFields());
    }

    /**
     * Load user profile from backend
     */
    private void loadProfile() {
        setLoading(true);
        
        Task<UserProfile> loadTask = new Task<UserProfile>() {
            @Override
            protected UserProfile call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/auth/profile");
                
                if (response.isSuccess() && response.getData() != null) {
                    return parseUserProfile(response.getData());
                } else {
                    throw new RuntimeException(response.getMessage() != null ? 
                                               response.getMessage() : "خطا در بارگذاری پروفایل");
                }
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                UserProfile profile = loadTask.getValue();
                displayProfile(profile);
                originalProfile = profile.copy();
                setStatus("پروفایل بارگذاری شد");
            });
        });
        
        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                Throwable exception = loadTask.getException();
                String errorMessage = "خطا در بارگذاری پروفایل";
                if (exception != null && exception.getMessage() != null) {
                    errorMessage = exception.getMessage();
                }
                setStatus(errorMessage);
                showError("خطا", errorMessage);
            });
        });
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /**
     * Parse user profile from JSON response
     */
    private UserProfile parseUserProfile(JsonNode data) {
        UserProfile profile = new UserProfile();
        profile.setId(data.get("userId").asLong());
        profile.setFullName(data.get("fullName").asText());
        profile.setPhone(data.get("phone").asText());
        profile.setEmail(data.has("email") ? data.get("email").asText() : "");
        profile.setAddress(data.has("address") ? data.get("address").asText() : "");
        profile.setRole(data.get("role").asText());
        profile.setActive(data.has("isActive") ? data.get("isActive").asBoolean() : true);
        return profile;
    }

    /**
     * Display user profile in form fields
     */
    private void displayProfile(UserProfile profile) {
        fullNameField.setText(profile.getFullName());
        phoneField.setText(profile.getPhone());
        emailField.setText(profile.getEmail());
        addressField.setText(profile.getAddress());
        roleLabel.setText(getRoleText(profile.getRole()));
        accountStatusLabel.setText(profile.isActive() ? "فعال" : "غیرفعال");
        accountStatusLabel.setStyle("-fx-text-fill: " + (profile.isActive() ? "#28a745" : "#dc3545"));
        memberSinceLabel.setText("کاربر سیستم");
    }

    /**
     * Get Persian text for user role
     */
    private String getRoleText(String role) {
        switch (role.toUpperCase()) {
            case "BUYER": return "خریدار";
            case "SELLER": return "فروشنده";
            case "COURIER": return "پیک";
            case "ADMIN": return "مدیر";
            default: return role;
        }
    }

    /**
     * Check if profile has been modified
     */
    private void checkForChanges() {
        if (originalProfile == null) return;
        
        boolean hasChanges = !fullNameField.getText().equals(originalProfile.getFullName()) ||
                           !emailField.getText().equals(originalProfile.getEmail()) ||
                           !addressField.getText().equals(originalProfile.getAddress());
        
        saveButton.setDisable(!hasChanges);
        cancelButton.setDisable(!hasChanges);
    }

    /**
     * Validate password fields for password change
     */
    private void validatePasswordFields() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String currentPassword = currentPasswordField.getText();
        
        boolean passwordsMatch = newPassword.equals(confirmPassword);
        boolean hasCurrentPassword = !currentPassword.trim().isEmpty();
        boolean hasNewPassword = !newPassword.trim().isEmpty();
        boolean passwordLengthValid = newPassword.length() >= 4;
        
        boolean canChangePassword = hasCurrentPassword && hasNewPassword && 
                                  passwordsMatch && passwordLengthValid;
        
        changePasswordButton.setDisable(!canChangePassword);
    }

    /**
     * Handle save profile changes
     */
    @FXML
    private void handleSave() {
        if (!validateProfileData()) {
            return;
        }
        
        setLoading(true);
        setStatus("در حال ذخیره تغییرات...");
        
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                UserProfile updatedProfile = new UserProfile();
                updatedProfile.setFullName(fullNameField.getText().trim());
                updatedProfile.setEmail(emailField.getText().trim());
                updatedProfile.setAddress(addressField.getText().trim());
                
                HttpClientUtil.ApiResponse response = HttpClientUtil.put("/auth/profile", updatedProfile);
                
                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getMessage() != null ? 
                                               response.getMessage() : "خطا در ذخیره تغییرات");
                }
                
                return null;
            }
        };
        
        saveTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                setStatus("تغییرات با موفقیت ذخیره شد");
                showSuccess("موفقیت", "اطلاعات پروفایل با موفقیت به‌روزرسانی شد");
                loadProfile(); // Reload to get updated data
            });
        });
        
        saveTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                Throwable exception = saveTask.getException();
                String errorMessage = "خطا در ذخیره تغییرات";
                if (exception != null && exception.getMessage() != null) {
                    errorMessage = exception.getMessage();
                }
                setStatus(errorMessage);
                showError("خطا", errorMessage);
            });
        });
        
        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }

    /**
     * Validate profile data before saving
     */
    private boolean validateProfileData() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (fullName.isEmpty()) {
            showError("خطای اعتبارسنجی", "نام کامل نمی‌تواند خالی باشد");
            fullNameField.requestFocus();
            return false;
        }
        
        if (!email.isEmpty() && !isValidEmail(email)) {
            showError("خطای اعتبارسنجی", "فرمت ایمیل صحیح نیست");
            emailField.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    /**
     * Handle change password
     */
    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (!newPassword.equals(confirmPassword)) {
            showError("خطا", "رمز عبور جدید و تکرار آن یکسان نیستند");
            return;
        }
        
        if (newPassword.length() < 4) {
            showError("خطا", "رمز عبور جدید باید حداقل 4 کاراکتر باشد");
            return;
        }
        
        setLoading(true);
        setStatus("در حال تغییر رمز عبور...");
        
        // TODO: Implement password change API call
        // For now, show a placeholder message
        Platform.runLater(() -> {
            setLoading(false);
            setStatus("تغییر رمز عبور در نسخه آینده پیاده‌سازی خواهد شد");
            showInfo("اطلاع", "قابلیت تغییر رمز عبور در نسخه بعدی اضافه خواهد شد");
            handleClearPasswordFields();
        });
    }

    /**
     * Clear password fields
     */
    @FXML
    private void handleClearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    /**
     * Handle cancel changes
     */
    @FXML
    private void handleCancel() {
        if (originalProfile != null) {
            displayProfile(originalProfile);
            setStatus("تغییرات لغو شد");
        }
    }

    /**
     * Handle refresh profile
     */
    @FXML
    private void handleRefresh() {
        loadProfile();
    }

    /**
     * Handle back to main menu
     */
    @FXML
    private void handleBack() {
        navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
    }

    /**
     * Handle order history navigation
     */
    @FXML
    private void handleOrderHistory() {
        navigationController.navigateTo(NavigationController.ORDER_HISTORY_SCENE);
    }

    /**
     * Handle cart navigation
     */
    @FXML
    private void handleCart() {
        navigationController.navigateTo(NavigationController.CART_SCENE);
    }

    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأیید خروج");
        confirmAlert.setHeaderText("آیا مطمئن هستید؟");
        confirmAlert.setContentText("آیا می‌خواهید از حساب کاربری خود خارج شوید؟");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                navigationController.logout();
            }
        });
    }

    /**
     * Set loading state
     */
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        saveButton.setDisable(loading);
        refreshButton.setDisable(loading);
        changePasswordButton.setDisable(loading);
        fullNameField.setDisable(loading);
        emailField.setDisable(loading);
        addressField.setDisable(loading);
        currentPasswordField.setDisable(loading);
        newPasswordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
    }

    /**
     * Set status message
     */
    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show success dialog
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show info dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== DATA MODEL ====================

    /**
     * User profile data model
     */
    public static class UserProfile {
        private Long id;
        private String fullName;
        private String phone;
        private String email;
        private String address;
        private String role;
        private boolean active;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        /**
         * Create a copy of this profile
         */
        public UserProfile copy() {
            UserProfile copy = new UserProfile();
            copy.setId(this.id);
            copy.setFullName(this.fullName);
            copy.setPhone(this.phone);
            copy.setEmail(this.email);
            copy.setAddress(this.address);
            copy.setRole(this.role);
            copy.setActive(this.active);
            return copy;
        }
    }
}
