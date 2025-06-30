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

import java.io.File;
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
     * 
     * این متد اطلاع‌رسانی‌های مختلف را بر اساس تنظیمات کاربر ارسال می‌کند:
     * - ایمیل: شامل رسید کامل و جزئیات تحویل
     * - پیامک: پیام کوتاه تأیید تحویل
     * - در نسخه آینده: Push notification و Telegram bot
     */
    private void sendCompletionNotification() {
        // بررسی و ارسال اطلاع‌رسانی ایمیل
        if (emailNotificationCheckBox != null && emailNotificationCheckBox.isSelected()) {
            sendEmailNotification();
        }
        
        // بررسی و ارسال اطلاع‌رسانی پیامکی
        if (smsNotificationCheckBox != null && smsNotificationCheckBox.isSelected()) {
            sendSMSNotification();
        }
    }
    
    /**
     * ارسال اطلاع‌رسانی ایمیل تکمیل سفارش
     * 
     * در پیاده‌سازی نهایی، این متد:
     * 1. قالب ایمیل را از template engine بارگذاری می‌کند
     * 2. اطلاعات سفارش را در قالب جایگذاری می‌کند
     * 3. رسید PDF را به عنوان پیوست اضافه می‌کند
     * 4. از SMTP server برای ارسال استفاده می‌کند
     */
    private void sendEmailNotification() {
        try {
            String customerEmail = getCurrentCustomerEmail();
            String emailSubject = "تکمیل سفارش شما - شماره " + currentOrder.getOrderId();
            String emailBody = generateEmailBody();
            
            // شبیه‌سازی ارسال ایمیل
            System.out.println("📧 ایمیل تکمیل سفارش ارسال شد");
            System.out.println("📧 آدرس گیرنده: " + customerEmail);
            System.out.println("📧 موضوع: " + emailSubject);
            
            // در پیاده‌سازی واقعی:
            // EmailService.sendOrderCompletionEmail(customerEmail, currentOrder);
            
        } catch (Exception e) {
            System.err.println("❌ خطا در ارسال ایمیل: " + e.getMessage());
        }
    }
    
    /**
     * ارسال اطلاع‌رسانی پیامکی تکمیل سفارش
     * 
     * پیامک شامل اطلاعات خلاصه:
     * - شماره سفارش
     * - زمان تحویل
     * - مبلغ نهایی
     * - لینک نظرسنجی
     */
    private void sendSMSNotification() {
        try {
            String customerPhone = currentOrder.getCustomerPhone();
            String smsMessage = generateSMSMessage();
            
            // شبیه‌سازی ارسال پیامک
            System.out.println("📱 پیامک تکمیل سفارش ارسال شد");
            System.out.println("📱 شماره گیرنده: " + customerPhone);
            System.out.println("📱 متن پیام: " + smsMessage);
            
            // در پیاده‌سازی واقعی:
            // SMSService.sendOrderCompletionSMS(customerPhone, smsMessage);
            
        } catch (Exception e) {
            System.err.println("❌ خطا در ارسال پیامک: " + e.getMessage());
        }
    }
    
    /**
     * دریافت ایمیل مشتری فعلی
     * 
     * @return ایمیل مشتری از session یا پروفایل کاربر
     */
    private String getCurrentCustomerEmail() {
        // در پیاده‌سازی واقعی از UserSession یا AuthService استفاده می‌شود
        return "customer@example.com";
    }
    
    /**
     * تولید متن ایمیل تکمیل سفارش
     * 
     * @return HTML content برای ایمیل
     */
    private String generateEmailBody() {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<!DOCTYPE html><html><body>");
        emailBody.append("<h2>سفارش شما با موفقیت تحویل داده شد!</h2>");
        emailBody.append("<p>شماره سفارش: ").append(currentOrder.getOrderId()).append("</p>");
        emailBody.append("<p>رستوران: ").append(currentOrder.getRestaurantName()).append("</p>");
        emailBody.append("<p>مبلغ کل: ").append(formatCurrency(currentOrder.getTotalAmount())).append(" تومان</p>");
        emailBody.append("<p>از انتخاب ما متشکریم!</p>");
        emailBody.append("</body></html>");
        return emailBody.toString();
    }
    
    /**
     * تولید متن پیامک تکمیل سفارش
     * 
     * @return متن کوتاه و مختصر پیامک
     */
    private String generateSMSMessage() {
        return String.format(
            "سفارش %s با موفقیت تحویل شد. مبلغ: %s تومان. از انتخاب شما متشکریم!",
            currentOrder.getOrderId(),
            formatCurrency(currentOrder.getTotalAmount())
        );
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
            processCancelOrder();
        }
    }
    
    /**
     * پردازش درخواست لغو سفارش
     * 
     * این متد مراحل زیر را انجام می‌دهد:
     * 1. بررسی امکان لغو سفارش (بر اساس وضعیت فعلی)
     * 2. ارسال درخواست لغو به backend
     * 3. بروزرسانی UI و غیرفعال کردن دکمه لغو
     * 4. ارسال اطلاع‌رسانی لغو به مشتری
     * 5. محاسبه و پردازش بازپرداخت (در صورت لزوم)
     */
    private void processCancelOrder() {
        setStatus("در حال پردازش درخواست لغو سفارش...");
        
        // بررسی امکان لغو بر اساس وضعیت سفارش
        if (!isOrderCancelable()) {
            showError("خطا", "امکان لغو سفارش در مرحله فعلی وجود ندارد");
            return;
        }
        
        // شبیه‌سازی ارسال درخواست لغو به backend
        Task<Boolean> cancelTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Thread.sleep(2000); // شبیه‌سازی تأخیر شبکه
                
                // در پیاده‌سازی واقعی:
                // return OrderService.cancelOrder(currentOrder.getOrderId());
                
                // شبیه‌سازی موفقیت 90% و شکست 10%
                return Math.random() > 0.1;
            }
        };
        
        cancelTask.setOnSucceeded(e -> Platform.runLater(() -> {
            boolean success = cancelTask.getValue();
            if (success) {
                handleCancelOrderSuccess();
            } else {
                handleCancelOrderFailure("خطای سرور در پردازش درخواست لغو");
            }
        }));
        
        cancelTask.setOnFailed(e -> Platform.runLater(() -> {
            Throwable exception = cancelTask.getException();
            String errorMessage = exception != null ? exception.getMessage() : "خطای نامشخص";
            handleCancelOrderFailure(errorMessage);
        }));
        
        new Thread(cancelTask).start();
    }
    
    /**
     * بررسی امکان لغو سفارش بر اساس وضعیت فعلی
     * 
     * سفارش تنها در مراحل زیر قابل لغو است:
     * - تأیید سفارش
     * - آماده‌سازی غذا (تا 5 دقیقه اول)
     * 
     * @return true اگر سفارش قابل لغو باشد
     */
    private boolean isOrderCancelable() {
        if (trackingSteps == null || trackingSteps.isEmpty()) {
            return false;
        }
        
        // محاسبه تعداد مراحل تکمیل شده
        long completedSteps = trackingSteps.stream()
            .mapToLong(step -> step.getStatus() == TrackingStatus.COMPLETED ? 1 : 0)
            .sum();
        
        // اگر بیش از 2 مرحله تکمیل شده، امکان لغو وجود ندارد
        if (completedSteps > 2) {
            return false;
        }
        
        // بررسی زمان سپری شده از ثبت سفارش
        LocalDateTime orderTime = currentOrder.getOrderDateTime();
        long minutesPassed = java.time.Duration.between(orderTime, LocalDateTime.now()).toMinutes();
        
        // حداکثر 10 دقیقه امکان لغو وجود دارد
        return minutesPassed <= 10;
    }
    
    /**
     * مدیریت موفقیت در لغو سفارش
     */
    private void handleCancelOrderSuccess() {
        setStatus("سفارش با موفقیت لغو شد");
        
        // غیرفعال کردن دکمه لغو و تغییر متن
            cancelOrderButton.setDisable(true);
        cancelOrderButton.setText("لغو شده");
        cancelOrderButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-opacity: 0.6;");
        
        // توقف timeline پیگیری
        if (statusUpdateTimeline != null) {
            statusUpdateTimeline.stop();
        }
        
        // ارسال اطلاع‌رسانی لغو
        sendCancellationNotification();
        
        // نمایش پیام موفقیت
        showInfo("لغو موفق", 
            "سفارش شما با موفقیت لغو شد.\n" +
            "در صورت پرداخت آنلاین، مبلغ طی 2-3 روز کاری بازگردانده خواهد شد.\n" +
            "شماره پیگیری لغو: CANCEL-" + System.currentTimeMillis());
    }
    
    /**
     * مدیریت شکست در لغو سفارش
     * 
     * @param errorMessage پیام خطا
     */
    private void handleCancelOrderFailure(String errorMessage) {
        setStatus("خطا در لغو سفارش");
        showError("خطا در لغو سفارش", 
            "متأسفانه امکان لغو سفارش وجود ندارد.\n" +
            "دلیل: " + errorMessage + "\n" +
            "برای کمک بیشتر با پشتیبانی تماس بگیرید.");
    }
    
    /**
     * ارسال اطلاع‌رسانی لغو سفارش
     */
    private void sendCancellationNotification() {
        if (emailNotificationCheckBox != null && emailNotificationCheckBox.isSelected()) {
            System.out.println("📧 ایمیل لغو سفارش ارسال شد");
        }
        
        if (smsNotificationCheckBox != null && smsNotificationCheckBox.isSelected()) {
            System.out.println("📱 پیامک لغو سفارش ارسال شد");
        }
    }

    @FXML
    private void handleDownloadReceipt() {
        if (currentOrder == null) {
            showError("خطا", "اطلاعات سفارش موجود نیست");
            return;
        }
        
        setStatus("رسید در حال دانلود...");
        
        Task<Void> downloadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // تعیین مسیر ذخیره فایل
                    String userHome = System.getProperty("user.home");
                    String downloadsPath = userHome + File.separator + "Downloads";
                    File downloadsDir = new File(downloadsPath);
                    
                    if (!downloadsDir.exists()) {
                        downloadsDir = new File(userHome);
                    }
                    
                    String fileName = "receipt_" + currentOrder.getOrderId() + ".txt";
                    File receiptFile = new File(downloadsDir, fileName);
                    
                    // استفاده از TextReceiptExporter برای تولید رسید
                    com.myapp.ui.order.internal.TextReceiptExporter exporter = 
                        new com.myapp.ui.order.internal.TextReceiptExporter();
                    exporter.export(currentOrder, receiptFile);
                    
                    Thread.sleep(1000); // شبیه‌سازی تأخیر
                    
                } catch (Exception e) {
                    throw new RuntimeException("خطا در تولید رسید: " + e.getMessage(), e);
                }
                return null;
            }
        };
        
        downloadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setStatus("رسید با موفقیت دانلود شد");
            showInfo("موفقیت", "رسید سفارش در پوشه Downloads ذخیره شد");
        }));
        
        downloadTask.setOnFailed(e -> Platform.runLater(() -> {
            setStatus("خطا در دانلود رسید");
            Throwable exception = downloadTask.getException();
            String errorMessage = exception != null ? exception.getMessage() : "خطای نامشخص";
            showError("خطا", errorMessage);
        }));
        
        new Thread(downloadTask).start();
    }

    @FXML
    private void handleContactSupport() {
        setStatus("در حال اتصال به پشتیبانی...");
        
        // شبیه‌سازی اتصال به پشتیبانی
        Task<Void> supportTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1500); // شبیه‌سازی تأخیر اتصال
                return null;
            }
        };
        
        supportTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setStatus("متصل به پشتیبانی");
            showInfo("پشتیبانی", 
                "شماره پشتیبانی: 021-91000000\n" +
                "ساعت کاری: 24 ساعته\n" +
                "شماره سفارش شما: " + (currentOrder != null ? currentOrder.getOrderId() : "نامشخص"));
        }));
        
        supportTask.setOnFailed(e -> Platform.runLater(() -> {
            setStatus("خطا در اتصال به پشتیبانی");
            showError("خطا", "امکان اتصال به پشتیبانی وجود ندارد. لطفاً مجدداً تلاش کنید.");
        }));
        
        new Thread(supportTask).start();
    }

    @FXML
    private void handleTrackOrder() {
        openDetailedTrackingView();
    }
    
    /**
     * باز کردن صفحه پیگیری تفصیلی سفارش
     * 
     * این متد صفحه‌ای جداگانه برای پیگیری دقیق‌تر سفارش باز می‌کند که شامل:
     * - نقشه مسیر پیک (در صورت وجود GPS tracking)
     * - اطلاعات تماس پیک و رستوران
     * - تاریخچه کامل تغییرات وضعیت
     * - تخمین زمان دقیق‌تر تحویل
     * - امکان چت مستقیم با پیک
     */
    private void openDetailedTrackingView() {
        try {
            // بررسی وجود سفارش جاری
            if (currentOrder == null) {
                showError("خطا", "اطلاعات سفارش برای پیگیری تفصیلی موجود نیست");
                return;
            }
            
            setStatus("در حال بارگذاری صفحه پیگیری تفصیلی...");
            
            // شبیه‌سازی بارگذاری صفحه پیگیری
            Task<Void> loadTrackingTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(1500); // شبیه‌سازی بارگذاری
                    return null;
                }
            };
            
            loadTrackingTask.setOnSucceeded(e -> Platform.runLater(() -> {
                setStatus("صفحه پیگیری تفصیلی بارگذاری شد");
                
                // در پیاده‌سازی واقعی، صفحه جدید باز می‌شود:
                // navigationController.navigateToOrderTracking(currentOrder.getOrderId());
                
                // فعلاً پیام اطلاعاتی نمایش می‌دهیم
                showInfo("پیگیری تفصیلی", 
                    "صفحه پیگیری تفصیلی (نسخه آینده):\n\n" +
                    "🗺️ نقشه مسیر پیک\n" +
                    "📞 تماس مستقیم با پیک\n" +
                    "💬 چت آنلاین\n" +
                    "⏰ بروزرسانی لحظه‌ای\n" +
                    "📍 موقعیت دقیق سفارش\n\n" +
                    "شماره پیگیری: " + currentOrder.getOrderId());
                
                // در حال حاضر به صفحه پیگیری ساده هدایت می‌کنیم
        navigationController.navigateTo("OrderTracking");
            }));
            
            loadTrackingTask.setOnFailed(e -> Platform.runLater(() -> {
                setStatus("خطا در بارگذاری صفحه پیگیری");
                showError("خطا", "امکان باز کردن صفحه پیگیری تفصیلی وجود ندارد");
            }));
            
            new Thread(loadTrackingTask).start();
            
        } catch (Exception e) {
            setStatus("خطا در پیگیری سفارش");
            showError("خطای سیستم", "خطا در باز کردن صفحه پیگیری: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToMenu() {
        navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
    }

    @FXML
    private void handleReorder() {
        if (currentOrder == null || currentOrder.getOrderItems() == null) {
            showError("خطا", "اطلاعات سفارش برای سفارش مجدد موجود نیست");
            return;
        }
        
        setStatus("در حال افزودن آیتم‌ها به سبد خرید...");
        
        // شبیه‌سازی افزودن آیتم‌ها به سبد
        Task<Void> reorderTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1000); // شبیه‌سازی عملیات افزودن
                return null;
            }
        };
        
        reorderTask.setOnSucceeded(e -> Platform.runLater(() -> {
        setStatus("آیتم‌های سفارش به سبد خرید اضافه شدند");
            showInfo("موفقیت", 
                currentOrder.getOrderItems().size() + " آیتم به سبد خرید شما اضافه شد.\n" +
                "اکنون می‌توانید سبد خرید را بررسی کنید.");
        navigationController.navigateTo(NavigationController.CART_SCENE);
        }));
        
        reorderTask.setOnFailed(e -> Platform.runLater(() -> {
            setStatus("خطا در افزودن آیتم‌ها");
            showError("خطا", "امکان افزودن آیتم‌ها به سبد خرید وجود ندارد");
        }));
        
        new Thread(reorderTask).start();
    }

    @FXML
    private void handleRefresh() {
        refreshOrderStatus();
    }

    @FXML
    private void handlePrintReceipt() {
        if (currentOrder == null) {
            showError("خطا", "اطلاعات سفارش برای چاپ موجود نیست");
            return;
        }
        
        setStatus("در حال آماده‌سازی برای چاپ...");
        
        // شبیه‌سازی آماده‌سازی چاپ
        Task<Void> printTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(2000); // شبیه‌سازی تأخیر آماده‌سازی
                return null;
            }
        };
        
        printTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setStatus("رسید آماده چاپ است");
            showInfo("چاپ رسید", 
                "رسید سفارش آماده چاپ شد.\n" +
                "شماره سفارش: " + currentOrder.getOrderId() + "\n" +
                "لطفاً چاپگر خود را بررسی کنید.");
        }));
        
        printTask.setOnFailed(e -> Platform.runLater(() -> {
            setStatus("خطا در آماده‌سازی چاپ");
            showError("خطا", "امکان چاپ رسید وجود ندارد. لطفاً از دانلود رسید استفاده کنید.");
        }));
        
        new Thread(printTask).start();
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
     * بروزرسانی تنظیمات اطلاع‌رسانی کاربر
     * 
     * این متد تنظیمات اطلاع‌رسانی را در پروفایل کاربر ذخیره می‌کند:
     * - ترجیح دریافت ایمیل
     * - ترجیح دریافت پیامک
     * - زمان‌بندی اطلاع‌رسانی‌ها
     * - نوع اطلاع‌رسانی‌های دریافتی
     */
    private void updateNotificationPreferences() {
        try {
            // دریافت تنظیمات جاری از UI
            boolean emailEnabled = emailNotificationCheckBox != null && emailNotificationCheckBox.isSelected();
            boolean smsEnabled = smsNotificationCheckBox != null && smsNotificationCheckBox.isSelected();
            
            // ایجاد شیء تنظیمات
            NotificationPreferences preferences = new NotificationPreferences();
            preferences.setEmailNotifications(emailEnabled);
            preferences.setSmsNotifications(smsEnabled);
            preferences.setOrderUpdates(true); // همیشه فعال
            preferences.setPromotionalMessages(false); // پیش‌فرض غیرفعال
            preferences.setUpdatedAt(LocalDateTime.now());
            
            // شبیه‌سازی ذخیره در backend
            Task<Boolean> saveTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    Thread.sleep(1000); // شبیه‌سازی درخواست شبکه
                    
                    // در پیاده‌سازی واقعی:
                    // return UserService.updateNotificationPreferences(getCurrentUserId(), preferences);
                    
                    return true; // شبیه‌سازی موفقیت
                }
            };
            
            saveTask.setOnSucceeded(e -> Platform.runLater(() -> {
                boolean success = saveTask.getValue();
                if (success) {
        setStatus("تنظیمات اطلاع‌رسانی بروزرسانی شد");
                    
                    // نمایش پیام تأیید کوتاه
                    showTemporaryMessage("✅ تنظیمات ذخیره شد", 2000);
                    
                    // ذخیره در حافظه محلی برای استفاده سریع‌تر
                    saveNotificationPreferencesLocally(preferences);
                    
                } else {
                    setStatus("خطا در ذخیره تنظیمات");
                    showError("خطا", "امکان ذخیره تنظیمات وجود ندارد");
                }
            }));
            
            saveTask.setOnFailed(e -> Platform.runLater(() -> {
                setStatus("خطا در اتصال به سرور");
                showError("خطای شبکه", "امکان بروزرسانی تنظیمات وجود ندارد");
            }));
            
            new Thread(saveTask).start();
            
        } catch (Exception e) {
            setStatus("خطا در بروزرسانی تنظیمات");
            showError("خطای سیستم", "خطا در ذخیره تنظیمات: " + e.getMessage());
        }
    }
    
    /**
     * ذخیره تنظیمات اطلاع‌رسانی در حافظه محلی
     * 
     * @param preferences تنظیمات اطلاع‌رسانی
     */
    private void saveNotificationPreferencesLocally(NotificationPreferences preferences) {
        try {
            // در پیاده‌سازی واقعی از SharedPreferences یا Properties file استفاده می‌شود
            System.out.println("💾 تنظیمات در حافظه محلی ذخیره شد:");
            System.out.println("📧 ایمیل: " + preferences.isEmailNotifications());
            System.out.println("📱 پیامک: " + preferences.isSmsNotifications());
            System.out.println("🔔 بروزرسانی سفارش: " + preferences.isOrderUpdates());
            
        } catch (Exception e) {
            System.err.println("❌ خطا در ذخیره محلی: " + e.getMessage());
        }
    }
    
    /**
     * نمایش پیام موقت در UI
     * 
     * @param message متن پیام
     * @param durationMs مدت نمایش به میلی‌ثانیه
     */
    private void showTemporaryMessage(String message, int durationMs) {
        if (statusMessageLabel != null) {
            String originalMessage = statusMessageLabel.getText();
            statusMessageLabel.setText(message);
            statusMessageLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            
            // برگرداندن پیام اصلی پس از مدت زمان مشخص
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(durationMs), e -> {
                statusMessageLabel.setText(originalMessage);
                statusMessageLabel.setStyle(""); // حذف style خاص
            }));
            timeline.play();
        }
    }
    
    /**
     * کلاس تنظیمات اطلاع‌رسانی
     */
    public static class NotificationPreferences {
        private boolean emailNotifications;
        private boolean smsNotifications;
        private boolean orderUpdates;
        private boolean promotionalMessages;
        private LocalDateTime updatedAt;
        
        // Getters and Setters
        public boolean isEmailNotifications() { return emailNotifications; }
        public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }
        
        public boolean isSmsNotifications() { return smsNotifications; }
        public void setSmsNotifications(boolean smsNotifications) { this.smsNotifications = smsNotifications; }
        
        public boolean isOrderUpdates() { return orderUpdates; }
        public void setOrderUpdates(boolean orderUpdates) { this.orderUpdates = orderUpdates; }
        
        public boolean isPromotionalMessages() { return promotionalMessages; }
        public void setPromotionalMessages(boolean promotionalMessages) { this.promotionalMessages = promotionalMessages; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
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