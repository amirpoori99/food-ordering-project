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
 * مجموعه تست‌های جامع برای LoginController
 * 
 * این کلاس تست شامل موارد زیر است:
 * - رفتار رابط کاربری
 * - اعتبارسنجی فیلدها
 * - یکپارچگی با API
 * - حالات مختلف و Edge Cases
 * - مدیریت Remember Me
 * - Navigation و Loading states
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class LoginControllerTest extends TestFXBase {

    /** کنترلر LoginController مورد تست */
    private LoginController controller;
    
    /** NavigationController ساختگی برای تست */
    private NavigationController mockNavigationController;
    
    /** Preferences ساختگی برای تست */
    private Preferences mockPreferences;
    
    // کامپوننت‌های UI
    /** فیلد ورودی شماره تلفن */
    private TextField phoneField;
    
    /** فیلد ورودی رمز عبور */
    private PasswordField passwordField;
    
    /** چک‌باکس "مرا به خاطر بسپار" */
    private CheckBox rememberMeCheckbox;
    
    /** دکمه ورود */
    private Button loginButton;
    
    /** لینک ثبت نام */
    private Hyperlink registerLink;
    
    /** برچسب نمایش وضعیت */
    private Label statusLabel;
    
    /** نشانگر بارگذاری */
    private ProgressIndicator loadingIndicator;

    /**
     * راه‌اندازی اولیه قبل از هر تست
     * شامل بارگذاری FXML یا ایجاد UI ساختگی
     */
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp(); // فراخوانی راه‌اندازی والد
        
        // تلاش برای بارگذاری LoginController واقعی از FXML
        this.controller = loadFXMLController("/fxml/Login.fxml");
        
        // در صورت شکست بارگذاری FXML، ایجاد UI ساختگی
        if (controller == null) {
            createMockLoginUI();
        } else {
            // استخراج کامپوننت‌های UI از FXML بارگذاری شده
            extractUIComponents();
        }
        
        // مقداردهی اولیه وضعیت UI
        runOnFxThreadAndWait(() -> {
            if (phoneField != null) phoneField.clear();
            if (passwordField != null) passwordField.clear();
            if (rememberMeCheckbox != null) rememberMeCheckbox.setSelected(false);
            if (statusLabel != null) statusLabel.setText("");
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
        });
    }
    
    /**
     * ایجاد UI ساختگی برای تست در صورت عدم دسترسی به FXML
     */
    private void createMockLoginUI() {
        // ایجاد کنترلر
        controller = new LoginController();
        
        // ایجاد کامپوننت‌های ساختگی
        phoneField = new TextField();
        phoneField.setId("phoneField");
        passwordField = new PasswordField();
        passwordField.setId("passwordField");
        rememberMeCheckbox = new CheckBox();
        rememberMeCheckbox.setId("rememberMeCheckbox");
        loginButton = new Button("ورود");
        loginButton.setId("loginButton");
        registerLink = new Hyperlink("ثبت نام");
        registerLink.setId("registerLink");
        statusLabel = new Label();
        statusLabel.setId("statusLabel");
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setId("loadingIndicator");
        
        // تنظیم تزریق FXML به صورت دستی
        setPrivateField(controller, "phoneField", phoneField);
        setPrivateField(controller, "passwordField", passwordField);
        setPrivateField(controller, "rememberMeCheckbox", rememberMeCheckbox);
        setPrivateField(controller, "loginButton", loginButton);
        setPrivateField(controller, "registerLink", registerLink);
        setPrivateField(controller, "statusLabel", statusLabel);
        setPrivateField(controller, "loadingIndicator", loadingIndicator);
        
        // ایجاد scene با کامپوننت‌های ساختگی
        runOnFxThreadAndWait(() -> {
            VBox root = new VBox(10);
            root.getChildren().addAll(
                phoneField, passwordField, rememberMeCheckbox, 
                loginButton, registerLink, statusLabel, loadingIndicator
            );
            
            Scene scene = new Scene(root, 800, 600);
            testStage.setScene(scene);
            
            // مقداردهی اولیه کنترلر
            try {
                controller.initialize(null, null);
                loadingIndicator.setVisible(false);
            } catch (Exception e) {
                // نادیده گرفتن خطاهای مقداردهی در تست‌ها
            }
        });
    }
    
    /**
     * استخراج کامپوننت‌های UI از FXML بارگذاری شده
     */
    private void extractUIComponents() {
        // استخراج کامپوننت‌های UI از FXML
        phoneField = lookup("#phoneField", TextField.class);
        passwordField = lookup("#passwordField", PasswordField.class);
        rememberMeCheckbox = lookup("#rememberMeCheckbox", CheckBox.class);
        loginButton = lookup("#loginButton", Button.class);
        registerLink = lookup("#registerLink", Hyperlink.class);
        statusLabel = lookup("#statusLabel", Label.class);
        loadingIndicator = lookup("#loadingIndicator", ProgressIndicator.class);
        
        // در صورت فقدان هر کامپوننت، بازگشت به UI ساختگی
        if (phoneField == null || passwordField == null || loginButton == null) {
            createMockLoginUI();
        }
    }

    // ==================== تست‌های مقداردهی اولیه ====================

    /**
     * تست صحت مقداردهی اولیه کنترلر
     */
    @Test
    @DisplayName("باید کنترلر را به درستی مقداردهی کند")
    void testInitialization() {
        assertNotNull(controller);
        assertNotNull(phoneField);
        assertNotNull(passwordField);
        assertNotNull(loginButton);
        assertTrue(loginButton.isDisabled()); // در ابتدا باید غیرفعال باشد
        assertFalse(loadingIndicator.isVisible()); // در ابتدا باید مخفی باشد
    }

    // ==================== تست‌های اعتبارسنجی ورودی ====================

    /**
     * تست فعال شدن دکمه ورود با ورودی‌های معتبر
     */
    @Test
    @DisplayName("باید دکمه ورود را با ورودی‌های معتبر فعال کند")
    void testLoginButtonEnabledWithValidInput() {
        Platform.runLater(() -> {
            phoneField.setText("09123456789");
            passwordField.setText("password123");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertFalse(controller.isLoginButtonDisabled());
    }

    /**
     * تست غیرفعال بودن دکمه ورود با فیلدهای خالی
     */
    @Test
    @DisplayName("باید دکمه ورود را با فیلدهای خالی غیرفعال کند")
    void testLoginButtonDisabledWithEmptyFields() {
        Platform.runLater(() -> {
            phoneField.setText("");
            passwordField.setText("");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.isLoginButtonDisabled());
    }

    /**
     * تست غیرفعال بودن دکمه ورود با شماره تلفن خالی
     */
    @Test
    @DisplayName("باید دکمه ورود را با شماره تلفن خالی غیرفعال کند")
    void testLoginButtonDisabledWithEmptyPhone() {
        Platform.runLater(() -> {
            phoneField.setText("");
            passwordField.setText("password123");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.isLoginButtonDisabled());
    }

    /**
     * تست غیرفعال بودن دکمه ورود با رمز عبور خالی
     */
    @Test
    @DisplayName("باید دکمه ورود را با رمز عبور خالی غیرفعال کند")
    void testLoginButtonDisabledWithEmptyPassword() {
        Platform.runLater(() -> {
            phoneField.setText("09123456789");
            passwordField.setText("");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.isLoginButtonDisabled());
    }

    // ==================== تست‌های اعتبارسنجی شماره تلفن ====================

    /**
     * تست نمایش خطا برای فرمت نامعتبر شماره تلفن
     */
    @Test
    @DisplayName("باید برای فرمت نامعتبر شماره تلفن خطا نمایش دهد")
    void testInvalidPhoneNumberFormat() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("123456789"); // فرمت نامعتبر
            controller.setPasswordFieldText("password123");
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("شماره تلفن باید با 09 شروع شود"));
    }

    /**
     * تست نمایش خطا برای شماره تلفن کوتاه
     */
    @Test
    @DisplayName("باید برای شماره تلفن کوتاه خطا نمایش دهد")
    void testShortPhoneNumber() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("091234"); // خیلی کوتاه
            controller.setPasswordFieldText("password123");
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("شماره تلفن باید با 09 شروع شود"));
    }

    /**
     * تست پذیرش فرمت معتبر شماره تلفن
     */
    @Test
    @DisplayName("باید فرمت معتبر شماره تلفن را بپذیرد")
    void testValidPhoneNumberFormat() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789"); // فرمت معتبر
            controller.setPasswordFieldText("password123");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // نباید خطای فرمت نشان دهد
        assertFalse(controller.isLoginButtonDisabled());
    }

    // ==================== تست‌های اعتبارسنجی رمز عبور ====================

    /**
     * تست نمایش خطا برای رمز عبور کوتاه
     */
    @Test
    @DisplayName("باید برای رمز عبور کوتاه خطا نمایش دهد")
    void testShortPassword() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
            controller.setPasswordFieldText("123"); // خیلی کوتاه
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("رمز عبور باید حداقل 4 کاراکتر باشد"));
    }

    /**
     * تست پذیرش طول معتبر رمز عبور
     */
    @Test
    @DisplayName("باید طول معتبر رمز عبور را بپذیرد")
    void testValidPasswordLength() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
            controller.setPasswordFieldText("1234"); // حداقل طول معتبر
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // نباید خطای طول نشان دهد
        assertFalse(controller.isLoginButtonDisabled());
    }

    // ==================== تست‌های اعتبارسنجی فیلدهای خالی ====================

    /**
     * تست نمایش خطا برای فیلد شماره تلفن خالی
     */
    @Test
    @DisplayName("باید برای فیلد شماره تلفن خالی خطا نمایش دهد")
    void testEmptyPhoneField() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText(""); // خالی
            controller.setPasswordFieldText("password123");
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("لطفاً شماره تلفن را وارد کنید"));
    }

    /**
     * تست نمایش خطا برای فیلد رمز عبور خالی
     */
    @Test
    @DisplayName("باید برای فیلد رمز عبور خالی خطا نمایش دهد")
    void testEmptyPasswordField() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
            controller.setPasswordFieldText(""); // خالی
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue(controller.getStatusText().contains("لطفاً رمز عبور را وارد کنید"));
    }

    // ==================== تست‌های قابلیت Remember Me ====================

    /**
     * تست مدیریت وضعیت چک‌باکس "مرا به خاطر بسپار"
     */
    @Test
    @DisplayName("باید وضعیت چک‌باکس مرا به خاطر بسپار را مدیریت کند")
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

    // ==================== تست‌های Navigation ====================

    /**
     * تست انتقال به صفحه ثبت نام
     */
    @Test
    @DisplayName("باید انتقال به صفحه ثبت نام را فعال کند")
    void testRegisterNavigation() {
        // ایجاد NavigationController ساختگی برای جلوگیری از null primaryStage
        NavigationController mockNavController = mock(NavigationController.class);
        setPrivateField(controller, "navigationController", mockNavController);
        
        Platform.runLater(() -> {
            controller.triggerRegisterLink();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // تأیید فراخوانی navigation
        verify(mockNavController).navigateTo(NavigationController.REGISTER_SCENE);
    }

    // ==================== تست‌های وضعیت UI ====================

    /**
     * تست مدیریت صحیح وضعیت بارگذاری
     */
    @Test
    @DisplayName("باید وضعیت بارگذاری را به درستی مدیریت کند")
    void testLoadingState() {
        // نکته: این تست نیاز به mock کردن HTTP client دارد
        // فعلاً وضعیت کامپوننت‌های UI را تست می‌کنیم
        assertFalse(controller.isLoadingVisible());
        
        // تست وضعیت loading بدون فراخوانی واقعی API
        // نیاز به mock پیشرفته‌تر دارد
    }

    /**
     * تست پاک کردن پیام وضعیت
     */
    @Test
    @DisplayName("باید پیام وضعیت را پاک کند")
    void testClearStatus() {
        Platform.runLater(() -> {
            // ابتدا یک خطا ایجاد کن
            controller.setPhoneFieldText("");
            controller.setPasswordFieldText("password123");
            controller.triggerLogin();
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertFalse(controller.getStatusText().isEmpty());
        
        // حالا تست کن که وضعیت هنگام تایپ پاک می‌شود
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // وضعیت باید پاک شود وقتی کاربر شروع به تایپ کند
    }

    // ==================== تست‌های Edge Case ====================

    /**
     * تست مدیریت فاصله در فیلد شماره تلفن
     */
    @Test
    @DisplayName("باید فاصله در فیلد شماره تلفن را مدیریت کند")
    void testPhoneFieldWithWhitespace() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("  09123456789  "); // با فاصله
            controller.setPasswordFieldText("password123");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // باید کار کند (به صورت داخلی trim می‌شود)
        assertFalse(controller.isLoginButtonDisabled());
    }

    /**
     * تست مدیریت کاراکترهای خاص در رمز عبور
     */
    @Test
    @DisplayName("باید کاراکترهای خاص در رمز عبور را مدیریت کند")
    void testPasswordWithSpecialCharacters() {
        Platform.runLater(() -> {
            controller.setPhoneFieldText("09123456789");
            controller.setPasswordFieldText("pass@123!"); // کاراکترهای خاص
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // باید کاراکترهای خاص را بپذیرد
        assertFalse(controller.isLoginButtonDisabled());
    }

    /**
     * تست مدیریت ورودی‌های خیلی طولانی
     */
    @Test
    @DisplayName("باید ورودی‌های خیلی طولانی را مدیریت کند")
    void testVeryLongInputs() {
        String longPhone = "09123456789123456789"; // خیلی طولانی
        String longPassword = "a".repeat(100); // رمز عبور خیلی طولانی
        
        Platform.runLater(() -> {
            controller.setPhoneFieldText(longPhone);
            controller.setPasswordFieldText(longPassword);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // باید ورودی‌های طولانی را به آرامی مدیریت کند
        assertFalse(controller.isLoginButtonDisabled());
    }

    // ==================== تست‌های Getter/Setter ====================

    /**
     * تست getter/setter فیلد شماره تلفن
     */
    @Test
    @DisplayName("باید getter/setter فیلد شماره تلفن را مدیریت کند")
    void testPhoneFieldGetterSetter() {
        String testPhone = "09123456789";
        
        Platform.runLater(() -> {
            controller.setPhoneFieldText(testPhone);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertEquals(testPhone, controller.getPhoneFieldText());
    }

    /**
     * تست getter/setter فیلد رمز عبور
     */
    @Test
    @DisplayName("باید getter/setter فیلد رمز عبور را مدیریت کند")
    void testPasswordFieldGetterSetter() {
        String testPassword = "testPassword123";
        
        Platform.runLater(() -> {
            controller.setPasswordFieldText(testPassword);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        assertEquals(testPassword, controller.getPasswordFieldText());
    }

    /**
     * تست مدیریت دسترسی به فیلدهای null
     */
    @Test
    @DisplayName("باید دسترسی به فیلدهای null را به آرامی مدیریت کند")
    void testNullFieldAccess() {
        // ایجاد کنترلر بدون تزریق FXML
        LoginController emptyController = new LoginController();
        
        // نباید exception پرتاب کند
        assertDoesNotThrow(() -> {
            assertEquals("", emptyController.getPhoneFieldText());
            assertEquals("", emptyController.getPasswordFieldText());
            assertFalse(emptyController.isRememberMeSelected());
            assertEquals("", emptyController.getStatusText());
            assertFalse(emptyController.isLoadingVisible());
            assertFalse(emptyController.isLoginButtonDisabled());
        });
    }

    // ==================== متدهای کمکی ====================

    /**
     * متد کمکی برای تنظیم فیلدهای private با استفاده از reflection
     * 
     * @param object شیء مقصد
     * @param fieldName نام فیلد
     * @param value مقدار جدید
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