package com.myapp.ui.common;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * کنترلر بهبود UI/UX و قابلیت‌های نهایی
 * این کلاس شامل انیمیشن‌ها، افکت‌های بصری و بهبود تجربه کاربری
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class UIPolishController implements Initializable {

    @FXML private VBox mainContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;
    @FXML private Button actionButton;
    @FXML private ListView<String> notificationList;
    @FXML private TabPane mainTabPane;
    @FXML private VBox sidebarContainer;
    @FXML private HBox headerContainer;
    @FXML private VBox contentContainer;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;
    @FXML private Button closeButton;

    // Properties for reactive UI
    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final StringProperty currentStatus = new SimpleStringProperty("");
    private final ObservableList<String> notifications = FXCollections.observableArrayList();
    
    // Animation properties
    private FadeTransition fadeTransition;
    private ScaleTransition scaleTransition;
    private TranslateTransition slideTransition;
    
    // Background services
    private ScheduledExecutorService backgroundExecutor;
    private boolean isInitialized = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (isInitialized) return;
        
        setupUIComponents();
        setupAnimations();
        setupEventHandlers();
        setupBackgroundServices();
        applyTheme();
        
        isInitialized = true;
    }

    /**
     * راه‌اندازی کامپوننت‌های UI
     */
    private void setupUIComponents() {
        // Setup main container
        mainContainer.setSpacing(10);
        mainContainer.setPadding(new Insets(20));
        
        // Setup loading indicator
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(50, 50);
        
        // Setup status label
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        statusLabel.textProperty().bind(currentStatus);
        
        // Setup action button
        actionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        actionButton.setMinHeight(40);
        
        // Setup notification list
        notificationList.setItems(notifications);
        notificationList.setMaxHeight(200);
        notificationList.setStyle("-fx-background-color: transparent;");
        
        // Setup tab pane
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Setup sidebar
        sidebarContainer.setPrefWidth(250);
        sidebarContainer.setStyle("-fx-background-color: #2C3E50;");
        
        // Setup header
        headerContainer.setStyle("-fx-background-color: #34495E; -fx-padding: 10;");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: white;");
        
        // Setup content container
        contentContainer.setStyle("-fx-background-color: #ECF0F1;");
    }

    /**
     * راه‌اندازی انیمیشن‌ها
     */
    private void setupAnimations() {
        // Fade transition for loading
        fadeTransition = new FadeTransition(Duration.millis(500), loadingIndicator);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        
        // Scale transition for buttons
        scaleTransition = new ScaleTransition(Duration.millis(150), actionButton);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.95);
        scaleTransition.setToY(0.95);
        
        // Slide transition for notifications
        slideTransition = new TranslateTransition(Duration.millis(300), notificationList);
        slideTransition.setFromX(-300);
        slideTransition.setToX(0);
    }

    /**
     * راه‌اندازی event handlers
     */
    private void setupEventHandlers() {
        // Action button events
        actionButton.setOnMousePressed(e -> {
            scaleTransition.setRate(1.0);
            scaleTransition.play();
        });
        
        actionButton.setOnMouseReleased(e -> {
            scaleTransition.setRate(-1.0);
            scaleTransition.play();
        });
        
        actionButton.setOnAction(e -> handleActionButtonClick());
        
        // Window control buttons
        minimizeButton.setOnAction(e -> minimizeWindow());
        maximizeButton.setOnAction(e -> maximizeWindow());
        closeButton.setOnAction(e -> closeWindow());
        
        // Loading property binding
        loadingIndicator.visibleProperty().bind(isLoading);
        actionButton.disableProperty().bind(isLoading);
        
        // Tab change events
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                handleTabChange(newTab);
            }
        });
    }

    /**
     * راه‌اندازی سرویس‌های پس‌زمینه
     */
    private void setupBackgroundServices() {
        backgroundExecutor = Executors.newScheduledThreadPool(2);
        
        // Periodic UI updates
        backgroundExecutor.scheduleAtFixedRate(() -> {
            Platform.runLater(this::updateUI);
        }, 0, 5, TimeUnit.SECONDS);
        
        // Auto-clear notifications
        backgroundExecutor.scheduleAtFixedRate(() -> {
            Platform.runLater(this::clearOldNotifications);
        }, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * اعمال تم و استایل
     */
    private void applyTheme() {
        // Modern color scheme
        Color primaryColor = Color.web("#3498DB");
        Color secondaryColor = Color.web("#2ECC71");
        Color accentColor = Color.web("#E74C3C");
        Color backgroundColor = Color.web("#ECF0F1");
        Color textColor = Color.web("#2C3E50");
        
        // Apply drop shadow effects
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setSpread(0.1);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        
        mainContainer.setEffect(dropShadow);
        
        // Apply glow effect to active elements
        Glow glow = new Glow();
        glow.setLevel(0.3);
        
        // Style containers
        mainContainer.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 10;",
            backgroundColor.toString().replace("0x", "#")
        ));
        
        // Style buttons
        actionButton.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;",
            primaryColor.toString().replace("0x", "#")
        ));
    }

    /**
     * مدیریت کلیک دکمه عملیات
     */
    private void handleActionButtonClick() {
        setLoading(true);
        setStatus("در حال پردازش...");
        
        // Simulate async operation
        backgroundExecutor.schedule(() -> {
            Platform.runLater(() -> {
                setLoading(false);
                setStatus("عملیات با موفقیت انجام شد");
                addNotification("عملیات جدید تکمیل شد");
                showSuccessAnimation();
            });
        }, 2, TimeUnit.SECONDS);
    }

    /**
     * مدیریت تغییر تب
     */
    private void handleTabChange(Tab newTab) {
        String tabTitle = newTab.getText();
        setStatus("تب " + tabTitle + " انتخاب شد");
        
        // Apply tab-specific animations
        if (newTab.getContent() instanceof Region) {
            Region content = (Region) newTab.getContent();
            FadeTransition tabFade = new FadeTransition(Duration.millis(300), content);
            tabFade.setFromValue(0.0);
            tabFade.setToValue(1.0);
            tabFade.play();
        }
    }

    /**
     * به‌روزرسانی UI
     */
    private void updateUI() {
        // Update status based on current state
        if (isLoading.get()) {
            loadingIndicator.setProgress(-1); // Indeterminate progress
        } else {
            loadingIndicator.setProgress(0);
        }
        
        // Update notification count
        int notificationCount = notifications.size();
        if (notificationCount > 0) {
            subtitleLabel.setText(notificationCount + " اعلان جدید");
        } else {
            subtitleLabel.setText("هیچ اعلان جدیدی وجود ندارد");
        }
    }

    /**
     * پاک کردن اعلان‌های قدیمی
     */
    private void clearOldNotifications() {
        if (notifications.size() > 10) {
            notifications.remove(0, notifications.size() - 10);
        }
    }

    /**
     * نمایش انیمیشن موفقیت
     */
    private void showSuccessAnimation() {
        // Create success indicator
        Label successLabel = new Label("✓");
        successLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        successLabel.setStyle("-fx-text-fill: #27AE60;");
        
        // Position in center
        successLabel.setLayoutX(mainContainer.getWidth() / 2 - 20);
        successLabel.setLayoutY(mainContainer.getHeight() / 2 - 20);
        
        mainContainer.getChildren().add(successLabel);
        
        // Animate
        ScaleTransition successScale = new ScaleTransition(Duration.millis(500), successLabel);
        successScale.setFromX(0.0);
        successScale.setFromY(0.0);
        successScale.setToX(1.0);
        successScale.setToY(1.0);
        
        FadeTransition successFade = new FadeTransition(Duration.millis(1000), successLabel);
        successFade.setFromValue(1.0);
        successFade.setToValue(0.0);
        successFade.setDelay(Duration.millis(500));
        
        successScale.play();
        successFade.play();
        
        // Remove after animation
        successFade.setOnFinished(e -> mainContainer.getChildren().remove(successLabel));
    }

    /**
     * تنظیم وضعیت بارگذاری
     */
    public void setLoading(boolean loading) {
        isLoading.set(loading);
        
        if (loading) {
            fadeTransition.play();
        } else {
            fadeTransition.setRate(-1.0);
            fadeTransition.play();
        }
    }

    /**
     * تنظیم پیام وضعیت
     */
    public void setStatus(String status) {
        currentStatus.set(status);
    }

    /**
     * اضافه کردن اعلان جدید
     */
    public void addNotification(String notification) {
        notifications.add(notification);
        
        // Animate notification list
        if (notifications.size() == 1) {
            slideTransition.play();
        }
    }

    /**
     * پاک کردن تمام اعلان‌ها
     */
    public void clearNotifications() {
        notifications.clear();
    }

    /**
     * به حداقل رساندن پنجره
     */
    private void minimizeWindow() {
        if (mainContainer.getScene() != null && mainContainer.getScene().getWindow() instanceof javafx.stage.Stage) {
            javafx.stage.Stage stage = (javafx.stage.Stage) mainContainer.getScene().getWindow();
            stage.setIconified(true);
        }
    }

    /**
     * به حداکثر رساندن پنجره
     */
    private void maximizeWindow() {
        if (mainContainer.getScene() != null && mainContainer.getScene().getWindow() instanceof javafx.stage.Stage) {
            javafx.stage.Stage stage = (javafx.stage.Stage) mainContainer.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        }
    }

    /**
     * بستن پنجره
     */
    private void closeWindow() {
        if (mainContainer.getScene() != null && mainContainer.getScene().getWindow() != null) {
            mainContainer.getScene().getWindow().hide();
        }
    }

    /**
     * نمایش انیمیشن ورود
     */
    public void showEntranceAnimation() {
        // Slide in from top
        TranslateTransition entranceSlide = new TranslateTransition(Duration.millis(800), mainContainer);
        entranceSlide.setFromY(-mainContainer.getHeight());
        entranceSlide.setToY(0);
        
        // Fade in
        FadeTransition entranceFade = new FadeTransition(Duration.millis(800), mainContainer);
        entranceFade.setFromValue(0.0);
        entranceFade.setToValue(1.0);
        
        entranceSlide.play();
        entranceFade.play();
    }

    /**
     * نمایش انیمیشن خروج
     */
    public void showExitAnimation(Runnable onComplete) {
        // Slide out to bottom
        TranslateTransition exitSlide = new TranslateTransition(Duration.millis(500), mainContainer);
        exitSlide.setFromY(0);
        exitSlide.setToY(mainContainer.getHeight());
        
        // Fade out
        FadeTransition exitFade = new FadeTransition(Duration.millis(500), mainContainer);
        exitFade.setFromValue(1.0);
        exitFade.setToValue(0.0);
        
        exitSlide.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        exitSlide.play();
        exitFade.play();
    }

    /**
     * اعمال افکت hover روی دکمه‌ها
     */
    public void applyHoverEffects() {
        // Apply to all buttons in the container
        mainContainer.lookupAll("Button").forEach(node -> {
            if (node instanceof Button) {
                Button button = (Button) node;
                
                button.setOnMouseEntered(e -> {
                    button.setStyle(button.getStyle() + "; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
                });
                
                button.setOnMouseExited(e -> {
                    button.setStyle(button.getStyle().replace("; -fx-scale-x: 1.05; -fx-scale-y: 1.05;", ""));
                });
            }
        });
    }

    /**
     * پاکسازی منابع
     */
    public void cleanup() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
            try {
                if (!backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    backgroundExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                backgroundExecutor.shutdownNow();
            }
        }
    }

    // Getters for properties
    public BooleanProperty isLoadingProperty() {
        return isLoading;
    }

    public StringProperty currentStatusProperty() {
        return currentStatus;
    }

    public ObservableList<String> getNotifications() {
        return notifications;
    }
} 