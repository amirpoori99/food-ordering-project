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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Order History screen
 * Displays order history, filtering, and order details
 */
public class OrderHistoryController implements Initializable {

    // Header Components
    @FXML private Label orderCountLabel;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    
    // Filter Components
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> dateFilterCombo;
    @FXML private Button clearFiltersButton;
    
    // Order List
    @FXML private VBox orderListContainer;
    @FXML private Label emptyOrdersLabel;
    
    // Order Details Panel
    @FXML private VBox orderDetailsContainer;
    @FXML private VBox noSelectionContainer;
    
    // Order Detail Labels
    @FXML private Label orderNumberLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label orderStatusLabel;
    @FXML private Label restaurantNameLabel;
    @FXML private Label totalAmountLabel;
    
    @FXML private VBox orderItemsContainer;
    
    @FXML private Label deliveryAddressLabel;
    @FXML private Label deliveryPhoneLabel;
    @FXML private Label deliveryTimeLabel;
    
    @FXML private Label paymentMethodLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private Label trackingNumberLabel;
    
    // Action Buttons
    @FXML private Button reorderButton;
    @FXML private Button trackOrderButton;
    @FXML private Button cancelOrderButton;
    @FXML private Button downloadReceiptButton;
    
    // Menu Items
    @FXML private MenuItem backMenuItem;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem cartMenuItem;
    @FXML private MenuItem logoutMenuItem;
    
    @FXML private MenuItem allOrdersMenuItem;
    @FXML private MenuItem pendingOrdersMenuItem;
    @FXML private MenuItem completedOrdersMenuItem;
    @FXML private MenuItem cancelledOrdersMenuItem;
    
    // Status Components
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private NavigationController navigationController;
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("fa", "IR"));
    
    // Data
    private List<OrderHistory> allOrders = new ArrayList<>();
    private List<OrderHistory> filteredOrders = new ArrayList<>();
    private OrderHistory selectedOrder = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        
        setupUI();
        loadOrderHistory();
    }

    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Initialize currency format
        currencyFormat.setGroupingUsed(true);
        
        // Setup filter combos
        setupFilterCombos();
        
        // Setup search functionality
        setupSearchFunctionality();
        
        // Initial status
        setStatus("در حال بارگذاری تاریخچه سفارش‌ها...");
        
        // Initially show no selection
        showNoSelection();
    }

    /**
     * Setup filter combo boxes
     */
    private void setupFilterCombos() {
        // Status filter
        statusFilterCombo.getItems().addAll(
            "همه وضعیت‌ها",
            "در انتظار تأیید", 
            "در حال آماده‌سازی", 
            "آماده ارسال", 
            "در حال ارسال", 
            "تحویل داده شده", 
            "لغو شده"
        );
        statusFilterCombo.setValue("همه وضعیت‌ها");
        
        // Date filter
        dateFilterCombo.getItems().addAll(
            "همه تاریخ‌ها",
            "امروز",
            "هفته گذشته", 
            "ماه گذشته",
            "3 ماه گذشته"
        );
        dateFilterCombo.setValue("همه تاریخ‌ها");
    }

    /**
     * Setup search functionality
     */
    private void setupSearchFunctionality() {
        searchField.setOnAction(e -> handleSearch());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                applyFilters();
            }
        });
    }

    /**
     * Load order history from backend
     */
    private void loadOrderHistory() {
        setLoading(true);
        
        Task<List<OrderHistory>> loadTask = new Task<List<OrderHistory>>() {
            @Override
            protected List<OrderHistory> call() throws Exception {
                Thread.sleep(1500); // Simulate loading
                
                // Mock order data
                List<OrderHistory> mockOrders = new ArrayList<>();
                
                mockOrders.add(new OrderHistory(
                    "FO123456", 
                    LocalDateTime.now().minusDays(1),
                    "تحویل داده شده",
                    "رستوران ایتالیایی",
                    68000.0,
                    Arrays.asList(
                        new OrderItem("پیتزا مارگاریتا", 25000.0, 2),
                        new OrderItem("نوشابه", 9000.0, 2)
                    ),
                    "تهران، خیابان ولیعصر، پلاک 123",
                    "09123456789",
                    LocalDateTime.now().minusDays(1).plusHours(1),
                    "پرداخت با کارت",
                    "پرداخت شده",
                    "PAY789123"
                ));
                
                mockOrders.add(new OrderHistory(
                    "FO123457",
                    LocalDateTime.now().minusDays(3),
                    "در حال ارسال",
                    "فست فود سارا",
                    45000.0,
                    Arrays.asList(
                        new OrderItem("برگر کلاسیک", 18000.0, 1),
                        new OrderItem("سیب زمینی", 12000.0, 1),
                        new OrderItem("نوشابه", 9000.0, 1)
                    ),
                    "تهران، میدان آزادی، کوچه گل‌ها",
                    "09987654321",
                    LocalDateTime.now().plusMinutes(30),
                    "پرداخت در محل",
                    "در انتظار",
                    "COD456789"
                ));
                
                mockOrders.add(new OrderHistory(
                    "FO123458",
                    LocalDateTime.now().minusDays(7),
                    "لغو شده",
                    "رستوران سنتی",
                    85000.0,
                    Arrays.asList(
                        new OrderItem("چلو کباب کوبیده", 35000.0, 1),
                        new OrderItem("چلو کباب برگ", 45000.0, 1)
                    ),
                    "تهران، خیابان کریمخان، پلاک 45",
                    "09334455666",
                    null,
                    "کیف پول",
                    "لغو شده",
                    "CANCEL123"
                ));
                
                return mockOrders;
            }
        };
        
        loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false);
            allOrders = loadTask.getValue();
            applyFilters();
            updateOrderCount();
            setStatus("تاریخچه سفارش‌ها بارگذاری شد");
        }));
        
        loadTask.setOnFailed(e -> Platform.runLater(() -> {
            setLoading(false);
            setStatus("خطا در بارگذاری تاریخچه سفارش‌ها");
            showError("خطا", "خطا در بارگذاری تاریخچه سفارش‌ها");
        }));
        
        new Thread(loadTask).start();
    }

    /**
     * Apply current filters to order list
     */
    private void applyFilters() {
        filteredOrders = allOrders.stream()
            .filter(this::matchesStatusFilter)
            .filter(this::matchesDateFilter)
            .filter(this::matchesSearchFilter)
            .collect(Collectors.toList());
        
        displayOrders();
        updateOrderCount();
    }

    /**
     * Check if order matches status filter
     */
    private boolean matchesStatusFilter(OrderHistory order) {
        String selectedStatus = statusFilterCombo.getValue();
        return selectedStatus == null || 
               selectedStatus.equals("همه وضعیت‌ها") || 
               order.getStatus().equals(selectedStatus);
    }

    /**
     * Check if order matches date filter
     */
    private boolean matchesDateFilter(OrderHistory order) {
        String selectedDate = dateFilterCombo.getValue();
        if (selectedDate == null || selectedDate.equals("همه تاریخ‌ها")) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime orderDate = order.getOrderDate();
        
        switch (selectedDate) {
            case "امروز":
                return orderDate.toLocalDate().equals(now.toLocalDate());
            case "هفته گذشته":
                return orderDate.isAfter(now.minusWeeks(1));
            case "ماه گذشته":
                return orderDate.isAfter(now.minusMonths(1));
            case "3 ماه گذشته":
                return orderDate.isAfter(now.minusMonths(3));
            default:
                return true;
        }
    }

    /**
     * Check if order matches search filter
     */
    private boolean matchesSearchFilter(OrderHistory order) {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            return true;
        }
        
        return order.getOrderNumber().toLowerCase().contains(searchText) ||
               order.getRestaurantName().toLowerCase().contains(searchText) ||
               order.getStatus().toLowerCase().contains(searchText);
    }

    /**
     * Display filtered orders
     */
    private void displayOrders() {
        orderListContainer.getChildren().clear();
        
        if (filteredOrders.isEmpty()) {
            emptyOrdersLabel.setVisible(true);
            return;
        }
        
        emptyOrdersLabel.setVisible(false);
        
        for (OrderHistory order : filteredOrders) {
            VBox orderBox = createOrderListItem(order);
            orderListContainer.getChildren().add(orderBox);
        }
    }

    /**
     * Create UI for single order list item
     */
    private VBox createOrderListItem(OrderHistory order) {
        VBox orderBox = new VBox(8);
        orderBox.setPadding(new Insets(15));
        orderBox.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; " +
                         "-fx-border-radius: 5; -fx-background-radius: 5; " +
                         "-fx-cursor: hand;");
        
        // Order header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label orderNumberLabel = new Label("#" + order.getOrderNumber());
        orderNumberLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label statusLabel = new Label(order.getStatus());
        statusLabel.setStyle("-fx-background-color: " + getStatusColor(order.getStatus()) + 
                           "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10;");
        
        headerBox.getChildren().addAll(orderNumberLabel, statusLabel);
        
        // Restaurant and date
        Label restaurantLabel = new Label(order.getRestaurantName());
        restaurantLabel.setStyle("-fx-text-fill: #495057; -fx-font-size: 13;");
        
        Label dateLabel = new Label(formatDateTime(order.getOrderDate()));
        dateLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12;");
        
        // Amount
        Label amountLabel = new Label(formatCurrency(order.getTotalAmount()) + " تومان");
        amountLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        
        orderBox.getChildren().addAll(headerBox, restaurantLabel, dateLabel, amountLabel);
        
        // Click handler
        orderBox.setOnMouseClicked(e -> selectOrder(order));
        
        // Hover effect
        orderBox.setOnMouseEntered(e -> 
            orderBox.setStyle(orderBox.getStyle() + "-fx-background-color: #f8f9fa;"));
        orderBox.setOnMouseExited(e -> 
            orderBox.setStyle(orderBox.getStyle().replace("-fx-background-color: #f8f9fa;", 
                                                         "-fx-background-color: white;")));
        
        return orderBox;
    }

    /**
     * Select and display order details
     */
    private void selectOrder(OrderHistory order) {
        selectedOrder = order;
        displayOrderDetails(order);
        showOrderDetails();
    }

    /**
     * Display order details in detail panel
     */
    private void displayOrderDetails(OrderHistory order) {
        // Basic info
        orderNumberLabel.setText(order.getOrderNumber());
        orderDateLabel.setText(formatDateTime(order.getOrderDate()));
        orderStatusLabel.setText(order.getStatus());
        orderStatusLabel.setStyle("-fx-text-fill: " + getStatusColor(order.getStatus()) + ";");
        restaurantNameLabel.setText(order.getRestaurantName());
        totalAmountLabel.setText(formatCurrency(order.getTotalAmount()) + " تومان");
        
        // Order items
        displayOrderItems(order.getItems());
        
        // Delivery info
        deliveryAddressLabel.setText(order.getDeliveryAddress());
        deliveryPhoneLabel.setText(order.getDeliveryPhone());
        deliveryTimeLabel.setText(order.getDeliveryTime() != null ? 
                                  formatDateTime(order.getDeliveryTime()) : "نامشخص");
        
        // Payment info
        paymentMethodLabel.setText(order.getPaymentMethod());
        paymentStatusLabel.setText(order.getPaymentStatus());
        paymentStatusLabel.setStyle("-fx-text-fill: " + getPaymentStatusColor(order.getPaymentStatus()) + ";");
        trackingNumberLabel.setText(order.getTrackingNumber());
        
        // Update action buttons
        updateActionButtons(order);
    }

    /**
     * Display order items in detail panel
     */
    private void displayOrderItems(List<OrderItem> items) {
        orderItemsContainer.getChildren().clear();
        
        for (OrderItem item : items) {
            HBox itemBox = new HBox(10);
            itemBox.setPadding(new Insets(5));
            itemBox.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
            itemBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label nameLabel = new Label(item.getName());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            Label quantityLabel = new Label("× " + item.getQuantity());
            quantityLabel.setStyle("-fx-text-fill: #6c757d;");
            
            Label priceLabel = new Label(formatCurrency(item.getPrice() * item.getQuantity()) + " تومان");
            priceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            
            itemBox.getChildren().addAll(nameLabel, quantityLabel, priceLabel);
            orderItemsContainer.getChildren().add(itemBox);
        }
    }

    /**
     * Update action buttons based on order status
     */
    private void updateActionButtons(OrderHistory order) {
        String status = order.getStatus();
        
        // Show/hide cancel button based on status
        cancelOrderButton.setVisible(
            status.equals("در انتظار تأیید") || 
            status.equals("در حال آماده‌سازی")
        );
        
        // Enable/disable buttons based on status
        reorderButton.setDisable(false); // Always enabled
        trackOrderButton.setDisable(status.equals("لغو شده"));
        downloadReceiptButton.setDisable(
            status.equals("لغو شده") || 
            !order.getPaymentStatus().equals("پرداخت شده")
        );
    }

    /**
     * Show order details panel
     */
    private void showOrderDetails() {
        noSelectionContainer.setVisible(false);
        orderDetailsContainer.setVisible(true);
    }

    /**
     * Show no selection message
     */
    private void showNoSelection() {
        orderDetailsContainer.setVisible(false);
        noSelectionContainer.setVisible(true);
    }

    /**
     * Update order count display
     */
    private void updateOrderCount() {
        orderCountLabel.setText("(" + filteredOrders.size() + " سفارش)");
    }

    /**
     * Get color for order status
     */
    private String getStatusColor(String status) {
        switch (status) {
            case "تحویل داده شده": return "#28a745";
            case "در حال ارسال": return "#17a2b8";
            case "آماده ارسال": return "#fd7e14";
            case "در حال آماده‌سازی": return "#ffc107";
            case "در انتظار تأیید": return "#6c757d";
            case "لغو شده": return "#dc3545";
            default: return "#6c757d";
        }
    }

    /**
     * Get color for payment status
     */
    private String getPaymentStatusColor(String status) {
        switch (status) {
            case "پرداخت شده": return "#28a745";
            case "در انتظار": return "#ffc107";
            case "لغو شده": return "#dc3545";
            default: return "#6c757d";
        }
    }

    /**
     * Format date time for display
     */
    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Format currency value
     */
    private String formatCurrency(double amount) {
        return currencyFormat.format(amount);
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Handle search
     */
    @FXML
    private void handleSearch() {
        applyFilters();
    }

    /**
     * Handle status filter change
     */
    @FXML
    private void handleStatusFilter() {
        applyFilters();
    }

    /**
     * Handle date filter change
     */
    @FXML
    private void handleDateFilter() {
        applyFilters();
    }

    /**
     * Handle clear filters
     */
    @FXML
    private void handleClearFilters() {
        statusFilterCombo.setValue("همه وضعیت‌ها");
        dateFilterCombo.setValue("همه تاریخ‌ها");
        searchField.clear();
        applyFilters();
    }

    /**
     * Handle show all orders
     */
    @FXML
    private void handleShowAllOrders() {
        statusFilterCombo.setValue("همه وضعیت‌ها");
        applyFilters();
    }

    /**
     * Handle show pending orders
     */
    @FXML
    private void handleShowPendingOrders() {
        statusFilterCombo.setValue("در انتظار تأیید");
        applyFilters();
    }

    /**
     * Handle show completed orders
     */
    @FXML
    private void handleShowCompletedOrders() {
        statusFilterCombo.setValue("تحویل داده شده");
        applyFilters();
    }

    /**
     * Handle show cancelled orders
     */
    @FXML
    private void handleShowCancelledOrders() {
        statusFilterCombo.setValue("لغو شده");
        applyFilters();
    }

    /**
     * Handle reorder
     */
    @FXML
    private void handleReorder() {
        if (selectedOrder == null) return;
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأیید سفارش مجدد");
        confirmAlert.setHeaderText("سفارش مجدد");
        confirmAlert.setContentText("آیا می‌خواهید این سفارش را دوباره ثبت کنید؟");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Navigate to cart with pre-filled items
                navigationController.navigateTo(NavigationController.CART_SCENE);
                showInfo("موفقیت", "آیتم‌های سفارش به سبد خرید اضافه شدند");
            }
        });
    }

    /**
     * Handle track order
     */
    @FXML
    private void handleTrackOrder() {
        if (selectedOrder == null) return;
        
        showInfo("پیگیری سفارش", 
                "شماره پیگیری: " + selectedOrder.getTrackingNumber() + "\n" +
                "وضعیت: " + selectedOrder.getStatus() + "\n" +
                "زمان تحویل: " + (selectedOrder.getDeliveryTime() != null ? 
                                 formatDateTime(selectedOrder.getDeliveryTime()) : "نامشخص"));
    }

    /**
     * Handle cancel order
     */
    @FXML
    private void handleCancelOrder() {
        if (selectedOrder == null) return;
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("تأیید لغو سفارش");
        confirmAlert.setHeaderText("لغو سفارش");
        confirmAlert.setContentText("آیا مطمئن هستید که می‌خواهید این سفارش را لغو کنید؟");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Call cancel order API
                selectedOrder.setStatus("لغو شده");
                selectedOrder.setPaymentStatus("لغو شده");
                displayOrderDetails(selectedOrder);
                displayOrders(); // Refresh list
                showInfo("موفقیت", "سفارش با موفقیت لغو شد");
            }
        });
    }

    /**
     * Handle download receipt
     */
    @FXML
    private void handleDownloadReceipt() {
        if (selectedOrder == null) return;
        
        showInfo("دانلود فاکتور", "فاکتور سفارش " + selectedOrder.getOrderNumber() + 
                                " در حال آماده‌سازی است و به زودی دانلود خواهد شد.");
    }

    /**
     * Handle refresh
     */
    @FXML
    private void handleRefresh() {
        loadOrderHistory();
    }

    /**
     * Handle back navigation
     */
    @FXML
    private void handleBack() {
        navigationController.navigateTo(NavigationController.RESTAURANT_LIST_SCENE);
    }

    /**
     * Handle profile navigation
     */
    @FXML
    private void handleProfile() {
        navigationController.navigateTo(NavigationController.PROFILE_SCENE);
    }

    /**
     * Handle cart navigation
     */
    @FXML
    private void handleCart() {
        navigationController.navigateTo(NavigationController.CART_SCENE);
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
        refreshButton.setDisable(loading);
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

    /**
     * Show info dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== DATA MODELS ====================

    /**
     * Order history data model
     */
    public static class OrderHistory {
        private String orderNumber;
        private LocalDateTime orderDate;
        private String status;
        private String restaurantName;
        private double totalAmount;
        private List<OrderItem> items;
        private String deliveryAddress;
        private String deliveryPhone;
        private LocalDateTime deliveryTime;
        private String paymentMethod;
        private String paymentStatus;
        private String trackingNumber;

        public OrderHistory(String orderNumber, LocalDateTime orderDate, String status, 
                          String restaurantName, double totalAmount, List<OrderItem> items,
                          String deliveryAddress, String deliveryPhone, LocalDateTime deliveryTime,
                          String paymentMethod, String paymentStatus, String trackingNumber) {
            this.orderNumber = orderNumber;
            this.orderDate = orderDate;
            this.status = status;
            this.restaurantName = restaurantName;
            this.totalAmount = totalAmount;
            this.items = items;
            this.deliveryAddress = deliveryAddress;
            this.deliveryPhone = deliveryPhone;
            this.deliveryTime = deliveryTime;
            this.paymentMethod = paymentMethod;
            this.paymentStatus = paymentStatus;
            this.trackingNumber = trackingNumber;
        }

        // Getters and setters
        public String getOrderNumber() { return orderNumber; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRestaurantName() { return restaurantName; }
        public double getTotalAmount() { return totalAmount; }
        public List<OrderItem> getItems() { return items; }
        public String getDeliveryAddress() { return deliveryAddress; }
        public String getDeliveryPhone() { return deliveryPhone; }
        public LocalDateTime getDeliveryTime() { return deliveryTime; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public String getTrackingNumber() { return trackingNumber; }
    }

    /**
     * Order item data model
     */
    public static class OrderItem {
        private String name;
        private double price;
        private int quantity;

        public OrderItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        // Getters
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }
}
