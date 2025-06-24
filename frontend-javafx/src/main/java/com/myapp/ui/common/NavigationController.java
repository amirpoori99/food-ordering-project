package com.myapp.ui.common;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;

/**
 * Navigation controller for managing scene transitions and application flow
 * Handles FXML loading, scene switching, and maintains navigation history
 */
public class NavigationController {
    
    private static NavigationController instance;
    private Stage primaryStage;
    private Map<String, Scene> sceneCache = new HashMap<>();
    private String currentScene;
    
    // Scene constants
    public static final String LOGIN_SCENE = "Login";
    public static final String REGISTER_SCENE = "Register";
    public static final String RESTAURANT_LIST_SCENE = "RestaurantList";
    public static final String CART_SCENE = "Cart";
    public static final String ORDER_HISTORY_SCENE = "OrderHistory";
    public static final String ORDER_DETAIL_SCENE = "OrderDetail";
    public static final String PROFILE_SCENE = "Profile";
    public static final String PAYMENT_SCENE = "Payment";
    public static final String WALLET_SCENE = "Wallet";
    public static final String ADMIN_DASHBOARD_SCENE = "AdminDashboard";
    public static final String CREATE_RESTAURANT_SCENE = "CreateRestaurant";
    public static final String EDIT_RESTAURANT_SCENE = "EditRestaurant";
    public static final String ITEM_MANAGEMENT_SCENE = "ItemManagement";
    public static final String MENU_MANAGEMENT_SCENE = "MenuManagement";
    public static final String COURIER_AVAILABLE_SCENE = "CourierAvailable";
    public static final String COURIER_HISTORY_SCENE = "CourierHistory";
    public static final String VENDOR_SEARCH_SCENE = "VendorSearch";
    public static final String REVIEW_SCENE = "Review";
    public static final String COUPON_VALIDATION_SCENE = "CouponValidation";
    
    private NavigationController() {}
    
    public static NavigationController getInstance() {
        if (instance == null) {
            instance = new NavigationController();
        }
        return instance;
    }
    
    /**
     * Initialize navigation with primary stage
     */
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Food Ordering System");
        this.primaryStage.setResizable(true);
        this.primaryStage.setMinWidth(800);
        this.primaryStage.setMinHeight(600);
    }
    
    /**
     * Navigate to a scene
     */
    public void navigateTo(String sceneName) {
        if (Platform.isFxApplicationThread()) {
            navigateToInternal(sceneName);
        } else {
            Platform.runLater(() -> navigateToInternal(sceneName));
        }
    }
    
    /**
     * Internal navigation method (must be called on FX thread)
     */
    private void navigateToInternal(String sceneName) {
        try {
            Scene scene = getScene(sceneName);
            if (scene != null) {
                primaryStage.setScene(scene);
                primaryStage.show();
                currentScene = sceneName;
                
                // Update window title based on scene
                updateWindowTitle(sceneName);
            }
        } catch (IOException e) {
            showError("Navigation Error", "Could not load scene: " + sceneName, e);
        }
    }
    
    /**
     * Navigate to a scene with data
     */
    public void navigateTo(String sceneName, Object data) {
        // For now, just navigate to the scene
        // In the future, we can implement data passing mechanism
        navigateTo(sceneName);
    }
    
    /**
     * Get scene from cache or load it
     */
    private Scene getScene(String sceneName) throws IOException {
        if (sceneCache.containsKey(sceneName)) {
            return sceneCache.get(sceneName);
        }
        
        // Load FXML file
        String fxmlPath = "/fxml/" + sceneName + ".fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        
        if (loader.getLocation() == null) {
            throw new IOException("FXML file not found: " + fxmlPath);
        }
        
        Parent root = loader.load();
        Scene scene = new Scene(root);
        
        // Apply CSS if exists
        String cssPath = "/css/" + sceneName + ".css";
        if (getClass().getResource(cssPath) != null) {
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        }
        
        // Cache the scene
        sceneCache.put(sceneName, scene);
        
        return scene;
    }
    
    /**
     * Clear scene cache (useful for reloading scenes)
     */
    public void clearCache() {
        sceneCache.clear();
    }
    
    /**
     * Clear specific scene from cache
     */
    public void clearCache(String sceneName) {
        sceneCache.remove(sceneName);
    }
    
    /**
     * Get current scene name
     */
    public String getCurrentScene() {
        return currentScene;
    }
    
    /**
     * Check if user is authenticated and redirect accordingly
     */
    public void checkAuthenticationAndRedirect() {
        if (HttpClientUtil.isAuthenticated()) {
            // User is authenticated, go to main screen
            navigateTo(RESTAURANT_LIST_SCENE);
        } else {
            // User not authenticated, go to login
            navigateTo(LOGIN_SCENE);
        }
    }
    
    /**
     * Logout and redirect to login
     */
    public void logout() {
        try {
            HttpClientUtil.logout();
        } catch (IOException e) {
            // Even if logout request fails, clear local tokens
        }
        
        HttpClientUtil.clearTokens();
        clearCache(); // Clear all cached scenes
        navigateTo(LOGIN_SCENE);
    }
    
    /**
     * Show error dialog
     */
    public void showError(String title, String message, Exception exception) {
        // For now, print to console
        // In a full implementation, this would show a proper error dialog
        System.err.println("ERROR: " + title);
        System.err.println("Message: " + message);
        if (exception != null) {
            exception.printStackTrace();
        }
    }
    
    /**
     * Show info dialog
     */
    public void showInfo(String title, String message) {
        // For now, print to console
        // In a full implementation, this would show a proper info dialog
        System.out.println("INFO: " + title + " - " + message);
    }
    
    /**
     * Show success dialog
     */
    public void showSuccess(String title, String message) {
        // For now, print to console
        // In a full implementation, this would show a proper success dialog
        System.out.println("SUCCESS: " + title + " - " + message);
    }
    
    /**
     * Update window title based on current scene
     */
    private void updateWindowTitle(String sceneName) {
        String title = "Food Ordering System";
        
        switch (sceneName) {
            case LOGIN_SCENE:
                title += " - Login";
                break;
            case REGISTER_SCENE:
                title += " - Register";
                break;
            case RESTAURANT_LIST_SCENE:
                title += " - Restaurants";
                break;
            case CART_SCENE:
                title += " - Shopping Cart";
                break;
            case ORDER_HISTORY_SCENE:
                title += " - Order History";
                break;
            case PROFILE_SCENE:
                title += " - Profile";
                break;
            case PAYMENT_SCENE:
                title += " - Payment";
                break;
            case WALLET_SCENE:
                title += " - Wallet";
                break;
            case ADMIN_DASHBOARD_SCENE:
                title += " - Admin Dashboard";
                break;
            case CREATE_RESTAURANT_SCENE:
                title += " - Create Restaurant";
                break;
            case EDIT_RESTAURANT_SCENE:
                title += " - Edit Restaurant";
                break;
            case ITEM_MANAGEMENT_SCENE:
                title += " - Item Management";
                break;
            case MENU_MANAGEMENT_SCENE:
                title += " - Menu Management";
                break;
            case COURIER_AVAILABLE_SCENE:
                title += " - Available Deliveries";
                break;
            case COURIER_HISTORY_SCENE:
                title += " - Delivery History";
                break;
            case VENDOR_SEARCH_SCENE:
                title += " - Search Vendors";
                break;
            case REVIEW_SCENE:
                title += " - Reviews";
                break;
            case COUPON_VALIDATION_SCENE:
                title += " - Coupon Validation";
                break;
            default:
                title += " - " + sceneName;
        }
        
        primaryStage.setTitle(title);
    }
    
    /**
     * Get primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
