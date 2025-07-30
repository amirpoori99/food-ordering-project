package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.Restaurant;
import com.foodi.appFrontend.models.Order;
import com.foodi.appFrontend.models.Transaction;
import com.foodi.appFrontend.models.User;
import com.foodi.appFrontend.models.FoodItem;
import com.foodi.appFrontend.models.ItemRating;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.tabs.dashbord.OrderDetailsController;
import com.foodi.appFrontend.tabs.dashbord.FoodItemDetailsController;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuyerDashboard {

    private static final Logger logger = LoggerFactory.getLogger(BuyerDashboard.class);

    @FXML
    private ListView<String> actionList;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private TabPane mainTabPane;

    // Tab navigation buttons
    @FXML
    private MFXButton restaurantsTab;
    @FXML
    private MFXButton orderHistoryTab;
    @FXML
    private MFXButton profileTab;
    @FXML
    private MFXButton favoritesTab;
    @FXML
    private MFXButton walletTab;

    @FXML
    private TextField searchRestaurantField;
    @FXML
    private TextField keywordsField;
    @FXML
    private TextField minRatingField;
    @FXML
    private TableView<Restaurant> restaurantsTable;
    @FXML
    private TableColumn<Restaurant, Integer> restaurantIdColumn;
    @FXML
    private TableColumn<Restaurant, String> restaurantNameColumn;
    @FXML
    private TableColumn<Restaurant, String> restaurantAddressColumn;
    @FXML
    private TableColumn<Restaurant, String> restaurantPhoneColumn;
    @FXML
    private TableColumn<Restaurant, Double> restaurantRatingColumn;

    // Food Items related fields
    @FXML
    private MFXButton foodItemsTab;
    @FXML
    private TextField searchFoodItemField;
    @FXML
    private TextField foodItemKeywordsField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private TableView<FoodItem> foodItemsTable;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemIdColumn;
    @FXML
    private TableColumn<FoodItem, String> foodItemNameColumn;
    @FXML
    private TableColumn<FoodItem, String> foodItemDescriptionColumn;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemPriceColumn;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemVendorIdColumn;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemSupplyColumn;
    @FXML
    private TableColumn<FoodItem, String> foodItemRatingColumn;

    @FXML
    private TableView<Order> orderHistoryTable;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, Integer> orderVendorColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;
    @FXML
    private TableColumn<Order, Integer> orderPriceColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn;

    // Order History Filters
    @FXML private ComboBox<String> buyerTimePeriodFilter;
    @FXML private ComboBox<String> buyerOrderStatusFilter;

    @FXML
    private ToggleGroup paymentMethodGroup;
    @FXML
    private RadioButton walletRadioButton;
    @FXML
    private RadioButton onlineRadioButton;

    @FXML
    private TableView<Restaurant> favoriteRestaurantTable;
    @FXML
    private TableColumn<Restaurant, Integer> favRestaurantIdColumn;
    @FXML
    private TableColumn<Restaurant, String> favRestaurantNameColumn;
    @FXML
    private TableColumn<Restaurant, String> favRestaurantAddressColumn;
    @FXML
    private TableColumn<Restaurant, String> favRestaurantPhoneColumn;

    @FXML
    private Label currentWalletBalanceLabel;
    @FXML
    private Label currentWalletBalanceLabel2;
    @FXML
    private TextField topUpAmountField;
    @FXML
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, Integer> transactionIdColumn;
    @FXML
    private TableColumn<Transaction, Integer> transactionOrderIdColumn;
    @FXML
    private TableColumn<Transaction, Integer> transactionUserIdColumn;
    @FXML
    private TableColumn<Transaction, String> transactionMethodColumn;
    @FXML
    private TableColumn<Transaction, String> transactionStatusColumn;

    @FXML
    private UserProfileController userProfileViewController;
    @FXML
    private MFXButton viewMenuButton;
    @FXML
    private AnchorPane myProfileContainer;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        ObservableList<String> actions = FXCollections.observableArrayList(
                "View Restaurants",
                "Order History",
                "My Profile",
                "Manage Favorites",
                "Wallet and Payments",
                "Logout"
        );
        if (actionList != null) {
            actionList.setItems(actions);
            actionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    handleActionSelection(newValue);
                }
            });
        }

        // Initialize Restaurant Table Columns
        if (restaurantIdColumn != null) restaurantIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (restaurantNameColumn != null) restaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (restaurantAddressColumn != null)
            restaurantAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (restaurantPhoneColumn != null)
            restaurantPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (restaurantRatingColumn != null)
            restaurantRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Initialize Food Items Table Columns
        if (foodItemIdColumn != null) foodItemIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (foodItemNameColumn != null) foodItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (foodItemDescriptionColumn != null) foodItemDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (foodItemPriceColumn != null) foodItemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (foodItemVendorIdColumn != null) foodItemVendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (foodItemSupplyColumn != null) foodItemSupplyColumn.setCellValueFactory(new PropertyValueFactory<>("supply"));
        if (foodItemRatingColumn != null) {
            foodItemRatingColumn.setCellValueFactory(cellData -> {
                FoodItem item = cellData.getValue();
                if (item != null && item.getRating() != null) {
                    return new javafx.beans.property.SimpleStringProperty(String.format("%.1f ⭐", item.getRating()));
                } else {
                    return new javafx.beans.property.SimpleStringProperty("No ratings");
                }
            });
        }

        // Initialize Order History Table Columns
        if (orderIdColumn != null) orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (orderVendorColumn != null) orderVendorColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (orderStatusColumn != null) orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (orderPriceColumn != null) orderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        if (orderDateColumn != null) orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        if (paymentMethodGroup != null && walletRadioButton != null) {
            walletRadioButton.setSelected(true);
        }

        // Initialize Order History Filters
        if (buyerTimePeriodFilter != null) {
            ObservableList<String> timePeriods = FXCollections.observableArrayList(
                "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 3 Months", "Last 6 Months", "Last Year"
            );
            buyerTimePeriodFilter.setItems(timePeriods);
            buyerTimePeriodFilter.getSelectionModel().selectFirst();
            buyerTimePeriodFilter.valueProperty().addListener((obs, oldVal, newVal) -> viewOrderHistory());
        }

        if (buyerOrderStatusFilter != null) {
            ObservableList<String> orderStatuses = FXCollections.observableArrayList(
                "All Statuses", "submitted", "unpaid_and_cancelled", "waiting_vendor",
                "cancelled", "finding_courier", "on_the_way", "completed", "accepted", "rejected", "served"
            );
            buyerOrderStatusFilter.setItems(orderStatuses);
            buyerOrderStatusFilter.getSelectionModel().selectFirst();
            buyerOrderStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> viewOrderHistory());
        }

        if (favoriteRestaurantTable != null) {
            favRestaurantIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            favRestaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            favRestaurantAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            favRestaurantPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        }
        if (transactionsTable != null) {
            transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            transactionOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
            transactionUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("UserId"));
            transactionMethodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
            transactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        // Load initial data
        viewRestaurants();
        viewFoodItems();
        viewOrderHistory();
        viewFavoriteRestaurants();
        viewWalletAndPayments();
        updateTabStyles();
    }

    // Tab navigation methods
    @FXML
    private void switchToRestaurantsTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(0);
            updateTabStyles();
            viewRestaurants();
        }
    }

    @FXML
    private void switchToFoodItemsTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(1);
            updateTabStyles();
            viewFoodItems();
        }
    }

    @FXML
    private void switchToOrderHistoryTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(2);
            updateTabStyles();
            viewOrderHistory();
        }
    }

    @FXML
    private void switchToProfileTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(3);
            updateTabStyles();
            loadUserProfileView();
        }
    }

    @FXML
    private void switchToFavoritesTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(4);
            updateTabStyles();
            viewFavoriteRestaurants();
        }
    }

    @FXML
    private void switchToWalletTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(5);
            updateTabStyles();
            viewWalletAndPayments();
        }
    }

    private void updateTabStyles() {
        if (mainTabPane == null) return;

        int selectedIndex = mainTabPane.getSelectionModel().getSelectedIndex();

        // Reset all tab styles to inactive
        String inactiveStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #6b7280; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 500; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: white; -fx-text-fill: #374151; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-border-color: #e2e8f0; -fx-border-width: 1 1 0 1; -fx-background-radius: 8 8 0 0; -fx-border-radius: 8 8 0 0; -fx-cursor: hand;";

        if (restaurantsTab != null) restaurantsTab.setStyle(selectedIndex == 0 ? activeStyle : inactiveStyle);
        if (foodItemsTab != null) foodItemsTab.setStyle(selectedIndex == 1 ? activeStyle : inactiveStyle);
        if (orderHistoryTab != null) orderHistoryTab.setStyle(selectedIndex == 2 ? activeStyle : inactiveStyle);
        if (profileTab != null) profileTab.setStyle(selectedIndex == 3 ? activeStyle : inactiveStyle);
        if (favoritesTab != null) favoritesTab.setStyle(selectedIndex == 4 ? activeStyle : inactiveStyle);
        if (walletTab != null) walletTab.setStyle(selectedIndex == 5 ? activeStyle : inactiveStyle);
    }

    private void handleActionSelection(String action) {
        switch (action) {
            case "View Restaurants":
                switchToRestaurantsTab();
                break;
            case "Order History":
                switchToOrderHistoryTab();
                break;
            case "My Profile":
                switchToProfileTab();
                break;
            case "Manage Favorites":
                switchToFavoritesTab();
                break;
            case "Wallet and Payments":
                switchToWalletTab();
                break;
            case "Logout":
                logout();
                break;
            default:
                break;
        }
    }

    private void loadUserProfileView() {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Loading profile view...");
        }
        executorService.submit(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/UserProfileView.fxml"));
                Parent userProfileView = loader.load();

                Platform.runLater(() -> {
                    if (myProfileContainer != null) {
                        myProfileContainer.getChildren().setAll(userProfileView);
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Profile view loaded successfully.");
                        }
                    } else {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Error: My profile container (myProfileContainer) is null in FXML.");
                        }
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("Critical Error loading User Profile View dynamically: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewRestaurants() {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Loading restaurants...");
        }
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token missing. Please log in again.");
                        }
                    });
                    return;
                }

                Map<String, Object> requestBody = new HashMap<>();
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);

                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/vendors", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Restaurant> restaurants = JsonUtil.getObjectMapper().readerForListOf(Restaurant.class).readValue(rootNode);
                                ObservableList<Restaurant> restaurantObservableList = FXCollections.observableArrayList(restaurants);
                                if (restaurantsTable != null) {
                                    restaurantsTable.setItems(restaurantObservableList);
                                }
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Restaurants loaded successfully.");
                                }
                            } catch (IOException e) {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing restaurant data: " + e.getMessage());
                                }
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error viewing restaurants: " + errorMessage);
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for restaurants.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred while fetching restaurants: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void searchRestaurants() {
        String searchTerm = searchRestaurantField != null ? searchRestaurantField.getText().trim() : "";
        String keywordsText = keywordsField != null ? keywordsField.getText().trim() : "";
        String minRatingText = minRatingField != null ? minRatingField.getText().trim() : "";
        
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Searching for restaurants...");
        }
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token missing. Please log in again.");
                        }
                    });
                    return;
                }

                Map<String, Object> requestBody = new HashMap<>();
                
                // Add search term if provided
                if (!searchTerm.isEmpty()) {
                    requestBody.put("search", searchTerm);
                }
                
                // Add keywords if provided
                if (!keywordsText.isEmpty()) {
                    String[] keywords = keywordsText.split(",\\s*");
                    requestBody.put("keywords", keywords);
                }
                
                // Add min rating if provided
                if (!minRatingText.isEmpty()) {
                    try {
                        double minRating = Double.parseDouble(minRatingText);
                        if (minRating >= 0.0 && minRating <= 5.0) {
                            requestBody.put("min_rating", minRating);
                        } else {
                            Platform.runLater(() -> {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Min rating must be between 0.0 and 5.0");
                                }
                            });
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Platform.runLater(() -> {
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Invalid min rating format. Please enter a number between 0.0 and 5.0");
                            }
                        });
                        return;
                    }
                }
                
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);

                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/vendors", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Restaurant> restaurants = JsonUtil.getObjectMapper().readerForListOf(Restaurant.class).readValue(rootNode);
                                ObservableList<Restaurant> restaurantObservableList = FXCollections.observableArrayList(restaurants);
                                if (restaurantsTable != null) {
                                    restaurantsTable.setItems(restaurantObservableList);
                                }
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Search completed.");
                                }
                            } catch (IOException e) {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing search results: " + e.getMessage());
                                }
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error searching restaurants: " + errorMessage);
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for search.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred during search: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewFoodItems() {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Loading food items...");
        }
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token missing. Please log in again.");
                        }
                    });
                    return;
                }

                // Call the /items endpoint with empty body to get all items
                String jsonBody = "{}";
                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/items", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<FoodItem> foodItems = JsonUtil.getObjectMapper().readerForListOf(FoodItem.class).readValue(rootNode);
                                
                                // Load ratings for each food item
                                loadFoodItemRatings(foodItems, token);
                                
                                ObservableList<FoodItem> foodItemObservableList = FXCollections.observableArrayList(foodItems);
                                if (foodItemsTable != null) {
                                    foodItemsTable.setItems(foodItemObservableList);
                                }
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Food items loaded successfully. Found " + foodItems.size() + " items.");
                                }
                            } catch (IOException e) {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing food items: " + e.getMessage());
                                }
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error viewing food items: " + errorMessage);
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for food items.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred while fetching food items: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }
    
    private void loadFoodItemRatings(List<FoodItem> foodItems, String token) {
        executorService.submit(() -> {
            try {
                for (FoodItem foodItem : foodItems) {
                    try {
                        // Call the ratings endpoint for each food item
                        String ratingUrl = "/ratings/items/" + foodItem.getId();
                        Optional<HttpResponse<String>> ratingResponseOpt = ApiClient.get(ratingUrl, token);
                        
                        if (ratingResponseOpt.isPresent()) {
                            HttpResponse<String> ratingResponse = ratingResponseOpt.get();
                            
                            if (ratingResponse.statusCode() == 200) {
                                try {
                                    JsonNode ratingRootNode = JsonUtil.getObjectMapper().readTree(ratingResponse.body());
                                    ItemRating itemRating = JsonUtil.getObjectMapper().treeToValue(ratingRootNode, ItemRating.class);
                                
                                // Set the average rating on the food item
                                if (itemRating != null && itemRating.getAvgRating() != null) {
                                    Platform.runLater(() -> {
                                        foodItem.setRating(itemRating.getAvgRating());
                                        // Refresh the table to show the updated rating
                                        if (foodItemsTable != null) {
                                            foodItemsTable.refresh();
                                        }
                                    });
                                }
                                } catch (Exception e) {
                                    logger.error("Error parsing rating response for food item {}: {}", foodItem.getId(), e.getMessage());
                                    // Set rating to null on parsing error
                                    Platform.runLater(() -> {
                                        foodItem.setRating(null);
                                        if (foodItemsTable != null) {
                                            foodItemsTable.refresh();
                                        }
                                    });
                                }
                            } else if (ratingResponse.statusCode() == 400 || ratingResponse.statusCode() == 404) {
                                // Item has no ratings yet - this is normal for new items
                                Platform.runLater(() -> {
                                    foodItem.setRating(null); // Explicitly set to null to show "No ratings"
                                    if (foodItemsTable != null) {
                                        foodItemsTable.refresh();
                                    }
                                });
                            } else {
                                logger.warn("Failed to load rating for food item {}: {}", foodItem.getId(), ratingResponse.statusCode());
                            }
                        } else {
                            logger.warn("No response received for food item rating {}", foodItem.getId());
                        }
                    } catch (Exception e) {
                        logger.error("Error loading rating for food item {}", foodItem.getId(), e);
                    }
                }
            } catch (Exception e) {
                logger.error("Error in loadFoodItemRatings", e);
            }
        });
    }

    @FXML
    private void searchFoodItems() {
        String searchTerm = searchFoodItemField != null ? searchFoodItemField.getText().trim() : "";
        String keywordsText = foodItemKeywordsField != null ? foodItemKeywordsField.getText().trim() : "";
        String maxPriceText = maxPriceField != null ? maxPriceField.getText().trim() : "";
        
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Searching for food items...");
        }
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token missing. Please log in again.");
                        }
                    });
                    return;
                }

                Map<String, Object> requestBody = new HashMap<>();
                
                // Add search term if provided
                if (!searchTerm.isEmpty()) {
                    requestBody.put("search", searchTerm);
                }
                
                // Add keywords if provided
                if (!keywordsText.isEmpty()) {
                    String[] keywords = keywordsText.split(",\\s*");
                    requestBody.put("keywords", keywords);
                }
                
                // Add max price if provided
                if (!maxPriceText.isEmpty()) {
                    try {
                        int maxPrice = Integer.parseInt(maxPriceText);
                        if (maxPrice > 0) {
                            requestBody.put("price", maxPrice);
                        } else {
                            Platform.runLater(() -> {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Max price must be a positive number");
                                }
                            });
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Platform.runLater(() -> {
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Invalid max price format. Please enter a valid number");
                            }
                        });
                        return;
                    }
                }
                
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);

                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/items", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<FoodItem> foodItems = JsonUtil.getObjectMapper().readerForListOf(FoodItem.class).readValue(rootNode);
                                
                                // Load ratings for each food item
                                loadFoodItemRatings(foodItems, token);
                                
                                ObservableList<FoodItem> foodItemObservableList = FXCollections.observableArrayList(foodItems);
                                if (foodItemsTable != null) {
                                    foodItemsTable.setItems(foodItemObservableList);
                                }
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Food items search completed. Found " + foodItems.size() + " items.");
                                }
                            } catch (IOException e) {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing food items search results: " + e.getMessage());
                                }
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error searching food items: " + errorMessage);
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for food items search.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred while searching food items: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleViewRestaurant(ActionEvent event) {
        logger.info("handleViewRestaurant called from food items search");
        
        if (foodItemsTable == null) {
            logger.error("Food items table is null");
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Food items table not found.");
            }
            return;
        }

        FoodItem selectedFoodItem = foodItemsTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            logger.warn("No food item selected in table");
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select a food item to view its restaurant.");
            }
            return;
        }

        logger.info("Selected food item: ID={}, Name={}, VendorID={}", 
                   selectedFoodItem.getId(), selectedFoodItem.getName(), selectedFoodItem.getVendorId());

        // Get the vendor ID from the selected food item
        Integer vendorId = selectedFoodItem.getVendorId();
        if (vendorId == null) {
            logger.error("Vendor ID is null for selected food item: {}", selectedFoodItem.getId());
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("No restaurant information available for this food item.");
            }
            return;
        }

        logger.info("Attempting to fetch restaurant details for vendor ID: {}", vendorId);
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Loading restaurant details...");
        }

        // Fetch restaurant details by vendor ID
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token missing. Please log in again.");
                        }
                    });
                    return;
                }

                logger.info("Making API call to /vendors/{}", vendorId.toString());
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/vendors/" + vendorId.toString(), token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    logger.info("API response received - Status: {}, Body length: {}", 
                               response.statusCode(), response.body().length());
                    logger.debug("API response body: {}", response.body());
                    
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            logger.info("API call successful, processing restaurant data");
                            try {
                                logger.info("Parsing restaurant data from JSON response");
                                // Extract vendor data from the nested structure
                                JsonNode vendorNode = rootNode.get("vendor");
                                if (vendorNode == null) {
                                    logger.error("No vendor data found in API response");
                                    if (errorMessageLabel != null) {
                                        errorMessageLabel.setText("Invalid restaurant data received from server.");
                                    }
                                    return;
                                }
                                
                                Restaurant restaurant = JsonUtil.getObjectMapper().treeToValue(vendorNode, Restaurant.class);
                                logger.info("Restaurant parsed successfully: ID={}, Name={}", 
                                           restaurant.getId(), restaurant.getName());
                                
                                // Check if the restaurant has menu data
                                JsonNode menuTitleNode = rootNode.get("menu_titles");
                                logger.info("Menu titles node: {}", menuTitleNode);
                                if (menuTitleNode == null || !menuTitleNode.isArray() || menuTitleNode.size() == 0) {
                                    logger.warn("Restaurant has no menu items - menuTitleNode: {}", menuTitleNode);
                                    if (errorMessageLabel != null) {
                                        errorMessageLabel.setText("This restaurant doesn't have any menu items available yet.");
                                    }
                                    return;
                                }
                                logger.info("Restaurant has {} menu categories", menuTitleNode.size());
                                
                                // Open restaurant menu view using shared method
                                openRestaurantMenuView(restaurant, "Restaurant: " + restaurant.getName());
                            } catch (IOException e) {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing restaurant data: " + e.getMessage());
                                }
                                e.printStackTrace();
                            }
                        } else if (response.statusCode() == 404) {
                            logger.error("Restaurant not found - 404 response for vendor ID: {}", vendorId);
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Restaurant not found. The restaurant with ID " + vendorId + " may not exist or may have been removed.");
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            logger.error("API error response - Status: {}, Error: {}", response.statusCode(), errorMessage);
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error loading restaurant details: " + errorMessage + " (Status: " + response.statusCode() + ")");
                            }
                        }
                    });
                } else {
                    logger.error("No API response received for vendor ID: {}", vendorId);
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for restaurant details.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Exception during API call for vendor ID: {}", vendorId, e);
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred while loading restaurant details: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleViewFoodItemDetails(ActionEvent event) {
        logger.info("handleViewFoodItemDetails called from food items search");
        
        if (foodItemsTable == null) {
            logger.error("Food items table is null");
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Food items table not found.");
            }
            return;
        }

        FoodItem selectedFoodItem = foodItemsTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            logger.warn("No food item selected in table");
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select a food item to view details.");
            }
            return;
        }

        logger.info("Selected food item for details: ID={}, Name={}, VendorID={}", 
                   selectedFoodItem.getId(), selectedFoodItem.getName(), selectedFoodItem.getVendorId());

        // Get the vendor ID from the selected food item
        Integer vendorId = selectedFoodItem.getVendorId();
        if (vendorId == null) {
            logger.error("Vendor ID is null for selected food item: {}", selectedFoodItem.getId());
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("No restaurant information available for this food item.");
            }
            return;
        }

        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Loading restaurant details...");
        }

        // Fetch restaurant details by vendor ID
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token missing. Please log in again.");
                        }
                    });
                    return;
                }

                logger.info("Making API call to /vendors/{} for restaurant details", vendorId.toString());
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/vendors/" + vendorId.toString(), token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    logger.info("API response received - Status: {}, Body length: {}", 
                               response.statusCode(), response.body().length());
                    
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                // Extract vendor data from the nested structure
                                JsonNode vendorNode = rootNode.get("vendor");
                                if (vendorNode == null) {
                                    logger.error("No vendor data found in API response");
                                    if (errorMessageLabel != null) {
                                        errorMessageLabel.setText("Invalid restaurant data received from server.");
                                    }
                                    return;
                                }
                                
                                Restaurant restaurant = JsonUtil.getObjectMapper().treeToValue(vendorNode, Restaurant.class);
                                logger.info("Restaurant parsed successfully: ID={}, Name={}", 
                                           restaurant.getId(), restaurant.getName());

                                // Open food item details view with restaurant info
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/FoodItemDetailsView.fxml"));
                                    Parent foodItemDetailsView = loader.load();

                                    FoodItemDetailsController controller = loader.getController();
                                    controller.setFoodItem(selectedFoodItem);
                                    controller.setRestaurant(restaurant);

                                    Stage stage = new Stage();
                                    stage.initModality(Modality.APPLICATION_MODAL);
                                    stage.setTitle("Food Item Details - " + selectedFoodItem.getName());
                                    Scene scene = new Scene(foodItemDetailsView);
                                    scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
                                    stage.setScene(scene);
                                    stage.showAndWait();

                                    if (errorMessageLabel != null) {
                                        errorMessageLabel.setText("Food item details opened successfully.");
                                    }
                                } catch (IOException e) {
                                    logger.error("Error opening food item details", e);
                                    if (errorMessageLabel != null) {
                                        errorMessageLabel.setText("Error opening food item details: " + e.getMessage());
                                    }
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                logger.error("Error parsing restaurant data", e);
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing restaurant data: " + e.getMessage());
                                }
                                e.printStackTrace();
                            }
                        } else if (response.statusCode() == 404) {
                            logger.error("Restaurant not found - 404 response for vendor ID: {}", vendorId);
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Restaurant not found. The restaurant with ID " + vendorId + " may not exist or may have been removed.");
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            logger.error("API error response - Status: {}, Error: {}", response.statusCode(), errorMessage);
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error loading restaurant details: " + errorMessage + " (Status: " + response.statusCode() + ")");
                            }
                        }
                    });
                } else {
                    logger.error("No API response received for vendor ID: {}", vendorId);
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for restaurant details.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Exception during API call for vendor ID: {}", vendorId, e);
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred while loading restaurant details: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewOrderHistory() {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Loading order history...");
        }
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token missing. Please log in again.");
                        }
                    });
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/orders/history", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                
                                // Apply filters
                                List<Order> filteredOrders = applyOrderFilters(orders);
                                
                                ObservableList<Order> orderObservableList = FXCollections.observableArrayList(filteredOrders);
                                if (orderHistoryTable != null) {
                                    orderHistoryTable.setItems(orderObservableList);
                                }
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Order history loaded successfully. Showing " + filteredOrders.size() + " of " + orders.size() + " orders.");
                                }
                            } catch (IOException e) {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing order history: " + e.getMessage());
                                }
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error viewing order history: " + errorMessage);
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for order history.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred while fetching order history: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleMakePayment(ActionEvent event) {
        if (orderHistoryTable == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Order history table not found.");
            }
            return;
        }

        Order selectedOrder = orderHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select an order to pay");
            }
            return;
        }

        String paymentMethod;
        if (paymentMethodGroup != null && paymentMethodGroup.getSelectedToggle() != null) {
            paymentMethod = (String) paymentMethodGroup.getSelectedToggle().getUserData();
        } else {
            paymentMethod = null;
        }

        if (paymentMethod == null || paymentMethod.isEmpty()) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select a payment method");
            }
            return;
        }

        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Paying price with order id : " + selectedOrder.getId());
        }

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token is missing. Please login again.");
                        }
                    });
                    return;
                }

                Integer orderId = selectedOrder.getId();
                Map<String, Object> paymentData = new HashMap<>();
                paymentData.put("order_id", orderId);
                paymentData.put("method", paymentMethod);
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(paymentData);
                Optional<HttpResponse<String>> responseOptional = ApiClient.post("/payment/online", jsonBody, token);

                if (responseOptional.isPresent()) {
                    HttpResponse<String> response = responseOptional.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Payment was successful");
                            }
                            viewOrderHistory();
                            viewWalletAndPayments();
                        } else {
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error in payment :" + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Could not connect to server to Payment");
                        }
                    });
                }

            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred during payment: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewFavoriteRestaurants() {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Loading favorite restaurant ...");
        }
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token missing. Please log in again.");
                        }
                    });
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/favorites", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Restaurant> favorites = JsonUtil.getObjectMapper().readerForListOf(Restaurant.class).readValue(rootNode);
                                ObservableList<Restaurant> favoriteObservableList = FXCollections.observableArrayList(favorites);
                                if (favoriteRestaurantTable != null) {
                                    favoriteRestaurantTable.setItems(favoriteObservableList);
                                    if (errorMessageLabel != null) {
                                        errorMessageLabel.setText("Favorite restaurants successfully loaded.");
                                    }
                                } else {
                                    if (errorMessageLabel != null) {
                                        errorMessageLabel.setText("Favorite restaurants table not found.");
                                    }
                                }
                            } catch (IOException e) {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing favorite restaurants data:" + e.getMessage());
                                }
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error viewing favorite restaurants:" + errorMessage);
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Could not connect to server for favorite restaurants.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("Unexpected error fetching favorite restaurants: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleAddFavorite(javafx.event.ActionEvent event) {
        if (restaurantsTable == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Restaurants table not found.");
            }
            return;
        }

        Restaurant selectedRestaurant = restaurantsTable.getSelectionModel().getSelectedItem();
        if (selectedRestaurant == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select a restaurant to add to favorites.");
            }
            return;
        }

        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Adding " + selectedRestaurant.getName() + " to favorite...");
        }
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token does not exist. Please log in again.");
                        }
                    });
                    return;
                }

                String restaurantId = selectedRestaurant.getId().toString();
                Optional<HttpResponse<String>> responseOpt = ApiClient.put("/favorites/" + restaurantId, "", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Restaurant successfully added to favorites!");
                            }
                            viewFavoriteRestaurants();
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error in adding to favorite: " + errorMessage);
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Could not connect to server to add to favorites.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("Unexpected error adding to favorites:" + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleRemoveFavorite(javafx.event.ActionEvent event) {
        if (favoriteRestaurantTable == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Favorite restaurants table not found.");
            }
            return;
        }

        Restaurant selectedFavorite = favoriteRestaurantTable.getSelectionModel().getSelectedItem();
        if (selectedFavorite == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select a restaurant to remove from favorites.");
            }
            return;
        }

        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Removing " + selectedFavorite.getName() + " from favorites...");
        }
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token does not exist. Please log in again.");
                        }
                    });
                    return;
                }

                String restaurantId = selectedFavorite.getId().toString();
                Optional<HttpResponse<String>> responseOpt = ApiClient.delete("/favorites/" + restaurantId, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "The restaurant was successfully removed from favorites.");
                            }
                            viewFavoriteRestaurants();
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error during removing: " + errorMessage);
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Could not connect to server to remove from favorites.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("Unexpected error while removing from favorites:" + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewWalletAndPayments() {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("Loading wallet and payment info ...");
        }
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();

                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Authentication token missing. Please log in again.");
                        }
                    });
                    return;
                }

                // Load user profile to get user ID
                Optional<HttpResponse<String>> profileResponseOptional = ApiClient.get("/auth/profile", token);
                if (profileResponseOptional.isPresent()) {
                    HttpResponse<String> profileResponse = profileResponseOptional.get();
                    JsonNode profileRootNode = JsonUtil.getObjectMapper().readTree(profileResponse.body());
                    
                    if (profileResponse.statusCode() == 200) {
                        try {
                            logger.info("profileRootNode: {}", profileRootNode);
                            User currentUser = JsonUtil.getObjectMapper().treeToValue(profileRootNode, User.class);
                            
                            // Load wallet balance using the new API endpoint
                            loadWalletBalance(currentUser.getId(), token);
                            
                        } catch (IOException e) {
                            logger.error("Error parsing profile data", e);
                            Platform.runLater(() -> {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing profile data " + e.getMessage());
                                }
                            });
                        }
                    } else {
                        String errorMessage = profileRootNode.has("error") ? profileRootNode.get("error").asText() : "An Unknown error occurred.";
                        logger.error("Error loading profile: {}", errorMessage);
                        Platform.runLater(() -> {
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error loading profile: " + errorMessage);
                            }
                        });
                    }
                } else {
                    logger.error("No response received when loading profile");
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for profile.");
                        }
                    });
                }

                Optional<HttpResponse<String>> transactionResponseOpt = ApiClient.get("/transactions", token);
                if (transactionResponseOpt.isPresent()) {
                    HttpResponse<String> transactionResponse = transactionResponseOpt.get();
                    JsonNode transactionRootNode = JsonUtil.getObjectMapper().readTree(transactionResponse.body());
                    Platform.runLater(() -> {
                        if (transactionResponse.statusCode() == 200) {
                            try {
                                List<Transaction> transactions = JsonUtil.getObjectMapper().readerForListOf(Transaction.class).readValue(transactionRootNode);
                                ObservableList<Transaction> transactionObservableList = FXCollections.observableArrayList(transactions);
                                if (transactionsTable != null) {
                                    transactionsTable.setItems(transactionObservableList);
                                }
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Wallet and transaction history loaded successfully.");
                                }
                            } catch (IOException e) {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing transaction data " + e.getMessage());
                                }
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = transactionRootNode.has("error") ? transactionRootNode.get("error").asText() : "An unknown error occurred ";
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error viewing transaction :" + errorMessage);
                            }
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for transaction history");
                        }
                    });
                }

            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred while loading wallet and transactions: " + e.getMessage());
                    }
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleTopUp(ActionEvent event) {
        String amountText = topUpAmountField != null ? topUpAmountField.getText() : "";
        if (amountText.isEmpty()) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please enter an amount to top up");
            }
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                if (errorMessageLabel != null) {
                    errorMessageLabel.setText("Amount must be a positive number.");
                }
                return;
            }
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Topping up wallet with " + amount + "...");
            }
            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Authentication token missing.Please login again.");
                            }
                        });
                        return;
                    }
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("amount", amount);
                    String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);
                    Optional<HttpResponse<String>> responseOptional = ApiClient.post("/wallet/top-up", jsonBody, token);
                    if (responseOptional.isPresent()) {
                        HttpResponse<String> response = responseOptional.get();
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Wallet topped up successfully");
                                }
                                if (topUpAmountField != null) {
                                    topUpAmountField.clear();
                                }
                                viewWalletAndPayments();
                            } else {
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error topping up wallet :" + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                                }
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Failed to connect to server for top up");
                            }
                        });
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("An unexpected error in during top up : " + e.getMessage());
                        }
                        e.printStackTrace();
                    });
                }
            });
        } catch (NumberFormatException e) {
            Platform.runLater(() -> {
                if (errorMessageLabel != null) {
                    errorMessageLabel.setText("Please enter a valid number for amount.");
                }
            });
        }
    }

    @FXML
    private void logout() {
        AuthManager.logout();
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) (actionList != null ? actionList.getScene().getWindow() :
                        errorMessageLabel != null ? errorMessageLabel.getScene().getWindow() : null);
                if (stage != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/login.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
                    stage.setScene(scene);
                    stage.setTitle("Fooody - Login");
                    stage.show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (errorMessageLabel != null) {
                    errorMessageLabel.setText("Error navigating to login: " + e.getMessage());
                }
            }
        });
    }

    private List<Order> applyOrderFilters(List<Order> orders) {
        return orders.stream()
                .filter(order -> {
                    // Apply status filter
                    if (buyerOrderStatusFilter != null && buyerOrderStatusFilter.getValue() != null && 
                        !buyerOrderStatusFilter.getValue().equals("All Statuses")) {
                        if (!order.getStatus().equalsIgnoreCase(buyerOrderStatusFilter.getValue())) {
                            return false;
                        }
                    }
                    
                    // Apply time period filter
                    if (buyerTimePeriodFilter != null && buyerTimePeriodFilter.getValue() != null && 
                        !buyerTimePeriodFilter.getValue().equals("All Time")) {
                        try {
                            LocalDateTime orderDate = LocalDateTime.parse(order.getCreatedAt());
                            LocalDateTime now = LocalDateTime.now();
                            
                            switch (buyerTimePeriodFilter.getValue()) {
                                case "Today":
                                    return orderDate.toLocalDate().equals(now.toLocalDate());
                                case "Last 7 Days":
                                    return orderDate.isAfter(now.minusDays(7));
                                case "Last 30 Days":
                                    return orderDate.isAfter(now.minusDays(30));
                                case "Last 3 Months":
                                    return orderDate.isAfter(now.minusMonths(3));
                                case "Last 6 Months":
                                    return orderDate.isAfter(now.minusMonths(6));
                                case "Last Year":
                                    return orderDate.isAfter(now.minusYears(1));
                                default:
                                    return true;
                            }
                        } catch (Exception e) {
                            // If date parsing fails, include the order
                            return true;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (userProfileViewController != null) {
            userProfileViewController.shutdown();
        }
    }

    // Load wallet balance using the new API endpoint
    private void loadWalletBalance(String userId, String token) {
        logger.info("Loading wallet balance for user ID: {}", userId);
        
        executorService.submit(() -> {
            try {
                Optional<HttpResponse<String>> balanceResponseOptional = ApiClient.get("/auth/balance/" + userId, token);
                
                if (balanceResponseOptional.isPresent()) {
                    HttpResponse<String> balanceResponse = balanceResponseOptional.get();
                    JsonNode balanceRootNode = JsonUtil.getObjectMapper().readTree(balanceResponse.body());
                    logger.debug("Balance response status: {}", balanceResponse.statusCode());
                    logger.debug("Balance response body: {}", balanceResponse.body());
                    
                    Platform.runLater(() -> {
                        if (balanceResponse.statusCode() == 200) {
                            try {
                                BigDecimal balance = balanceRootNode.has("balance") ? 
                                    new BigDecimal(balanceRootNode.get("balance").asText()) : BigDecimal.ZERO;
                                
                                String balanceText = "$" + balance.toPlainString();
                                logger.info("Wallet balance loaded: {}", balanceText);
                                
                                if (currentWalletBalanceLabel != null) {
                                    currentWalletBalanceLabel.setText(balanceText);
                                }
                                if (currentWalletBalanceLabel2 != null) {
                                    currentWalletBalanceLabel2.setText(balanceText);
                                }
                                
                            } catch (Exception e) {
                                logger.error("Error parsing balance data", e);
                                if (errorMessageLabel != null) {
                                    errorMessageLabel.setText("Error parsing balance data: " + e.getMessage());
                                }
                            }
                        } else {
                            String errorMessage = balanceRootNode.has("error") ? 
                                balanceRootNode.get("error").asText() : "An unknown error occurred.";
                            logger.error("Error loading wallet balance: {}", errorMessage);
                            if (errorMessageLabel != null) {
                                errorMessageLabel.setText("Error loading wallet balance: " + errorMessage);
                            }
                        }
                    });
                } else {
                    logger.error("No response received when loading wallet balance");
                    Platform.runLater(() -> {
                        if (errorMessageLabel != null) {
                            errorMessageLabel.setText("Failed to connect to server for wallet balance.");
                        }
                    });
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Exception occurred while loading wallet balance", e);
                Platform.runLater(() -> {
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("An unexpected error occurred while loading wallet balance: " + e.getMessage());
                    }
                });
            }
        });
    }

    @FXML
    private void handleRateSelectedOrder(ActionEvent event) {
        if (orderHistoryTable == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Order history table not found.");
            }
            return;
        }

        Order selectedOrder = orderHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select an order to rate.");
            }
            return;
        }

        // Check if the order is completed
        if (!"COMPLETED".equals(selectedOrder.getStatus())) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("You can only rate completed orders.");
            }
            return;
        }

        handleRateOrder(selectedOrder);
    }

    private void handleRateOrder(Order order) {
        try {
            // Load the rating dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/RatingDialogView.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the order
            RatingDialogController controller = loader.getController();
            controller.setOrder(order);
            
            // Create and configure the rating window
            Stage ratingWindow = new Stage();
            ratingWindow.setTitle("Rate Order #" + order.getId());
            ratingWindow.setScene(new Scene(root));
            ratingWindow.setResizable(true);
            
            // Set window properties - no size limits
            ratingWindow.setMinWidth(600);
            ratingWindow.setMinHeight(700);
            
            // Center the window on the screen
            ratingWindow.centerOnScreen();
            
            // Ensure the window appears in front
            ratingWindow.toFront();
            
            // Set the window stage in the controller
            controller.setDialogStage(ratingWindow);
            
            // Show the window
            ratingWindow.show();
            
            // Since it's a non-modal window, we'll refresh the order history when the window closes
            ratingWindow.setOnCloseRequest(event -> {
                if (controller.isRatingSubmitted()) {
                    viewOrderHistory();
                    if (errorMessageLabel != null) {
                        errorMessageLabel.setText("Rating submitted successfully!");
                        errorMessageLabel.setStyle("-fx-text-fill: #10b981;");
                    }
                }
            });
            
        } catch (IOException e) {
            logger.error("Error opening rating dialog", e);
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Error opening rating dialog: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewOrderDetails(ActionEvent event) {
        if (orderHistoryTable == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Order history table not found.");
            }
            return;
        }

        Order selectedOrder = orderHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select an order to view details.");
            }
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/OrderDetailsView.fxml"));
            Parent orderDetailsView = loader.load();

            OrderDetailsController controller = loader.getController();
            controller.setOrder(selectedOrder);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Order Details - Order #" + selectedOrder.getId());
            Scene scene = new Scene(orderDetailsView);
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Error opening order details: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewMenu(ActionEvent event) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("");
        }

        if (restaurantsTable == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Restaurants table not found.");
            }
            return;
        }

        Restaurant selectedRestaurant = restaurantsTable.getSelectionModel().getSelectedItem();
        if (selectedRestaurant == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select a restaurant to view its menu.");
            }
            return;
        }

        openRestaurantMenuView(selectedRestaurant, "Menu for " + selectedRestaurant.getName());
    }

    @FXML
    private void handleViewFavoriteMenu(ActionEvent event) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("");
        }

        if (favoriteRestaurantTable == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Favorite restaurants table not found.");
            }
            return;
        }

        Restaurant selectedRestaurant = favoriteRestaurantTable.getSelectionModel().getSelectedItem();
        if (selectedRestaurant == null) {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Please select a favorite restaurant to view its menu.");
            }
            return;
        }

        openRestaurantMenuView(selectedRestaurant, "Menu for " + selectedRestaurant.getName() + " (Favorite)");
    }

    /**
     * Shared method to open restaurant menu view
     * @param restaurant The restaurant to display
     * @param windowTitle The title for the window
     */
    private void openRestaurantMenuView(Restaurant restaurant, String windowTitle) {
        try {
            logger.info("Opening restaurant menu view for: ID={}, Name={}", restaurant.getId(), restaurant.getName());
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/RestaurantMenuView.fxml"));
            Parent restaurantMenuView = loader.load();

            RestaurantMenuController controller = loader.getController();
            controller.setRestaurant(restaurant);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(windowTitle);
            Scene scene = new Scene(restaurantMenuView);
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Restaurant view opened successfully.");
            }
        } catch (IOException e) {
            logger.error("Error opening restaurant menu view", e);
            if (errorMessageLabel != null) {
                errorMessageLabel.setText("Error opening restaurant view: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
}