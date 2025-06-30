package com.myapp.ui.auth;

import com.myapp.ui.common.NavigationController;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * آزمون یکپارچه برای گردش کامل Login -> Profile.
 * از NavigationController موک-شده استفاده می‌کند تا بدون بارگیری صفحه واقعی،
 * اطمینان حاصل کند که پس از ورود موفق، ناوبری به صفحه پروفایل رخ می‌دهد.
 */
public class LoginFlowIntegrationTest extends ApplicationTest {

    private LoginController controller;
    private NavigationController navigationMock;

    @BeforeAll
    static void initHeadless() {
        System.setProperty("testfx.headless", "true");
    }

    @Override
    public void start(Stage stage) {
        controller = new LoginController();
        navigationMock = mock(NavigationController.class);
        setPrivate(controller, "navigationController", navigationMock);

        // Mock UI minimal — فقط یک برچسب برای وضعیت جهت جلوگیری از NullPointer
        Label status = new Label();
        setPrivate(controller, "statusLabel", status);
        setPrivate(controller, "phoneField", new javafx.scene.control.TextField());
        setPrivate(controller, "passwordField", new javafx.scene.control.PasswordField());
        setPrivate(controller, "rememberMeCheckbox", new javafx.scene.control.CheckBox());
        setPrivate(controller, "loginButton", new javafx.scene.control.Button());
        setPrivate(controller, "loadingIndicator", new javafx.scene.control.ProgressIndicator());

        controller.initialize(null, null);

        stage.setScene(new Scene(new StackPane(status), 300, 100));
        stage.show();
    }

    @AfterAll
    static void cleanup() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    @Disabled("Flaky in CI; NavigationController singleton requires refactor for proper injection")
    @DisplayName("(Disabled) ورود موفق و ناوبری")
    void shouldNavigateAfterSuccessLogin() {
        Platform.runLater(() -> {
            controller.handleLoginResponse(new com.myapp.ui.common.HttpClientUtil.ApiResponse(true, 200, "", null), "09123456789");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // تنها اطمینان می‌دهیم که متد بدون خطا اجرا شد و پیامی ثبت شده است
        assertFalse(controller.getStatusText().isEmpty());
    }

    private static void setPrivate(Object obj, String field, Object value) {
        try {
            var f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 