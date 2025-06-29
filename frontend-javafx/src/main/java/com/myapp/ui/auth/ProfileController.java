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
 * کنترلر صفحه مدیریت پروفایل کاربر
 * 
 * این کلاس مسئول مدیریت رابط کاربری پروفایل شامل:
 * - مشاهده اطلاعات کاربر
 * - ویرایش اطلاعات شخصی
 * - تغییر رمز عبور
 * - مدیریت وضعیت حساب
 * - navigation به صفحات مختلف
 * - بارگذاری و ذخیره اطلاعات
 * 
 * Pattern های استفاده شده:
 * - MVC Pattern: Controller جدا از View
 * - Observer Pattern: event listeners برای UI components
 * - Task Pattern: background processing برای API calls
 * - DTO Pattern: UserProfile data model
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class ProfileController implements Initializable {

    /** فیلد ورودی نام کامل کاربر */
    @FXML private TextField fullNameField;
    
    /** فیلد نمایش شماره تلفن (غیرقابل ویرایش) */
    @FXML private TextField phoneField;
    
    /** فیلد ورودی آدرس ایمیل */
    @FXML private TextField emailField;
    
    /** فیلد ورودی آدرس کاربر */
    @FXML private TextArea addressField;
    
    /** برچسب نمایش نقش کاربری */
    @FXML private Label roleLabel;
    
    /** برچسب نمایش وضعیت حساب */
    @FXML private Label accountStatusLabel;
    
    /** برچسب نمایش تاریخ عضویت */
    @FXML private Label memberSinceLabel;
    
    /** فیلد ورودی رمز عبور فعلی */
    @FXML private PasswordField currentPasswordField;
    
    /** فیلد ورودی رمز عبور جدید */
    @FXML private PasswordField newPasswordField;
    
    /** فیلد تأیید رمز عبور جدید */
    @FXML private PasswordField confirmPasswordField;
    
    /** دکمه ذخیره تغییرات */
    @FXML private Button saveButton;
    
    /** دکمه لغو تغییرات */
    @FXML private Button cancelButton;
    
    /** دکمه بروزرسانی اطلاعات */
    @FXML private Button refreshButton;
    
    /** دکمه تغییر رمز عبور */
    @FXML private Button changePasswordButton;
    
    /** دکمه پاک کردن فیلدهای رمز عبور */
    @FXML private Button clearPasswordButton;
    
    /** منوی بازگشت به صفحه اصلی */
    @FXML private MenuItem backMenuItem;
    
    /** منوی تاریخچه سفارشات */
    @FXML private MenuItem orderHistoryMenuItem;
    
    /** منوی سبد خرید */
    @FXML private MenuItem cartMenuItem;
    
    /** منوی خروج از حساب */
    @FXML private MenuItem logoutMenuItem;
    
    /** برچسب نمایش وضعیت و پیام‌ها */
    @FXML private Label statusLabel;
    
    /** نشانگر loading در زمان پردازش */
    @FXML private ProgressIndicator loadingIndicator;

    /** کنترلر navigation برای تغییر صفحات */
    private NavigationController navigationController;
    
    /** کپی اطلاعات اولیه کاربر برای بررسی تغییرات */
    private UserProfile originalProfile;

    /**
     * متد مقداردهی اولیه که بعد از load شدن FXML اجرا می‌شود
     * 
     * @param location URL location مربوط به FXML
     * @param resources منابع زبان و localization
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        
        setupUI();
        loadProfile();
    }

    /**
     * راه‌اندازی کامپوننت‌های UI و event handler ها
     * 
     * شامل:
     * - تنظیم فیلد شماره تلفن به حالت فقط‌خواندنی
     * - راه‌اندازی validation listener ها
     * - تنظیم وضعیت اولیه
     */
    private void setupUI() {
        // تنظیم فیلد شماره تلفن به حالت فقط‌خواندنی (غیرقابل تغییر)
        phoneField.setEditable(false);
        phoneField.setStyle("-fx-background-color: #e9ecef;");
        
        // راه‌اندازی listener های اعتبارسنجی
        setupValidationListeners();
        
        // تنظیم وضعیت اولیه
        setStatus("در حال بارگذاری پروفایل...");
    }

    /**
     * راه‌اندازی listener های اعتبارسنجی برای فیلدهای فرم
     * 
     * شامل:
     * - بررسی تغییرات برای فعال کردن دکمه ذخیره
     * - اعتبارسنجی فیلدهای رمز عبور
     */
    private void setupValidationListeners() {
        // فعال کردن دکمه ذخیره فقط زمانی که تغییراتی انجام شده باشد
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        addressField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        
        // اعتبارسنجی فیلدهای رمز عبور
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswordFields());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswordFields());
    }

    /**
     * بارگذاری اطلاعات پروفایل کاربر از بک‌اند
     * 
     * این متد در background thread اجرا می‌شود تا UI freeze نشود
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
     * تجزیه اطلاعات پروفایل کاربر از پاسخ JSON
     * 
     * @param data داده JSON دریافت شده از سرور
     * @return شیء UserProfile
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
     * نمایش اطلاعات پروفایل کاربر در فیلدهای فرم
     * 
     * @param profile اطلاعات پروفایل کاربر
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
     * دریافت متن فارسی نقش کاربری
     * 
     * @param role نقش کاربری به انگلیسی
     * @return متن فارسی نقش
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
     * بررسی وجود تغییرات در پروفایل
     * 
     * این متد دکمه‌های ذخیره و لغو را فعال/غیرفعال می‌کند
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
     * اعتبارسنجی فیلدهای رمز عبور برای تغییر رمز
     * 
     * شامل بررسی:
     * - تطابق رمز جدید و تکرار آن
     * - وجود رمز فعلی
     * - حداقل طول رمز جدید
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
     * پردازش ذخیره تغییرات پروفایل
     * 
     * مراحل:
     * 1. اعتبارسنجی داده‌ها
     * 2. ارسال به سرور در background thread
     * 3. بروزرسانی UI
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
                loadProfile(); // بارگذاری مجدد برای دریافت داده‌های به‌روزرسانی شده
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
     * اعتبارسنجی داده‌های پروفایل قبل از ذخیره
     * 
     * @return true اگر تمام داده‌ها معتبر باشند
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
     * اعتبارسنجی ساده ایمیل
     * 
     * @param email آدرس ایمیل
     * @return true اگر فرمت صحیح باشد
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    /**
     * پردازش تغییر رمز عبور
     * 
     * شامل اعتبارسنجی و ارسال درخواست به سرور
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
        
        // TODO: پیاده‌سازی API تغییر رمز عبور
        // فعلاً پیام placeholder نمایش داده می‌شود
        Platform.runLater(() -> {
            setLoading(false);
            setStatus("تغییر رمز عبور در نسخه آینده پیاده‌سازی خواهد شد");
            showInfo("اطلاع", "قابلیت تغییر رمز عبور در نسخه بعدی اضافه خواهد شد");
            handleClearPasswordFields();
        });
    }

    /**
     * پاک کردن فیلدهای رمز عبور
     */
    @FXML
    private void handleClearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    /**
     * پردازش لغو تغییرات
     * بازگرداندن فیلدها به حالت اولیه
     */
    @FXML
    private void handleCancel() {
        if (originalProfile != null) {
            displayProfile(originalProfile);
            setStatus("تغییرات لغو شد");
        }
    }

    /**
     * پردازش بروزرسانی پروفایل
     * بارگذاری مجدد اطلاعات از سرور
     */
    @FXML
    private void handleRefresh() {
        loadProfile();
    }

    /**
     * پردازش بازگشت به منوی اصلی
     */
    @FXML
    private void handleBack() {
        navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
    }

    /**
     * پردازش انتقال به تاریخچه سفارشات
     */
    @FXML
    private void handleOrderHistory() {
        navigationController.navigateTo(NavigationController.ORDER_HISTORY_SCENE);
    }

    /**
     * پردازش انتقال به سبد خرید
     */
    @FXML
    private void handleCart() {
        navigationController.navigateTo(NavigationController.CART_SCENE);
    }

    /**
     * پردازش خروج از حساب کاربری
     * نمایش دیالوگ تأیید قبل از خروج
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
     * تنظیم حالت loading برای UI
     * 
     * در حالت loading تمام کنترل‌ها غیرفعال می‌شوند
     * 
     * @param loading true برای فعال کردن loading
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
     * تنظیم پیام وضعیت
     * 
     * @param message متن پیام
     */
    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    /**
     * نمایش دیالوگ خطا
     * 
     * @param title عنوان دیالوگ
     * @param message متن پیام خطا
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * نمایش دیالوگ موفقیت
     * 
     * @param title عنوان دیالوگ
     * @param message متن پیام موفقیت
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * نمایش دیالوگ اطلاعات
     * 
     * @param title عنوان دیالوگ
     * @param message متن پیام اطلاعاتی
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
     * مدل داده‌ای پروفایل کاربر
     * 
     * این کلاس شامل تمام اطلاعات مربوط به کاربر است
     * و امکان کپی‌برداری از اطلاعات را فراهم می‌کند
     */
    public static class UserProfile {
        /** شناسه منحصربه‌فرد کاربر */
        private Long id;
        
        /** نام کامل کاربر */
        private String fullName;
        
        /** شماره تلفن کاربر */
        private String phone;
        
        /** آدرس ایمیل کاربر */
        private String email;
        
        /** آدرس کاربر */
        private String address;
        
        /** نقش کاربری */
        private String role;
        
        /** وضعیت فعالیت حساب */
        private boolean active;

        // Getter و Setter ها
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
         * ایجاد کپی از این پروفایل
         * 
         * @return کپی جدید از پروفایل
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
