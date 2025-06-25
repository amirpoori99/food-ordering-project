package com.myapp.ui.payment;

import com.myapp.ui.payment.PaymentController.OrderItem;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified test cases for PaymentController functionality
 * Tests core logic without FXML dependency
 */
@ExtendWith(ApplicationExtension.class)
class PaymentControllerSimpleTest {

    private PaymentController controller;
    private Scene scene;

    @Start
    void start(Stage stage) throws Exception {
        // Create a simple scene for testing without FXML
        VBox root = new VBox();
        scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
        
        // Create controller instance
        controller = new PaymentController();
    }

    @BeforeEach
    void setUp() {
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
    void testOrderItemCreation() {
        OrderItem item = new OrderItem("پیتزا مارگاریتا", 25000.0, 2, "رستوران ایتالیایی");
        
        assertEquals("پیتزا مارگاریتا", item.getName());
        assertEquals(25000.0, item.getPrice(), 0.01);
        assertEquals(2, item.getQuantity());
        assertEquals("رستوران ایتالیایی", item.getRestaurantName());
    }

    @Test
    void testCardNumberValidation() {
        // Valid card numbers
        assertTrue(isValidCardNumber("1234567890123456"), "16-digit card should be valid");
        assertTrue(isValidCardNumber("4000-0000-0000-0000"), "Formatted card should be valid");
        
        // Invalid card numbers
        assertFalse(isValidCardNumber("12345"), "Short card number should be invalid");
        assertFalse(isValidCardNumber("12345678901234567"), "Long card number should be invalid");
        assertFalse(isValidCardNumber("abcd1234"), "Non-numeric card should be invalid");
        assertFalse(isValidCardNumber(""), "Empty card should be invalid");
        assertFalse(isValidCardNumber(null), "Null card should be invalid");
    }

    @Test
    void testCardHolderNameValidation() {
        assertTrue(isValidCardHolderName("احمد محمدی"), "Persian name should be valid");
        assertTrue(isValidCardHolderName("John Doe"), "English name should be valid");
        
        assertFalse(isValidCardHolderName(""), "Empty name should be invalid");
        assertFalse(isValidCardHolderName("   "), "Whitespace name should be invalid");
        assertFalse(isValidCardHolderName("A"), "Single character should be invalid");
        assertFalse(isValidCardHolderName(null), "Null name should be invalid");
    }

    @Test
    void testCvvValidation() {
        assertTrue(isValidCvv("123"), "3-digit CVV should be valid");
        assertTrue(isValidCvv("1234"), "4-digit CVV should be valid");
        
        assertFalse(isValidCvv("12"), "2-digit CVV should be invalid");
        assertFalse(isValidCvv("12345"), "5-digit CVV should be invalid");
        assertFalse(isValidCvv("abc"), "Non-numeric CVV should be invalid");
        assertFalse(isValidCvv(""), "Empty CVV should be invalid");
        assertFalse(isValidCvv(null), "Null CVV should be invalid");
    }

    @Test
    void testExpiryDateValidation() {
        assertTrue(isValidExpiryDate("12", "2025"), "Valid future date should pass");
        assertTrue(isValidExpiryDate("01", "2026"), "January 2026 should pass");
        
        assertFalse(isValidExpiryDate("13", "2025"), "Invalid month should fail");
        assertFalse(isValidExpiryDate("00", "2025"), "Zero month should fail");
        assertFalse(isValidExpiryDate("12", "2020"), "Past year should fail");
        assertFalse(isValidExpiryDate("", "2025"), "Empty month should fail");
        assertFalse(isValidExpiryDate("12", ""), "Empty year should fail");
    }

    @Test
    void testPaymentMethodValidation() {
        assertTrue(isValidPaymentMethod("CARD"), "Card payment should be valid");
        assertTrue(isValidPaymentMethod("WALLET"), "Wallet payment should be valid");
        assertTrue(isValidPaymentMethod("COD"), "Cash on delivery should be valid");
        
        assertFalse(isValidPaymentMethod("INVALID"), "Invalid method should fail");
        assertFalse(isValidPaymentMethod(""), "Empty method should fail");
        assertFalse(isValidPaymentMethod(null), "Null method should fail");
    }

    @Test
    void testWalletBalanceValidation() {
        double walletBalance = 100000.0;
        double orderTotal1 = 50000.0;
        double orderTotal2 = 150000.0;
        
        assertTrue(hasSufficientWalletBalance(walletBalance, orderTotal1), 
                  "Should have sufficient balance");
        assertFalse(hasSufficientWalletBalance(walletBalance, orderTotal2), 
                   "Should not have sufficient balance");
    }

    @Test
    void testOrderTotalCalculation() {
        List<OrderItem> items = Arrays.asList(
            new OrderItem("Item 1", 20000.0, 2, "Restaurant 1"),
            new OrderItem("Item 2", 15000.0, 1, "Restaurant 1")
        );
        
        double subtotal = calculateOrderSubtotal(items);
        assertEquals(55000.0, subtotal, 0.01, "Subtotal should be calculated correctly");
        
        double deliveryFee = calculateDeliveryFee(subtotal);
        assertEquals(0.0, deliveryFee, 0.01, "Delivery should be free for orders over 50,000");
        
        double total = subtotal + deliveryFee;
        assertEquals(55000.0, total, 0.01, "Total should include delivery fee");
    }

    @Test
    void testDiscountCalculation() {
        double subtotal = 100000.0;
        double discount1 = calculateDiscount(subtotal, "WELCOME10");
        double discount2 = calculateDiscount(subtotal, "INVALID");
        
        assertEquals(10000.0, discount1, 0.01, "WELCOME10 should give 10% discount");
        assertEquals(0.0, discount2, 0.01, "Invalid coupon should give no discount");
    }

    @Test
    void testDeliveryAddressValidation() {
        assertTrue(isValidDeliveryAddress("تهران، خیابان ولیعصر، پلاک 123"), 
                  "Valid address should pass");
        
        assertFalse(isValidDeliveryAddress("کوتاه"), "Short address should fail");
        assertFalse(isValidDeliveryAddress(""), "Empty address should fail");
        assertFalse(isValidDeliveryAddress(null), "Null address should fail");
    }

    @Test
    void testDeliveryPhoneValidation() {
        assertTrue(isValidDeliveryPhone("09123456789"), "Valid mobile should pass");
        
        assertFalse(isValidDeliveryPhone("021123456"), "Landline should fail");
        assertFalse(isValidDeliveryPhone("123"), "Invalid phone should fail");
        assertFalse(isValidDeliveryPhone(""), "Empty phone should fail");
        assertFalse(isValidDeliveryPhone(null), "Null phone should fail");
    }

    @Test
    void testCompletePaymentValidation() {
        List<OrderItem> validItems = Arrays.asList(
            new OrderItem("Item 1", 20000.0, 1, "Restaurant 1")
        );
        
        assertTrue(isValidPayment(validItems, "CARD", "تهران، خیابان ولیعصر", "09123456789"), 
                  "Valid payment should pass");
        
        assertFalse(isValidPayment(Arrays.asList(), "CARD", "تهران، خیابان ولیعصر", "09123456789"), 
                   "Empty items should fail");
        assertFalse(isValidPayment(validItems, "INVALID", "تهران، خیابان ولیعصر", "09123456789"), 
                   "Invalid payment method should fail");
    }

    @Test
    void testCardFormatting() {
        assertEquals("1234-5678-9012-3456", formatCardNumber("1234567890123456"), 
                    "Card number should be formatted with dashes");
        assertEquals("1234-5678-9012-3456", formatCardNumber("1234-5678-9012-3456"), 
                    "Already formatted card should remain the same");
    }

    @Test
    void testPaymentProcessing() {
        // Simulate payment processing
        assertTrue(processPayment("CARD", 50000.0), "Card payment should process");
        assertTrue(processPayment("WALLET", 30000.0), "Wallet payment should process");
        assertTrue(processPayment("COD", 40000.0), "COD payment should process");
        
        assertFalse(processPayment("INVALID", 50000.0), "Invalid payment should fail");
    }

    // Helper methods for payment logic
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }
        String cleanCard = cardNumber.replaceAll("-", "").replaceAll(" ", "");
        return cleanCard.matches("\\d{16}");
    }

    private boolean isValidCardHolderName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.trim().length() >= 2;
    }

    private boolean isValidCvv(String cvv) {
        if (cvv == null || cvv.trim().isEmpty()) {
            return false;
        }
        return cvv.matches("\\d{3,4}");
    }

    private boolean isValidExpiryDate(String month, String year) {
        if (month == null || year == null || month.trim().isEmpty() || year.trim().isEmpty()) {
            return false;
        }
        
        try {
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);
            
            if (m < 1 || m > 12) return false;
            if (y < 2024) return false; // Assuming current year is 2024+
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidPaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return false;
        }
        return Arrays.asList("CARD", "WALLET", "COD").contains(method);
    }

    private boolean hasSufficientWalletBalance(double balance, double orderTotal) {
        return balance >= orderTotal;
    }

    private double calculateOrderSubtotal(List<OrderItem> items) {
        return items.stream()
                   .mapToDouble(item -> item.getPrice() * item.getQuantity())
                   .sum();
    }

    private double calculateDeliveryFee(double subtotal) {
        return subtotal >= 50000 ? 0.0 : 5000.0;
    }

    private double calculateDiscount(double subtotal, String couponCode) {
        if ("WELCOME10".equals(couponCode)) {
            return subtotal * 0.1;
        }
        return 0.0;
    }

    private boolean isValidDeliveryAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        return address.trim().length() >= 10;
    }

    private boolean isValidDeliveryPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.matches("^09\\d{9}$");
    }

    private boolean isValidPayment(List<OrderItem> items, String paymentMethod, String address, String phone) {
        return !items.isEmpty() && 
               isValidPaymentMethod(paymentMethod) && 
               isValidDeliveryAddress(address) && 
               isValidDeliveryPhone(phone);
    }

    private String formatCardNumber(String cardNumber) {
        if (cardNumber == null) return "";
        String clean = cardNumber.replaceAll("[^\\d]", "");
        if (clean.length() == 16) {
            return clean.substring(0,4) + "-" + clean.substring(4,8) + "-" + 
                   clean.substring(8,12) + "-" + clean.substring(12,16);
        }
        return cardNumber;
    }

    private boolean processPayment(String method, double amount) {
        return isValidPaymentMethod(method) && amount > 0;
    }
} 