package com.myapp.ui.integration;

import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.common.SessionManager;
import com.myapp.ui.auth.LoginController;
import com.myapp.ui.order.CartController;
import com.myapp.ui.restaurant.RestaurantListController;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;

@DisplayName("Full Stack Integration Tests")
class FullStackIntegrationTest extends ApplicationTest {

    private Stage stage;
    private LoginController loginController;
    private CartController cartController;
    private RestaurantListController restaurantController;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        loginController = new LoginController();
        cartController = new CartController();
        restaurantController = new RestaurantListController();
        
        // Initialize with login scene
        Scene loginScene = new Scene(loginController.getView(), 800, 600);
        stage.setScene(loginScene);
        stage.show();
    }

    @Nested
    @DisplayName("User Authentication Flow Tests")
    class AuthenticationFlowTests {

        @Test
        @DisplayName("Should complete full login-to-order flow")
        void completeUserFlow_LoginToOrder_Success() {
            // Step 1: Login
            clickOn("#usernameField").write("testuser@test.com");
            clickOn("#passwordField").write("password123");
            clickOn("#loginButton");
            
            // Verify login success
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> 
                SessionManager.getCurrentUser() != null);
            
            assertThat(SessionManager.getCurrentUser()).isNotNull();
            
            // Step 2: Navigate to restaurants
            clickOn("#restaurantsTab");
            WaitForAsyncUtils.waitForFxEvents();
            
            // Step 3: Add items to cart
            clickOn("#restaurantCard").clickOn("#addToCartButton");
            
            // Step 4: Complete order
            clickOn("#cartButton");
            clickOn("#checkoutButton");
            
            // Verify order completion
            verifyThat("#orderSuccessMessage", LabeledMatchers.hasText("Order placed successfully!"));
        }

        @Test
        @DisplayName("Should handle authentication failures gracefully")
        void loginFlow_InvalidCredentials_ShowsError() {
            // Given
            clickOn("#usernameField").write("invalid@test.com");
            clickOn("#passwordField").write("wrongpassword");
            
            // When
            clickOn("#loginButton");
            WaitForAsyncUtils.waitForFxEvents();
            
            // Then
            verifyThat("#errorLabel", LabeledMatchers.hasText("Invalid credentials"));
            assertThat(SessionManager.getCurrentUser()).isNull();
        }

        @Test
        @DisplayName("Should maintain session across navigation")
        void navigation_WithActiveSession_MaintainsUser() {
            // Login first
            performSuccessfulLogin();
            
            // Navigate through different screens
            clickOn("#restaurantsTab");
            clickOn("#ordersTab");
            clickOn("#profileTab");
            
            // Verify session maintained
            assertThat(SessionManager.getCurrentUser()).isNotNull();
            assertThat(SessionManager.getCurrentUser().getEmail()).isEqualTo("testuser@test.com");
        }
    }

    @Nested
    @DisplayName("Order Management Integration Tests")
    class OrderManagementTests {

        @BeforeEach
        void setUp() {
            performSuccessfulLogin();
        }

        @Test
        @DisplayName("Should handle complete order lifecycle")
        void orderLifecycle_CreateToDelivery_Success() {
            // Create order
            navigateToRestaurants();
            addItemsToCart();
            
            // Checkout
            proceedToCheckout();
            
            // Verify order status updates
            clickOn("#ordersTab");
            verifyThat("#orderStatus", LabeledMatchers.hasText("PENDING"));
            
            // Simulate status updates
            WaitForAsyncUtils.waitFor(3, TimeUnit.SECONDS, () -> {
                clickOn("#refreshButton");
                return lookup("#orderStatus").queryLabeled().getText().equals("PREPARING");
            });
        }

        @Test
        @DisplayName("Should handle cart modifications")
        void cartOperations_AddRemoveItems_UpdatesCorrectly() {
            navigateToRestaurants();
            
            // Add multiple items
            clickOn("#pizzaCard").clickOn("#addToCartButton");
            clickOn("#burgerCard").clickOn("#addToCartButton");
            clickOn("#pastaCard").clickOn("#addToCartButton");
            
            // Verify cart count
            verifyThat("#cartCount", LabeledMatchers.hasText("3"));
            
            // Remove one item
            clickOn("#cartButton");
            clickOn("#removeItemButton");
            
            // Verify updated count
            verifyThat("#cartCount", LabeledMatchers.hasText("2"));
        }

        @Test
        @DisplayName("Should calculate totals correctly")
        void cartTotals_MultipleItems_CalculatesCorrectly() {
            navigateToRestaurants();
            
            // Add items with known prices
            addItemWithPrice("Pizza", 25000);
            addItemWithPrice("Burger", 15000);
            
            clickOn("#cartButton");
            
            // Verify subtotal
            verifyThat("#subtotalLabel", LabeledMatchers.hasText("40,000 تومان"));
            
            // Verify total with tax/delivery
            verifyThat("#totalLabel", LabeledMatchers.hasText("45,000 تومان"));
        }
    }

    @Nested
    @DisplayName("Real-time Updates Tests")
    class RealTimeUpdatesTests {

        @BeforeEach
        void setUp() {
            performSuccessfulLogin();
        }

        @Test  
        @DisplayName("Should receive order status updates")
        void orderStatusUpdates_RealTime_UpdatesUI() {
            // Create order
            createTestOrder();
            
            // Navigate to order tracking
            clickOn("#ordersTab");
            clickOn("#trackOrderButton");
            
            // Verify real-time updates
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> {
                return !lookup("#statusLabel").queryLabeled().getText().equals("PENDING");
            });
            
            verifyThat("#statusLabel", LabeledMatchers.hasText("PREPARING"));
        }

        @Test
        @DisplayName("Should handle connection issues gracefully")
        void networkIssues_ConnectionLost_ShowsOfflineMode() {
            // Simulate network disconnection
            HttpClientUtil.setOfflineMode(true);
            
            // Try to perform network operation
            clickOn("#refreshButton");
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify offline message
            verifyThat("#networkStatusLabel", LabeledMatchers.hasText("Offline - Limited functionality"));
        }
    }

    @Nested
    @DisplayName("Performance Integration Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large restaurant lists efficiently")
        void largeDataSets_RestaurantList_LoadsQuickly() {
            performSuccessfulLogin();
            
            long startTime = System.currentTimeMillis();
            
            clickOn("#restaurantsTab");
            WaitForAsyncUtils.waitForFxEvents();
            
            long loadTime = System.currentTimeMillis() - startTime;
            
            // Should load within 2 seconds
            assertThat(loadTime).isLessThan(2000);
            
            // Verify all restaurants loaded
            assertThat(lookup("#restaurantCard").queryAll()).hasSizeGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle concurrent cart operations")
        void concurrentOperations_CartUpdates_HandledCorrectly() {
            performSuccessfulLogin();
            navigateToRestaurants();
            
            // Simulate rapid cart operations
            for (int i = 0; i < 10; i++) {
                clickOn("#addToCartButton");
                WaitForAsyncUtils.waitForFxEvents();
            }
            
            // Verify final cart state
            verifyThat("#cartCount", LabeledMatchers.hasText("10"));
        }
    }

    @Nested
    @DisplayName("Error Handling Integration Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should recover from server errors")
        void serverErrors_ApiFailure_ShowsUserFriendlyMessage() {
            performSuccessfulLogin();
            
            // Simulate server error
            HttpClientUtil.setSimulateServerError(true);
            
            clickOn("#restaurantsTab");
            WaitForAsyncUtils.waitForFxEvents();
            
            // Should show error message
            verifyThat("#errorMessageLabel", LabeledMatchers.hasText("Unable to load restaurants. Please try again."));
            
            // Should provide retry option
            verifyThat("#retryButton", Button::isVisible);
        }

        @Test
        @DisplayName("Should handle payment failures gracefully")
        void paymentErrors_CardDeclined_AllowsRetry() {
            performSuccessfulLogin();
            createTestOrder();
            
            // Proceed to payment with invalid card
            clickOn("#paymentButton");
            clickOn("#cardNumberField").write("4111111111111112"); // Invalid card
            clickOn("#processPaymentButton");
            
            WaitForAsyncUtils.waitForFxEvents();
            
            // Should show payment error
            verifyThat("#paymentErrorLabel", LabeledMatchers.hasText("Payment failed. Please check your card details."));
            
            // Should allow retry
            verifyThat("#retryPaymentButton", Button::isVisible);
        }
    }

    // Helper methods
    private void performSuccessfulLogin() {
        clickOn("#usernameField").write("testuser@test.com");
        clickOn("#passwordField").write("password123");
        clickOn("#loginButton");
        WaitForAsyncUtils.waitFor(3, TimeUnit.SECONDS, () -> 
            SessionManager.getCurrentUser() != null);
    }
    
    private void navigateToRestaurants() {
        clickOn("#restaurantsTab");
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    private void addItemsToCart() {
        clickOn("#pizzaCard").clickOn("#addToCartButton");
        clickOn("#burgerCard").clickOn("#addToCartButton");
    }
    
    private void addItemWithPrice(String itemName, int price) {
        clickOn("#" + itemName.toLowerCase() + "Card").clickOn("#addToCartButton");
    }
    
    private void proceedToCheckout() {
        clickOn("#cartButton");
        clickOn("#checkoutButton");
        clickOn("#confirmOrderButton");
    }
    
    private void createTestOrder() {
        navigateToRestaurants();
        addItemsToCart();
        proceedToCheckout();
    }

    @AfterEach
    void cleanup() {
        // Reset test state
        HttpClientUtil.setOfflineMode(false);
        HttpClientUtil.setSimulateServerError(false);
        SessionManager.clearSession();
    }
} 