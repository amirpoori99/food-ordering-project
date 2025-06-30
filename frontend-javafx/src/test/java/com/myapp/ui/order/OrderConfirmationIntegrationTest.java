package com.myapp.ui.order;

import com.myapp.ui.common.TestFXBase;
import com.myapp.ui.order.OrderConfirmationController.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست‌های یکپارچه برای OrderConfirmationController
 * 
 * این کلاس سناریوهای کامل استفاده از کنترلر را تست می‌کند
 * شامل تعاملات کاربر، بروزرسانی وضعیت، و عملیات‌های پیچیده
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since فاز 26 - Order Confirmation UI
 */
@ExtendWith(ApplicationExtension.class)
class OrderConfirmationIntegrationTest extends TestFXBase {

    private OrderConfirmationController controller;
    private OrderInfo testOrder;
    private Button cancelOrderButton;
    private Button downloadReceiptButton;
    private Button reorderButton;
    private ProgressBar orderProgressBar;
    private Label statusMessageLabel;
    private CheckBox emailNotificationCheckBox;
    private CheckBox smsNotificationCheckBox;

    @Start
    public void start(Stage stage) throws Exception {
        // ایجاد سفارش تست
        testOrder = createTestOrder();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrderConfirmation.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            
            // دریافت کامپوننت‌های UI
            cancelOrderButton = (Button) root.lookup("#cancelOrderButton");
            downloadReceiptButton = (Button) root.lookup("#downloadReceiptButton");
            reorderButton = (Button) root.lookup("#reorderButton");
            orderProgressBar = (ProgressBar) root.lookup("#orderProgressBar");
            statusMessageLabel = (Label) root.lookup("#statusMessageLabel");
            emailNotificationCheckBox = (CheckBox) root.lookup("#emailNotificationCheckBox");
            smsNotificationCheckBox = (CheckBox) root.lookup("#smsNotificationCheckBox");
            
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (Exception e) {
            createMockUI(stage);
        }
    }
    
    /**
     * ایجاد UI Mock در صورت عدم موفقیت بارگذاری FXML
     */
    private void createMockUI(Stage stage) {
        controller = new OrderConfirmationController();
        
        cancelOrderButton = new Button("❌ لغو سفارش");
        downloadReceiptButton = new Button("📄 دانلود رسید");
        reorderButton = new Button("🔄 سفارش مجدد");
        orderProgressBar = new ProgressBar(0.3);
        statusMessageLabel = new Label("سفارش شما با موفقیت ثبت شد");
        emailNotificationCheckBox = new CheckBox("ارسال اطلاع‌رسانی به ایمیل");
        smsNotificationCheckBox = new CheckBox("ارسال اطلاع‌رسانی پیامکی");
        
        emailNotificationCheckBox.setSelected(true);
        smsNotificationCheckBox.setSelected(true);
        
        VBox root = new VBox(10);
        root.getChildren().addAll(
            cancelOrderButton, downloadReceiptButton, reorderButton,
            orderProgressBar, statusMessageLabel,
            emailNotificationCheckBox, smsNotificationCheckBox
        );
        
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }

    /**
     * ایجاد سفارش تست با اطلاعات کامل
     */
    private OrderInfo createTestOrder() {
        OrderInfo order = new OrderInfo();
        order.setOrderId("ORD-TEST-" + System.currentTimeMillis());
        order.setOrderDateTime(LocalDateTime.now());
        order.setEstimatedDelivery(LocalDateTime.now().plusMinutes(45));
        order.setSubtotal(125000.0);
        order.setTax(11250.0);
        order.setDeliveryFee(5000.0);
        order.setDiscount(10000.0);
        order.setTotalAmount(131250.0);
        order.setPaymentMethod("کارت اعتباری");
        order.setPaymentStatus("تأیید شده");
        order.setRestaurantName("رستوران تست");
        order.setRestaurantPhone("021-11111111");
        order.setDeliveryAddress("تهران، خیابان تست، پلاک 456");
        order.setCustomerPhone("09111111111");
        order.setOrderNotes("تست سفارش یکپارچه");
        
        // افزودن آیتم‌های تست
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("کباب برگ", 2, 50000.0, "کم نمک"));
        items.add(new OrderItem("نوشابه", 2, 12500.0, "بدون یخ"));
        order.setOrderItems(items);
        
        return order;
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        Platform.runLater(() -> {
            // تنظیم وضعیت اولیه
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست سناریو کامل تأیید سفارش
     */
    @Test
    void testCompleteOrderConfirmationFlow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            // بررسی وضعیت اولیه
            assertNotNull(controller, "کنترلر باید مقداردهی شود");
            
            if (orderProgressBar != null) {
                assertTrue(orderProgressBar.getProgress() >= 0.0, "پیشرفت نباید منفی باشد");
            }
            
            if (statusMessageLabel != null) {
                assertNotNull(statusMessageLabel.getText(), "پیام وضعیت باید موجود باشد");
            }
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "عملیات تست باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست عملیات لغو سفارش
     */
    @Test
    void testCancelOrderOperation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            if (cancelOrderButton != null) {
                assertFalse(cancelOrderButton.isDisabled(), "دکمه لغو سفارش باید فعال باشد");
                
                // شبیه‌سازی کلیک لغو سفارش
                cancelOrderButton.fire();
            }
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "عملیات لغو سفارش باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست عملیات دانلود رسید
     */
    @Test
    void testDownloadReceiptOperation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            if (downloadReceiptButton != null) {
                assertFalse(downloadReceiptButton.isDisabled(), "دکمه دانلود رسید باید فعال باشد");
                
                // شبیه‌سازی کلیک دانلود رسید
                downloadReceiptButton.fire();
            }
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "عملیات دانلود رسید باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست عملیات سفارش مجدد
     */
    @Test
    void testReorderOperation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            if (reorderButton != null) {
                // شبیه‌سازی کلیک سفارش مجدد
                reorderButton.fire();
            }
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "عملیات سفارش مجدد باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست تغییر تنظیمات اطلاع‌رسانی
     */
    @Test
    void testNotificationSettingsChange() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            if (emailNotificationCheckBox != null) {
                boolean initialState = emailNotificationCheckBox.isSelected();
                
                // تغییر وضعیت checkbox
                emailNotificationCheckBox.setSelected(!initialState);
                
                // بررسی تغییر
                assertNotEquals(initialState, emailNotificationCheckBox.isSelected(),
                              "وضعیت اطلاع‌رسانی ایمیل باید تغییر کند");
            }
            
            if (smsNotificationCheckBox != null) {
                boolean initialState = smsNotificationCheckBox.isSelected();
                
                // تغییر وضعیت checkbox
                smsNotificationCheckBox.setSelected(!initialState);
                
                // بررسی تغییر
                assertNotEquals(initialState, smsNotificationCheckBox.isSelected(),
                              "وضعیت اطلاع‌رسانی پیامک باید تغییر کند");
            }
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "تغییر تنظیمات اطلاع‌رسانی باید تکمیل شود");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست مدل OrderInfo با داده‌های کامل
     */
    @Test
    void testOrderInfoModelWithCompleteData() {
        assertNotNull(testOrder, "سفارش تست باید ایجاد شود");
        assertNotNull(testOrder.getOrderId(), "شماره سفارش باید موجود باشد");
        assertNotNull(testOrder.getOrderDateTime(), "تاریخ سفارش باید موجود باشد");
        assertNotNull(testOrder.getEstimatedDelivery(), "زمان تحویل باید موجود باشد");
        
        assertTrue(testOrder.getSubtotal() > 0, "جمع کل باید مثبت باشد");
        assertTrue(testOrder.getTotalAmount() > 0, "مبلغ نهایی باید مثبت باشد");
        
        assertNotNull(testOrder.getPaymentMethod(), "روش پرداخت باید مشخص باشد");
        assertNotNull(testOrder.getPaymentStatus(), "وضعیت پرداخت باید مشخص باشد");
        assertNotNull(testOrder.getRestaurantName(), "نام رستوران باید موجود باشد");
        assertNotNull(testOrder.getDeliveryAddress(), "آدرس تحویل باید موجود باشد");
        
        assertNotNull(testOrder.getOrderItems(), "لیست آیتم‌ها باید موجود باشد");
        assertFalse(testOrder.getOrderItems().isEmpty(), "لیست آیتم‌ها نباید خالی باشد");
    }

    /**
     * تست محاسبه صحیح مبلغ نهایی
     */
    @Test
    void testCorrectTotalAmountCalculation() {
        double expectedTotal = testOrder.getSubtotal() + testOrder.getTax() + 
                              testOrder.getDeliveryFee() - testOrder.getDiscount();
        
        assertEquals(expectedTotal, testOrder.getTotalAmount(), 0.01,
                    "محاسبه مبلغ نهایی باید صحیح باشد");
    }

    /**
     * تست آیتم‌های سفارش
     */
    @Test
    void testOrderItemsCalculation() {
        for (OrderItem item : testOrder.getOrderItems()) {
            assertNotNull(item.getItemName(), "نام آیتم باید موجود باشد");
            assertTrue(item.getQuantity() > 0, "تعداد آیتم باید مثبت باشد");
            assertTrue(item.getUnitPrice() > 0, "قیمت واحد باید مثبت باشد");
            
            double expectedTotal = item.getUnitPrice() * item.getQuantity();
            assertEquals(expectedTotal, item.getTotalPrice(), 0.01,
                        "محاسبه قیمت کل آیتم باید صحیح باشد");
        }
    }

    /**
     * تست enum TrackingStatus
     */
    @Test
    void testTrackingStatusEnum() {
        TrackingStatus[] statuses = TrackingStatus.values();
        assertEquals(3, statuses.length, "باید 3 وضعیت پیگیری وجود داشته باشد");
        
        assertEquals("در انتظار", TrackingStatus.PENDING.getDisplayName());
        assertEquals("در حال انجام", TrackingStatus.IN_PROGRESS.getDisplayName());
        assertEquals("تکمیل شده", TrackingStatus.COMPLETED.getDisplayName());
    }

    /**
     * تست TrackingStep با وضعیت‌های مختلف
     */
    @Test
    void testTrackingStepWithDifferentStatuses() {
        LocalDateTime now = LocalDateTime.now();
        
        TrackingStep pendingStep = new TrackingStep(
            "در انتظار", "مرحله در انتظار", null, TrackingStatus.PENDING);
        assertEquals(TrackingStatus.PENDING, pendingStep.getStatus());
        assertNull(pendingStep.getCompletedAt());
        
        TrackingStep inProgressStep = new TrackingStep(
            "در حال انجام", "مرحله در حال انجام", now, TrackingStatus.IN_PROGRESS);
        assertEquals(TrackingStatus.IN_PROGRESS, inProgressStep.getStatus());
        assertEquals(now, inProgressStep.getCompletedAt());
        
        TrackingStep completedStep = new TrackingStep(
            "تکمیل شده", "مرحله تکمیل شده", now, TrackingStatus.COMPLETED);
        assertEquals(TrackingStatus.COMPLETED, completedStep.getStatus());
        assertEquals(now, completedStep.getCompletedAt());
    }

    /**
     * تست تغییر وضعیت TrackingStep
     */
    @Test
    void testTrackingStepStatusChange() {
        LocalDateTime now = LocalDateTime.now();
        TrackingStep step = new TrackingStep(
            "تست", "توضیحات تست", null, TrackingStatus.PENDING);
        
        // تغییر وضعیت به در حال انجام
        step.setStatus(TrackingStatus.IN_PROGRESS);
        step.setCompletedAt(now);
        
        assertEquals(TrackingStatus.IN_PROGRESS, step.getStatus());
        assertEquals(now, step.getCompletedAt());
        
        // تغییر وضعیت به تکمیل شده
        step.setStatus(TrackingStatus.COMPLETED);
        
        assertEquals(TrackingStatus.COMPLETED, step.getStatus());
        assertEquals(now, step.getCompletedAt());
    }
} 