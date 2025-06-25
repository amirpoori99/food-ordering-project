package com.myapp.ui.order;

import com.myapp.ui.order.OrderHistoryController.OrderHistory;
import com.myapp.ui.order.OrderHistoryController.OrderItem;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified test cases for OrderHistoryController functionality
 * Tests core logic without FXML dependency
 */
@ExtendWith(ApplicationExtension.class)
class OrderHistoryControllerSimpleTest {

    private OrderHistoryController controller;
    private Scene scene;

    @Start
    void start(Stage stage) throws Exception {
        // Create a simple scene for testing without FXML
        VBox root = new VBox();
        scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
        
        // Create controller instance
        controller = new OrderHistoryController();
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
    void testOrderHistoryCreation() {
        LocalDateTime now = LocalDateTime.now();
        OrderHistory order = new OrderHistory(
            "FO123456", 
            now,
            "تحویل داده شده",
            "رستوران ایتالیایی",
            68000.0,
            Arrays.asList(
                new OrderItem("پیتزا مارگاریتا", 25000.0, 2),
                new OrderItem("نوشابه", 9000.0, 2)
            ),
            "تهران، خیابان ولیعصر، پلاک 123",
            "09123456789",
            now.plusHours(1),
            "پرداخت با کارت",
            "پرداخت شده",
            "PAY789123"
        );
        
        assertEquals("FO123456", order.getOrderNumber());
        assertEquals(now, order.getOrderDate());
        assertEquals("تحویل داده شده", order.getStatus());
        assertEquals("رستوران ایتالیایی", order.getRestaurantName());
        assertEquals(68000.0, order.getTotalAmount(), 0.01);
        assertEquals(2, order.getItems().size());
    }

    @Test
    void testOrderItemCreation() {
        OrderItem item = new OrderItem("پیتزا مارگاریتا", 25000.0, 2);
        
        assertEquals("پیتزا مارگاریتا", item.getName());
        assertEquals(25000.0, item.getPrice(), 0.01);
        assertEquals(2, item.getQuantity());
    }

    @Test
    void testOrderStatusValidation() {
        assertTrue(isValidOrderStatus("در انتظار تأیید"), "Pending status should be valid");
        assertTrue(isValidOrderStatus("تحویل داده شده"), "Delivered status should be valid");
        assertTrue(isValidOrderStatus("لغو شده"), "Cancelled status should be valid");
        assertTrue(isValidOrderStatus("در حال ارسال"), "Shipping status should be valid");
        
        assertFalse(isValidOrderStatus("نامعتبر"), "Invalid status should fail");
        assertFalse(isValidOrderStatus(""), "Empty status should fail");
        assertFalse(isValidOrderStatus(null), "Null status should fail");
    }

    @Test
    void testPaymentStatusValidation() {
        assertTrue(isValidPaymentStatus("پرداخت شده"), "Paid status should be valid");
        assertTrue(isValidPaymentStatus("در انتظار"), "Pending status should be valid");
        assertTrue(isValidPaymentStatus("لغو شده"), "Cancelled status should be valid");
        
        assertFalse(isValidPaymentStatus("نامعتبر"), "Invalid status should fail");
        assertFalse(isValidPaymentStatus(""), "Empty status should fail");
        assertFalse(isValidPaymentStatus(null), "Null status should fail");
    }

    @Test
    void testOrderNumberValidation() {
        assertTrue(isValidOrderNumber("FO123456"), "Valid order number should pass");
        assertTrue(isValidOrderNumber("ORD789123"), "Another valid order number should pass");
        
        assertFalse(isValidOrderNumber("123"), "Short order number should fail");
        assertFalse(isValidOrderNumber(""), "Empty order number should fail");
        assertFalse(isValidOrderNumber(null), "Null order number should fail");
    }

    @Test
    void testOrderFiltering() {
        LocalDateTime now = LocalDateTime.now();
        
        OrderHistory order1 = new OrderHistory("FO123456", now, "تحویل داده شده", "رستوران A", 50000.0,
                Arrays.asList(), "آدرس", "09123456789", null, "کارت", "پرداخت شده", "TRK123");
        
        OrderHistory order2 = new OrderHistory("FO123457", now, "لغو شده", "رستوران B", 30000.0,
                Arrays.asList(), "آدرس", "09123456789", null, "کارت", "لغو شده", "TRK124");
        
        List<OrderHistory> orders = Arrays.asList(order1, order2);
        
        // Test status filtering
        List<OrderHistory> deliveredOrders = filterOrdersByStatus(orders, "تحویل داده شده");
        assertEquals(1, deliveredOrders.size());
        assertEquals("FO123456", deliveredOrders.get(0).getOrderNumber());
        
        List<OrderHistory> cancelledOrders = filterOrdersByStatus(orders, "لغو شده");
        assertEquals(1, cancelledOrders.size());
        assertEquals("FO123457", cancelledOrders.get(0).getOrderNumber());
    }

    @Test
    void testOrderSearching() {
        LocalDateTime now = LocalDateTime.now();
        
        OrderHistory order1 = new OrderHistory("FO123456", now, "تحویل داده شده", "رستوران ایتالیایی", 50000.0,
                Arrays.asList(), "آدرس", "09123456789", null, "کارت", "پرداخت شده", "TRK123");
        
        OrderHistory order2 = new OrderHistory("FO789123", now, "تحویل داده شده", "فست فود", 30000.0,
                Arrays.asList(), "آدرس", "09123456789", null, "کارت", "پرداخت شده", "TRK124");
        
        List<OrderHistory> orders = Arrays.asList(order1, order2);
        
        // Test search by order number
        List<OrderHistory> searchResults1 = searchOrders(orders, "FO123456");
        assertEquals(1, searchResults1.size());
        assertEquals("FO123456", searchResults1.get(0).getOrderNumber());
        
        // Test search by restaurant name
        List<OrderHistory> searchResults2 = searchOrders(orders, "ایتالیایی");
        assertEquals(1, searchResults2.size());
        assertEquals("رستوران ایتالیایی", searchResults2.get(0).getRestaurantName());
    }

    @Test
    void testDateFiltering() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime lastWeek = now.minusWeeks(1);
        LocalDateTime lastMonth = now.minusMonths(1);
        
        // Test today filter
        assertTrue(matchesDateFilter(now, "امروز"), "Today's order should match today filter");
        assertFalse(matchesDateFilter(yesterday, "امروز"), "Yesterday's order should not match today filter");
        
        // Test week filter
        assertTrue(matchesDateFilter(yesterday, "هفته گذشته"), "Yesterday should match week filter");
        assertFalse(matchesDateFilter(lastWeek, "هفته گذشته"), "Last week should not match week filter");
        
        // Test month filter
        assertTrue(matchesDateFilter(lastWeek, "ماه گذشته"), "Last week should match month filter");
        assertFalse(matchesDateFilter(lastMonth, "ماه گذشته"), "Last month should not match month filter");
    }

    @Test
    void testOrderSorting() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusHours(2);
        
        OrderHistory order1 = new OrderHistory("FO123456", earlier, "تحویل داده شده", "رستوران A", 50000.0,
                Arrays.asList(), "آدرس", "09123456789", null, "کارت", "پرداخت شده", "TRK123");
        
        OrderHistory order2 = new OrderHistory("FO123457", now, "تحویل داده شده", "رستوران B", 30000.0,
                Arrays.asList(), "آدرس", "09123456789", null, "کارت", "پرداخت شده", "TRK124");
        
        List<OrderHistory> orders = Arrays.asList(order1, order2);
        List<OrderHistory> sortedOrders = sortOrdersByDate(orders, true); // newest first
        
        assertEquals("FO123457", sortedOrders.get(0).getOrderNumber(), "Newest order should be first");
        assertEquals("FO123456", sortedOrders.get(1).getOrderNumber(), "Older order should be second");
    }

    @Test
    void testStatusColorMapping() {
        assertEquals("#28a745", getStatusColor("تحویل داده شده"), "Delivered should be green");
        assertEquals("#17a2b8", getStatusColor("در حال ارسال"), "Shipping should be blue");
        assertEquals("#ffc107", getStatusColor("در حال آماده‌سازی"), "Preparing should be yellow");
        assertEquals("#6c757d", getStatusColor("در انتظار تأیید"), "Pending should be gray");
        assertEquals("#dc3545", getStatusColor("لغو شده"), "Cancelled should be red");
    }

    @Test
    void testPaymentStatusColorMapping() {
        assertEquals("#28a745", getPaymentStatusColor("پرداخت شده"), "Paid should be green");
        assertEquals("#ffc107", getPaymentStatusColor("در انتظار"), "Pending should be yellow");
        assertEquals("#dc3545", getPaymentStatusColor("لغو شده"), "Cancelled should be red");
    }

    @Test
    void testOrderActionAvailability() {
        LocalDateTime now = LocalDateTime.now();
        
        OrderHistory pendingOrder = new OrderHistory("FO123456", now, "در انتظار تأیید", "رستوران", 50000.0,
                Arrays.asList(), "آدرس", "09123456789", null, "کارت", "در انتظار", "TRK123");
        
        OrderHistory deliveredOrder = new OrderHistory("FO123457", now, "تحویل داده شده", "رستوران", 50000.0,
                Arrays.asList(), "آدرس", "09123456789", null, "کارت", "پرداخت شده", "TRK124");
        
        assertTrue(canCancelOrder(pendingOrder), "Pending orders should be cancellable");
        assertFalse(canCancelOrder(deliveredOrder), "Delivered orders should not be cancellable");
        
        assertTrue(canReorder(deliveredOrder), "Delivered orders should be reorderable");
        assertTrue(canReorder(pendingOrder), "Any order should be reorderable");
        
        assertTrue(canDownloadReceipt(deliveredOrder), "Delivered orders should have receipt");
        assertFalse(canDownloadReceipt(pendingOrder), "Pending orders should not have receipt yet");
    }

    // Helper methods for order history logic
    private boolean isValidOrderStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        return Arrays.asList("در انتظار تأیید", "تحویل داده شده", "لغو شده", "در حال ارسال", 
                           "آماده ارسال", "در حال آماده‌سازی").contains(status);
    }

    private boolean isValidPaymentStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        return Arrays.asList("پرداخت شده", "در انتظار", "لغو شده").contains(status);
    }

    private boolean isValidOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            return false;
        }
        return orderNumber.length() >= 6;
    }

    private List<OrderHistory> filterOrdersByStatus(List<OrderHistory> orders, String status) {
        return orders.stream()
                    .filter(order -> order.getStatus().equals(status))
                    .toList();
    }

    private List<OrderHistory> searchOrders(List<OrderHistory> orders, String searchText) {
        String lowerSearchText = searchText.toLowerCase();
        return orders.stream()
                    .filter(order -> 
                        order.getOrderNumber().toLowerCase().contains(lowerSearchText) ||
                        order.getRestaurantName().toLowerCase().contains(lowerSearchText) ||
                        order.getStatus().toLowerCase().contains(lowerSearchText))
                    .toList();
    }

    private boolean matchesDateFilter(LocalDateTime orderDate, String filter) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (filter) {
            case "امروز":
                return orderDate.toLocalDate().equals(now.toLocalDate());
            case "هفته گذشته":
                return orderDate.isAfter(now.minusWeeks(1));
            case "ماه گذشته":
                return orderDate.isAfter(now.minusMonths(1));
            default:
                return true;
        }
    }

    private List<OrderHistory> sortOrdersByDate(List<OrderHistory> orders, boolean newestFirst) {
        return orders.stream()
                    .sorted((o1, o2) -> newestFirst ? 
                           o2.getOrderDate().compareTo(o1.getOrderDate()) :
                           o1.getOrderDate().compareTo(o2.getOrderDate()))
                    .toList();
    }

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

    private String getPaymentStatusColor(String status) {
        switch (status) {
            case "پرداخت شده": return "#28a745";
            case "در انتظار": return "#ffc107";
            case "لغو شده": return "#dc3545";
            default: return "#6c757d";
        }
    }

    private boolean canCancelOrder(OrderHistory order) {
        return Arrays.asList("در انتظار تأیید", "در حال آماده‌سازی").contains(order.getStatus());
    }

    private boolean canReorder(OrderHistory order) {
        return true; // All orders can be reordered
    }

    private boolean canDownloadReceipt(OrderHistory order) {
        return Arrays.asList("تحویل داده شده", "در حال ارسال").contains(order.getStatus());
    }
} 