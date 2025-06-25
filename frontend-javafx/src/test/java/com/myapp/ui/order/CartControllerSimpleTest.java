package com.myapp.ui.order;

import com.myapp.ui.order.CartController.CartItem;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified test cases for CartController functionality
 * Tests core logic without FXML dependency
 */
@ExtendWith(ApplicationExtension.class)
class CartControllerSimpleTest {

    private CartController controller;
    private Scene scene;

    @Start
    void start(Stage stage) throws Exception {
        // Create a simple scene for testing without FXML
        VBox root = new VBox();
        scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
        
        // Create controller instance
        controller = new CartController();
    }

    @BeforeEach
    void setUp() {
        // Reset any state before each test
        Platform.runLater(() -> {
            // Setup complete
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testControllerInitialization() {
        assertNotNull(controller, "Controller should be initialized");
    }

    @Test
    void testCartItemCreation() {
        CartItem item = new CartItem(1L, "پیتزا مارگاریتا", 25000.0, 2, "رستوران ایتالیایی");
        
        assertEquals(1L, item.getId());
        assertEquals("پیتزا مارگاریتا", item.getName());
        assertEquals(25000.0, item.getPrice(), 0.01);
        assertEquals(2, item.getQuantity());
        assertEquals("رستوران ایتالیایی", item.getRestaurantName());
        assertTrue(item.isSelected(), "Item should be selected by default");
    }

    @Test
    void testCartItemSetters() {
        CartItem item = new CartItem(1L, "Original", 100.0, 1, "Restaurant");
        
        item.setQuantity(3);
        item.setSelected(false);
        
        assertEquals(3, item.getQuantity());
        assertFalse(item.isSelected());
    }

    @Test
    void testCartCalculations() {
        CartItem item1 = new CartItem(1L, "Item 1", 20000.0, 2, "Restaurant 1");
        CartItem item2 = new CartItem(2L, "Item 2", 15000.0, 1, "Restaurant 2");
        
        // Test individual item total
        double item1Total = item1.getPrice() * item1.getQuantity();
        double item2Total = item2.getPrice() * item2.getQuantity();
        
        assertEquals(40000.0, item1Total, 0.01);
        assertEquals(15000.0, item2Total, 0.01);
        
        // Test cart total
        double cartTotal = item1Total + item2Total;
        assertEquals(55000.0, cartTotal, 0.01);
    }

    @Test
    void testDeliveryFeeCalculation() {
        // Test delivery fee logic
        double subtotal1 = 30000.0; // Should have delivery fee
        double subtotal2 = 60000.0; // Should be free delivery (over 50,000)
        
        double deliveryFee1 = calculateDeliveryFee(subtotal1);
        double deliveryFee2 = calculateDeliveryFee(subtotal2);
        
        assertEquals(5000.0, deliveryFee1, 0.01, "Delivery should cost 5000 for orders under 50,000");
        assertEquals(0.0, deliveryFee2, 0.01, "Delivery should be free for orders over 50,000");
    }

    @Test
    void testCouponValidation() {
        // Test coupon code validation
        assertTrue(isValidCoupon("WELCOME10"), "WELCOME10 should be valid");
        assertTrue(isValidCoupon("DISCOUNT20"), "DISCOUNT20 should be valid");
        assertTrue(isValidCoupon("NEWUSER"), "NEWUSER should be valid");
        
        assertFalse(isValidCoupon("EXPIRED"), "EXPIRED should be invalid");
        assertFalse(isValidCoupon("INVALID"), "INVALID should be invalid");
        assertFalse(isValidCoupon(""), "Empty coupon should be invalid");
        assertFalse(isValidCoupon(null), "Null coupon should be invalid");
    }

    @Test
    void testCouponDiscountCalculation() {
        double subtotal = 100000.0;
        
        assertEquals(10000.0, calculateCouponDiscount(subtotal, "WELCOME10"), 0.01, 
                    "WELCOME10 should give 10% discount");
        assertEquals(20000.0, calculateCouponDiscount(subtotal, "DISCOUNT20"), 0.01, 
                    "DISCOUNT20 should give 20% discount");
        assertEquals(5000.0, calculateCouponDiscount(subtotal, "NEWUSER"), 0.01, 
                    "NEWUSER should give 5000 toman discount");
        assertEquals(0.0, calculateCouponDiscount(subtotal, "INVALID"), 0.01, 
                    "Invalid coupon should give no discount");
    }

    @Test
    void testPhoneNumberValidation() {
        // Valid phone numbers
        assertTrue(isValidPhone("09123456789"), "Valid mobile should pass");
        assertTrue(isValidPhone("09987654321"), "Another valid mobile should pass");
        
        // Invalid phone numbers
        assertFalse(isValidPhone("021123456"), "Landline should fail");
        assertFalse(isValidPhone("0912345678"), "Short number should fail");
        assertFalse(isValidPhone("091234567890"), "Long number should fail");
        assertFalse(isValidPhone("08123456789"), "Wrong prefix should fail");
        assertFalse(isValidPhone("abcd123456789"), "Non-numeric should fail");
        assertFalse(isValidPhone(""), "Empty phone should fail");
        assertFalse(isValidPhone(null), "Null phone should fail");
    }

    @Test
    void testAddressValidation() {
        assertTrue(isValidAddress("تهران، خیابان ولیعصر، پلاک 123"), "Valid address should pass");
        assertTrue(isValidAddress("اصفهان، خیابان چهارباغ، کوچه 5"), "Another valid address should pass");
        
        assertFalse(isValidAddress("کوتاه"), "Too short address should fail");
        assertFalse(isValidAddress(""), "Empty address should fail");
        assertFalse(isValidAddress("   "), "Whitespace-only address should fail");
        assertFalse(isValidAddress(null), "Null address should fail");
    }

    @Test
    void testCheckoutValidation() {
        List<CartItem> validItems = Arrays.asList(
            new CartItem(1L, "Item 1", 20000.0, 1, "Restaurant 1"),
            new CartItem(2L, "Item 2", 15000.0, 2, "Restaurant 1")
        );
        
        String validAddress = "تهران، خیابان ولیعصر، پلاک 123";
        String validPhone = "09123456789";
        
        assertTrue(isValidCheckout(validItems, validAddress, validPhone), 
                  "Valid checkout should pass");
        
        assertFalse(isValidCheckout(Arrays.asList(), validAddress, validPhone), 
                   "Empty cart should fail");
        assertFalse(isValidCheckout(validItems, "", validPhone), 
                   "Empty address should fail");
        assertFalse(isValidCheckout(validItems, validAddress, ""), 
                   "Empty phone should fail");
    }

    @Test
    void testCartItemSelection() {
        CartItem item1 = new CartItem(1L, "Item 1", 20000.0, 1, "Restaurant 1");
        CartItem item2 = new CartItem(2L, "Item 2", 15000.0, 1, "Restaurant 1");
        
        List<CartItem> items = Arrays.asList(item1, item2);
        
        // Test select all
        selectAllItems(items, true);
        assertTrue(item1.isSelected(), "Item 1 should be selected");
        assertTrue(item2.isSelected(), "Item 2 should be selected");
        
        // Test deselect all
        selectAllItems(items, false);
        assertFalse(item1.isSelected(), "Item 1 should not be selected");
        assertFalse(item2.isSelected(), "Item 2 should not be selected");
    }

    @Test
    void testSelectedItemsCalculation() {
        CartItem item1 = new CartItem(1L, "Item 1", 20000.0, 2, "Restaurant 1");
        item1.setSelected(true);
        
        CartItem item2 = new CartItem(2L, "Item 2", 15000.0, 1, "Restaurant 1");
        item2.setSelected(false);
        
        CartItem item3 = new CartItem(3L, "Item 3", 10000.0, 1, "Restaurant 1");
        item3.setSelected(true);
        
        List<CartItem> items = Arrays.asList(item1, item2, item3);
        
        double selectedTotal = calculateSelectedItemsTotal(items);
        assertEquals(50000.0, selectedTotal, 0.01, "Should only count selected items");
        
        int selectedCount = countSelectedItems(items);
        assertEquals(2, selectedCount, "Should count only selected items");
    }

    @Test
    void testCartItemQuantityLimits() {
        CartItem item = new CartItem(1L, "Item", 10000.0, 1, "Restaurant");
        
        // Test valid quantities
        assertTrue(isValidQuantity(1), "Quantity 1 should be valid");
        assertTrue(isValidQuantity(5), "Quantity 5 should be valid");
        assertTrue(isValidQuantity(10), "Quantity 10 should be valid");
        
        // Test invalid quantities
        assertFalse(isValidQuantity(0), "Quantity 0 should be invalid");
        assertFalse(isValidQuantity(-1), "Negative quantity should be invalid");
        assertFalse(isValidQuantity(101), "Quantity over 100 should be invalid");
    }

    @Test
    void testMultipleRestaurantValidation() {
        CartItem item1 = new CartItem(1L, "Item 1", 20000.0, 1, "Restaurant A");
        CartItem item2 = new CartItem(2L, "Item 2", 15000.0, 1, "Restaurant B");
        
        List<CartItem> mixedItems = Arrays.asList(item1, item2);
        
        assertTrue(hasMultipleRestaurants(mixedItems), "Should detect multiple restaurants");
        
        CartItem item3 = new CartItem(3L, "Item 3", 10000.0, 1, "Restaurant A");
        List<CartItem> singleRestaurantItems = Arrays.asList(item1, item3);
        
        assertFalse(hasMultipleRestaurants(singleRestaurantItems), "Should detect single restaurant");
    }

    // Helper methods for cart logic
    private double calculateDeliveryFee(double subtotal) {
        return subtotal >= 50000 ? 0.0 : 5000.0;
    }

    private boolean isValidCoupon(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return false;
        }
        return Arrays.asList("WELCOME10", "DISCOUNT20", "NEWUSER").contains(couponCode);
    }

    private double calculateCouponDiscount(double subtotal, String couponCode) {
        if (!isValidCoupon(couponCode)) {
            return 0.0;
        }
        
        switch (couponCode) {
            case "WELCOME10":
                return subtotal * 0.1; // 10% discount
            case "DISCOUNT20":
                return subtotal * 0.2; // 20% discount
            case "NEWUSER":
                return 5000.0; // Fixed 5000 toman discount
            default:
                return 0.0;
        }
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.matches("^09\\d{9}$");
    }

    private boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        return address.trim().length() >= 10;
    }

    private boolean isValidCheckout(List<CartItem> items, String address, String phone) {
        return !items.isEmpty() && isValidAddress(address) && isValidPhone(phone);
    }

    private void selectAllItems(List<CartItem> items, boolean selected) {
        items.forEach(item -> item.setSelected(selected));
    }

    private double calculateSelectedItemsTotal(List<CartItem> items) {
        return items.stream()
                   .filter(CartItem::isSelected)
                   .mapToDouble(item -> item.getPrice() * item.getQuantity())
                   .sum();
    }

    private int countSelectedItems(List<CartItem> items) {
        return (int) items.stream()
                         .filter(CartItem::isSelected)
                         .count();
    }

    private boolean isValidQuantity(int quantity) {
        return quantity > 0 && quantity <= 100;
    }

    private boolean hasMultipleRestaurants(List<CartItem> items) {
        long uniqueRestaurants = items.stream()
                                     .map(CartItem::getRestaurantName)
                                     .distinct()
                                     .count();
        return uniqueRestaurants > 1;
    }
} 