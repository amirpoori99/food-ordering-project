package com.myapp.ui.common;

import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import static org.assertj.core.api.Assertions.*;

/**
 * Test cases for NavigationController
 * Tests scene management, navigation flow, and authentication handling
 */
@DisplayName("NavigationController Tests")
class NavigationControllerTest extends ApplicationTest {

    private NavigationController navigationController;
    private Stage testStage;

    @Override
    public void start(Stage stage) {
        this.testStage = stage;
        this.navigationController = NavigationController.getInstance();
    }

    @BeforeEach
    void setUp() {
        // Clear authentication state before each test
        HttpClientUtil.clearTokens();
        
        // Reset singleton instance for clean state
        NavigationController.resetInstance();
        navigationController = NavigationController.getInstance();
        
        // Clear navigation cache
        navigationController.clearCache();
    }

    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonPatternTests {

        @Test
        @DisplayName("NavigationController is singleton")
        void navigationController_isSingleton() {
            NavigationController instance1 = NavigationController.getInstance();
            NavigationController instance2 = NavigationController.getInstance();
            
            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Multiple getInstance calls return same instance")
        void multipleGetInstance_returnsSameInstance() {
            NavigationController[] instances = new NavigationController[10];
            
            for (int i = 0; i < 10; i++) {
                instances[i] = NavigationController.getInstance();
            }
            
            for (int i = 1; i < 10; i++) {
                assertThat(instances[i]).isSameAs(instances[0]);
            }
        }
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Initialize sets primary stage correctly")
        void initialize_setsPrimaryStageCorrectly() {
            navigationController.initialize(testStage);
            
            assertThat(navigationController.getPrimaryStage()).isSameAs(testStage);
        }

        @Test
        @DisplayName("Initialize sets stage properties correctly")
        void initialize_setsStagePropertiesCorrectly() {
            // Ensure clean state and no authentication
            HttpClientUtil.clearTokens();
            navigationController.clearCache();
            
            navigationController.initialize(testStage);
            
            // Wait for JavaFX Platform.runLater to complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            assertThat(testStage.getTitle()).isEqualTo("Food Ordering System");
            assertThat(testStage.isResizable()).isTrue();
            assertThat(testStage.getMinWidth()).isEqualTo(800);
            assertThat(testStage.getMinHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("Initialize with null stage throws exception")
        void initialize_withNullStage_throwsException() {
            assertThatThrownBy(() -> navigationController.initialize(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Scene Constants Tests")
    class SceneConstantsTests {

        @Test
        @DisplayName("All scene constants are defined")
        void allSceneConstants_areDefined() {
            assertThat(NavigationController.LOGIN_SCENE).isEqualTo("Login");
            assertThat(NavigationController.REGISTER_SCENE).isEqualTo("Register");
            assertThat(NavigationController.RESTAURANT_LIST_SCENE).isEqualTo("RestaurantList");
            assertThat(NavigationController.CART_SCENE).isEqualTo("Cart");
            assertThat(NavigationController.ORDER_HISTORY_SCENE).isEqualTo("OrderHistory");
            assertThat(NavigationController.ORDER_DETAIL_SCENE).isEqualTo("OrderDetail");
            assertThat(NavigationController.PROFILE_SCENE).isEqualTo("Profile");
            assertThat(NavigationController.PAYMENT_SCENE).isEqualTo("Payment");
            assertThat(NavigationController.WALLET_SCENE).isEqualTo("Wallet");
            assertThat(NavigationController.ADMIN_DASHBOARD_SCENE).isEqualTo("AdminDashboard");
        }

        @Test
        @DisplayName("Scene constants are not empty")
        void sceneConstants_areNotEmpty() {
            assertThat(NavigationController.LOGIN_SCENE).isNotEmpty();
            assertThat(NavigationController.REGISTER_SCENE).isNotEmpty();
            assertThat(NavigationController.RESTAURANT_LIST_SCENE).isNotEmpty();
            assertThat(NavigationController.CART_SCENE).isNotEmpty();
            assertThat(NavigationController.ORDER_HISTORY_SCENE).isNotEmpty();
        }

        @Test
        @DisplayName("All restaurant management scene constants are defined")
        void restaurantManagementSceneConstants_areDefined() {
            assertThat(NavigationController.CREATE_RESTAURANT_SCENE).isEqualTo("CreateRestaurant");
            assertThat(NavigationController.EDIT_RESTAURANT_SCENE).isEqualTo("EditRestaurant");
            assertThat(NavigationController.ITEM_MANAGEMENT_SCENE).isEqualTo("ItemManagement");
            assertThat(NavigationController.MENU_MANAGEMENT_SCENE).isEqualTo("MenuManagement");
        }

        @Test
        @DisplayName("All courier scene constants are defined")
        void courierSceneConstants_areDefined() {
            assertThat(NavigationController.COURIER_AVAILABLE_SCENE).isEqualTo("CourierAvailable");
            assertThat(NavigationController.COURIER_HISTORY_SCENE).isEqualTo("CourierHistory");
        }

        @Test
        @DisplayName("Additional feature scene constants are defined")
        void additionalFeatureSceneConstants_areDefined() {
            assertThat(NavigationController.VENDOR_SEARCH_SCENE).isEqualTo("VendorSearch");
            assertThat(NavigationController.REVIEW_SCENE).isEqualTo("Review");
            assertThat(NavigationController.COUPON_VALIDATION_SCENE).isEqualTo("CouponValidation");
        }
    }

    @Nested
    @DisplayName("Cache Management Tests")
    class CacheManagementTests {

        @Test
        @DisplayName("Clear cache works correctly")
        void clearCache_worksCorrectly() {
            // This is a behavior test since we can't directly inspect the cache
            navigationController.clearCache();
            
            // If no exception is thrown, the method works
            assertThat(navigationController).isNotNull();
        }

        @Test
        @DisplayName("Clear specific scene from cache works correctly")
        void clearSpecificSceneFromCache_worksCorrectly() {
            navigationController.clearCache(NavigationController.LOGIN_SCENE);
            
            // If no exception is thrown, the method works
            assertThat(navigationController).isNotNull();
        }

        @Test
        @DisplayName("Clear cache with null scene name doesn't throw exception")
        void clearCacheWithNullSceneName_doesntThrowException() {
            assertThatCode(() -> navigationController.clearCache(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Clear cache with empty scene name doesn't throw exception")
        void clearCacheWithEmptySceneName_doesntThrowException() {
            assertThatCode(() -> navigationController.clearCache(""))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Authentication Redirect Tests")
    class AuthenticationRedirectTests {

        @Test
        @DisplayName("Check authentication when not authenticated")
        void checkAuthentication_whenNotAuthenticated() {
            // Ensure not authenticated
            HttpClientUtil.clearTokens();
            assertThat(HttpClientUtil.isAuthenticated()).isFalse();
            
            navigationController.initialize(testStage);
            
            // This should attempt to navigate to login
            // Since FXML files don't exist in test, we expect this to not crash
            assertThatCode(() -> navigationController.checkAuthenticationAndRedirect())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Check authentication when authenticated")
        void checkAuthentication_whenAuthenticated() {
            // Set authentication
            HttpClientUtil.setTokens("test-access-token", "test-refresh-token");
            assertThat(HttpClientUtil.isAuthenticated()).isTrue();
            
            navigationController.initialize(testStage);
            
            // This should attempt to navigate to restaurant list
            // Since FXML files don't exist in test, we expect this to not crash
            assertThatCode(() -> navigationController.checkAuthenticationAndRedirect())
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Logout clears authentication and cache")
        void logout_clearsAuthenticationAndCache() {
            // Set up authenticated state
            HttpClientUtil.setTokens("access", "refresh");
            assertThat(HttpClientUtil.isAuthenticated()).isTrue();
            
            navigationController.initialize(testStage);
            
            // Logout
            assertThatCode(() -> navigationController.logout())
                    .doesNotThrowAnyException();
            
            // Should be unauthenticated after logout
            assertThat(HttpClientUtil.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("Logout when already logged out doesn't crash")
        void logout_whenAlreadyLoggedOut_doesntCrash() {
            // Ensure not authenticated
            HttpClientUtil.clearTokens();
            assertThat(HttpClientUtil.isAuthenticated()).isFalse();
            
            navigationController.initialize(testStage);
            
            // Logout should not crash even when already logged out
            assertThatCode(() -> navigationController.logout())
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Show error doesn't crash with null values")
        void showError_doesntCrashWithNullValues() {
            assertThatCode(() -> navigationController.showError(null, null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Show error with valid parameters")
        void showError_withValidParameters() {
            String title = "Test Error";
            String message = "Test error message";
            Exception exception = new RuntimeException("Test exception");
            
            assertThatCode(() -> navigationController.showError(title, message, exception))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Show info doesn't crash")
        void showInfo_doesntCrash() {
            assertThatCode(() -> navigationController.showInfo("Title", "Message"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Show success doesn't crash")
        void showSuccess_doesntCrash() {
            assertThatCode(() -> navigationController.showSuccess("Title", "Message"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Current Scene Tests")
    class CurrentSceneTests {

        @Test
        @DisplayName("Initially no current scene")
        void initially_noCurrentScene() {
            assertThat(navigationController.getCurrentScene()).isNull();
        }

        @Test
        @DisplayName("Get primary stage returns correct stage")
        void getPrimaryStage_returnsCorrectStage() {
            navigationController.initialize(testStage);
            
            assertThat(navigationController.getPrimaryStage()).isSameAs(testStage);
        }
    }

    @Nested
    @DisplayName("Navigation Edge Cases")
    class NavigationEdgeCaseTests {

        @Test
        @DisplayName("Navigate to null scene doesn't crash")
        void navigateToNullScene_doesntCrash() {
            navigationController.initialize(testStage);
            
            assertThatCode(() -> navigationController.navigateTo(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Navigate to empty scene doesn't crash")
        void navigateToEmptyScene_doesntCrash() {
            navigationController.initialize(testStage);
            
            assertThatCode(() -> navigationController.navigateTo(""))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Navigate to non-existent scene doesn't crash")
        void navigateToNonExistentScene_doesntCrash() {
            navigationController.initialize(testStage);
            
            assertThatCode(() -> navigationController.navigateTo("NonExistentScene"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Navigate with data doesn't crash")
        void navigateWithData_doesntCrash() {
            navigationController.initialize(testStage);
            
            assertThatCode(() -> navigationController.navigateTo("TestScene", "Test Data"))
                    .doesNotThrowAnyException();
        }
    }
} 