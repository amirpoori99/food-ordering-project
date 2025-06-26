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
 * کنترلر صفحه ورود به سیستم
 * 
 * این کلاس مسئول مدیریت رابط کاربری ورود شامل:
 * - اعتبارسنجی ورودی‌های کاربر
 * - ارتباط با backend برای احراز هویت
 * - مدیریت حالت‌های loading و error
 * - ذخیره اطلاعات کاربر (remember me)
 * - navigation به صفحات مختلف
 * 
 * Pattern های استفاده شده:
 * - MVC Pattern: Controller جدا از View
 * - Observer Pattern: event listeners برای UI components
 * - Task Pattern: background processing برای API calls
 * - Singleton Pattern: NavigationController
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class LoginController implements Initializable {

    /** فیلد ورودی شماره تلفن */
    @FXML private TextField phoneField;
    
    /** فیلد ورودی رمز عبور */
    @FXML private PasswordField passwordField;
    
    /** چک‌باکس "مرا به خاطر بسپار" */
    @FXML private CheckBox rememberMeCheckbox;
    
    /** دکمه ورود */
    @FXML private Button loginButton;
    
    /** لینک ثبت نام */
    @FXML private Hyperlink registerLink;
    
    /** برچسب نمایش وضعیت و پیام‌های خطا */
    @FXML private Label statusLabel;
    
    /** نشانگر loading در زمان پردازش درخواست */
    @FXML private ProgressIndicator loadingIndicator;

    /** کنترلر navigation برای تغییر صفحات */
    private NavigationController navigationController;
    
    /** Preferences برای ذخیره اطلاعات کاربر */
    private Preferences preferences;
    
    /** flag برای جلوگیری از درخواست‌های همزمان ورود */
    private boolean isLoginInProgress = false;

    /**
     * متد مقداردهی اولیه که بعد از load شدن FXML اجرا می‌شود
     * 
     * @param location URL location مربوط به FXML
     * @param resources منابع زبان و localization
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // دریافت instance های singleton
        this.navigationController = NavigationController.getInstance();
        this.preferences = Preferences.userNodeForPackage(LoginController.class);
        
        // راه‌اندازی رابط کاربری
        setupUI();
        
        // بارگذاری اطلاعات ذخیره شده قبلی
        loadSavedCredentials();
    }

    /**
     * راه‌اندازی کامپوننت‌های UI و event handler ها
     * 
     * شامل:
     * - تنظیم listener ها برای enable/disable کردن دکمه ورود
     * - تنظیم keyboard navigation (Enter key)
     * - تنظیم حالت اولیه فیلدها
     */
    private void setupUI() {
        // فعال/غیرفعال کردن دکمه ورود بر اساس پر بودن فیلدها
        phoneField.textProperty().addListener((obs, oldText, newText) -> updateLoginButtonState());
        passwordField.textProperty().addListener((obs, oldText, newText) -> updateLoginButtonState());
        
        // تنظیم enter key handler ها برای بهبود UX
        phoneField.setOnAction(e -> passwordField.requestFocus()); // انتقال focus به فیلد رمز
        passwordField.setOnAction(e -> handleLogin()); // اجرای ورود با enter
        
        // تنظیم حالت اولیه
        updateLoginButtonState();
        clearStatus();
    }

    /**
     * بارگذاری اطلاعات ذخیره شده کاربر
     * 
     * اگر قبلاً گزینه "مرا به خاطر بسپار" انتخاب شده بود،
     * شماره تلفن و وضعیت checkbox بارگذاری می‌شود
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
     * پردازش کلیک روی دکمه ورود
     * 
     * مراحل:
     * 1. جلوگیری از درخواست‌های همزمان
     * 2. اعتبارسنجی ورودی‌ها
     * 3. اجرای background task برای ارتباط با API
     * 4. پردازش نتیجه و navigation
     */
    @FXML
    private void handleLogin() {
        // جلوگیری از چندین درخواست همزمان
        if (isLoginInProgress) {
            return;
        }
        
        // دریافت مقادیر فیلدها
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        
        // اعتبارسنجی ورودی‌ها
        if (!validateInput(phone, password)) {
            return;
        }
        
        // شروع فرآیند ورود
        isLoginInProgress = true;
        setLoading(true);
        clearStatus();
        
        // ایجاد background task برای جلوگیری از freeze شدن UI
        Task<HttpClientUtil.ApiResponse> loginTask = new Task<HttpClientUtil.ApiResponse>() {
            @Override
            protected HttpClientUtil.ApiResponse call() throws Exception {
                // فراخوانی API ورود
                return HttpClientUtil.login(phone, password);
            }
        };
        
        // پردازش نتیجه موفق
        loginTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                isLoginInProgress = false;
                setLoading(false);
                HttpClientUtil.ApiResponse response = loginTask.getValue();
                handleLoginResponse(response, phone);
            });
        });
        
        // پردازش خطا
        loginTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                isLoginInProgress = false;
                setLoading(false);
                Throwable exception = loginTask.getException();
                handleLoginError(exception);
            });
        });
        
        // اجرای task در thread جداگانه
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true); // daemon thread تا با بسته شدن برنامه تمام شود
        loginThread.start();
    }

    /**
     * اعتبارسنجی ورودی‌های کاربر
     * 
     * بررسی‌های انجام شده:
     * - خالی نبودن فیلدها
     * - فرمت صحیح شماره تلفن (09xxxxxxxxx)
     * - حداقل طول رمز عبور
     * 
     * @param phone شماره تلفن وارد شده
     * @param password رمز عبور وارد شده
     * @return true اگر ورودی‌ها معتبر باشند
     */
    private boolean validateInput(String phone, String password) {
        // بررسی خالی نبودن شماره تلفن
        if (phone.isEmpty()) {
            showError("لطفاً شماره تلفن را وارد کنید");
            phoneField.requestFocus();
            return false;
        }
        
        // بررسی خالی نبودن رمز عبور
        if (password.isEmpty()) {
            showError("لطفاً رمز عبور را وارد کنید");
            passwordField.requestFocus();
            return false;
        }
        
        // بررسی فرمت شماره تلفن (11 رقم، شروع با 09)
        if (!phone.matches("^09\\d{9}$")) {
            showError("شماره تلفن باید با 09 شروع شود و 11 رقم باشد");
            phoneField.requestFocus();
            return false;
        }
        
        // بررسی حداقل طول رمز عبور
        if (password.length() < 4) {
            showError("رمز عبور باید حداقل 4 کاراکتر باشد");
            passwordField.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * پردازش پاسخ دریافت شده از API ورود
     * 
     * @param response پاسخ API
     * @param phone شماره تلفن برای ذخیره در صورت موفقیت
     */
    public void handleLoginResponse(HttpClientUtil.ApiResponse response, String phone) {
        if (response.isSuccess()) {
            // نمایش پیام موفقیت
            showSuccess("ورود موفقیت‌آمیز بود!");
            
            // ذخیره اطلاعات در صورت انتخاب "مرا به خاطر بسپار"
            if (rememberMeCheckbox.isSelected()) {
                preferences.put("saved_phone", phone);
                preferences.putBoolean("remember_me", true);
            } else {
                // پاک کردن اطلاعات ذخیره شده
                preferences.remove("saved_phone");
                preferences.putBoolean("remember_me", false);
            }
            
            // انتقال به صفحه اصلی (لیست رستوران‌ها)
            navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
            
        } else {
            // نمایش پیام خطا دریافت شده از سرور
            String errorMessage = response.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "خطا در ورود به سیستم";
            }
            showError(errorMessage);
        }
    }

    /**
     * پردازش خطاهای ارتباط با سرور
     * 
     * انواع خطاهای پردازش شده:
     * - Timeout: خطای زمان انتظار
     * - Connection: خطای اتصال
     * - Unknown Host: عدم دسترسی به سرور
     * - IO Exception: خطاهای عمومی شبکه
     * 
     * @param exception خطای رخ داده
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
     * پردازش کلیک روی لینک ثبت نام
     * انتقال کاربر به صفحه ثبت نام
     */
    @FXML
    private void handleRegisterLink() {
        navigationController.navigateTo(NavigationController.REGISTER_SCENE);
    }

    /**
     * به‌روزرسانی وضعیت دکمه ورود بر اساس پر بودن فیلدها
     * 
     * دکمه ورود فقط زمانی فعال است که هر دو فیلد پر باشند
     */
    private void updateLoginButtonState() {
        boolean hasInput = !phoneField.getText().trim().isEmpty() && 
                          !passwordField.getText().isEmpty();
        loginButton.setDisable(!hasInput);
    }

    /**
     * تنظیم حالت loading برای UI
     * 
     * در حالت loading:
     * - نشانگر loading نمایش داده می‌شود
     * - تمام کنترل‌ها غیرفعال می‌شوند
     * - پیام "در حال ورود..." نمایش داده می‌شود
     * 
     * @param loading true برای فعال کردن loading
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
     * نمایش پیام خطا با رنگ قرمز
     * 
     * @param message متن پیام خطا
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    /**
     * نمایش پیام موفقیت با رنگ سبز
     * 
     * @param message متن پیام موفقیت
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    /**
     * پاک کردن پیام وضعیت
     */
    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setStyle("");
    }

    // ==================== PUBLIC METHODS FOR TESTING ====================

    /**
     * دریافت متن فیلد شماره تلفن (برای تست)
     * @return متن فیلد شماره تلفن
     */
    public String getPhoneFieldText() {
        return phoneField != null ? phoneField.getText() : "";
    }

    /**
     * تنظیم متن فیلد شماره تلفن (برای تست)
     * @param text متن جدید
     */
    public void setPhoneFieldText(String text) {
        if (phoneField != null) {
            phoneField.setText(text);
        }
    }

    /**
     * دریافت متن فیلد رمز عبور (برای تست)
     * @return متن فیلد رمز عبور
     */
    public String getPasswordFieldText() {
        return passwordField != null ? passwordField.getText() : "";
    }

    /**
     * تنظیم متن فیلد رمز عبور (برای تست)
     * @param text متن جدید
     */
    public void setPasswordFieldText(String text) {
        if (passwordField != null) {
            passwordField.setText(text);
        }
    }

    /**
     * دریافت وضعیت چک‌باکس "مرا به خاطر بسپار" (برای تست)
     * @return true اگر انتخاب شده باشد
     */
    public boolean isRememberMeSelected() {
        return rememberMeCheckbox != null && rememberMeCheckbox.isSelected();
    }

    /**
     * تنظیم وضعیت چک‌باکس "مرا به خاطر بسپار" (برای تست)
     * @param selected وضعیت جدید
     */
    public void setRememberMeSelected(boolean selected) {
        if (rememberMeCheckbox != null) {
            rememberMeCheckbox.setSelected(selected);
        }
    }

    /**
     * دریافت متن برچسب وضعیت (برای تست)
     * @return متن وضعیت
     */
    public String getStatusText() {
        return statusLabel != null ? statusLabel.getText() : "";
    }

    /**
     * بررسی غیرفعال بودن دکمه ورود (برای تست)
     * @return true اگر دکمه غیرفعال باشد
     */
    public boolean isLoginButtonDisabled() {
        return loginButton != null && loginButton.isDisabled();
    }

    /**
     * بررسی نمایش نشانگر loading (برای تست)
     * @return true اگر loading نمایش داده شود
     */
    public boolean isLoadingVisible() {
        return loadingIndicator != null && loadingIndicator.isVisible();
    }

    /**
     * اجرای ورود به صورت برنامه‌نویسی (برای تست)
     */
    public void triggerLogin() {
        handleLogin();
    }

    /**
     * اجرای لینک ثبت نام به صورت برنامه‌نویسی (برای تست)
     */
    public void triggerRegisterLink() {
        handleRegisterLink();
    }
}
