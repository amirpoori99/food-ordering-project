package com.myapp.ui.common;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * سرویس مدیریت اعلان‌ها و پیام‌های سیستم
 * 
 * این کلاس مسئول نمایش انواع مختلف اعلان‌ها و پیام‌ها شامل:
 * - اعلان‌های Toast (پیام‌های کوتاه موقت)
 * - Dialog های تأیید
 * - پیام‌های خطا، موفقیت، هشدار و اطلاعات
 * - اعلان‌های پیشرفته با انیمیشن
 * - مدیریت موقعیت نمایش
 * 
 * Pattern های استفاده شده:
 * - Singleton Pattern: یک instance در کل برنامه
 * - Builder Pattern: برای ساخت notification های پیچیده
 * - Observer Pattern: برای callback ها
 * - Factory Pattern: برای انواع مختلف notification
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class NotificationService {
    
    /** instance singleton */
    private static NotificationService instance;
    
    /** Stage اصلی برنامه برای محاسبه موقعیت */
    private Stage primaryStage;
    
    /**
     * سازنده private برای پیاده‌سازی Singleton
     */
    private NotificationService() {}
    
    /**
     * دریافت instance singleton
     * 
     * @return instance NotificationService
     */
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    /**
     * مقداردهی اولیه NotificationService
     * 
     * @param primaryStage Stage اصلی برنامه
     */
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    // ==================== TOAST NOTIFICATIONS ====================
    
    /**
     * نمایش اعلان Toast با نوع موفقیت
     * 
     * @param message متن پیام
     */
    public void showSuccess(String message) {
        showToast(message, NotificationType.SUCCESS, Duration.seconds(3));
    }
    
    /**
     * نمایش اعلان Toast با نوع خطا
     * 
     * @param message متن پیام
     */
    public void showError(String message) {
        showToast(message, NotificationType.ERROR, Duration.seconds(5));
    }
    
    /**
     * نمایش اعلان Toast با نوع هشدار
     * 
     * @param message متن پیام
     */
    public void showWarning(String message) {
        showToast(message, NotificationType.WARNING, Duration.seconds(4));
    }
    
    /**
     * نمایش اعلان Toast با نوع اطلاعات
     * 
     * @param message متن پیام
     */
    public void showInfo(String message) {
        showToast(message, NotificationType.INFO, Duration.seconds(3));
    }
    
    /**
     * نمایش اعلان Toast با پارامترهای کامل
     * 
     * @param message متن پیام
     * @param type نوع اعلان
     * @param duration مدت زمان نمایش
     */
    public void showToast(String message, NotificationType type, Duration duration) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showToast(message, type, duration));
            return;
        }
        
        // ایجاد Toast UI
        Label toastLabel = createToastLabel(message, type);
        HBox toastContainer = new HBox(toastLabel);
        toastContainer.setAlignment(Pos.CENTER);
        toastContainer.setPadding(new Insets(10, 20, 10, 20));
        toastContainer.setBackground(new Background(new BackgroundFill(
            getBackgroundColor(type), 
            new CornerRadii(5), 
            Insets.EMPTY
        )));
        
        // ایجاد Popup
        Popup popup = new Popup();
        popup.getContent().add(toastContainer);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        
        // محاسبه موقعیت نمایش
        if (primaryStage != null) {
            double x = primaryStage.getX() + primaryStage.getWidth() - 350;
            double y = primaryStage.getY() + 50;
            popup.show(primaryStage, x, y);
        } else {
            popup.show(null);
        }
        
        // بستن خودکار بعد از مدت زمان مشخص
        CompletableFuture.delayedExecutor((long) duration.toMillis(), 
            java.util.concurrent.TimeUnit.MILLISECONDS)
            .execute(() -> Platform.runLater(popup::hide));
    }
    
    /**
     * ایجاد Label برای Toast
     * 
     * @param message متن پیام
     * @param type نوع اعلان
     * @return Label آماده نمایش
     */
    private Label createToastLabel(String message, NotificationType type) {
        Label label = new Label(message);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        label.setMaxWidth(300);
        label.setWrapText(true);
        return label;
    }
    
    /**
     * دریافت رنگ پس‌زمینه بر اساس نوع اعلان
     * 
     * @param type نوع اعلان
     * @return رنگ پس‌زمینه
     */
    private Color getBackgroundColor(NotificationType type) {
        switch (type) {
            case SUCCESS: return Color.web("#28a745");
            case ERROR: return Color.web("#dc3545");
            case WARNING: return Color.web("#ffc107");
            case INFO: return Color.web("#17a2b8");
            default: return Color.web("#6c757d");
        }
    }
    
    // ==================== DIALOG NOTIFICATIONS ====================
    
    /**
     * نمایش dialog اطلاعات
     * 
     * @param title عنوان dialog
     * @param message متن پیام
     */
    public void showInfoDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * نمایش dialog خطا
     * 
     * @param title عنوان dialog
     * @param message متن پیام خطا
     */
    public void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * نمایش dialog هشدار
     * 
     * @param title عنوان dialog
     * @param message متن پیام هشدار
     */
    public void showWarningDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * نمایش dialog تأیید و دریافت پاسخ کاربر
     * 
     * @param title عنوان dialog
     * @param message متن پیام
     * @return CompletableFuture<Boolean> که true برای تأیید و false برای لغو
     */
    public CompletableFuture<Boolean> showConfirmDialog(String title, String message) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            
            Optional<ButtonType> result = alert.showAndWait();
            future.complete(result.isPresent() && result.get() == ButtonType.YES);
        });
        
        return future;
    }
    
    /**
     * نمایش dialog تأیید برای خروج
     * 
     * @return CompletableFuture<Boolean> که true برای تأیید خروج
     */
    public CompletableFuture<Boolean> showLogoutConfirm() {
        return showConfirmDialog("تأیید خروج", 
            "آیا مطمئن هستید که می‌خواهید از حساب کاربری خود خارج شوید؟");
    }
    
    /**
     * نمایش dialog تأیید برای حذف
     * 
     * @param itemName نام آیتم برای حذف
     * @return CompletableFuture<Boolean> که true برای تأیید حذف
     */
    public CompletableFuture<Boolean> showDeleteConfirm(String itemName) {
        return showConfirmDialog("تأیید حذف", 
            "آیا مطمئن هستید که می‌خواهید " + itemName + " را حذف کنید؟\n" +
            "این عمل قابل بازگشت نیست.");
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * نمایش پیام در console (برای debugging)
     * 
     * @param message متن پیام
     * @param type نوع پیام
     */
    public void logMessage(String message, NotificationType type) {
        String prefix = "[" + type.name() + "] ";
        System.out.println(prefix + message);
    }
    
    /**
     * بررسی در دسترس بودن سرویس notification
     * 
     * @return true اگر سرویس آماده استفاده باشد
     */
    public boolean isAvailable() {
        return primaryStage != null;
    }
    
    /**
     * ریست کردن instance singleton (برای تست)
     */
    public static void resetInstance() {
        instance = null;
    }
    
    // ==================== NOTIFICATION BUILDER ====================
    
    /**
     * Builder pattern برای ساخت notification های پیچیده
     */
    public static class NotificationBuilder {
        private String message;
        private String title;
        private NotificationType type = NotificationType.INFO;
        private Duration duration = Duration.seconds(3);
        private boolean showAsDialog = false;
        
        public NotificationBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public NotificationBuilder title(String title) {
            this.title = title;
            return this;
        }
        
        public NotificationBuilder type(NotificationType type) {
            this.type = type;
            return this;
        }
        
        public NotificationBuilder duration(Duration duration) {
            this.duration = duration;
            return this;
        }
        
        public NotificationBuilder asDialog() {
            this.showAsDialog = true;
            return this;
        }
        
        public void show() {
            NotificationService service = NotificationService.getInstance();
            
            if (showAsDialog) {
                switch (type) {
                    case SUCCESS:
                    case INFO:
                        service.showInfoDialog(title != null ? title : "اطلاع", message);
                        break;
                    case ERROR:
                        service.showErrorDialog(title != null ? title : "خطا", message);
                        break;
                    case WARNING:
                        service.showWarningDialog(title != null ? title : "هشدار", message);
                        break;
                }
            } else {
                service.showToast(message, type, duration);
            }
        }
    }
    
    /**
     * ایجاد NotificationBuilder جدید
     * 
     * @return instance جدید NotificationBuilder
     */
    public static NotificationBuilder create() {
        return new NotificationBuilder();
    }
    
    // ==================== ENUMS ====================
    
    /**
     * انواع اعلان‌های موجود
     */
    public enum NotificationType {
        /** اعلان موفقیت (سبز) */
        SUCCESS,
        
        /** اعلان خطا (قرمز) */
        ERROR,
        
        /** اعلان هشدار (زرد) */
        WARNING,
        
        /** اعلان اطلاعات (آبی) */
        INFO
    }
}
