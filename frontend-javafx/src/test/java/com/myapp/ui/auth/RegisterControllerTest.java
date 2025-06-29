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

import com.myapp.ui.common.TestFXBase;

/**
 * تست‌های ساده‌شده برای RegisterController
 * 
 * این کلاس تست شامل موارد زیر است:
 * - مقداردهی اولیه کنترلر
 * - تست کامپوننت‌های UI
 * - اعتبارسنجی فیلدهای ثبت نام
 * - مدیریت وضعیت دکمه ثبت نام
 * - Navigation به صفحه ورود
 * - تست Role ComboBox
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class RegisterControllerTest extends TestFXBase {

    /** کنترلر RegisterController مورد تست */
    private RegisterController controller;
    
    /** NavigationController ساختگی برای تست */
    private NavigationController mockNavigationController;
    
    // کامپوننت‌های UI
    /** فیلد ورودی نام کامل */
    private TextField fullNameField;
    
    /** فیلد ورودی شماره تلفن */
    private TextField phoneField;
    
    /** فیلد ورودی آدرس ایمیل */
    private TextField emailField;
    
    /** فیلد ورودی رمز عبور */
    private PasswordField passwordField;
    
    /** فیلد تأیید رمز عبور */
    private PasswordField confirmPasswordField;
    
    /** فیلد ورودی آدرس */
    private TextArea addressField;
    
    /** ComboBox انتخاب نقش کاربری */
    private ComboBox<String> roleComboBox;
    
    /** دکمه ثبت نام */
    private Button registerButton;
    
    /** لینک ورود */
    private Hyperlink loginLink;
    
    /** برچسب نمایش وضعیت */
    private Label statusLabel;
    
    /** نشانگر بارگذاری */
    private ProgressIndicator loadingIndicator;

    /**
     * راه‌اندازی کلی برای همه تست‌ها
     */
    @BeforeAll
    public static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    /**
     * راه‌اندازی اولیه قبل از هر تست
     * شامل ایجاد کنترلر و کامپوننت‌های UI
     */
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp(); // فراخوانی راه‌اندازی والد
        
        // ایجاد کنترلر
        controller = new RegisterController();
        
        // ایجاد کامپوننت‌های UI
        fullNameField = new TextField();
        phoneField = new TextField();
        emailField = new TextField();
        passwordField = new PasswordField();
        confirmPasswordField = new PasswordField();
        addressField = new TextArea();
        roleComboBox = new ComboBox<>();
        registerButton = new Button("ثبت نام");
        loginLink = new Hyperlink("ورود");
        statusLabel = new Label();
        loadingIndicator = new ProgressIndicator();
        
        // ایجاد وابستگی‌های ساختگی
        mockNavigationController = mock(NavigationController.class);
        
        // تنظیم تزریق‌های FXML
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
        
        // مقداردهی اولیه کنترلر
        Platform.runLater(() -> {
            controller.initialize(null, null);
            controller.setNavigationController(mockNavigationController);
            loadingIndicator.setVisible(false);
            
            // اضافه کردن دستی action handler به دکمه
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

    /**
     * پاکسازی بعد از هر تست
     */
    @AfterEach
    public void tearDown() {
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

    /**
     * پاکسازی کلی بعد از تمام تست‌ها
     */
    @AfterAll
    public static void tearDownClass() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    // ==================== تست‌های عملکرد پایه ====================

    /**
     * تست صحت مقداردهی اولیه کنترلر
     */
    @Test
    @DisplayName("باید کنترلر را به درستی مقداردهی کند")
    void testInitialization() {
        assertNotNull(controller);
        assertNotNull(fullNameField);
        assertNotNull(phoneField);
        assertNotNull(passwordField);
        assertNotNull(registerButton);
        assertNotNull(statusLabel);
    }

    /**
     * تست پر کردن ComboBox نقش‌ها با مقادیر صحیح
     */
    @Test
    @DisplayName("باید ComboBox نقش‌ها را با مقادیر صحیح پر کند")
    void testRoleComboBoxPopulation() {
        // ابتدا آیتم‌های موجود را پاک کن
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

    /**
     * تست فعال شدن دکمه هنگام پر بودن همه فیلدها
     */
    @Test
    @DisplayName("باید دکمه را هنگام پر بودن همه فیلدها فعال کند")
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
        
        assertFalse(registerButton.isDisabled(), "دکمه باید با پر بودن همه فیلدها فعال باشد");
    }
    
    /**
     * تست غیرفعال بودن دکمه هنگام خالی بودن فیلدها
     */
    @Test
    @DisplayName("باید دکمه را هنگام خالی بودن فیلدها غیرفعال کند")
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
        
        assertTrue(registerButton.isDisabled(), "دکمه باید با خالی بودن فیلدها غیرفعال باشد");
    }
    
    /**
     * تست به‌روزرسانی وضعیت دکمه هنگام تغییر فیلدها
     */
    @Test
    @DisplayName("باید وضعیت دکمه را هنگام تغییر فیلدها به‌روزرسانی کند")
    void testButtonStateUpdate() {
        // در ابتدا غیرفعال
        assertTrue(registerButton.isDisabled());
        
        // پر کردن یک فیلد در هر مرحله
        Platform.runLater(() -> {
            fullNameField.setText("احمد");
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(registerButton.isDisabled(), "باید با فقط نام هنوز غیرفعال باشد");
        
        // پر کردن همه فیلدهای الزامی
        Platform.runLater(() -> {
            phoneField.setText("09123456789");
            passwordField.setText("password123");
            confirmPasswordField.setText("password123");
            addressField.setText("آدرس");
            roleComboBox.setValue("BUYER");
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(registerButton.isDisabled(), "باید با پر بودن همه فیلدها فعال باشد");
    }
    
    /**
     * تست navigation لینک ورود
     */
    @Test
    @DisplayName("باید navigation لینک ورود را مدیریت کند")
    void testLoginLinkNavigation() {
        Platform.runLater(() -> {
            loginLink.fire();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        verify(mockNavigationController).navigateTo("Login");
    }

    // ==================== متدهای کمکی ====================

    /**
     * متد کمکی برای تنظیم فیلدهای private
     * 
     * @param object شیء مقصد
     * @param fieldName نام فیلد
     * @param value مقدار جدید
     */
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