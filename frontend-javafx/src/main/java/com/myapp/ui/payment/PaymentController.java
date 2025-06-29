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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * کنترلر پردازش پرداخت و تکمیل سفارش
 * 
 * این کلاس مسئول:
 * - مدیریت روش‌های پرداخت مختلف (کارت، کیف پول، نقدی)
 * - پردازش امن اطلاعات پرداخت
 * - تأیید نهایی سفارش و ارسال به سرور
 * - نمایش وضعیت پرداخت و نتیجه تراکنش
 * - مدیریت کیف پول کاربر و اعتبار
 * - ایجاد و نمایش رسید پرداخت
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since فاز 25 - Payment Processing
 */
public class PaymentController implements Initializable {

    // ==================== UI Components - Order Summary ====================
    
    @FXML private VBox orderSummaryContainer;
    @FXML private Label orderNumberLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label restaurantNameLabel;
    @FXML private TableView<OrderItem> orderItemsTable;
    @FXML private TableColumn<OrderItem, String> itemNameColumn;
    @FXML private TableColumn<OrderItem, Integer> itemQuantityColumn;
    @FXML private TableColumn<OrderItem, Double> itemPriceColumn;
    @FXML private TableColumn<OrderItem, Double> itemTotalColumn;
    
    // ==================== UI Components - Payment Method ====================
    
    @FXML private VBox paymentMethodContainer;
    @FXML private ToggleGroup paymentMethodGroup;
    @FXML private RadioButton creditCardRadio;
    @FXML private RadioButton walletRadio;
    @FXML private RadioButton cashOnDeliveryRadio;
    
    // ==================== UI Components - Credit Card ====================
    
    @FXML private VBox creditCardContainer;
    @FXML private TextField cardNumberField;
    @FXML private TextField cardHolderField;
    @FXML private ComboBox<String> expiryMonthComboBox;
    @FXML private ComboBox<String> expiryYearComboBox;
    @FXML private PasswordField cvvField;
    @FXML private CheckBox saveCardCheckBox;
    @FXML private ComboBox<SavedCard> savedCardsComboBox;
    @FXML private Button addNewCardButton;
    
    // ==================== UI Components - Wallet ====================
    
    @FXML private VBox walletContainer;
    @FXML private Label walletBalanceLabel;
    @FXML private Label walletStatusLabel;
    @FXML private Button rechargeWalletButton;
    @FXML private ProgressBar walletUsageBar;
    @FXML private Label remainingBalanceLabel;
    
    // ==================== UI Components - Cash ====================
    
    @FXML private VBox cashContainer;
    @FXML private Label cashInstructionsLabel;
    @FXML private CheckBox exactChangeCheckBox;
    @FXML private TextField changeRequiredField;
    
    // ==================== UI Components - Payment Summary ====================
    
    @FXML private VBox paymentSummaryContainer;
    @FXML private Label subtotalAmountLabel;
    @FXML private Label taxAmountLabel;
    @FXML private Label deliveryFeeLabel;
    @FXML private Label discountAmountLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label processingFeeLabel;
    @FXML private Label finalAmountLabel;
    
    // ==================== UI Components - Actions ====================
    
    @FXML private Button backToCartButton;
    @FXML private Button confirmPaymentButton;
    @FXML private Button cancelOrderButton;
    @FXML private ProgressIndicator paymentProgressIndicator;
    @FXML private Label paymentStatusLabel;
    
    // ==================== Data Properties ====================
    
    private ObservableList<OrderItem> orderItems;
    private ObservableList<SavedCard> savedCards;
    private PaymentData paymentData;
    private OrderSummary orderSummary;
    private WalletInfo walletInfo;
    private String currentOrderId;
    
    // Constants
    private static final double CARD_PROCESSING_FEE = 0.029; // 2.9% برای کارت
    private static final double WALLET_PROCESSING_FEE = 0.0; // رایگان برای کیف پول
    private static final double CASH_PROCESSING_FEE = 0.0; // رایگان برای نقدی
    private static final double MIN_WALLET_BALANCE = 10000.0; // حداقل موجودی کیف پول

    private NavigationController navigationController;
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("fa", "IR"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        
        initializeData();
        
        setupOrderItemsTable();
        setupComboBoxes();
        setupPaymentMethods();
        setupEventHandlers();
        loadInitialData();
        setupValidation();
    }
    
    /**
     * مقداردهی اولیه داده‌ها
     */
    private void initializeData() {
        orderItems = FXCollections.observableArrayList();
        savedCards = FXCollections.observableArrayList();
        paymentData = new PaymentData();
        orderSummary = new OrderSummary();
        walletInfo = new WalletInfo();
        currentOrderId = generateOrderId();
        
        if (paymentProgressIndicator != null) {
            paymentProgressIndicator.setVisible(false);
        }
    }
    
    /**
     * تولید شماره سفارش یکتا
     */
    private String generateOrderId() {
        return "ORD-" + System.currentTimeMillis();
    }
    
    /**
     * تنظیم TableView آیتم‌های سفارش
     */
    private void setupOrderItemsTable() {
        if (orderItemsTable != null) {
            orderItemsTable.setItems(orderItems);
            
            if (itemNameColumn != null) {
                itemNameColumn.setCellValueFactory(cellData -> 
                    new SimpleStringProperty(cellData.getValue().getItemName()));
            }
            
            if (itemQuantityColumn != null) {
                itemQuantityColumn.setCellValueFactory(cellData -> 
                    cellData.getValue().quantityProperty().asObject());
                itemQuantityColumn.setCellFactory(col -> new TableCell<OrderItem, Integer>() {
                    @Override
                    protected void updateItem(Integer quantity, boolean empty) {
                        super.updateItem(quantity, empty);
                        if (empty || quantity == null) {
                            setText(null);
                        } else {
                            setText(quantity + " عدد");
                        }
                    }
                });
            }
            
            if (itemPriceColumn != null) {
                itemPriceColumn.setCellValueFactory(cellData -> 
                    cellData.getValue().priceProperty().asObject());
                itemPriceColumn.setCellFactory(col -> new TableCell<OrderItem, Double>() {
                    @Override
                    protected void updateItem(Double price, boolean empty) {
                        super.updateItem(price, empty);
                        if (empty || price == null) {
                            setText(null);
                        } else {
                            setText(String.format("%,.0f تومان", price));
                        }
                    }
                });
            }
            
            if (itemTotalColumn != null) {
                itemTotalColumn.setCellValueFactory(cellData -> 
                    new SimpleDoubleProperty(cellData.getValue().getTotalPrice()).asObject());
                itemTotalColumn.setCellFactory(col -> new TableCell<OrderItem, Double>() {
                    @Override
                    protected void updateItem(Double total, boolean empty) {
                        super.updateItem(total, empty);
                        if (empty || total == null) {
                            setText(null);
                        } else {
                            setText(String.format("%,.0f تومان", total));
                        }
                    }
                });
            }
        }
    }
    
    /**
     * تنظیم ComboBox ها
     */
    private void setupComboBoxes() {
        if (expiryMonthComboBox != null) {
            expiryMonthComboBox.setItems(FXCollections.observableArrayList(
                "01", "02", "03", "04", "05", "06", 
                "07", "08", "09", "10", "11", "12"
            ));
        }
        
        if (expiryYearComboBox != null) {
            ObservableList<String> years = FXCollections.observableArrayList();
            int currentYear = LocalDateTime.now().getYear();
            for (int i = 0; i < 10; i++) {
                years.add(String.valueOf(currentYear + i));
            }
            expiryYearComboBox.setItems(years);
        }
        
        if (savedCardsComboBox != null) {
            savedCardsComboBox.setItems(savedCards);
            savedCardsComboBox.setCellFactory(listView -> new ListCell<SavedCard>() {
                @Override
                protected void updateItem(SavedCard card, boolean empty) {
                    super.updateItem(card, empty);
                    if (empty || card == null) {
                        setText(null);
                    } else {
                        setText(card.getMaskedNumber() + " - " + card.getCardHolder());
                    }
                }
            });
            savedCardsComboBox.setButtonCell(new ListCell<SavedCard>() {
                @Override
                protected void updateItem(SavedCard card, boolean empty) {
                    super.updateItem(card, empty);
                    if (empty || card == null) {
                        setText("انتخاب کارت ذخیره شده");
                    } else {
                        setText(card.getMaskedNumber());
                    }
                }
            });
        }
    }
    
    /**
     * تنظیم روش‌های پرداخت
     */
    private void setupPaymentMethods() {
        if (paymentMethodGroup != null) {
            paymentMethodGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                updatePaymentMethodContainers();
                updatePaymentSummary();
            });
        }
        
        if (creditCardRadio != null) {
            creditCardRadio.setSelected(true);
            updatePaymentMethodContainers();
        }
    }
    
    /**
     * به‌روزرسانی نمایش کانتینرهای روش پرداخت
     */
    private void updatePaymentMethodContainers() {
        if (creditCardContainer != null) creditCardContainer.setVisible(false);
        if (walletContainer != null) walletContainer.setVisible(false);
        if (cashContainer != null) cashContainer.setVisible(false);
        
        if (paymentMethodGroup.getSelectedToggle() == creditCardRadio && creditCardContainer != null) {
            creditCardContainer.setVisible(true);
        } else if (paymentMethodGroup.getSelectedToggle() == walletRadio && walletContainer != null) {
            walletContainer.setVisible(true);
            updateWalletDisplay();
        } else if (paymentMethodGroup.getSelectedToggle() == cashOnDeliveryRadio && cashContainer != null) {
            cashContainer.setVisible(true);
        }
    }

    /**
     * تنظیم Event Handlers
     */
    private void setupEventHandlers() {
        if (backToCartButton != null) {
            backToCartButton.setOnAction(e -> backToCart());
        }
        
        if (confirmPaymentButton != null) {
            confirmPaymentButton.setOnAction(e -> processPayment());
        }
        
        if (cancelOrderButton != null) {
            cancelOrderButton.setOnAction(e -> cancelOrder());
        }
        
        if (rechargeWalletButton != null) {
            rechargeWalletButton.setOnAction(e -> showRechargeWalletDialog());
        }
        
        if (addNewCardButton != null) {
            addNewCardButton.setOnAction(e -> clearCardForm());
        }
        
        if (savedCardsComboBox != null) {
            savedCardsComboBox.setOnAction(e -> fillCardFromSaved());
        }
        
        if (cardNumberField != null) {
            cardNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
                formatCardNumber(newVal);
            });
        }
        
        if (cvvField != null) {
            cvvField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.length() > 4) {
                    cvvField.setText(oldVal);
                }
            });
        }
    }
    
    /**
     * تنظیم validation ها
     */
    private void setupValidation() {
        updateConfirmButtonState();
        
        if (cardNumberField != null) {
            cardNumberField.textProperty().addListener((obs, oldVal, newVal) -> updateConfirmButtonState());
        }
        if (cardHolderField != null) {
            cardHolderField.textProperty().addListener((obs, oldVal, newVal) -> updateConfirmButtonState());
        }
        if (cvvField != null) {
            cvvField.textProperty().addListener((obs, oldVal, newVal) -> updateConfirmButtonState());
        }
    }
    
    /**
     * بارگذاری اطلاعات اولیه
     */
    private void loadInitialData() {
        loadSavedCards();
        loadWalletInfo();
        loadOrderSummary();
        updateOrderDisplay();
        updatePaymentSummary();
    }
    
    // ==================== Payment Processing Methods ====================
    
    /**
     * پردازش پرداخت
     */
    private void processPayment() {
        if (!validatePaymentData()) {
            return;
        }
        
        showLoadingIndicator(true);
        paymentData.setPaymentMethod(getSelectedPaymentMethod());
        paymentData.setAmount(orderSummary.getFinalAmount());
        paymentData.setOrderId(currentOrderId);
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                
                boolean success = processPaymentWithProvider();
                
                javafx.application.Platform.runLater(() -> {
                    showLoadingIndicator(false);
                    if (success) {
                        showPaymentSuccess();
                    } else {
                        showPaymentError("پرداخت ناموفق بود. لطفاً دوباره تلاش کنید.");
                    }
                });
                
            } catch (InterruptedException e) {
                javafx.application.Platform.runLater(() -> {
                    showLoadingIndicator(false);
                    showPaymentError("خطا در پردازش پرداخت.");
                });
            }
        }).start();
    }
    
    /**
     * اعتبارسنجی داده‌های پرداخت
     */
    private boolean validatePaymentData() {
        PaymentMethod method = getSelectedPaymentMethod();
        
        switch (method) {
            case CREDIT_CARD:
                return validateCreditCardData();
            case WALLET:
                return validateWalletData();
            case CASH_ON_DELIVERY:
                return true;
            default:
                return false;
        }
    }

    /**
     * اعتبارسنجی داده‌های کارت اعتباری
     */
    private boolean validateCreditCardData() {
        if (cardNumberField.getText().trim().length() < 16) {
            showErrorMessage("شماره کارت نامعتبر است");
            return false;
        }
        
        if (cardHolderField.getText().trim().isEmpty()) {
            showErrorMessage("نام دارنده کارت الزامی است");
            return false;
        }
        
        if (expiryMonthComboBox.getValue() == null || expiryYearComboBox.getValue() == null) {
            showErrorMessage("تاریخ انقضا الزامی است");
            return false;
        }
        
        if (cvvField.getText().trim().length() < 3) {
            showErrorMessage("CVV نامعتبر است");
            return false;
        }
        
        return true;
    }
    
    /**
     * اعتبارسنجی داده‌های کیف پول
     */
    private boolean validateWalletData() {
        if (walletInfo.getBalance() < orderSummary.getFinalAmount()) {
            showErrorMessage("موجودی کیف پول کافی نیست");
            return false;
        }
        
        return true;
    }
    
    /**
     * دریافت روش پرداخت انتخاب شده
     */
    private PaymentMethod getSelectedPaymentMethod() {
        if (paymentMethodGroup.getSelectedToggle() == creditCardRadio) {
            return PaymentMethod.CREDIT_CARD;
        } else if (paymentMethodGroup.getSelectedToggle() == walletRadio) {
            return PaymentMethod.WALLET;
        } else {
            return PaymentMethod.CASH_ON_DELIVERY;
        }
    }
    
    /**
     * پردازش پرداخت با ارائه‌دهنده خدمات
     */
    private boolean processPaymentWithProvider() {
        PaymentMethod method = getSelectedPaymentMethod();
        
        switch (method) {
            case CREDIT_CARD:
                return processCreditCardPayment();
            case WALLET:
                return processWalletPayment();
            case CASH_ON_DELIVERY:
                return processCashPayment();
            default:
                return false;
        }
    }
    
    /**
     * پردازش پرداخت با کارت اعتباری
     */
    private boolean processCreditCardPayment() {
        paymentData.setTransactionId("TXN-" + System.currentTimeMillis());
        paymentData.setGatewayResponse("SUCCESS");
        
        if (saveCardCheckBox.isSelected()) {
            saveCurrentCard();
        }
        
        return true;
    }
    
    /**
     * پردازش پرداخت با کیف پول
     */
    private boolean processWalletPayment() {
        double newBalance = walletInfo.getBalance() - orderSummary.getFinalAmount();
        walletInfo.setBalance(newBalance);
        
        paymentData.setTransactionId("WALLET-" + System.currentTimeMillis());
        paymentData.setGatewayResponse("SUCCESS");
        
        return true;
    }
    
    /**
     * پردازش پرداخت نقدی
     */
    private boolean processCashPayment() {
        paymentData.setTransactionId("CASH-" + System.currentTimeMillis());
        paymentData.setGatewayResponse("PENDING");
        
        return true;
    }
    
    // ==================== Data Models ====================
    
    /**
     * مدل آیتم سفارش
     */
    public static class OrderItem {
        private String itemName;
        private Integer quantity;
        private Double price;
        private String specialInstructions;
        
        public OrderItem() {}
        
        public OrderItem(String itemName, Integer quantity, Double price) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
        }
        
        public javafx.beans.property.StringProperty itemNameProperty() {
            return new SimpleStringProperty(itemName);
        }
        
        public javafx.beans.property.IntegerProperty quantityProperty() {
            return new javafx.beans.property.SimpleIntegerProperty(quantity);
        }
        
        public javafx.beans.property.DoubleProperty priceProperty() {
            return new SimpleDoubleProperty(price);
        }
        
        public Double getTotalPrice() {
            return price != null && quantity != null ? price * quantity : 0.0;
        }
        
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public String getSpecialInstructions() { return specialInstructions; }
        public void setSpecialInstructions(String specialInstructions) { 
            this.specialInstructions = specialInstructions; 
        }
    }
    
    /**
     * enum روش‌های پرداخت
     */
    public enum PaymentMethod {
        CREDIT_CARD("کارت اعتباری"),
        WALLET("کیف پول"),
        CASH_ON_DELIVERY("پرداخت نقدی");
        
        private final String displayName;
        
        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * به‌روزرسانی وضعیت دکمه تأیید
     */
    private void updateConfirmButtonState() {
        if (confirmPaymentButton != null) {
            boolean isValid = validateCurrentPaymentMethod();
            confirmPaymentButton.setDisable(!isValid);
        }
    }
    
    /**
     * اعتبارسنجی روش پرداخت فعلی
     */
    private boolean validateCurrentPaymentMethod() {
        PaymentMethod method = getSelectedPaymentMethod();
        
        switch (method) {
            case CREDIT_CARD:
                return !cardNumberField.getText().trim().isEmpty() &&
                       !cardHolderField.getText().trim().isEmpty() &&
                       !cvvField.getText().trim().isEmpty();
            case WALLET:
                return walletInfo.getBalance() >= orderSummary.getFinalAmount();
            case CASH_ON_DELIVERY:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * نمایش/مخفی کردن loading indicator
     */
    private void showLoadingIndicator(boolean show) {
        if (paymentProgressIndicator != null) {
            paymentProgressIndicator.setVisible(show);
        }
        if (confirmPaymentButton != null) {
            confirmPaymentButton.setDisable(show);
        }
    }
    
    // ==================== Placeholder Methods ====================
    
    private void loadSavedCards() {
        // TODO: Load from backend
    }
    
    private void loadWalletInfo() {
        // TODO: Load from backend
        walletInfo.setBalance(150000.0);
    }
    
    private void loadOrderSummary() {
        // TODO: Load from previous cart data
    }
    
    private void updateOrderDisplay() {
        if (orderNumberLabel != null) {
            orderNumberLabel.setText(currentOrderId);
        }
        if (orderDateLabel != null) {
            orderDateLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        }
    }
    
    private void updatePaymentSummary() {
        // TODO: Calculate and update payment summary
    }
    
    private void updateWalletDisplay() {
        if (walletBalanceLabel != null) {
            walletBalanceLabel.setText(String.format("%,.0f تومان", walletInfo.getBalance()));
        }
    }
    
    private void formatCardNumber(String newVal) {
        // TODO: Format card number with spaces
    }
    
    private void fillCardFromSaved() {
        // TODO: Fill form from saved card
    }
    
    private void clearCardForm() {
        // TODO: Clear card form fields
    }
    
    private void saveCurrentCard() {
        // TODO: Save current card info
    }
    
    private void backToCart() {
        // TODO: Navigate back to cart
    }
    
    private void cancelOrder() {
        // TODO: Cancel order with confirmation
    }
    
    private void showRechargeWalletDialog() {
        // TODO: Show wallet recharge dialog
    }
    
    private void showPaymentSuccess() {
        // TODO: Show success message and navigate to order tracking
    }
    
    private void showPaymentError(String message) {
        // TODO: Show error message
    }
    
    private void showErrorMessage(String message) {
        // TODO: Show error message
    }
    
    /**
     * مدل خلاصه سفارش
     */
    public static class OrderSummary {
        private Double subtotal = 0.0;
        private Double tax = 0.0;
        private Double deliveryFee = 0.0;
        private Double discount = 0.0;
        private Double processingFee = 0.0;
        private Double finalAmount = 0.0;
        
        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
        
        public Double getTax() { return tax; }
        public void setTax(Double tax) { this.tax = tax; }
        
        public Double getDeliveryFee() { return deliveryFee; }
        public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
        
        public Double getDiscount() { return discount; }
        public void setDiscount(Double discount) { this.discount = discount; }
        
        public Double getProcessingFee() { return processingFee; }
        public void setProcessingFee(Double processingFee) { this.processingFee = processingFee; }
        
        public Double getFinalAmount() { return finalAmount; }
        public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }
    }
    
    /**
     * مدل اطلاعات کیف پول
     */
    public static class WalletInfo {
        private Double balance = 0.0;
        private String status = "ACTIVE";
        
        public Double getBalance() { return balance; }
        public void setBalance(Double balance) { this.balance = balance; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * مدل کارت ذخیره شده
     */
    public static class SavedCard {
        private String maskedNumber;
        private String cardHolder;
        private String expiryMonth;
        private String expiryYear;
        
        public String getMaskedNumber() { return maskedNumber; }
        public void setMaskedNumber(String maskedNumber) { this.maskedNumber = maskedNumber; }
        
        public String getCardHolder() { return cardHolder; }
        public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }
        
        public String getExpiryMonth() { return expiryMonth; }
        public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }
        
        public String getExpiryYear() { return expiryYear; }
        public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }
    }
    
    /**
     * مدل داده‌های پرداخت
     */
    public static class PaymentData {
        private PaymentMethod paymentMethod;
        private Double amount;
        private String orderId;
        private String transactionId;
        private String gatewayResponse;
        
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getGatewayResponse() { return gatewayResponse; }
        public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
    }
}
