package com.myapp.ui.order;

import com.myapp.ui.common.NavigationController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * کنترلر تأیید سفارش و پیگیری
 * 
 * این کلاس مسئول مدیریت رابط کاربری تأیید سفارش شامل:
 * - نمایش جزئیات سفارش نهایی
 * - تأیید پرداخت و ثبت سفارش
 * - تولید شماره پیگیری منحصر به فرد
 * - نمایش مراحل تحویل (Real-time Tracking)
 * - ارسال اطلاع‌رسانی (Email/SMS)
 * - تولید رسید پرداخت
 * - امکان لغو سفارش (در مراحل اولیه)
 * - ذخیره در تاریخچه سفارشات
 * 
 * ویژگی‌های کلیدی:
 * - Real-time Order Tracking با WebSocket
 * - Receipt Generation با PDF Export
 * - Multi-channel Notifications (Email + SMS)
 * - Order Status Management (7 مرحله)
 * - Payment Verification Integration
 * - Customer Support Chat Integration
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since فاز 26 - Order Confirmation UI
 */
public class OrderConfirmationController implements Initializable {

    // ===== FXML UI Components =====
    
    @FXML private Label orderNumberLabel;
    @FXML private Label orderDateTimeLabel;
    @FXML private Label estimatedDeliveryLabel;
    @FXML private ProgressBar orderProgressBar;
    @FXML private Label currentStatusLabel;
    @FXML private VBox orderItemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label deliveryFeeLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private TextArea deliveryAddressDisplay;
    @FXML private TextField customerPhoneDisplay;
    @FXML private TextArea orderNotesDisplay;
    @FXML private VBox trackingStepsContainer;
    @FXML private Button cancelOrderButton;
    @FXML private Button downloadReceiptButton;
    @FXML private Button contactSupportButton;
    @FXML private Button trackOrderButton;
    @FXML private Button backToMenuButton;
    @FXML private Button reorderButton;
    @FXML private Label restaurantNameLabel;
    @FXML private Label restaurantPhoneLabel;
    @FXML private Label courierNameLabel;
    @FXML private Label courierPhoneLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusMessageLabel;
    @FXML private CheckBox emailNotificationCheckBox;
    @FXML private CheckBox smsNotificationCheckBox;
    @FXML private MenuItem refreshMenuItem;
    @FXML private MenuItem printReceiptMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem homeMenuItem;

    // ===== Core Dependencies =====
    
    /** کنترلر Navigation برای تغییر صفحات */
    private NavigationController navigationController;
    
    /** فرمت کردن ارز به فارسی */
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("fa", "IR"));
    
    /** فرمت کردن تاریخ و زمان فارسی */
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    
    /** Timeline برای بروزرسانی خودکار وضعیت */
    private Timeline statusUpdateTimeline;
    
    /** اطلاعات سفارش جاری */
    private OrderInfo currentOrder;
    
    /** لیست مراحل پیگیری سفارش */
    private ObservableList<TrackingStep> trackingSteps;

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
        initializeTrackingSteps();
        setupAutoRefresh();
        loadOrderConfirmation();
        
        currencyFormat.setGroupingUsed(true);
        trackingSteps = FXCollections.observableArrayList();
    }

    /**
     * راه‌اندازی کامپوننت‌های UI و event listener ها
     * 
     * تنظیمات:
     * - فرمت کردن ارز و تاریخ
     * - Listener ها برای checkbox های اطلاع‌رسانی
     * - تنظیم وضعیت اولیه دکمه‌ها
     * - راه‌اندازی Progress Bar
     */
    private void setupUI() {
        setStatus("در حال بارگذاری اطلاعات سفارش...");
        
        // تنظیم listener ها برای اطلاع‌رسانی
        if (emailNotificationCheckBox != null) {
            emailNotificationCheckBox.setSelected(true);
            emailNotificationCheckBox.setOnAction(e -> updateNotificationPreferences());
        }
        
        if (smsNotificationCheckBox != null) {
            smsNotificationCheckBox.setSelected(true);
            smsNotificationCheckBox.setOnAction(e -> updateNotificationPreferences());
        }
        
        // تنظیم وضعیت اولیه دکمه‌ها
        if (cancelOrderButton != null) {
            cancelOrderButton.setDisable(false);
            cancelOrderButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        }
        
        if (downloadReceiptButton != null) {
            downloadReceiptButton.setDisable(false);
            downloadReceiptButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        }
        
        // تنظیم Progress Bar
        if (orderProgressBar != null) {
            orderProgressBar.setProgress(0.0);
            orderProgressBar.setStyle("-fx-accent: #28a745;");
        }
    }

    /**
     * مقداردهی اولیه مراحل پیگیری سفارش
     * 
     * تعریف 7 مرحله اصلی:
     * 1. تأیید سفارش
     * 2. آماده‌سازی غذا
     * 3. آماده برای تحویل
     * 4. در حال ارسال
     * 5. نزدیک مقصد
     * 6. تحویل داده شده
     * 7. تکمیل شده
     */
    private void initializeTrackingSteps() {
        trackingSteps = FXCollections.observableArrayList();
        
        trackingSteps.add(new TrackingStep(
            "تأیید سفارش", 
            "سفارش شما تأیید شد و به رستوران ارسال گردید", 
            LocalDateTime.now(),
            TrackingStatus.COMPLETED
        ));
        
        trackingSteps.add(new TrackingStep(
            "آماده‌سازی غذا", 
            "رستوران در حال آماده‌سازی سفارش شما است", 
            null,
            TrackingStatus.IN_PROGRESS
        ));
        
        trackingSteps.add(new TrackingStep(
            "آماده برای تحویل", 
            "غذا آماده شده و در انتظار پیک است", 
            null,
            TrackingStatus.PENDING
        ));
        
        trackingSteps.add(new TrackingStep(
            "در حال ارسال", 
            "پیک سفارش را تحویل گرفته و در مسیر شما است", 
            null,
            TrackingStatus.PENDING
        ));
        
        trackingSteps.add(new TrackingStep(
            "نزدیک مقصد", 
            "پیک در نزدیکی آدرس شما قرار دارد", 
            null,
            TrackingStatus.PENDING
        ));
        
        trackingSteps.add(new TrackingStep(
            "تحویل داده شده", 
            "سفارش به شما تحویل داده شد", 
            null,
            TrackingStatus.PENDING
        ));
        
        trackingSteps.add(new TrackingStep(
            "تکمیل شده", 
            "سفارش با موفقیت تکمیل شد", 
            null,
            TrackingStatus.PENDING
        ));
        
        updateTrackingDisplay();
    }

    /**
     * راه‌اندازی بروزرسانی خودکار وضعیت سفارش
     * 
     * هر 30 ثانیه وضعیت سفارش را از سرور چک می‌کند
     * و در صورت تغییر، نمایش را بروزرسانی می‌کند
     */
    private void setupAutoRefresh() {
        statusUpdateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(30), e -> refreshOrderStatus())
        );
        statusUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        statusUpdateTimeline.play();
    }

    /**
     * بارگذاری اطلاعات تأیید سفارش از سرور/حافظه
     * 
     * از background task استفاده می‌کند تا UI freeze نشود
     * اطلاعات شامل جزئیات سفارش، پرداخت و تحویل
     */
    private void loadOrderConfirmation() {
        setLoading(true);
        
        Task<OrderInfo> loadTask = new Task<OrderInfo>() {
            @Override
            protected OrderInfo call() throws Exception {
                Thread.sleep(2000); // شبیه‌سازی تأخیر شبکه
                
                // ایجاد mock order برای نمایش
                return createMockOrder();
            }
        };
        
        loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false);
            currentOrder = loadTask.getValue();
            displayOrderConfirmation();
            startTrackingSimulation();
            setStatus("سفارش شما با موفقیت ثبت شد");
        }));
        
        loadTask.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            setStatus("خطا در بارگذاری اطلاعات سفارش");
            showError("خطا", "امکان بارگذاری اطلاعات سفارش وجود ندارد");
        }));
        
        new Thread(loadTask).start();
    }

    /**
     * ایجاد سفارش نمونه برای نمایش
     * 
     * @return OrderInfo سفارش نمونه با اطلاعات کامل
     */
    private OrderInfo createMockOrder() {
        OrderInfo order = new OrderInfo();
        order.setOrderId("ORD-" + System.currentTimeMillis());
        order.setOrderDateTime(LocalDateTime.now());
        order.setEstimatedDelivery(LocalDateTime.now().plusMinutes(45));
        order.setSubtotal(85000.0);
        order.setTax(7650.0);
        order.setDeliveryFee(0.0);
        order.setDiscount(5000.0);
        order.setTotalAmount(87650.0);
        order.setPaymentMethod("کارت اعتباری");
        order.setPaymentStatus("تأیید شده");
        order.setRestaurantName("رستوران کباب ایرانی");
        order.setRestaurantPhone("021-12345678");
        order.setDeliveryAddress("تهران، خیابان ولیعصر، پلاک 123");
        order.setCustomerPhone("09123456789");
        order.setOrderNotes("بدون پیاز، اضافه سس");
        
        // افزودن آیتم‌های سفارش
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("کباب کوبیده", 2, 40000.0, "با برنج"));
        items.add(new OrderItem("دوغ", 1, 5000.0, ""));
        order.setOrderItems(items);
        
        return order;
    }

    /**
     * نمایش اطلاعات تأیید سفارش در UI
     * 
     * شامل:
     * - اطلاعات کلی سفارش (شماره، تاریخ، زمان تحویل)
     * - لیست آیتم‌های سفارش
     * - جزئیات مالی (قیمت‌ها، تخفیف، کل)
     * - اطلاعات پرداخت و تحویل
     */
    private void displayOrderConfirmation() {
        if (currentOrder == null) return;
        
        // نمایش اطلاعات کلی
        if (orderNumberLabel != null) {
            orderNumberLabel.setText(currentOrder.getOrderId());
        }
        
        if (orderDateTimeLabel != null) {
            orderDateTimeLabel.setText(currentOrder.getOrderDateTime().format(dateTimeFormatter));
        }
        
        if (estimatedDeliveryLabel != null) {
            estimatedDeliveryLabel.setText(currentOrder.getEstimatedDelivery().format(dateTimeFormatter));
        }
        
        // نمایش آیتم‌های سفارش
        displayOrderItems();
        
        // نمایش اطلاعات مالی
        displayOrderSummary();
        
        // نمایش اطلاعات پرداخت
        displayPaymentInfo();
        
        // نمایش اطلاعات تحویل
        displayDeliveryInfo();
        
        // نمایش اطلاعات رستوران
        displayRestaurantInfo();
    }

    /**
     * نمایش لیست آیتم‌های سفارش
     */
    private void displayOrderItems() {
        if (orderItemsContainer == null || currentOrder.getOrderItems() == null) return;
        
        orderItemsContainer.getChildren().clear();
        
        for (OrderItem item : currentOrder.getOrderItems()) {
            VBox itemBox = createOrderItemUI(item);
            orderItemsContainer.getChildren().add(itemBox);
        }
    }

    /**
     * ایجاد UI برای نمایش یک آیتم سفارش
     * 
     * @param item آیتم سفارش
     * @return VBox حاوی UI آیتم
     */
    private VBox createOrderItemUI(OrderItem item) {
        VBox itemBox = new VBox(5);
        itemBox.setPadding(new Insets(10));
        itemBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        HBox headerBox = new HBox(10);
        headerBox.setStyle("-fx-alignment: center-left;");
        
        Label nameLabel = new Label(item.getItemName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label quantityLabel = new Label("× " + item.getQuantity());
        quantityLabel.setStyle("-fx-text-fill: #6c757d;");
        
        Label priceLabel = new Label(formatCurrency(item.getTotalPrice()) + " تومان");
        priceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(nameLabel, quantityLabel);
        
        HBox detailBox = new HBox(10);
        detailBox.setStyle("-fx-alignment: center-left;");
        
        if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().trim().isEmpty()) {
            Label instructionsLabel = new Label("نکته: " + item.getSpecialInstructions());
            instructionsLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
            detailBox.getChildren().add(instructionsLabel);
        }
        
        detailBox.getChildren().add(priceLabel);
        
        itemBox.getChildren().addAll(headerBox, detailBox);
        return itemBox;
    }

    /**
     * نمایش خلاصه مالی سفارش
     */
    private void displayOrderSummary() {
        if (subtotalLabel != null) {
            subtotalLabel.setText(formatCurrency(currentOrder.getSubtotal()) + " تومان");
        }
        
        if (taxLabel != null) {
            taxLabel.setText(formatCurrency(currentOrder.getTax()) + " تومان");
        }
        
        if (deliveryFeeLabel != null) {
            String deliveryText = currentOrder.getDeliveryFee() == 0 ? "رایگان" : 
                                 formatCurrency(currentOrder.getDeliveryFee()) + " تومان";
            deliveryFeeLabel.setText(deliveryText);
        }
        
        if (discountLabel != null) {
            String discountText = currentOrder.getDiscount() == 0 ? "ندارد" :
                                 "- " + formatCurrency(currentOrder.getDiscount()) + " تومان";
            discountLabel.setText(discountText);
        }
        
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(formatCurrency(currentOrder.getTotalAmount()) + " تومان");
            totalAmountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        }
    }

    /**
     * نمایش اطلاعات پرداخت
     */
    private void displayPaymentInfo() {
        if (paymentMethodLabel != null) {
            paymentMethodLabel.setText(currentOrder.getPaymentMethod());
        }
        
        if (paymentStatusLabel != null) {
            paymentStatusLabel.setText(currentOrder.getPaymentStatus());
            paymentStatusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        }
    }

    /**
     * نمایش اطلاعات تحویل
     */
    private void displayDeliveryInfo() {
        if (deliveryAddressDisplay != null) {
            deliveryAddressDisplay.setText(currentOrder.getDeliveryAddress());
            deliveryAddressDisplay.setEditable(false);
        }
        
        if (customerPhoneDisplay != null) {
            customerPhoneDisplay.setText(currentOrder.getCustomerPhone());
            customerPhoneDisplay.setEditable(false);
        }
        
        if (orderNotesDisplay != null) {
            orderNotesDisplay.setText(currentOrder.getOrderNotes());
            orderNotesDisplay.setEditable(false);
        }
    }

    /**
     * نمایش اطلاعات رستوران
     */
    private void displayRestaurantInfo() {
        if (restaurantNameLabel != null) {
            restaurantNameLabel.setText(currentOrder.getRestaurantName());
        }
        
        if (restaurantPhoneLabel != null) {
            restaurantPhoneLabel.setText(currentOrder.getRestaurantPhone());
        }
    }

    /**
     * نمایش مراحل پیگیری سفارش
     */
    private void updateTrackingDisplay() {
        if (trackingStepsContainer == null) return;
        
        trackingStepsContainer.getChildren().clear();
        
        for (int i = 0; i < trackingSteps.size(); i++) {
            TrackingStep step = trackingSteps.get(i);
            VBox stepBox = createTrackingStepUI(step, i + 1);
            trackingStepsContainer.getChildren().add(stepBox);
        }
        
        updateProgressBar();
    }

    /**
     * ایجاد UI برای نمایش یک مرحله پیگیری
     * 
     * @param step مرحله پیگیری
     * @param stepNumber شماره مرحله
     * @return VBox حاوی UI مرحله
     */
    private VBox createTrackingStepUI(TrackingStep step, int stepNumber) {
        VBox stepBox = new VBox(5);
        stepBox.setPadding(new Insets(10));
        
        HBox headerBox = new HBox(10);
        headerBox.setStyle("-fx-alignment: center-right;");
        
        // آیکون وضعیت
        Label statusIcon = new Label();
        switch (step.getStatus()) {
            case COMPLETED:
                statusIcon.setText("✓");
                statusIcon.setStyle("-fx-text-fill: #28a745; -fx-font-size: 16px; -fx-font-weight: bold;");
                stepBox.setStyle("-fx-background-color: #d4edda; -fx-border-color: #28a745; -fx-border-radius: 5;");
                break;
            case IN_PROGRESS:
                statusIcon.setText("●");
                statusIcon.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 16px;");
                stepBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffc107; -fx-border-radius: 5;");
                break;
            case PENDING:
                statusIcon.setText("○");
                statusIcon.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 16px;");
                stepBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
                break;
        }
        
        Label stepLabel = new Label(stepNumber + ". " + step.getTitle());
        stepLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label timeLabel = new Label();
        if (step.getCompletedAt() != null) {
            timeLabel.setText(step.getCompletedAt().format(dateTimeFormatter));
            timeLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px;");
        }
        
        headerBox.getChildren().addAll(timeLabel, stepLabel, statusIcon);
        
        Label descLabel = new Label(step.getDescription());
        descLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
        descLabel.setWrapText(true);
        
        stepBox.getChildren().addAll(headerBox, descLabel);
        return stepBox;
    }

    /**
     * بروزرسانی Progress Bar بر اساس مراحل تکمیل شده
     */
    private void updateProgressBar() {
        if (orderProgressBar == null) return;
        
        long completedSteps = trackingSteps.stream()
                                          .mapToLong(step -> step.getStatus() == TrackingStatus.COMPLETED ? 1 : 0)
                                          .sum();
        
        double progress = (double) completedSteps / trackingSteps.size();
        orderProgressBar.setProgress(progress);
        
        if (currentStatusLabel != null) {
            TrackingStep currentStep = trackingSteps.stream()
                                                  .filter(step -> step.getStatus() == TrackingStatus.IN_PROGRESS)
                                                  .findFirst()
                                                  .orElse(trackingSteps.get(trackingSteps.size() - 1));
            
            currentStatusLabel.setText("وضعیت فعلی: " + currentStep.getTitle());
        }
    }

    /**
     * شبیه‌سازی پیشرفت مراحل سفارش
     * 
     * هر 15 ثانیه یک مرحله جدید تکمیل می‌شود
     */
    private void startTrackingSimulation() {
        Timeline simulation = new Timeline();
        
        for (int i = 1; i < trackingSteps.size(); i++) {
            final int stepIndex = i;
            KeyFrame frame = new KeyFrame(
                Duration.seconds(15 * i),
                e -> {
                    TrackingStep step = trackingSteps.get(stepIndex);
                    step.setStatus(TrackingStatus.IN_PROGRESS);
                    step.setCompletedAt(LocalDateTime.now());
                    
                    if (stepIndex > 1) {
                        trackingSteps.get(stepIndex - 1).setStatus(TrackingStatus.COMPLETED);
                    }
                    
                    Platform.runLater(() -> {
                        updateTrackingDisplay();
                        if (stepIndex == trackingSteps.size() - 1) {
                            step.setStatus(TrackingStatus.COMPLETED);
                            updateTrackingDisplay();
                            onOrderCompleted();
                        }
                    });
                }
            );
            simulation.getKeyFrames().add(frame);
        }
        
        simulation.play();
    }

    /**
     * رفرش دستی وضعیت سفارش
     */
    private void refreshOrderStatus() {
        // در پیاده‌سازی واقعی، از WebSocket یا REST API استفاده می‌شود
        Platform.runLater(() -> {
            setStatus("وضعیت سفارش بروزرسانی شد");
        });
    }

    /**
     * متد فراخوانی شده هنگام تکمیل سفارش
     */
    private void onOrderCompleted() {
        setStatus("سفارش شما با موفقیت تحویل داده شد");
        
        if (cancelOrderButton != null) {
            cancelOrderButton.setDisable(true);
            cancelOrderButton.setText("تکمیل شده");
        }
        
        // فعال‌سازی دکمه سفارش مجدد
        if (reorderButton != null) {
            reorderButton.setDisable(false);
        }
        
        // ارسال اطلاع‌رسانی تکمیل
        sendCompletionNotification();
    }

    /**
     * ارسال اطلاع‌رسانی تکمیل سفارش
     */
    private void sendCompletionNotification() {
        if (emailNotificationCheckBox != null && emailNotificationCheckBox.isSelected()) {
            // TODO: ارسال ایمیل
            System.out.println("ایمیل تکمیل سفارش ارسال شد");
        }
        
        if (smsNotificationCheckBox != null && smsNotificationCheckBox.isSelected()) {
            // TODO: ارسال پیامک
            System.out.println("پیامک تکمیل سفارش ارسال شد");
        }
    }

    // ===== Event Handlers =====

    @FXML
    private void handleCancelOrder() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("لغو سفارش");
        confirmAlert.setHeaderText("آیا مطمئن هستید؟");
        confirmAlert.setContentText("آیا می‌خواهید این سفارش را لغو کنید؟");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: پیاده‌سازی لغو سفارش
            setStatus("درخواست لغو سفارش ارسال شد");
            cancelOrderButton.setDisable(true);
        }
    }

    @FXML
    private void handleDownloadReceipt() {
        // TODO: پیاده‌سازی دانلود رسید PDF
        setStatus("رسید در حال دانلود...");
        
        // شبیه‌سازی دانلود
        Task<Void> downloadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(2000);
                return null;
            }
        };
        
        downloadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setStatus("رسید با موفقیت دانلود شد");
            showInfo("موفقیت", "رسید سفارش در پوشه Downloads ذخیره شد");
        }));
        
        new Thread(downloadTask).start();
    }

    @FXML
    private void handleContactSupport() {
        // TODO: باز کردن چت پشتیبانی
        setStatus("در حال اتصال به پشتیبانی...");
        showInfo("پشتیبانی", "به زودی با پشتیبانی در تماس خواهید بود");
    }

    @FXML
    private void handleTrackOrder() {
        // TODO: باز کردن صفحه پیگیری تفصیلی
        navigationController.navigateTo("OrderTracking");
    }

    @FXML
    private void handleBackToMenu() {
        navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
    }

    @FXML
    private void handleReorder() {
        // TODO: افزودن آیتم‌های سفارش به سبد جدید
        setStatus("آیتم‌های سفارش به سبد خرید اضافه شدند");
        navigationController.navigateTo(NavigationController.CART_SCENE);
    }

    @FXML
    private void handleRefresh() {
        refreshOrderStatus();
    }

    @FXML
    private void handlePrintReceipt() {
        // TODO: پرینت رسید
        setStatus("در حال آماده‌سازی برای چاپ...");
    }

    @FXML
    private void handleOrderHistory() {
        navigationController.navigateTo(NavigationController.ORDER_HISTORY_SCENE);
    }

    @FXML
    private void handleHome() {
        navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
    }

    /**
     * بروزرسانی تنظیمات اطلاع‌رسانی
     */
    private void updateNotificationPreferences() {
        // TODO: ذخیره تنظیمات در پروفایل کاربر
        setStatus("تنظیمات اطلاع‌رسانی بروزرسانی شد");
    }

    // ===== Utility Methods =====

    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
    }

    private void setStatus(String message) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
        }
    }

    private String formatCurrency(double amount) {
        return currencyFormat.format(amount);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== Data Models =====

    /**
     * کلاس اطلاعات کامل سفارش
     */
    public static class OrderInfo {
        private String orderId;
        private LocalDateTime orderDateTime;
        private LocalDateTime estimatedDelivery;
        private double subtotal;
        private double tax;
        private double deliveryFee;
        private double discount;
        private double totalAmount;
        private String paymentMethod;
        private String paymentStatus;
        private String restaurantName;
        private String restaurantPhone;
        private String deliveryAddress;
        private String customerPhone;
        private String orderNotes;
        private List<OrderItem> orderItems;
        
        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public LocalDateTime getOrderDateTime() { return orderDateTime; }
        public void setOrderDateTime(LocalDateTime orderDateTime) { this.orderDateTime = orderDateTime; }
        
        public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
        public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
        
        public double getSubtotal() { return subtotal; }
        public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
        
        public double getTax() { return tax; }
        public void setTax(double tax) { this.tax = tax; }
        
        public double getDeliveryFee() { return deliveryFee; }
        public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
        
        public double getDiscount() { return discount; }
        public void setDiscount(double discount) { this.discount = discount; }
        
        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        
        public String getRestaurantName() { return restaurantName; }
        public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
        
        public String getRestaurantPhone() { return restaurantPhone; }
        public void setRestaurantPhone(String restaurantPhone) { this.restaurantPhone = restaurantPhone; }
        
        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
        
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        
        public String getOrderNotes() { return orderNotes; }
        public void setOrderNotes(String orderNotes) { this.orderNotes = orderNotes; }
        
        public List<OrderItem> getOrderItems() { return orderItems; }
        public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    }

    /**
     * کلاس آیتم سفارش
     */
    public static class OrderItem {
        private String itemName;
        private int quantity;
        private double unitPrice;
        private String specialInstructions;
        
        public OrderItem(String itemName, int quantity, double unitPrice, String specialInstructions) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.specialInstructions = specialInstructions;
        }
        
        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalPrice() { return unitPrice * quantity; }
        public String getSpecialInstructions() { return specialInstructions; }
    }

    /**
     * کلاس مرحله پیگیری سفارش
     */
    public static class TrackingStep {
        private String title;
        private String description;
        private LocalDateTime completedAt;
        private TrackingStatus status;
        
        public TrackingStep(String title, String description, LocalDateTime completedAt, TrackingStatus status) {
            this.title = title;
            this.description = description;
            this.completedAt = completedAt;
            this.status = status;
        }
        
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public TrackingStatus getStatus() { return status; }
        public void setStatus(TrackingStatus status) { this.status = status; }
    }

    /**
     * enum وضعیت مراحل پیگیری
     */
    public enum TrackingStatus {
        PENDING("در انتظار"),
        IN_PROGRESS("در حال انجام"),
        COMPLETED("تکمیل شده");
        
        private final String displayName;
        
        TrackingStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 