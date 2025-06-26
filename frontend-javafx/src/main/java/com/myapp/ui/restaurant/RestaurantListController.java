package com.myapp.ui.restaurant;

import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.common.NavigationController;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * کنترلر لیست رستوران‌ها
 * 
 * این کلاس مسئول نمایش و مدیریت لیست رستوران‌ها شامل:
 * - بارگذاری رستوران‌ها از backend
 * - جستجو در رستوران‌ها بر اساس نام و آدرس
 * - نمایش اطلاعات رستوران با custom cell factory
 * - مدیریت منوی navigation
 * - تعامل با رستوران‌ها (مشاهده منو، علاقه‌مندی)
 * 
 * ویژگی‌ها:
 * - Real-time search
 * - Custom ListView cells
 * - Background data loading
 * - Error handling
 * - Status management
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class RestaurantListController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private ListView<Restaurant> restaurantListView;
    @FXML private Label statusLabel;
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem orderHistoryMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuItem cartMenuItem;
    @FXML private MenuItem favoritesMenuItem;

    private NavigationController navigationController;
    private List<Restaurant> allRestaurants = new ArrayList<>();
    private List<Restaurant> filteredRestaurants = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.navigationController = NavigationController.getInstance();
        
        setupUI();
        loadRestaurants();
    }

    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Setup restaurant list view with custom cell factory
        restaurantListView.setCellFactory(listView -> new RestaurantListCell());
        
        // Setup search functionality
        searchField.setOnAction(e -> handleSearch());
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.trim().isEmpty()) {
                displayRestaurants(allRestaurants);
            }
        });
        
        // Initial status
        setStatus("در حال بارگذاری...");
    }

    /**
     * Load restaurants from backend
     */
    private void loadRestaurants() {
        Task<List<Restaurant>> loadTask = new Task<List<Restaurant>>() {
            @Override
            protected List<Restaurant> call() throws Exception {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants");
                
                if (response.isSuccess() && response.getData() != null) {
                    List<Restaurant> restaurants = new ArrayList<>();
                    JsonNode dataArray = response.getData();
                    
                    if (dataArray.isArray()) {
                        for (JsonNode restaurantNode : dataArray) {
                            Restaurant restaurant = parseRestaurant(restaurantNode);
                            restaurants.add(restaurant);
                        }
                    }
                    
                    return restaurants;
                } else {
                    throw new RuntimeException(response.getMessage() != null ? 
                                               response.getMessage() : "خطا در بارگذاری رستوران‌ها");
                }
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                allRestaurants = loadTask.getValue();
                filteredRestaurants = new ArrayList<>(allRestaurants);
                displayRestaurants(filteredRestaurants);
                setStatus(allRestaurants.size() + " رستوران یافت شد");
            });
        });
        
        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = "خطا در بارگذاری رستوران‌ها";
                if (exception != null && exception.getMessage() != null) {
                    errorMessage = exception.getMessage();
                }
                setStatus(errorMessage);
                showError("خطا", errorMessage);
            });
        });
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /**
     * Parse restaurant from JSON node
     */
    private Restaurant parseRestaurant(JsonNode node) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(node.get("id").asLong());
        restaurant.setName(node.get("name").asText());
        restaurant.setAddress(node.get("address").asText());
        restaurant.setPhone(node.get("phone").asText());
        restaurant.setStatus(node.get("status").asText());
        return restaurant;
    }

    /**
     * Display restaurants in the list view
     */
    private void displayRestaurants(List<Restaurant> restaurants) {
        restaurantListView.getItems().clear();
        restaurantListView.getItems().addAll(restaurants);
    }

    /**
     * Handle search functionality
     */
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            displayRestaurants(allRestaurants);
            setStatus(allRestaurants.size() + " رستوران یافت شد");
        } else {
            filteredRestaurants = allRestaurants.stream()
                .filter(restaurant -> 
                    restaurant.getName().toLowerCase().contains(searchText) ||
                    restaurant.getAddress().toLowerCase().contains(searchText))
                .toList();
            
            displayRestaurants(filteredRestaurants);
            setStatus(filteredRestaurants.size() + " رستوران در نتایج جستجو");
        }
    }

    /**
     * Handle refresh button
     */
    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadRestaurants();
    }

    /**
     * Handle profile menu item
     */
    @FXML
    private void handleProfile() {
        navigationController.navigateTo(NavigationController.PROFILE_SCENE);
    }

    /**
     * Handle order history menu item
     */
    @FXML
    private void handleOrderHistory() {
        navigationController.navigateTo(NavigationController.ORDER_HISTORY_SCENE);
    }

    /**
     * Handle cart menu item
     */
    @FXML
    private void handleCart() {
        navigationController.navigateTo(NavigationController.CART_SCENE);
    }

    /**
     * Handle favorites menu item
     */
    @FXML
    private void handleFavorites() {
        // TODO: Navigate to favorites scene when implemented
        showInfo("اطلاع", "صفحه علاقه‌مندی‌ها در حال توسعه است");
    }

    /**
     * Handle logout menu item
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

    // ==================== INNER CLASSES ====================

    /**
     * Custom list cell for displaying restaurant information
     */
    private static class RestaurantListCell extends ListCell<Restaurant> {
        @Override
        protected void updateItem(Restaurant restaurant, boolean empty) {
            super.updateItem(restaurant, empty);
            
            if (empty || restaurant == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox content = createRestaurantCell(restaurant);
                setGraphic(content);
                setText(null);
            }
        }
        
        private VBox createRestaurantCell(Restaurant restaurant) {
            VBox vbox = new VBox(5);
            vbox.setPadding(new Insets(10));
            vbox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
            
            // Restaurant name
            Label nameLabel = new Label(restaurant.getName());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            
            // Restaurant address
            Label addressLabel = new Label("📍 " + restaurant.getAddress());
            addressLabel.setStyle("-fx-text-fill: #666666;");
            
            // Restaurant phone
            Label phoneLabel = new Label("📞 " + restaurant.getPhone());
            phoneLabel.setStyle("-fx-text-fill: #666666;");
            
            // Status
            Label statusLabel = new Label("وضعیت: " + getStatusText(restaurant.getStatus()));
            statusLabel.setStyle("-fx-text-fill: " + getStatusColor(restaurant.getStatus()) + ";");
            
            // Action buttons
            HBox buttonBox = new HBox(10);
            Button viewMenuButton = new Button("مشاهده منو");
            Button addToFavoritesButton = new Button("افزودن به علاقه‌مندی‌ها");
            
            viewMenuButton.setOnAction(e -> {
                // TODO: Navigate to menu view
                System.out.println("نمایش منوی رستوران: " + restaurant.getName());
            });
            
            addToFavoritesButton.setOnAction(e -> {
                // TODO: Add to favorites
                System.out.println("افزودن به علاقه‌مندی‌ها: " + restaurant.getName());
            });
            
            buttonBox.getChildren().addAll(viewMenuButton, addToFavoritesButton);
            
            vbox.getChildren().addAll(nameLabel, addressLabel, phoneLabel, statusLabel, buttonBox);
            
            return vbox;
        }
        
        private String getStatusText(String status) {
            switch (status.toUpperCase()) {
                case "APPROVED": return "تأیید شده";
                case "PENDING": return "در انتظار تأیید";
                case "REJECTED": return "رد شده";
                case "SUSPENDED": return "تعلیق شده";
                default: return status;
            }
        }
        
        private String getStatusColor(String status) {
            switch (status.toUpperCase()) {
                case "APPROVED": return "#4CAF50";
                case "PENDING": return "#FF9800";
                case "REJECTED": return "#F44336";
                case "SUSPENDED": return "#9E9E9E";
                default: return "#666666";
            }
        }
    }

    // ==================== DATA MODEL ====================

    /**
     * Restaurant data model for UI
     */
    public static class Restaurant {
        private Long id;
        private String name;
        private String address;
        private String phone;
        private String status;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
