package com.myapp.ui.admin;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست‌های جامع برای کنترلر داشبورد ادمین
 * این کلاس تمام عملکردهای داشبورد ادمین را تست می‌کند
 */
@ExtendWith(ApplicationExtension.class)
class AdminDashboardControllerTest {

    private AdminDashboardController controller;
    private Stage stage;

    @BeforeAll
    static void setupJavaFX() {
        // راه‌اندازی JavaFX برای تست‌ها
        System.setProperty("testfx.robot", "awt");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
    }

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        
        // بارگذاری FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        
        // تنظیم صحنه
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        // انتظار برای بارگذاری کامل
        WaitForAsyncUtils.waitForFxEvents();
    }

    @BeforeEach
    void setUp() {
        // انتظار برای آماده شدن UI
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * تست بارگذاری اولیه داشبورد
     */
    @Test
    void testInitialLoad() {
        // بررسی بارگذاری صحیح UI
        assertNotNull(controller.usersTable, "جدول کاربران باید بارگذاری شود");
        assertNotNull(controller.ordersTable, "جدول سفارشات باید بارگذاری شود");
        assertNotNull(controller.transactionsTable, "جدول تراکنش‌ها باید بارگذاری شود");
        assertNotNull(controller.restaurantsTable, "جدول رستوران‌ها باید بارگذاری شود");
        
        // بررسی بارگذاری نمودارها
        assertNotNull(controller.ordersPieChart, "نمودار دایره‌ای سفارشات باید بارگذاری شود");
        assertNotNull(controller.revenueLineChart, "نمودار خطی درآمد باید بارگذاری شود");
        
        // بررسی بارگذاری فیلترها
        assertNotNull(controller.userRoleFilter, "فیلتر نقش کاربران باید بارگذاری شود");
        assertNotNull(controller.orderStatusFilter, "فیلتر وضعیت سفارشات باید بارگذاری شود");
        assertNotNull(controller.transactionTypeFilter, "فیلتر نوع تراکنش‌ها باید بارگذاری شود");
    }

    /**
     * تست بارگذاری داده‌های کاربران
     */
    @Test
    void testLoadUsers() {
        // بررسی بارگذاری داده‌های کاربران
        ObservableList<AdminDashboardController.UserData> users = controller.usersTable.getItems();
        assertNotNull(users, "لیست کاربران نباید null باشد");
        assertFalse(users.isEmpty(), "لیست کاربران نباید خالی باشد");
        
        // بررسی ساختار داده‌ها
        AdminDashboardController.UserData firstUser = users.get(0);
        assertNotNull(firstUser.getName(), "نام کاربر نباید null باشد");
        assertNotNull(firstUser.getEmail(), "ایمیل کاربر نباید null باشد");
        assertNotNull(firstUser.getRole(), "نقش کاربر نباید null باشد");
    }

    /**
     * تست فیلتر کاربران
     */
    @Test
    void testFilterUsers() {
        // تنظیم متن جستجو
        controller.userSearchField.setText("احمد");
        WaitForAsyncUtils.waitForFxEvents();
        
        // بررسی فیلتر شدن نتایج
        ObservableList<AdminDashboardController.UserData> filteredUsers = controller.usersTable.getItems();
        assertFalse(filteredUsers.isEmpty(), "نتایج فیلتر شده نباید خالی باشد");
        
        // بررسی تطابق نتایج با متن جستجو
        boolean hasMatchingUser = filteredUsers.stream()
            .anyMatch(user -> user.getName().contains("احمد"));
        assertTrue(hasMatchingUser, "نتایج باید شامل کاربران با نام 'احمد' باشند");
    }

    /**
     * تست فیلتر بر اساس نقش کاربر
     */
    @Test
    void testFilterUsersByRole() {
        // انتخاب نقش "ادمین" در JavaFX thread
        Platform.runLater(() -> {
            controller.userRoleFilter.setValue("ادمین");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // بررسی فیلتر شدن نتایج
        ObservableList<AdminDashboardController.UserData> filteredUsers = controller.usersTable.getItems();
        assertFalse(filteredUsers.isEmpty(), "نتایج فیلتر شده نباید خالی باشد");
        
        // بررسی تطابق نتایج با نقش انتخاب شده
        boolean allAdmins = filteredUsers.stream()
            .allMatch(user -> user.getRole().equals("ادمین"));
        assertTrue(allAdmins, "تمام نتایج باید نقش 'ادمین' داشته باشند");
    }

    /**
     * تست بارگذاری داده‌های سفارشات
     */
    @Test
    void testLoadOrders() {
        // بررسی بارگذاری داده‌های سفارشات
        ObservableList<AdminDashboardController.OrderData> orders = controller.ordersTable.getItems();
        assertNotNull(orders, "لیست سفارشات نباید null باشد");
        assertFalse(orders.isEmpty(), "لیست سفارشات نباید خالی باشد");
        
        // بررسی ساختار داده‌ها
        AdminDashboardController.OrderData firstOrder = orders.get(0);
        assertNotNull(firstOrder.getCustomerName(), "نام مشتری نباید null باشد");
        assertNotNull(firstOrder.getRestaurantName(), "نام رستوران نباید null باشد");
        assertNotNull(firstOrder.getStatus(), "وضعیت سفارش نباید null باشد");
        assertTrue(firstOrder.getAmount() > 0, "مبلغ سفارش باید مثبت باشد");
    }

    /**
     * تست فیلتر سفارشات
     */
    @Test
    void testFilterOrders() {
        // تنظیم متن جستجو
        controller.orderSearchField.setText("تهران");
        WaitForAsyncUtils.waitForFxEvents();
        
        // بررسی فیلتر شدن نتایج
        ObservableList<AdminDashboardController.OrderData> filteredOrders = controller.ordersTable.getItems();
        assertFalse(filteredOrders.isEmpty(), "نتایج فیلتر شده نباید خالی باشد");
        
        // بررسی تطابق نتایج با متن جستجو
        boolean hasMatchingOrder = filteredOrders.stream()
            .anyMatch(order -> order.getRestaurantName().contains("تهران"));
        assertTrue(hasMatchingOrder, "نتایج باید شامل سفارشات رستوران 'تهران' باشند");
    }

    /**
     * تست فیلتر سفارشات بر اساس وضعیت
     */
    @Test
    void testFilterOrdersByStatus() {
        // انتخاب وضعیت "تحویل شده" در JavaFX thread
        Platform.runLater(() -> {
            controller.orderStatusFilter.setValue("تحویل شده");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // بررسی فیلتر شدن نتایج
        ObservableList<AdminDashboardController.OrderData> filteredOrders = controller.ordersTable.getItems();
        assertFalse(filteredOrders.isEmpty(), "نتایج فیلتر شده نباید خالی باشد");
        
        // بررسی تطابق نتایج با وضعیت انتخاب شده
        boolean allDelivered = filteredOrders.stream()
            .allMatch(order -> order.getStatus().equals("تحویل شده"));
        assertTrue(allDelivered, "تمام نتایج باید وضعیت 'تحویل شده' داشته باشند");
    }

    /**
     * تست بارگذاری داده‌های تراکنش‌ها
     */
    @Test
    void testLoadTransactions() {
        // بررسی بارگذاری داده‌های تراکنش‌ها
        ObservableList<AdminDashboardController.TransactionData> transactions = controller.transactionsTable.getItems();
        assertNotNull(transactions, "لیست تراکنش‌ها نباید null باشد");
        assertFalse(transactions.isEmpty(), "لیست تراکنش‌ها نباید خالی باشد");
        
        // بررسی ساختار داده‌ها
        AdminDashboardController.TransactionData firstTransaction = transactions.get(0);
        assertNotNull(firstTransaction.getUserName(), "نام کاربر نباید null باشد");
        assertNotNull(firstTransaction.getType(), "نوع تراکنش نباید null باشد");
        assertNotNull(firstTransaction.getStatus(), "وضعیت تراکنش نباید null باشد");
        assertTrue(firstTransaction.getAmount() > 0, "مبلغ تراکنش باید مثبت باشد");
    }

    /**
     * تست فیلتر تراکنش‌ها
     */
    @Test
    void testFilterTransactions() {
        // تنظیم متن جستجو
        controller.transactionSearchField.setText("احمد");
        WaitForAsyncUtils.waitForFxEvents();
        
        // بررسی فیلتر شدن نتایج
        ObservableList<AdminDashboardController.TransactionData> filteredTransactions = controller.transactionsTable.getItems();
        assertFalse(filteredTransactions.isEmpty(), "نتایج فیلتر شده نباید خالی باشد");
        
        // بررسی تطابق نتایج با متن جستجو
        boolean hasMatchingTransaction = filteredTransactions.stream()
            .anyMatch(transaction -> transaction.getUserName().contains("احمد"));
        assertTrue(hasMatchingTransaction, "نتایج باید شامل تراکنش‌های کاربر 'احمد' باشند");
    }

    /**
     * تست فیلتر تراکنش‌ها بر اساس نوع
     */
    @Test
    void testFilterTransactionsByType() {
        // انتخاب نوع "پرداخت" در JavaFX thread
        Platform.runLater(() -> {
            controller.transactionTypeFilter.setValue("پرداخت");
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // بررسی فیلتر شدن نتایج
        ObservableList<AdminDashboardController.TransactionData> filteredTransactions = controller.transactionsTable.getItems();
        assertFalse(filteredTransactions.isEmpty(), "نتایج فیلتر شده نباید خالی باشد");
        
        // بررسی تطابق نتایج با نوع انتخاب شده
        boolean allPayments = filteredTransactions.stream()
            .allMatch(transaction -> transaction.getType().equals("پرداخت"));
        assertTrue(allPayments, "تمام نتایج باید نوع 'پرداخت' داشته باشند");
    }

    /**
     * تست بارگذاری داده‌های رستوران‌ها
     */
    @Test
    void testLoadRestaurants() {
        // بررسی بارگذاری داده‌های رستوران‌ها
        ObservableList<AdminDashboardController.RestaurantData> restaurants = controller.restaurantsTable.getItems();
        assertNotNull(restaurants, "لیست رستوران‌ها نباید null باشد");
        assertFalse(restaurants.isEmpty(), "لیست رستوران‌ها نباید خالی باشد");
        
        // بررسی ساختار داده‌ها
        AdminDashboardController.RestaurantData firstRestaurant = restaurants.get(0);
        assertNotNull(firstRestaurant.getName(), "نام رستوران نباید null باشد");
        assertNotNull(firstRestaurant.getAddress(), "آدرس رستوران نباید null باشد");
        assertNotNull(firstRestaurant.getStatus(), "وضعیت رستوران نباید null باشد");
        assertTrue(firstRestaurant.getRating() >= 0, "امتیاز رستوران باید غیرمنفی باشد");
    }

    /**
     * تست فیلتر رستوران‌ها
     */
    @Test
    void testFilterRestaurants() {
        // تنظیم متن جستجو
        controller.restaurantSearchField.setText("تهران");
        WaitForAsyncUtils.waitForFxEvents();
        
        // بررسی فیلتر شدن نتایج
        ObservableList<AdminDashboardController.RestaurantData> filteredRestaurants = controller.restaurantsTable.getItems();
        assertFalse(filteredRestaurants.isEmpty(), "نتایج فیلتر شده نباید خالی باشد");
        
        // بررسی تطابق نتایج با متن جستجو
        boolean hasMatchingRestaurant = filteredRestaurants.stream()
            .anyMatch(restaurant -> restaurant.getName().contains("تهران") || 
                                   restaurant.getAddress().contains("تهران"));
        assertTrue(hasMatchingRestaurant, "نتایج باید شامل رستوران‌های 'تهران' باشند");
    }

    /**
     * تست بارگذاری نمودار سفارشات
     */
    @Test
    void testLoadOrdersChart() {
        // بررسی بارگذاری نمودار دایره‌ای سفارشات
        ObservableList<PieChart.Data> chartData = controller.ordersPieChart.getData();
        assertNotNull(chartData, "داده‌های نمودار نباید null باشد");
        assertFalse(chartData.isEmpty(), "نمودار باید داده داشته باشد");
        
        // بررسی وجود دسته‌بندی‌های مختلف
        boolean hasPending = chartData.stream().anyMatch(data -> data.getName().equals("در انتظار"));
        boolean hasDelivered = chartData.stream().anyMatch(data -> data.getName().equals("تحویل شده"));
        
        assertTrue(hasPending, "نمودار باید شامل دسته 'در انتظار' باشد");
        assertTrue(hasDelivered, "نمودار باید شامل دسته 'تحویل شده' باشد");
    }

    /**
     * تست بارگذاری نمودار درآمد
     */
    @Test
    void testLoadRevenueChart() {
        // بررسی بارگذاری نمودار خطی درآمد
        ObservableList<LineChart.Series<String, Number>> chartData = controller.revenueLineChart.getData();
        assertNotNull(chartData, "داده‌های نمودار نباید null باشد");
        assertFalse(chartData.isEmpty(), "نمودار باید داده داشته باشد");
        
        // بررسی وجود سری داده
        LineChart.Series<String, Number> series = chartData.get(0);
        assertNotNull(series, "سری داده نباید null باشد");
        assertFalse(series.getData().isEmpty(), "سری داده نباید خالی باشد");
    }

    /**
     * تست عملکرد دکمه‌های مدیریت کاربران
     */
    @Test
    void testUserManagementButtons() {
        // انتخاب یک کاربر
        controller.usersTable.getSelectionModel().select(0);
        WaitForAsyncUtils.waitForFxEvents();
        
        // بررسی فعال بودن دکمه‌های ویرایش و حذف
        assertFalse(controller.editUserButton.isDisabled(), "دکمه ویرایش باید فعال باشد");
        assertFalse(controller.deleteUserButton.isDisabled(), "دکمه حذف باید فعال باشد");
        
        // بررسی فعال بودن دکمه افزودن
        assertFalse(controller.addUserButton.isDisabled(), "دکمه افزودن باید فعال باشد");
    }

    /**
     * تست عملکرد دکمه‌های مدیریت رستوران‌ها
     */
    @Test
    void testRestaurantManagementButtons() {
        // انتخاب یک رستوران
        controller.restaurantsTable.getSelectionModel().select(0);
        WaitForAsyncUtils.waitForFxEvents();
        
        // بررسی فعال بودن دکمه‌های ویرایش و حذف
        assertFalse(controller.editRestaurantButton.isDisabled(), "دکمه ویرایش باید فعال باشد");
        assertFalse(controller.deleteRestaurantButton.isDisabled(), "دکمه حذف باید فعال باشد");
        
        // بررسی فعال بودن دکمه افزودن
        assertFalse(controller.addRestaurantButton.isDisabled(), "دکمه افزودن باید فعال باشد");
    }

    /**
     * تست عملکرد دکمه‌های Export
     */
    @Test
    void testExportButtons() {
        // بررسی فعال بودن دکمه‌های Export
        assertFalse(controller.exportOrdersButton.isDisabled(), "دکمه Export سفارشات باید فعال باشد");
        assertFalse(controller.exportTransactionsButton.isDisabled(), "دکمه Export تراکنش‌ها باید فعال باشد");
        assertFalse(controller.exportReportButton.isDisabled(), "دکمه Export گزارش‌ها باید فعال باشد");
    }

    /**
     * تست عملکرد دکمه‌های تنظیمات
     */
    @Test
    void testSettingsButtons() {
        // بررسی فعال بودن دکمه‌های تنظیمات
        assertFalse(controller.saveSettingsButton.isDisabled(), "دکمه ذخیره تنظیمات باید فعال باشد");
        assertFalse(controller.resetSettingsButton.isDisabled(), "دکمه بازنشانی تنظیمات باید فعال باشد");
    }

    /**
     * تست عملکرد فیلترها
     */
    @Test
    void testFilters() {
        // بررسی فعال بودن فیلترها
        assertFalse(controller.userSearchField.isDisabled(), "فیلتر جستجوی کاربران باید فعال باشد");
        assertFalse(controller.userRoleFilter.isDisabled(), "فیلتر نقش کاربران باید فعال باشد");
        assertFalse(controller.orderSearchField.isDisabled(), "فیلتر جستجوی سفارشات باید فعال باشد");
        assertFalse(controller.orderStatusFilter.isDisabled(), "فیلتر وضعیت سفارشات باید فعال باشد");
        assertFalse(controller.transactionSearchField.isDisabled(), "فیلتر جستجوی تراکنش‌ها باید فعال باشد");
        assertFalse(controller.transactionTypeFilter.isDisabled(), "فیلتر نوع تراکنش‌ها باید فعال باشد");
        assertFalse(controller.restaurantSearchField.isDisabled(), "فیلتر جستجوی رستوران‌ها باید فعال باشد");
    }

    /**
     * تست عملکرد TabPane
     */
    @Test
    void testTabPane() {
        // بررسی وجود تب‌های مختلف
        TabPane tabPane = controller.mainTabPane;
        assertNotNull(tabPane, "TabPane نباید null باشد");
        assertFalse(tabPane.getTabs().isEmpty(), "TabPane باید تب داشته باشد");
        
        // بررسی وجود تب‌های اصلی
        boolean hasUsersTab = tabPane.getTabs().stream().anyMatch(tab -> tab.getText().contains("کاربران"));
        boolean hasOrdersTab = tabPane.getTabs().stream().anyMatch(tab -> tab.getText().contains("سفارشات"));
        boolean hasTransactionsTab = tabPane.getTabs().stream().anyMatch(tab -> tab.getText().contains("تراکنش"));
        boolean hasRestaurantsTab = tabPane.getTabs().stream().anyMatch(tab -> tab.getText().contains("رستوران"));
        boolean hasReportsTab = tabPane.getTabs().stream().anyMatch(tab -> tab.getText().contains("گزارش"));
        boolean hasSettingsTab = tabPane.getTabs().stream().anyMatch(tab -> tab.getText().contains("تنظیمات"));
        
        assertTrue(hasUsersTab, "تب کاربران باید وجود داشته باشد");
        assertTrue(hasOrdersTab, "تب سفارشات باید وجود داشته باشد");
        assertTrue(hasTransactionsTab, "تب تراکنش‌ها باید وجود داشته باشد");
        assertTrue(hasRestaurantsTab, "تب رستوران‌ها باید وجود داشته باشد");
        assertTrue(hasReportsTab, "تب گزارش‌ها باید وجود داشته باشد");
        assertTrue(hasSettingsTab, "تب تنظیمات باید وجود داشته باشد");
    }

    /**
     * تست عملکرد Label وضعیت
     */
    @Test
    void testStatusLabel() {
        // بررسی وجود Label وضعیت
        assertNotNull(controller.statusLabel, "Label وضعیت نباید null باشد");
        assertFalse(controller.statusLabel.getText().isEmpty(), "متن وضعیت نباید خالی باشد");
    }

    /**
     * تست عملکرد ProgressIndicator
     */
    @Test
    void testLoadingIndicator() {
        // بررسی وجود ProgressIndicator
        assertNotNull(controller.loadingIndicator, "ProgressIndicator نباید null باشد");
        // در حالت عادی باید مخفی باشد
        assertFalse(controller.loadingIndicator.isVisible(), "ProgressIndicator در حالت عادی باید مخفی باشد");
    }
} 