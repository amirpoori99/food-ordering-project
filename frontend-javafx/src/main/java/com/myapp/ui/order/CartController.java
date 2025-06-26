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

import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

/**
 * کنترلر سبد خرید
 * 
 * این کلاس مسئول مدیریت رابط کاربری سبد خرید شامل:
 * - نمایش آیتم‌های اضافه شده به سبد
 * - مدیریت تعداد و انتخاب آیتم‌ها
 * - محاسبه مبلغ کل و هزینه ارسال
 * - اعمال کد تخفیف
 * - اعتبارسنجی اطلاعات تحویل
 * - انتقال به صفحه پرداخت
 * 
 * ویژگی‌ها:
 * - Real-time calculation مبلغ
 * - Dynamic UI creation برای cart items
 * - Validation برای checkout
 * - Currency formatting فارسی
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class CartController implements Initializable {

    @FXML private Label itemCountLabel;
    @FXML private VBox cartItemsContainer;
    @FXML private Label emptyCartLabel;
    @FXML private Button selectAllButton;
    @FXML private Button removeSelectedButton;
    @FXML private Label totalItemsLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label deliveryFeeLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private TextField couponCodeField;
    @FXML private Button applyCouponButton;
    @FXML private Label couponStatusLabel;
    @FXML private TextArea deliveryAddressField;
    @FXML private TextField deliveryPhoneField;
    @FXML private TextArea orderNotesField;
    @FXML private Button checkoutButton;
    @FXML private Button saveForLaterButton;
    @FXML private MenuItem backMenuItem;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuItem clearCartMenuItem;
    @FXML private MenuItem refreshMenuItem;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    /** کنترلر navigation برای تغییر صفحات */
    private NavigationController navigationController;
    
    /** لیست آیتم‌های موجود در سبد خرید */
    private List<CartItem> cartItems = new ArrayList<>();
    
    /** فرمت کردن ارز به فارسی */
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("fa", "IR"));

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
        loadCart();
    }

    /**
     * راه‌اندازی کامپوننت‌های UI و event listener ها
     * 
     * تنظیمات:
     * - فرمت کردن ارز
     * - listener ها برای اعتبارسنجی فیلدها
     * - فعال/غیرفعال کردن دکمه تسویه حساب
     */
    private void setupUI() {
        currencyFormat.setGroupingUsed(true);
        setStatus("در حال بارگذاری سبد خرید...");
        
        // تنظیم listener ها برای اعتبارسنجی realtime
        deliveryAddressField.textProperty().addListener((obs, oldVal, newVal) -> validateCheckoutButton());
        deliveryPhoneField.textProperty().addListener((obs, oldVal, newVal) -> validateCheckoutButton());
        couponCodeField.textProperty().addListener((obs, oldVal, newVal) -> 
            applyCouponButton.setDisable(newVal.trim().isEmpty()));
    }

    /**
     * بارگذاری آیتم‌های سبد خرید از سرور
     * 
     * از background task استفاده می‌کند تا UI freeze نشود
     * در حال حاضر از mock data استفاده می‌کند
     */
    private void loadCart() {
        setLoading(true);
        
        // background task برای بارگذاری داده‌ها
        Task<List<CartItem>> loadTask = new Task<List<CartItem>>() {
            @Override
            protected List<CartItem> call() throws Exception {
                Thread.sleep(1000); // شبیه‌سازی تأخیر شبکه
                // ایجاد mock data برای نمایش
                List<CartItem> mockItems = new ArrayList<>();
                mockItems.add(new CartItem(1L, "پیتزا مارگاریتا", 25000.0, 2, "رستوران ایتالیایی"));
                mockItems.add(new CartItem(2L, "برگر کلاسیک", 18000.0, 1, "فست فود سارا"));
                return mockItems;
            }
        };
        
        // پردازش نتیجه موفق
        loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false);
            cartItems = loadTask.getValue();
            displayCartItems();
            updateCartSummary();
            setStatus("سبد خرید بارگذاری شد");
        }));
        
        // پردازش خطا
        loadTask.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            setStatus("خطا در بارگذاری سبد خرید");
        }));
        
        new Thread(loadTask).start();
    }

    private void displayCartItems() {
        cartItemsContainer.getChildren().clear();
        
        if (cartItems.isEmpty()) {
            emptyCartLabel.setVisible(true);
            itemCountLabel.setText("(0 آیتم)");
            return;
        }
        
        emptyCartLabel.setVisible(false);
        itemCountLabel.setText("(" + cartItems.size() + " آیتم)");
        
        for (CartItem item : cartItems) {
            cartItemsContainer.getChildren().add(createCartItemUI(item));
        }
    }

    private VBox createCartItemUI(CartItem item) {
        VBox itemBox = new VBox(10);
        itemBox.setPadding(new Insets(15));
        itemBox.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        HBox headerBox = new HBox(10);
        CheckBox selectCheckBox = new CheckBox();
        selectCheckBox.setSelected(item.isSelected());
        selectCheckBox.setOnAction(e -> {
            item.setSelected(selectCheckBox.isSelected());
            updateCartSummary();
        });
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        headerBox.getChildren().addAll(selectCheckBox, nameLabel);
        
        HBox detailsBox = new HBox(20);
        Label priceLabel = new Label(formatCurrency(item.getPrice()) + " تومان");
        
        HBox quantityBox = new HBox(5);
        Button decreaseButton = new Button("-");
        decreaseButton.setOnAction(e -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                updateCartSummary();
                displayCartItems();
            }
        });
        
        Label quantityLabel = new Label(String.valueOf(item.getQuantity()));
        Button increaseButton = new Button("+");
        increaseButton.setOnAction(e -> {
            item.setQuantity(item.getQuantity() + 1);
            updateCartSummary();
            displayCartItems();
        });
        
        quantityBox.getChildren().addAll(decreaseButton, quantityLabel, increaseButton);
        
        Button removeButton = new Button("حذف");
        removeButton.setOnAction(e -> {
            cartItems.remove(item);
            displayCartItems();
            updateCartSummary();
        });
        
        detailsBox.getChildren().addAll(priceLabel, quantityBox, removeButton);
        itemBox.getChildren().addAll(headerBox, detailsBox);
        
        return itemBox;
    }

    private void updateCartSummary() {
        double subtotal = cartItems.stream()
                .filter(CartItem::isSelected)
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        double deliveryFee = subtotal >= 50000 ? 0.0 : 5000.0;
        double total = subtotal + deliveryFee;
        
        int selectedItemCount = (int) cartItems.stream().filter(CartItem::isSelected).count();
        totalItemsLabel.setText(String.valueOf(selectedItemCount));
        subtotalLabel.setText(formatCurrency(subtotal) + " تومان");
        deliveryFeeLabel.setText(deliveryFee == 0 ? "رایگان" : formatCurrency(deliveryFee) + " تومان");
        totalAmountLabel.setText(formatCurrency(total) + " تومان");
        
        validateCheckoutButton();
    }

    private void validateCheckoutButton() {
        boolean hasSelectedItems = cartItems.stream().anyMatch(CartItem::isSelected);
        boolean hasDeliveryAddress = !deliveryAddressField.getText().trim().isEmpty();
        boolean hasDeliveryPhone = !deliveryPhoneField.getText().trim().isEmpty();
        
        checkoutButton.setDisable(!hasSelectedItems || !hasDeliveryAddress || !hasDeliveryPhone);
    }

    private String formatCurrency(double amount) {
        return currencyFormat.format(amount);
    }

    @FXML
    private void handleSelectAll() {
        boolean allSelected = cartItems.stream().allMatch(CartItem::isSelected);
        cartItems.forEach(item -> item.setSelected(!allSelected));
        displayCartItems();
        updateCartSummary();
    }

    @FXML
    private void handleRemoveSelected() {
        cartItems.removeIf(CartItem::isSelected);
        displayCartItems();
        updateCartSummary();
    }

    @FXML
    private void handleApplyCoupon() {
        String couponCode = couponCodeField.getText().trim();
        // TODO: Implement coupon validation
        couponStatusLabel.setText("کد تخفیف اعمال شد");
    }

    @FXML
    private void handleCheckout() {
        navigationController.navigateTo(NavigationController.PAYMENT_SCENE);
    }

    @FXML
    private void handleSaveForLater() {
        showInfo("اطلاع", "سبد خرید ذخیره شد");
    }

    @FXML
    private void handleClearCart() {
        cartItems.clear();
        displayCartItems();
        updateCartSummary();
    }

    @FXML
    private void handleRefresh() {
        loadCart();
    }

    @FXML
    private void handleBack() {
        navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
    }

    @FXML
    private void handleProfile() {
        navigationController.navigateTo(NavigationController.PROFILE_SCENE);
    }

    @FXML
    private void handleOrderHistory() {
        navigationController.navigateTo(NavigationController.ORDER_HISTORY_SCENE);
    }

    @FXML
    private void handleLogout() {
        navigationController.logout();
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class CartItem {
        private Long id;
        private String name;
        private double price;
        private int quantity;
        private String restaurantName;
        private boolean selected = true;

        public CartItem(Long id, String name, double price, int quantity, String restaurantName) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.restaurantName = restaurantName;
        }

        // Getters and setters
        public Long getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getRestaurantName() { return restaurantName; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }
} 