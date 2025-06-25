package com.myapp.ui.payment;

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
 * Controller for Payment screen
 * Handles payment method selection, validation, and processing
 */
public class PaymentController implements Initializable {

    // Payment Method Radio Buttons
    @FXML private ToggleGroup paymentMethodGroup;
    @FXML private RadioButton cardPaymentRadio;
    @FXML private RadioButton walletPaymentRadio;
    @FXML private RadioButton codPaymentRadio;
    
    // Payment Sections
    @FXML private VBox cardPaymentSection;
    @FXML private VBox walletPaymentSection;
    @FXML private VBox codPaymentSection;
    
    // Card Payment Fields
    @FXML private TextField cardNumberField;
    @FXML private TextField cardHolderNameField;
    @FXML private TextField cardExpiryMonthField;
    @FXML private TextField cardExpiryYearField;
    @FXML private TextField cardCvvField;
    
    // Wallet Payment Fields
    @FXML private Label walletBalanceLabel;
    @FXML private Button refreshWalletButton;
    @FXML private Label walletStatusLabel;
    
    // Delivery Information Display
    @FXML private TextArea deliveryAddressDisplay;
    @FXML private TextField deliveryPhoneDisplay;
    @FXML private TextArea orderNotesDisplay;
    
    // Order Summary
    @FXML private ScrollPane orderItemsScrollPane;
    @FXML private VBox orderItemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label deliveryFeeLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalAmountLabel;
    
    // Action Buttons
    @FXML private Button confirmPaymentButton;
    @FXML private Button backToCartButton;
    
    // Menu Items
    @FXML private MenuItem backToCartMenuItem;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem logoutMenuItem;
    
    // Status Components
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private NavigationController navigationController;
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("fa", "IR"));
    
    // Order data (in real app, this would come from previous screen)
    private List<OrderItem> orderItems = new ArrayList<>();
    private double subtotal = 0.0;
    private double deliveryFee = 0.0;
    private double discount = 0.0;
    private double walletBalance = 75000.0; // Mock wallet balance

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        
        setupUI();
        loadOrderData();
        loadDeliveryInfo();
    }

    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Initialize currency format
        currencyFormat.setGroupingUsed(true);
        
        // Setup payment method listeners
        setupPaymentMethodListeners();
        
        // Setup card number formatting
        setupCardNumberFormatting();
        
        // Setup validation listeners
        setupValidationListeners();
        
        // Initial status
        setStatus("آماده برای پرداخت");
        
        // Initially show COD section
        updatePaymentSectionVisibility();
    }

    /**
     * Setup payment method change listeners
     */
    private void setupPaymentMethodListeners() {
        paymentMethodGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            updatePaymentSectionVisibility();
            validatePaymentForm();
        });
    }

    /**
     * Setup card number formatting
     */
    private void setupCardNumberFormatting() {
        cardNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Format card number with dashes
            String formatted = newVal.replaceAll("\\D", ""); // Remove non-digits
            if (formatted.length() > 16) {
                formatted = formatted.substring(0, 16);
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < formatted.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    sb.append("-");
                }
                sb.append(formatted.charAt(i));
            }
            
            if (!sb.toString().equals(newVal)) {
                cardNumberField.setText(sb.toString());
            }
        });
        
        // Restrict CVV to 3-4 digits
        cardCvvField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("\\D", "");
            if (filtered.length() > 4) {
                filtered = filtered.substring(0, 4);
            }
            if (!filtered.equals(newVal)) {
                cardCvvField.setText(filtered);
            }
        });
        
        // Restrict month/year fields
        cardExpiryMonthField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("\\D", "");
            if (filtered.length() > 2) {
                filtered = filtered.substring(0, 2);
            }
            if (!filtered.equals(newVal)) {
                cardExpiryMonthField.setText(filtered);
            }
        });
        
        cardExpiryYearField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtered = newVal.replaceAll("\\D", "");
            if (filtered.length() > 4) {
                filtered = filtered.substring(0, 4);
            }
            if (!filtered.equals(newVal)) {
                cardExpiryYearField.setText(filtered);
            }
        });
    }

    /**
     * Setup validation listeners
     */
    private void setupValidationListeners() {
        // Card payment validation
        cardNumberField.textProperty().addListener((obs, oldVal, newVal) -> validatePaymentForm());
        cardHolderNameField.textProperty().addListener((obs, oldVal, newVal) -> validatePaymentForm());
        cardExpiryMonthField.textProperty().addListener((obs, oldVal, newVal) -> validatePaymentForm());
        cardExpiryYearField.textProperty().addListener((obs, oldVal, newVal) -> validatePaymentForm());
        cardCvvField.textProperty().addListener((obs, oldVal, newVal) -> validatePaymentForm());
    }

    /**
     * Load order data (mock data for now)
     */
    private void loadOrderData() {
        // Mock order data (in real app, get from previous screen or session)
        orderItems.clear();
        orderItems.add(new OrderItem("پیتزا مارگاریتا", 25000.0, 2, "رستوران ایتالیایی"));
        orderItems.add(new OrderItem("برگر کلاسیک", 18000.0, 1, "فست فود سارا"));
        
        // Calculate totals
        subtotal = orderItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        deliveryFee = subtotal >= 50000 ? 0.0 : 5000.0;
        discount = 5000.0; // Mock discount
        
        // Display order items
        displayOrderItems();
        updatePricingDisplay();
    }

    /**
     * Load delivery information (mock data)
     */
    private void loadDeliveryInfo() {
        // Mock delivery info (in real app, get from cart screen)
        deliveryAddressDisplay.setText("تهران، خیابان ولیعصر، پلاک 123");
        deliveryPhoneDisplay.setText("09123456789");
        orderNotesDisplay.setText("لطفاً زنگ در طبقه دوم را بزنید");
    }

    /**
     * Display order items in the summary
     */
    private void displayOrderItems() {
        orderItemsContainer.getChildren().clear();
        
        for (OrderItem item : orderItems) {
            VBox itemBox = createOrderItemDisplay(item);
            orderItemsContainer.getChildren().add(itemBox);
        }
    }

    /**
     * Create display for a single order item
     */
    private VBox createOrderItemDisplay(OrderItem item) {
        VBox itemBox = new VBox(5);
        itemBox.setPadding(new Insets(8));
        itemBox.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
        
        // Item name and restaurant
        Label nameLabel = new Label(item.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label restaurantLabel = new Label(item.getRestaurantName());
        restaurantLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12;");
        
        // Quantity and price
        HBox detailsBox = new HBox(10);
        detailsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label quantityLabel = new Label("تعداد: " + item.getQuantity());
        Label priceLabel = new Label(formatCurrency(item.getPrice() * item.getQuantity()) + " تومان");
        priceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        
        detailsBox.getChildren().addAll(quantityLabel, priceLabel);
        
        itemBox.getChildren().addAll(nameLabel, restaurantLabel, detailsBox);
        
        return itemBox;
    }

    /**
     * Update pricing display
     */
    private void updatePricingDisplay() {
        subtotalLabel.setText(formatCurrency(subtotal) + " تومان");
        deliveryFeeLabel.setText(deliveryFee == 0 ? "رایگان" : formatCurrency(deliveryFee) + " تومان");
        discountLabel.setText(formatCurrency(discount) + " تومان");
        
        double total = subtotal + deliveryFee - discount;
        totalAmountLabel.setText(formatCurrency(total) + " تومان");
    }

    /**
     * Update payment section visibility based on selected method
     */
    private void updatePaymentSectionVisibility() {
        // Hide all sections first
        cardPaymentSection.setVisible(false);
        walletPaymentSection.setVisible(false);
        codPaymentSection.setVisible(true); // Default visible
        
        RadioButton selected = (RadioButton) paymentMethodGroup.getSelectedToggle();
        if (selected != null) {
            if (selected == cardPaymentRadio) {
                cardPaymentSection.setVisible(true);
                codPaymentSection.setVisible(false);
            } else if (selected == walletPaymentRadio) {
                walletPaymentSection.setVisible(true);
                codPaymentSection.setVisible(false);
                updateWalletDisplay();
            }
            // COD is visible by default
        }
    }

    /**
     * Update wallet display
     */
    private void updateWalletDisplay() {
        walletBalanceLabel.setText(formatCurrency(walletBalance) + " تومان");
        
        double total = subtotal + deliveryFee - discount;
        if (walletBalance >= total) {
            walletStatusLabel.setText("موجودی کافی برای پرداخت");
            walletStatusLabel.setStyle("-fx-text-fill: #28a745;");
        } else {
            walletStatusLabel.setText("موجودی ناکافی - نیاز به شارژ: " + 
                                    formatCurrency(total - walletBalance) + " تومان");
            walletStatusLabel.setStyle("-fx-text-fill: #dc3545;");
        }
    }

    /**
     * Validate payment form based on selected method
     */
    private void validatePaymentForm() {
        boolean isValid = false;
        
        RadioButton selected = (RadioButton) paymentMethodGroup.getSelectedToggle();
        if (selected != null) {
            if (selected == cardPaymentRadio) {
                isValid = validateCardPayment();
            } else if (selected == walletPaymentRadio) {
                isValid = validateWalletPayment();
            } else if (selected == codPaymentRadio) {
                isValid = true; // COD is always valid
            }
        }
        
        confirmPaymentButton.setDisable(!isValid);
    }

    /**
     * Validate card payment form
     */
    private boolean validateCardPayment() {
        String cardNumber = cardNumberField.getText().replaceAll("-", "");
        String holderName = cardHolderNameField.getText().trim();
        String month = cardExpiryMonthField.getText().trim();
        String year = cardExpiryYearField.getText().trim();
        String cvv = cardCvvField.getText().trim();
        
        return cardNumber.length() == 16 && 
               !holderName.isEmpty() && 
               month.length() == 2 && 
               year.length() == 4 && 
               cvv.length() >= 3;
    }

    /**
     * Validate wallet payment
     */
    private boolean validateWalletPayment() {
        double total = subtotal + deliveryFee - discount;
        return walletBalance >= total;
    }

    /**
     * Format currency value
     */
    private String formatCurrency(double amount) {
        return currencyFormat.format(amount);
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Handle refresh wallet balance
     */
    @FXML
    private void handleRefreshWallet() {
        setLoading(true);
        setStatus("در حال بروزرسانی موجودی کیف پول...");
        
        // Mock refresh operation
        Task<Double> refreshTask = new Task<Double>() {
            @Override
            protected Double call() throws Exception {
                Thread.sleep(1500); // Simulate API call
                return 85000.0; // Mock updated balance
            }
        };
        
        refreshTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false);
            walletBalance = refreshTask.getValue();
            updateWalletDisplay();
            validatePaymentForm();
            setStatus("موجودی کیف پول بروزرسانی شد");
        }));
        
        refreshTask.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            setStatus("خطا در بروزرسانی موجودی");
        }));
        
        new Thread(refreshTask).start();
    }

    /**
     * Handle confirm payment
     */
    @FXML
    private void handleConfirmPayment() {
        RadioButton selected = (RadioButton) paymentMethodGroup.getSelectedToggle();
        if (selected == null) {
            showError("خطا", "لطفاً روش پرداخت را انتخاب کنید");
            return;
        }
        
        setLoading(true);
        setStatus("در حال پردازش پرداخت...");
        
        Task<String> paymentTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                Thread.sleep(3000); // Simulate payment processing
                
                if (selected == cardPaymentRadio) {
                    return processCardPayment();
                } else if (selected == walletPaymentRadio) {
                    return processWalletPayment();
                } else {
                    return processCODPayment();
                }
            }
        };
        
        paymentTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false);
            String result = paymentTask.getValue();
            setStatus("پرداخت موفق");
            
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("پرداخت موفق");
            successAlert.setHeaderText("سفارش شما با موفقیت ثبت شد!");
            successAlert.setContentText(result);
            
            successAlert.showAndWait().ifPresent(response -> {
                // Navigate to order history or main page
                navigationController.navigateTo(NavigationController.ORDER_HISTORY_SCENE);
            });
        }));
        
        paymentTask.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            setStatus("خطا در پرداخت");
            showError("خطا در پرداخت", paymentTask.getException().getMessage());
        }));
        
        new Thread(paymentTask).start();
    }

    /**
     * Process card payment
     */
    private String processCardPayment() throws Exception {
        // Mock card payment processing
        String cardNumber = cardNumberField.getText();
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        
        // Simulate potential failure
        if (cardNumber.contains("0000")) {
            throw new Exception("کارت بانکی نامعتبر است");
        }
        
        return "پرداخت با کارت ****" + lastFourDigits + " انجام شد.\n" +
               "شماره پیگیری: " + generateTrackingNumber() + "\n" +
               "سفارش شما تا 45 دقیقه آینده تحویل داده خواهد شد.";
    }

    /**
     * Process wallet payment
     */
    private String processWalletPayment() throws Exception {
        double total = subtotal + deliveryFee - discount;
        
        if (walletBalance < total) {
            throw new Exception("موجودی کیف پول کافی نیست");
        }
        
        // Deduct from wallet balance
        walletBalance -= total;
        
        return "پرداخت از کیف پول انجام شد.\n" +
               "مبلغ پرداخت شده: " + formatCurrency(total) + " تومان\n" +
               "موجودی باقی‌مانده: " + formatCurrency(walletBalance) + " تومان\n" +
               "شماره پیگیری: " + generateTrackingNumber();
    }

    /**
     * Process COD payment
     */
    private String processCODPayment() {
        double total = subtotal + deliveryFee - discount;
        
        return "سفارش شما ثبت شد و پرداخت در محل انجام خواهد شد.\n" +
               "مبلغ قابل پرداخت: " + formatCurrency(total) + " تومان\n" +
               "شماره سفارش: " + generateTrackingNumber() + "\n" +
               "لطفاً مبلغ دقیق آماده داشته باشید.";
    }

    /**
     * Generate random tracking number
     */
    private String generateTrackingNumber() {
        return "FO" + System.currentTimeMillis() % 1000000;
    }

    /**
     * Handle back to cart
     */
    @FXML
    private void handleBackToCart() {
        navigationController.navigateTo(NavigationController.CART_SCENE);
    }

    /**
     * Handle profile navigation
     */
    @FXML
    private void handleProfile() {
        navigationController.navigateTo(NavigationController.PROFILE_SCENE);
    }

    /**
     * Handle order history navigation
     */
    @FXML
    private void handleOrderHistory() {
        navigationController.navigateTo(NavigationController.ORDER_HISTORY_SCENE);
    }

    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأیید خروج");
        confirmAlert.setHeaderText("آیا مطمئن هستید؟");
        confirmAlert.setContentText("آیا می‌خواهید از حساب کاربری خود خارج شوید؟");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                navigationController.logout();
            }
        });
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Set loading state
     */
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        confirmPaymentButton.setDisable(loading);
        refreshWalletButton.setDisable(loading);
    }

    /**
     * Set status message
     */
    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== DATA MODEL ====================

    /**
     * Order item data model
     */
    public static class OrderItem {
        private String name;
        private double price;
        private int quantity;
        private String restaurantName;

        public OrderItem(String name, double price, int quantity, String restaurantName) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.restaurantName = restaurantName;
        }

        // Getters
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public String getRestaurantName() { return restaurantName; }
    }
}
