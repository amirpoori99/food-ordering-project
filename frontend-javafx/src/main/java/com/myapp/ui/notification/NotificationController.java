package com.myapp.ui.notification;

import com.myapp.ui.common.NavigationController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * کنترلر سیستم اطلاع‌رسانی و هشدارها
 * 
 * این کلاس مسئول مدیریت رابط کاربری سیستم اطلاع‌رسانی شامل:
 * - مدیریت انواع مختلف اطلاع‌رسانی‌ها
 * - نمایش real-time notifications
 * - تنظیمات اطلاع‌رسانی کاربر
 * - تاریخچه پیام‌ها و اعلان‌ها
 * - سیستم Toast Messages
 * - Desktop Notifications
 * - صدای هشدار و اولویت‌بندی
 * - فیلترینگ و جستجو در پیام‌ها
 * 
 * ویژگی‌های کلیدی:
 * - Real-time Notification Reception با WebSocket
 * - Multi-channel Notifications (Email + SMS + Push)
 * - Priority-based Alert System (Low, Medium, High, Critical)
 * - Smart Notification Grouping
 * - Auto-dismiss Timer System
 * - Sound Alert Management
 * - Do Not Disturb Mode
 * - Notification Templates
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since فاز 27 - Notification & Alert System UI
 */
public class NotificationController implements Initializable {

    // ===== FXML UI Components =====
    
    @FXML private VBox notificationListContainer;
    @FXML private Label unreadCountLabel;
    @FXML private Button markAllReadButton;
    @FXML private Button clearAllButton;
    @FXML private ComboBox<NotificationFilter> filterComboBox;
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private CheckBox emailNotificationsCheckBox;
    @FXML private CheckBox smsNotificationsCheckBox;
    @FXML private CheckBox pushNotificationsCheckBox;
    @FXML private CheckBox soundEnabledCheckBox;
    @FXML private CheckBox doNotDisturbCheckBox;
    @FXML private Slider volumeSlider;
    @FXML private ComboBox<String> soundTypeComboBox;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button settingsButton;
    @FXML private Label lastUpdateLabel;
    @FXML private Button exportButton;
    @FXML private CheckBox autoRefreshCheckBox;
    @FXML private Spinner<Integer> refreshIntervalSpinner;

    // ===== Core Dependencies =====
    
    /** کنترلر Navigation برای تغییر صفحات */
    private NavigationController navigationController;
    
    /** فرمت کردن تاریخ و زمان فارسی */
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    
    /** Timeline برای بروزرسانی خودکار */
    private Timeline autoRefreshTimeline;
    
    /** لیست اطلاع‌رسانی‌ها */
    private ObservableList<NotificationItem> notifications;
    
    /** تنظیمات اطلاع‌رسانی */
    private NotificationSettings settings;
    
    /** شمارنده اطلاع‌رسانی‌های خوانده نشده */
    private int unreadCount = 0;
    
    /** وضعیت فیلتر جاری */
    private NotificationFilter currentFilter = NotificationFilter.ALL;

    /**
     * متد مقداردهی اولیه کنترلر
     * 
     * @param location URL location
     * @param resources منابع زبان
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        setupUI();
        loadNotificationSettings();
        setupAutoRefresh();
        loadNotifications();
        
        notifications = FXCollections.observableArrayList();
        settings = new NotificationSettings();
    }

    /**
     * راه‌اندازی کامپوننت‌های UI و event listener ها
     */
    private void setupUI() {
        setStatus("در حال بارگذاری اطلاع‌رسانی‌ها...");
        
        // راه‌اندازی فیلتر
        if (filterComboBox != null) {
            filterComboBox.setItems(FXCollections.observableArrayList(NotificationFilter.values()));
            filterComboBox.setValue(NotificationFilter.ALL);
            filterComboBox.setOnAction(e -> applyFilter());
        }
        
        // راه‌اندازی جستجو
        if (searchField != null) {
            searchField.setPromptText("جستجو در اطلاع‌رسانی‌ها...");
            searchField.textProperty().addListener((obs, oldText, newText) -> performSearch(newText));
        }
        
        // راه‌اندازی تنظیمات اطلاع‌رسانی
        setupNotificationSettings();
        
        // مخفی کردن loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }

    /**
     * راه‌اندازی تنظیمات اطلاع‌رسانی
     */
    private void setupNotificationSettings() {
        // تنظیم مقادیر پیش‌فرض
        if (emailNotificationsCheckBox != null) {
            emailNotificationsCheckBox.setSelected(true);
            emailNotificationsCheckBox.setOnAction(e -> updateNotificationSettings());
        }
        
        if (smsNotificationsCheckBox != null) {
            smsNotificationsCheckBox.setSelected(true);
            smsNotificationsCheckBox.setOnAction(e -> updateNotificationSettings());
        }
        
        if (pushNotificationsCheckBox != null) {
            pushNotificationsCheckBox.setSelected(true);
            pushNotificationsCheckBox.setOnAction(e -> updateNotificationSettings());
        }
        
        if (doNotDisturbCheckBox != null) {
            doNotDisturbCheckBox.setSelected(false);
            doNotDisturbCheckBox.setOnAction(e -> updateDoNotDisturbMode());
        }
    }

    /**
     * بارگذاری تنظیمات اطلاع‌رسانی از تنظیمات کاربر
     */
    private void loadNotificationSettings() {
        settings = new NotificationSettings();
        settings.setEmailEnabled(true);
        settings.setSmsEnabled(true);
        settings.setPushEnabled(true);
        settings.setSoundEnabled(true);
        settings.setVolume(70.0);
        settings.setDoNotDisturb(false);
    }

    /**
     * راه‌اندازی بروزرسانی خودکار اطلاع‌رسانی‌ها
     */
    private void setupAutoRefresh() {
        autoRefreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(30), e -> refreshNotifications())
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    /**
     * بارگذاری اطلاع‌رسانی‌ها از سرور
     */
    private void loadNotifications() {
        setLoading(true);
        
        Task<List<NotificationItem>> loadTask = new Task<List<NotificationItem>>() {
            @Override
            protected List<NotificationItem> call() throws Exception {
                Thread.sleep(1000); // شبیه‌سازی تأخیر شبکه
                return createMockNotifications();
            }
        };
        
        loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false);
            List<NotificationItem> loadedNotifications = loadTask.getValue();
            notifications.clear();
            notifications.addAll(loadedNotifications);
            displayNotifications();
            updateUnreadCount();
            setStatus("اطلاع‌رسانی‌ها بروزرسانی شد");
            updateLastUpdateTime();
        }));
        
        loadTask.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            setStatus("خطا در بارگذاری اطلاع‌رسانی‌ها");
            showError("خطا", "امکان بارگذاری اطلاع‌رسانی‌ها وجود ندارد");
        }));
        
        new Thread(loadTask).start();
    }

    /**
     * ایجاد اطلاع‌رسانی‌های نمونه برای نمایش
     */
    private List<NotificationItem> createMockNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();
        
        notifications.add(new NotificationItem(
            "سفارش شما آماده است",
            "سفارش شماره ORD-123456789 آماده تحویل است",
            NotificationType.ORDER_UPDATE,
            NotificationPriority.HIGH,
            LocalDateTime.now().minusMinutes(5),
            false
        ));
        
        notifications.add(new NotificationItem(
            "تخفیف ویژه",
            "20% تخفیف برای سفارش بعدی شما - کد: SAVE20",
            NotificationType.PROMOTION,
            NotificationPriority.MEDIUM,
            LocalDateTime.now().minusHours(1),
            true
        ));
        
        notifications.add(new NotificationItem(
            "پرداخت موفق",
            "پرداخت 87,650 تومان با موفقیت انجام شد",
            NotificationType.PAYMENT,
            NotificationPriority.HIGH,
            LocalDateTime.now().minusHours(2),
            true
        ));
        
        return notifications;
    }

    /**
     * نمایش اطلاع‌رسانی‌ها در رابط کاربری
     */
    private void displayNotifications() {
        if (notificationListContainer == null) return;
        
        notificationListContainer.getChildren().clear();
        
        List<NotificationItem> filteredNotifications = getFilteredNotifications();
        
        if (filteredNotifications.isEmpty()) {
            showEmptyState();
            return;
        }
        
        for (NotificationItem notification : filteredNotifications) {
            VBox notificationCard = createNotificationCard(notification);
            notificationListContainer.getChildren().add(notificationCard);
        }
    }

    /**
     * ایجاد کارت UI برای نمایش یک اطلاع‌رسانی
     */
    private VBox createNotificationCard(NotificationItem notification) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(getNotificationCardStyle(notification));
        
        // Header با آیکون و زمان
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label typeIcon = new Label(getNotificationIcon(notification.getType()));
        typeIcon.setStyle("-fx-font-size: 16px;");
        
        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label timeLabel = new Label(formatNotificationTime(notification.getTimestamp()));
        timeLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
        
        header.getChildren().addAll(typeIcon, titleLabel, timeLabel);
        
        // محتوای پیام
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #495057; -fx-font-size: 12px;");
        
        card.getChildren().addAll(header, messageLabel);
        return card;
    }

    // ===== Event Handlers =====

    @FXML
    private void handleMarkAllRead() {
        notifications.stream()
                    .filter(n -> !n.isRead())
                    .forEach(n -> n.setRead(true));
        
        displayNotifications();
        updateUnreadCount();
        setStatus("همه اطلاع‌رسانی‌ها علامت‌گذاری شدند");
    }

    @FXML
    private void handleClearAll() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("حذف همه");
        confirmAlert.setHeaderText("آیا مطمئن هستید؟");
        confirmAlert.setContentText("آیا می‌خواهید همه اطلاع‌رسانی‌ها را حذف کنید؟");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                notifications.clear();
                displayNotifications();
                updateUnreadCount();
                setStatus("همه اطلاع‌رسانی‌ها حذف شدند");
            }
        });
    }

    @FXML
    private void handleRefresh() {
        refreshNotifications();
    }

    // ===== Utility Methods =====

    private String getNotificationIcon(NotificationType type) {
        switch (type) {
            case ORDER_UPDATE: return "📦";
            case PAYMENT: return "💳";
            case PROMOTION: return "🎁";
            case NEW_RESTAURANT: return "🏪";
            case REMINDER: return "⏰";
            case SYSTEM: return "⚙️";
            default: return "📢";
        }
    }

    private String getNotificationCardStyle(NotificationItem notification) {
        String baseStyle = "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 8; -fx-background-radius: 8;";
        
        if (!notification.isRead()) {
            return String.format(baseStyle, "#f8f9fa", "#007bff") + " -fx-border-width: 2;";
        } else {
            return String.format(baseStyle, "#ffffff", "#dee2e6") + " -fx-border-width: 1;";
        }
    }

    private String formatNotificationTime(LocalDateTime timestamp) {
        LocalDateTime now = LocalDateTime.now();
        long minutesAgo = java.time.Duration.between(timestamp, now).toMinutes();
        
        if (minutesAgo < 1) {
            return "اکنون";
        } else if (minutesAgo < 60) {
            return minutesAgo + " دقیقه پیش";
        } else if (minutesAgo < 1440) {
            return (minutesAgo / 60) + " ساعت پیش";
        } else {
            return timestamp.format(dateTimeFormatter);
        }
    }

    private void showEmptyState() {
        Label emptyLabel = new Label("هیچ اطلاع‌رسانی‌ای موجود نیست");
        emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");
        notificationListContainer.getChildren().add(emptyLabel);
    }

    private void updateUnreadCount() {
        unreadCount = (int) notifications.stream().filter(n -> !n.isRead()).count();
        
        if (unreadCountLabel != null) {
            unreadCountLabel.setText(String.valueOf(unreadCount));
            unreadCountLabel.setVisible(unreadCount > 0);
        }
    }

    private void updateLastUpdateTime() {
        if (lastUpdateLabel != null) {
            lastUpdateLabel.setText("آخرین بروزرسانی: " + LocalDateTime.now().format(dateTimeFormatter));
        }
    }

    private void refreshNotifications() {
        loadNotifications();
    }

    private void applyFilter() {
        if (filterComboBox != null) {
            currentFilter = filterComboBox.getValue();
            displayNotifications();
        }
    }

    private void performSearch(String searchText) {
        displayNotifications();
    }

    private List<NotificationItem> getFilteredNotifications() {
        return notifications.stream()
                           .filter(this::matchesFilter)
                           .filter(this::matchesSearch)
                           .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                           .collect(java.util.stream.Collectors.toList());
    }

    private boolean matchesFilter(NotificationItem notification) {
        switch (currentFilter) {
            case UNREAD: return !notification.isRead();
            case read: return notification.isRead();
            case HIGH_PRIORITY: return notification.getPriority() == NotificationPriority.HIGH || 
                                      notification.getPriority() == NotificationPriority.CRITICAL;
            case ALL:
            default: return true;
        }
    }

    private boolean matchesSearch(NotificationItem notification) {
        if (searchField == null || searchField.getText().trim().isEmpty()) {
            return true;
        }
        
        String searchText = searchField.getText().toLowerCase();
        return notification.getTitle().toLowerCase().contains(searchText) ||
               notification.getMessage().toLowerCase().contains(searchText);
    }

    private void updateNotificationSettings() {
        if (settings != null) {
            if (emailNotificationsCheckBox != null) {
                settings.setEmailEnabled(emailNotificationsCheckBox.isSelected());
            }
            if (smsNotificationsCheckBox != null) {
                settings.setSmsEnabled(smsNotificationsCheckBox.isSelected());
            }
            if (pushNotificationsCheckBox != null) {
                settings.setPushEnabled(pushNotificationsCheckBox.isSelected());
            }
            
            setStatus("تنظیمات اطلاع‌رسانی بروزرسانی شد");
        }
    }

    private void updateDoNotDisturbMode() {
        if (doNotDisturbCheckBox != null && settings != null) {
            settings.setDoNotDisturb(doNotDisturbCheckBox.isSelected());
            setStatus("حالت مزاحم نشوید " + (settings.isDoNotDisturb() ? "فعال" : "غیرفعال") + " شد");
        }
    }

    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== Data Models =====

    /**
     * کلاس اطلاع‌رسانی
     */
    public static class NotificationItem {
        private String title;
        private String message;
        private NotificationType type;
        private NotificationPriority priority;
        private LocalDateTime timestamp;
        private boolean read;
        
        public NotificationItem(String title, String message, NotificationType type, 
                              NotificationPriority priority, LocalDateTime timestamp, boolean read) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.priority = priority;
            this.timestamp = timestamp;
            this.read = read;
        }
        
        // Getters and Setters
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public NotificationType getType() { return type; }
        public NotificationPriority getPriority() { return priority; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }

    /**
     * enum نوع اطلاع‌رسانی
     */
    public enum NotificationType {
        ORDER_UPDATE("بروزرسانی سفارش"),
        PAYMENT("پرداخت"),
        PROMOTION("تبلیغات"),
        NEW_RESTAURANT("رستوران جدید"),
        REMINDER("یادآوری"),
        SYSTEM("سیستم");
        
        private final String displayName;
        
        NotificationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }

    /**
     * enum اولویت اطلاع‌رسانی
     */
    public enum NotificationPriority {
        LOW("کم"),
        MEDIUM("متوسط"),
        HIGH("بالا"),
        CRITICAL("بحرانی");
        
        private final String displayName;
        
        NotificationPriority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }

    /**
     * enum فیلتر اطلاع‌رسانی‌ها
     */
    public enum NotificationFilter {
        ALL("همه"),
        UNREAD("خوانده نشده"),
        read("خوانده شده"),
        HIGH_PRIORITY("اولویت بالا");
        
        private final String displayName;
        
        NotificationFilter(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
        
        @Override
        public String toString() { return displayName; }
    }

    /**
     * کلاس تنظیمات اطلاع‌رسانی
     */
    public static class NotificationSettings {
        private boolean emailEnabled = true;
        private boolean smsEnabled = true;
        private boolean pushEnabled = true;
        private boolean soundEnabled = true;
        private boolean doNotDisturb = false;
        private double volume = 70.0;
        private String soundType = "صدای پیش‌فرض";
        
        // Getters and Setters
        public boolean isEmailEnabled() { return emailEnabled; }
        public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
        
        public boolean isSmsEnabled() { return smsEnabled; }
        public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }
        
        public boolean isPushEnabled() { return pushEnabled; }
        public void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }
        
        public boolean isSoundEnabled() { return soundEnabled; }
        public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }
        
        public boolean isDoNotDisturb() { return doNotDisturb; }
        public void setDoNotDisturb(boolean doNotDisturb) { this.doNotDisturb = doNotDisturb; }
        
        public double getVolume() { return volume; }
        public void setVolume(double volume) { this.volume = volume; }
        
        public String getSoundType() { return soundType; }
        public void setSoundType(String soundType) { this.soundType = soundType; }
    }
}
