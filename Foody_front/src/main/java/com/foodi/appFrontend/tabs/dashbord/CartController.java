package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.FoodItem; // مدل FoodItem
import com.foodi.appFrontend.models.Coupon; // مدل Coupon
import com.foodi.appFrontend.utils.ApiClient; // برای فراخوانی API
import com.foodi.appFrontend.utils.AuthManager; // برای دریافت توکن
import com.foodi.appFrontend.utils.JsonUtil; // برای پردازش JSON
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog; // برای تغییر تعداد
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox; // برای دکمه‌های درون سلول
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal; // برای محاسبات دقیق قیمت
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer; // برای Callback

import javafx.scene.Node; // NEW: برای گرفتن Window

import java.util.ArrayList;


import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.foodi.appFrontend.models.Order; // NEW: مدل Order

public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @FXML private TableView<CartItemDisplay> cartItemsTable; // TableView برای نمایش آیتم‌های سبد خرید
    @FXML private TableColumn<CartItemDisplay, String> cartItemNameColumn;
    @FXML private TableColumn<CartItemDisplay, Integer> cartItemQuantityColumn;
    @FXML private TableColumn<CartItemDisplay, BigDecimal> cartItemPriceColumn; // استفاده از BigDecimal برای دقت قیمت
    @FXML private TableColumn<CartItemDisplay, BigDecimal> cartItemSubtotalColumn;
    @FXML private TableColumn<CartItemDisplay, Void> cartItemActionsColumn; // برای دکمه‌های حذف/ویرایش تعداد
    @FXML private Label totalPriceLabel; // لیبل برای نمایش قیمت کل
    @FXML private Label cartErrorMessageLabel; // لیبل برای نمایش پیام‌های خطا/وضعیت
    
    // New FXML elements for address and coupon
    @FXML private TextField deliveryAddressField;
    @FXML private TextField couponCodeField;
    @FXML private Label couponStatusLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label couponDiscountLabel;

    private Map<FoodItem, Integer> cartData; // سبد خرید (از RestaurantMenuController منتقل می‌شود)
    private String currentRestaurantId; // ID رستوران (برای ارسال در درخواست سفارش)
    private Consumer<Void> clearCartCallback; // Callback برای پاک کردن سبد خرید در RestaurantMenuController
    private Consumer<Order> openOrderDetailsCallback; // Callback برای باز کردن تب جزئیات سفارش
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    // Coupon and user related fields
    private Coupon appliedCoupon = null;
    private String userAddress = "";
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    // کلاس کمکی برای نمایش آیتم‌ها در TableView.
    // TableView نمی‌تواند مستقیماً Map<FoodItem, Integer> را نمایش دهد.
    // نیاز به یک کلاس مدل ساده‌تر برای نمایش داریم.
    public static class CartItemDisplay {
        public FoodItem foodItem;
        public int quantity;
        public BigDecimal subtotal;

        public CartItemDisplay(FoodItem foodItem, int quantity) {
            this.foodItem = foodItem;
            this.quantity = quantity;
            this.subtotal = BigDecimal.valueOf(foodItem.getPrice()).multiply(BigDecimal.valueOf(quantity));
        }

        public FoodItem getFoodItem() { return foodItem; }
        public String getItemName() { return foodItem.getName(); } // برای ستون Name
        public int getQuantity() { return quantity; } // برای ستون Quantity
        public BigDecimal getPrice() { return BigDecimal.valueOf(foodItem.getPrice()); } // برای ستون Price
        public BigDecimal getSubtotal() { return subtotal; } // برای ستون Subtotal
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.subtotal = BigDecimal.valueOf(foodItem.getPrice()).multiply(BigDecimal.valueOf(quantity));
        }
    }

    @FXML
    public void initialize() {
        logger.info("Initializing CartController");
        
        // Debug FXML field loading
        logger.info("FXML fields - subtotalLabel: {}, couponDiscountLabel: {}, totalPriceLabel: {}", 
                   subtotalLabel != null ? "loaded" : "NULL", 
                   couponDiscountLabel != null ? "loaded" : "NULL", 
                   totalPriceLabel != null ? "loaded" : "NULL");
        
        if (subtotalLabel == null) {
            logger.error("subtotalLabel is null - FXML loading issue!");
        }
        if (couponDiscountLabel == null) {
            logger.error("couponDiscountLabel is null - FXML loading issue!");
        }
        if (totalPriceLabel == null) {
            logger.error("totalPriceLabel is null - FXML loading issue!");
        }
        
        // پیکربندی ستون‌های جدول
        if (cartItemNameColumn != null) {
            cartItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        } else {
            logger.error("cartItemNameColumn is null");
        }
        
        if (cartItemQuantityColumn != null) {
            cartItemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        } else {
            logger.error("cartItemQuantityColumn is null");
        }
        
        if (cartItemPriceColumn != null) {
            cartItemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        } else {
            logger.error("cartItemPriceColumn is null");
        }
        
        if (cartItemSubtotalColumn != null) {
            cartItemSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        } else {
            logger.error("cartItemSubtotalColumn is null");
        }

        // پیکربندی ستون Actions با دکمه‌ها
        cartItemActionsColumn.setCellFactory(param -> new TableCell<CartItemDisplay, Void>() {
            private final Button deleteButton = new Button("Delete");
            private final Button editButton = new Button("Edit");
            private final HBox pane = new HBox(5, deleteButton, editButton);

            {
                deleteButton.setOnAction(event -> {
                    CartItemDisplay item = getTableView().getItems().get(getIndex());
                    handleRemoveItem(item.getFoodItem()); // فراخوانی متد حذف
                });
                editButton.setOnAction(event -> {
                    CartItemDisplay item = getTableView().getItems().get(getIndex());
                    handleEditQuantity(item.getFoodItem()); // فراخوانی متد ویرایش تعداد
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        // Load user address and set default delivery address
        loadUserAddress();
    }

    // Setter برای دریافت سبد خرید از کنترلر والد (RestaurantMenuController)
    public void setCart(Map<FoodItem, Integer> cart) {
        logger.info("setCart called with cart size: {}", cart != null ? cart.size() : "null");
        this.cartData = cart;
        populateCartTable(); // پر کردن جدول پس از دریافت سبد خرید
    }

    // Setter برای دریافت ID رستوران
    public void setCurrentRestaurantId(String restaurantId) {
        this.currentRestaurantId = restaurantId;
    }

    // Setter برای Callback جهت پاک کردن سبد خرید در کنترلر والد
    public void setClearCartCallback(Consumer<Void> callback) {
        this.clearCartCallback = callback;
    }

    // Setter برای Callback جهت باز کردن تب جزئیات سفارش
    public void setOpenOrderDetailsCallback(Consumer<Order> callback) {
        this.openOrderDetailsCallback = callback;
    }

    private void populateCartTable() {
        // Add null checks and debugging
        if (cartData == null) {
            logger.error("cartData is null");
            cartErrorMessageLabel.setText("Cart data is null");
            return;
        }
        
        if (cartItemsTable == null) {
            logger.error("cartItemsTable is null");
            cartErrorMessageLabel.setText("Cart table is null");
            return;
        }
        
        if (totalPriceLabel == null) {
            logger.error("totalPriceLabel is null");
            return;
        }
        
        logger.info("Populating cart table with {} items", cartData.size());
        
        // تبدیل Map<FoodItem, Integer> به ObservableList<CartItemDisplay>
        ObservableList<CartItemDisplay> displayItems = FXCollections.observableArrayList();
        BigDecimal total = BigDecimal.ZERO; //

        for (Map.Entry<FoodItem, Integer> entry : cartData.entrySet()) {
            CartItemDisplay displayItem = new CartItemDisplay(entry.getKey(), entry.getValue());
            displayItems.add(displayItem);
            total = total.add(displayItem.getSubtotal()); // محاسبه جمع کل
            logger.debug("Added item {} x{}", entry.getKey().getName(), entry.getValue());
        }

        cartItemsTable.setItems(displayItems);
        Platform.runLater(() -> {
            calculatePrices(); // Calculate prices including coupon discount
            logger.info("Cart table populated and prices calculated on UI thread");
        });
    }

    private void handleRemoveItem(FoodItem itemToRemove) {
        cartData.remove(itemToRemove); // حذف از Map اصلی
        populateCartTable(); // رفرش جدول
        cartErrorMessageLabel.setText(itemToRemove.getName() + " removed from cart.");
    }

    private void handleEditQuantity(FoodItem itemToEdit) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(cartData.get(itemToEdit)));
        dialog.setTitle("Edit Quantity");
        dialog.setHeaderText("Enter new quantity for " + itemToEdit.getName() + ":");
        dialog.setContentText("Quantity:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int newQuantity = Integer.parseInt(result.get().trim());
                if (newQuantity <= 0) {
                    cartErrorMessageLabel.setText("Quantity must be a positive number. Item not updated.");
                    return;
                }
                
                // Check if quantity exceeds available supply
                if (newQuantity > itemToEdit.getSupply()) {
                    cartErrorMessageLabel.setText("Quantity exceeds available supply. Available: " + itemToEdit.getSupply() + ". Item not updated.");
                    return;
                }
                
                cartData.put(itemToEdit, newQuantity); // به‌روزرسانی در Map اصلی
                populateCartTable(); // رفرش جدول
                cartErrorMessageLabel.setText(itemToEdit.getName() + " quantity updated to " + newQuantity + ".");
            } catch (NumberFormatException e) {
                cartErrorMessageLabel.setText("Invalid quantity. Please enter a valid number.");
            }
        } else {
            cartErrorMessageLabel.setText("Quantity edit cancelled.");
        }
    }


    @FXML
    private void handleClearCart(ActionEvent event) {
        cartData.clear(); // پاک کردن Map اصلی
        populateCartTable(); // رفرش جدول
        cartErrorMessageLabel.setText("Cart cleared.");
    }

    @FXML
    private void handlePlaceOrder(ActionEvent event) {
        if (cartData.isEmpty()) {
            cartErrorMessageLabel.setText("Your cart is empty. Cannot place an order.");
            return;
        }

        cartErrorMessageLabel.setText("Placing order..."); // برای نمایش در RestaurantMenuController

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> cartErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                // Prepare order items list for API
                List<Map<String, Integer>> itemsForOrder = new ArrayList<>();
                for (Map.Entry<FoodItem, Integer> entry : cartData.entrySet()) {
                    Map<String, Integer> item = new HashMap<>();
                    item.put("item_id", entry.getKey().getId()); //
                    item.put("quantity", entry.getValue());
                    itemsForOrder.add(item);
                }

                // Get delivery address from field
                String deliveryAddress = deliveryAddressField.getText().trim();
                if (deliveryAddress.isEmpty()) {
                    Platform.runLater(() -> cartErrorMessageLabel.setText("Please enter a delivery address."));
                    return;
                }

                // Construct request body for POST /orders
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("delivery_address", deliveryAddress);
                orderData.put("vendor_id", Integer.parseInt(currentRestaurantId)); // Restaurant ID is vendor_id in Order
                orderData.put("items", itemsForOrder);
                
                // Add coupon if applied
                if (appliedCoupon != null) {
                    orderData.put("coupon_id", appliedCoupon.getId());
                }

                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(orderData); //

                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/orders", jsonBody, token); //

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) { // API returns 200 OK on success
                            try {
                                Order order = JsonUtil.getObjectMapper().treeToValue(rootNode, Order.class);
                                cartErrorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Order placed successfully!");
                                if (openOrderDetailsCallback != null) {
                                    openOrderDetailsCallback.accept(order); // Notify parent to open order details
                                }
                                if (clearCartCallback != null) {
                                    clearCartCallback.accept(null); // Notify parent to clear cart
                                }
                                handleClose(null); // Close cart view
                            } catch (Exception e) {
                                cartErrorMessageLabel.setText("Error parsing order response: " + e.getMessage());
                                logger.error("Error parsing order response", e);
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            cartErrorMessageLabel.setText("Error placing order: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> cartErrorMessageLabel.setText("Failed to connect to server to place order."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    cartErrorMessageLabel.setText("An unexpected error occurred while placing order: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage;
        if (event != null && event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            // If event is null, find the stage through the scene
            stage = (Stage) cartItemsTable.getScene().getWindow();
        }
        stage.close();
        executorService.shutdown();
    }
    
    // Load user address from profile
    private void loadUserAddress() {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> cartErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/auth/profile", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                String address = rootNode.has("address") ? rootNode.get("address").asText() : "";
                                userAddress = address;
                                if (deliveryAddressField != null) {
                                    deliveryAddressField.setText(address);
                                }
                            } catch (Exception e) {
                                logger.error("Error parsing user profile", e);
                            }
                        } else {
                            logger.error("Failed to load user profile: {}", response.statusCode());
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    logger.error("Error loading user address", e);
                });
            }
        });
    }
    
    // Handle coupon application
    @FXML
    private void handleApplyCoupon(ActionEvent event) {
        String couponCode = couponCodeField.getText().trim();
        if (couponCode.isEmpty()) {
            couponStatusLabel.setText("Please enter a coupon code");
            return;
        }
        
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> cartErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/coupons?coupon_code=" + couponCode, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                Coupon coupon = JsonUtil.getObjectMapper().treeToValue(rootNode, Coupon.class);
                                applyCoupon(coupon);
                                couponStatusLabel.setText("Coupon applied successfully! Discount: " + 
                                    (coupon.getType().equals("fixed") ? "$" + coupon.getValue() : coupon.getValue() + "%"));
                            } catch (Exception e) {
                                couponStatusLabel.setText("Error processing coupon");
                                logger.error("Error processing coupon", e);
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "Invalid coupon code";
                            couponStatusLabel.setText(errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> couponStatusLabel.setText("Failed to connect to server"));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    couponStatusLabel.setText("Error applying coupon: " + e.getMessage());
                    logger.error("Error applying coupon", e);
                });
            }
        });
    }
    
    // Apply coupon and recalculate prices
    private void applyCoupon(Coupon coupon) {
        logger.info("Applying coupon: {} (Type: {}, Value: {}, MinPrice: {})", 
                   coupon.getCouponCode(), coupon.getType(), coupon.getValue(), coupon.getMinPrice());
        appliedCoupon = coupon;
        
        // Ensure UI updates happen on JavaFX Application Thread
        Platform.runLater(() -> {
            calculatePrices();
            logger.info("Coupon applied and prices recalculated on UI thread");
        });
    }
    
    // Calculate prices including coupon discount
    private void calculatePrices() {
        logger.info("Starting calculatePrices()");
        
        // Calculate subtotal from cart items
        subtotal = BigDecimal.ZERO;
        if (cartData != null) {
            for (Map.Entry<FoodItem, Integer> entry : cartData.entrySet()) {
                BigDecimal itemPrice = BigDecimal.valueOf(entry.getKey().getPrice());
                BigDecimal quantity = BigDecimal.valueOf(entry.getValue());
                subtotal = subtotal.add(itemPrice.multiply(quantity));
            }
        }
        
        // Calculate coupon discount
        couponDiscount = BigDecimal.ZERO;
        if (appliedCoupon != null) {
            logger.info("Processing coupon: type={}, value={}, minPrice={}", 
                       appliedCoupon.getType(), appliedCoupon.getValue(), appliedCoupon.getMinPrice());
            
            // Check if subtotal meets minimum price requirement
            if (subtotal.compareTo(BigDecimal.valueOf(appliedCoupon.getMinPrice())) >= 0) {
                String couponType = appliedCoupon.getType().toLowerCase();
                if ("fixed".equals(couponType)) {
                    couponDiscount = BigDecimal.valueOf(appliedCoupon.getValue());
                } else if ("percent".equals(couponType)) {
                    couponDiscount = subtotal.multiply(BigDecimal.valueOf(appliedCoupon.getValue() / 100.0));
                }
                else{
                    logger.error("Invalid coupon type: " + appliedCoupon.getType());
                }
            } else {
                logger.info("Subtotal {} does not meet minimum price requirement {}", 
                           subtotal, appliedCoupon.getMinPrice());
            }
        }
        
        // Calculate total
        BigDecimal total = subtotal.subtract(couponDiscount);
        
        logger.info("Price calculation - Subtotal: {}, Coupon Discount: {}, Total: {}", 
                   subtotal, couponDiscount, total);
        logger.info("Applied coupon: {}", appliedCoupon != null ? appliedCoupon.getCouponCode() : "null");
        
        // Update UI labels with detailed debugging
        Platform.runLater(() -> {
            logger.info("Updating UI labels on JavaFX thread");
            
            if (subtotalLabel != null) {
                String subtotalText = String.format("%.2f", subtotal);
                subtotalLabel.setText(subtotalText);
                logger.info("Updated subtotal label to: '{}'", subtotalText);
            } else {
                logger.error("subtotalLabel is null!");
            }
            
            if (couponDiscountLabel != null) {
                String discountText = String.format("%.2f", couponDiscount);
                couponDiscountLabel.setText(discountText);
                logger.info("Updated coupon discount label to: '{}'", discountText);
            } else {
                logger.error("couponDiscountLabel is null!");
            }
            
            if (totalPriceLabel != null) {
                String totalText = String.format("%.2f", total);
                totalPriceLabel.setText(totalText);
                logger.info("Updated total price label to: '{}'", totalText);
            } else {
                logger.error("totalPriceLabel is null!");
            }
            
            logger.info("UI label updates completed");
        });
    }
}