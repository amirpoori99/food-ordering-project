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
 * کنترلر صفحه ثبت نام در سیستم
 * 
 * این کلاس مسئول مدیریت رابط کاربری ثبت نام شامل:
 * - اعتبارسنجی کامل ورودی‌های کاربر
 * - ارتباط با backend برای ایجاد حساب جدید
 * - مدیریت انواع نقش کاربری (خریدار، فروشنده، پیک)
 * - مدیریت حالت‌های loading و error
 * - navigation به صفحات مختلف
 * - تأیید رمز عبور و اعتبارسنجی ایمیل
 * 
 * Pattern های استفاده شده:
 * - MVC Pattern: Controller جدا از View
 * - Observer Pattern: event listeners برای UI components
 * - Task Pattern: background processing برای API calls
 * - Validation Pattern: اعتبارسنجی مرحله‌ای
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class RegisterController implements Initializable {

    /** فیلد ورودی نام کامل کاربر */
    @FXML private TextField fullNameField;
    
    /** فیلد ورودی شماره تلفن */
    @FXML private TextField phoneField;
    
    /** فیلد ورودی آدرس ایمیل */
    @FXML private TextField emailField;
    
    /** فیلد ورودی رمز عبور */
    @FXML private PasswordField passwordField;
    
    /** فیلد تأیید رمز عبور */
    @FXML private PasswordField confirmPasswordField;
    
    /** فیلد ورودی آدرس کاربر */
    @FXML private TextArea addressField;
    
    /** ComboBox انتخاب نقش کاربری */
    @FXML private ComboBox<String> roleComboBox;
    
    /** دکمه ثبت نام */
    @FXML private Button registerButton;
    
    /** لینک ورود (برای کاربران موجود) */
    @FXML private Hyperlink loginLink;
    
    /** برچسب نمایش وضعیت و پیام‌های خطا */
    @FXML private Label statusLabel;
    
    /** نشانگر loading در زمان پردازش درخواست */
    @FXML private ProgressIndicator loadingIndicator;

    /** کنترلر navigation برای تغییر صفحات */
    private NavigationController navigationController;

    /**
     * متد مقداردهی اولیه که بعد از load شدن FXML اجرا می‌شود
     * 
     * @param location URL location مربوط به FXML
     * @param resources منابع زبان و localization
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // دریافت instance singleton NavigationController
        this.navigationController = NavigationController.getInstance();
        
        // راه‌اندازی کامپوننت‌های UI
        setupUI();
        
        // پر کردن لیست نقش‌های کاربری
        populateRoles();
    }

    /**
     * تنظیم NavigationController (مخصوص تست)
     * این متد امکان injection کردن mock NavigationController را برای تست فراهم می‌کند
     * 
     * @param navigationController instance جدید NavigationController
     */
    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    /**
     * راه‌اندازی کامپوننت‌های UI و event handler ها
     * 
     * شامل:
     * - تنظیم listener ها برای enable/disable کردن دکمه ثبت نام
     * - تنظیم action handler ها برای دکمه‌ها و لینک‌ها
     * - تنظیم حالت اولیه فیلدها
     */
    private void setupUI() {
        // فعال/غیرفعال کردن دکمه ثبت نام بر اساس پر بودن فیلدها
        fullNameField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        phoneField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        passwordField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        addressField.textProperty().addListener((obs, oldText, newText) -> updateRegisterButtonState());
        roleComboBox.valueProperty().addListener((obs, oldValue, newValue) -> updateRegisterButtonState());
        
        // تنظیم action handler ها برای تست‌های unit (FXML runtime را پوشش می‌دهد)
        registerButton.setOnAction(e -> handleRegister());
        loginLink.setOnAction(e -> handleLoginLink());
        
        // تنظیم حالت اولیه
        updateRegisterButtonState();
        clearStatus();
    }

    /**
     * پر کردن ComboBox نقش‌های کاربری
     * 
     * نقش‌های موجود:
     * - BUYER: خریدار (کاربر عادی)
     * - SELLER: فروشنده (صاحب رستوران)
     * - COURIER: پیک (تحویل‌دهنده سفارشات)
     */
    private void populateRoles() {
        roleComboBox.getItems().addAll("BUYER", "SELLER", "COURIER");
        roleComboBox.setValue("BUYER"); // انتخاب پیش‌فرض: خریدار
    }

    /**
     * پردازش کلیک روی دکمه ثبت نام
     * 
     * مراحل:
     * 1. دریافت مقادیر فیلدها
     * 2. اعتبارسنجی کامل ورودی‌ها
     * 3. اجرای background task برای ارتباط با API
     * 4. پردازش نتیجه و navigation
     */
    @FXML
    private void handleRegister() {
        // دریافت مقادیر فیلدها
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String address = addressField.getText().trim();
        String role = roleComboBox.getValue();
        
        // اعتبارسنجی ورودی‌ها
        if (!validateInput(fullName, phone, email, password, confirmPassword, address, role)) {
            return;
        }
        
        // شروع فرآیند ثبت نام
        setLoading(true);
        clearStatus();
        
        // ایجاد background task برای جلوگیری از freeze شدن UI
        Task<HttpClientUtil.ApiResponse> registerTask = new Task<HttpClientUtil.ApiResponse>() {
            @Override
            protected HttpClientUtil.ApiResponse call() throws Exception {
                // فراخوانی API ثبت نام
                return HttpClientUtil.register(fullName, phone, email, password, address);
            }
        };
        
        // پردازش نتیجه موفق
        registerTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                HttpClientUtil.ApiResponse response = registerTask.getValue();
                handleRegisterResponse(response);
            });
        });
        
        // پردازش خطا
        registerTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                setLoading(false);
                Throwable exception = registerTask.getException();
                handleRegisterError(exception);
            });
        });
        
        // اجرای task در thread جداگانه
        Thread registerThread = new Thread(registerTask);
        registerThread.setDaemon(true); // daemon thread تا با بسته شدن برنامه تمام شود
        registerThread.start();
    }

    /**
     * اعتبارسنجی کامل ورودی‌های کاربر
     * 
     * بررسی‌های انجام شده:
     * - خالی نبودن تمام فیلدهای الزامی
     * - فرمت صحیح شماره تلفن
     * - حداقل طول رمز عبور
     * - تطابق رمز عبور و تأیید آن
     * - انتخاب نقش کاربری
     * 
     * @param fullName نام کامل کاربر
     * @param phone شماره تلفن
     * @param email آدرس ایمیل
     * @param password رمز عبور
     * @param confirmPassword تأیید رمز عبور
     * @param address آدرس کاربر
     * @param role نقش کاربری
     * @return true اگر تمام ورودی‌ها معتبر باشند
     */
    private boolean validateInput(String fullName, String phone, String email, 
                                 String password, String confirmPassword, String address, String role) {
        // بررسی نام کامل
        if (fullName == null || fullName.trim().isEmpty()) {
            showError("لطفاً نام کامل را وارد کنید");
            fullNameField.requestFocus();
            return false;
        }
        
        // بررسی شماره تلفن
        if (phone == null || phone.trim().isEmpty()) {
            showError("لطفاً شماره تلفن را وارد کنید");
            phoneField.requestFocus();
            return false;
        }
        
        // اعتبارسنجی فرمت شماره تلفن (11 رقم، شروع با 09)
        if (!phone.trim().matches("^09\\d{9}$")) {
            showError("شماره تلفن باید با 09 شروع شود و 11 رقم باشد");
            phoneField.requestFocus();
            return false;
        }
        
        // بررسی رمز عبور
        if (password == null || password.isEmpty()) {
            showError("لطفاً رمز عبور را وارد کنید");
            passwordField.requestFocus();
            return false;
        }
        
        // بررسی حداقل طول رمز عبور
        if (password.length() < 4) {
            showError("رمز عبور باید حداقل 4 کاراکتر باشد");
            passwordField.requestFocus();
            return false;
        }
        
        // بررسی تطابق رمز عبور
        if (!password.equals(confirmPassword)) {
            showError("رمز عبور و تکرار آن یکسان نیستند");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        // بررسی آدرس
        if (address == null || address.trim().isEmpty()) {
            showError("لطفاً آدرس را وارد کنید");
            addressField.requestFocus();
            return false;
        }
        
        // بررسی انتخاب نقش
        if (role == null || role.isEmpty()) {
            showError("لطفاً نقش را انتخاب کنید");
            roleComboBox.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * پردازش پاسخ دریافت شده از API ثبت نام
     * 
     * در صورت موفقیت:
     * - نمایش پیام موفقیت
     * - انتقال به صفحه ورود بعد از 2 ثانیه
     * 
     * در صورت خطا:
     * - نمایش پیام خطای دریافت شده از سرور
     * 
     * @param response پاسخ API
     */
    private void handleRegisterResponse(HttpClientUtil.ApiResponse response) {
        if (response.isSuccess()) {
            // نمایش پیام موفقیت
            showSuccess("ثبت‌نام موفقیت‌آمیز بود! اکنون وارد شوید");
            
            // انتقال به صفحه ورود بعد از تأخیر کوتاه
            Platform.runLater(() -> {
                Task<Void> delayTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Thread.sleep(2000); // انتظار 2 ثانیه
                        return null;
                    }
                };
                delayTask.setOnSucceeded(event -> {
                    navigationController.navigateTo(NavigationController.LOGIN_SCENE);
                });
                new Thread(delayTask).start();
            });
            
        } else {
            // نمایش پیام خطا دریافت شده از سرور
            String errorMessage = response.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "خطا در ثبت‌نام";
            }
            showError(errorMessage);
        }
    }

    /**
     * پردازش خطاهای ارتباط با سرور
     * 
     * انواع خطاهای پردازش شده:
     * - IOException: خطاهای شبکه و اتصال
     * - سایر exception ها: خطاهای عمومی
     * 
     * @param exception خطای رخ داده
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
     * پردازش کلیک روی لینک ورود
     * انتقال کاربر به صفحه ورود (برای کاربران موجود)
     */
    @FXML
    public void handleLoginLink() {
        navigationController.navigateTo(NavigationController.LOGIN_SCENE);
    }

    /**
     * به‌روزرسانی وضعیت دکمه ثبت نام بر اساس پر بودن فیلدها
     * 
     * دکمه ثبت نام فقط زمانی فعال است که تمام فیلدهای الزامی پر باشند:
     * - نام کامل
     * - شماره تلفن  
     * - رمز عبور
     * - تأیید رمز عبور
     * - آدرس
     * - نقش کاربری
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
     * تنظیم حالت loading برای UI
     * 
     * در حالت loading:
     * - نشانگر loading نمایش داده می‌شود
     * - تمام کنترل‌ها غیرفعال می‌شوند
     * - پیام "در حال ثبت‌نام..." نمایش داده می‌شود
     * 
     * @param loading true برای فعال کردن loading
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
     * نمایش پیام خطا با رنگ قرمز
     * 
     * @param message متن پیام خطا
     */
    public void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    /**
     * نمایش پیام موفقیت با رنگ سبز
     * 
     * @param message متن پیام موفقیت
     */
    public void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    /**
     * پاک کردن پیام وضعیت
     */
    public void clearStatus() {
        statusLabel.setText("");
        statusLabel.setStyle("");
    }

    // ==================== PUBLIC METHODS FOR TESTING ====================

    /**
     * دریافت متن برچسب وضعیت (برای تست)
     * @return متن وضعیت
     */
    public String getStatusText() {
        return statusLabel.getText();
    }
    
    /**
     * بررسی نمایش نشانگر loading (برای تست)
     * @return true اگر loading نمایش داده شود
     */
    public boolean isLoadingVisible() {
        return loadingIndicator.isVisible();
    }
}
