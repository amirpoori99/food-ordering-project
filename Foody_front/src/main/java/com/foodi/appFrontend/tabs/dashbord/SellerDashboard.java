package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.FoodItem;
import com.foodi.appFrontend.models.Restaurant;
import com.foodi.appFrontend.models.Order;
import com.foodi.appFrontend.models.Transaction;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import io.github.palexdev.materialfx.controls.MFXButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SellerDashboard {

    private static final Logger logger = LoggerFactory.getLogger(SellerDashboard.class);

    @FXML
    private Label errorMessageLabel;
    @FXML
    private TabPane mainTabPane;

    @FXML
    private TableView<Restaurant> myRestaurantsTable;
    @FXML
    private TableColumn<Restaurant, String> myRestaurantIdColumn;
    @FXML
    private TableColumn<Restaurant, String> myRestaurantNameColumn;
    @FXML
    private TableColumn<Restaurant, String> myRestaurantAddressColumn;
    @FXML
    private TableColumn<Restaurant, String> myRestaurantPhoneColumn;
    @FXML
    private TableColumn<Restaurant, Integer> myRestaurantTaxFeeColumn;
    @FXML
    private TableColumn<Restaurant, Integer> myRestaurantAdditionalFeeColumn;

    @FXML
    private ComboBox<String> selectRestaurantForOrders;
    @FXML
    private ComboBox<String> filterOrderStatus;
    @FXML
    private ComboBox<String> sellerTimePeriodFilter;
    @FXML
    private TableView<Order> restaurantOrdersTable;
    @FXML
    private TableColumn<Order, Integer> sellerOrderIdColumn;
    @FXML
    private TableColumn<Order, Integer> sellerOrderCustomerColumn;
    @FXML
    private TableColumn<Order, String> sellerOrderAddressColumn;
    @FXML
    private TableColumn<Order, String> sellerOrderStatusColumn;
    @FXML
    private TableColumn<Order, Integer> sellerOrderPriceColumn;
    @FXML
    private TableColumn<Order, String> sellerOrderCreatedAtColumn;

    @FXML
    private TableView<FoodItem> foodItemTable;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemIdColumn;
    @FXML
    private TableColumn<FoodItem, String> foodItemNameColumn;
    @FXML
    private TableColumn<FoodItem, String> foodItemDescription;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemVendorId;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemPrice;
    @FXML
    private TableColumn<FoodItem, Integer> foodItemSupply;
    @FXML
    private TableColumn<FoodItem, String[]> foodItemKeywords;

    @FXML
    private TableView<Transaction> sellerTransactionsTable;
    @FXML
    private TableColumn<Transaction, Integer> sellerTransactionIdColumn;
    @FXML
    private TableColumn<Transaction, Integer> sellerTransactionOrderIdColumn;
    @FXML
    private TableColumn<Transaction, Integer> sellerTransactionUserIdColumn;
    @FXML
    private TableColumn<Transaction, String> sellerTransactionMethodColumn;
    @FXML
    private TableColumn<Transaction, String> sellerTransactionStatusColumn;

    @FXML
    private ComboBox<String> selectRestaurantForMenu;
    @FXML
    public ComboBox<String> selectMenuComboBox;

    @FXML
    private HBox menuFilterButtonsContainer;

    @FXML
    private AnchorPane myProfileContainer;

    // Tab navigation buttons
    @FXML
    private MFXButton restaurantsTab;
    @FXML
    private MFXButton menuTab;
    @FXML
    private MFXButton ordersTab;
    @FXML
    private MFXButton profileTab;
    @FXML
    private MFXButton transactionsTab;

    private String selectedRestaurantForMenuId;
    private String selectedMenuTitle;
    private String selectedFilterMenu = null;
    private ObservableList<FoodItem> allFoodItems = FXCollections.observableArrayList();
    private Map<String, List<FoodItem>> menuItemsMap = new HashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ObservableList<Restaurant> sellerRestaurants = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize My Restaurants Table Columns
        if (myRestaurantIdColumn != null) myRestaurantIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (myRestaurantNameColumn != null) myRestaurantNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (myRestaurantAddressColumn != null) myRestaurantAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (myRestaurantPhoneColumn != null) myRestaurantPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (myRestaurantTaxFeeColumn != null) myRestaurantTaxFeeColumn.setCellValueFactory(new PropertyValueFactory<>("taxFee"));
        if (myRestaurantAdditionalFeeColumn != null) myRestaurantAdditionalFeeColumn.setCellValueFactory(new PropertyValueFactory<>("additionalFee"));

        // Initialize Restaurant Orders Table Columns
        if (sellerOrderIdColumn != null) sellerOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (sellerOrderCustomerColumn != null) sellerOrderCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        if (sellerOrderAddressColumn != null) sellerOrderAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (sellerOrderStatusColumn != null) sellerOrderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (sellerOrderPriceColumn != null) sellerOrderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        if (sellerOrderCreatedAtColumn != null) sellerOrderCreatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        if (foodItemIdColumn != null) foodItemIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (foodItemNameColumn != null) foodItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (foodItemDescription != null) foodItemDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (foodItemPrice != null) foodItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (foodItemVendorId != null) foodItemVendorId.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (foodItemSupply != null) foodItemSupply.setCellValueFactory(new PropertyValueFactory<>("supply"));
        if (foodItemKeywords != null) {
            foodItemKeywords.setCellValueFactory(new PropertyValueFactory<>("keywords"));
            foodItemKeywords.setCellFactory(column -> new TableCell<FoodItem, String[]>() {
                @Override
                protected void updateItem(String[] keywords, boolean empty) {
                    super.updateItem(keywords, empty);
                    if (empty || keywords == null) {
                        setText(null);
                    } else {
                        setText(String.join(", ", keywords));
                    }
                }
            });
        }

        // Initialize Seller Transactions Table Columns
        if (sellerTransactionIdColumn != null) sellerTransactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (sellerTransactionOrderIdColumn != null) sellerTransactionOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        if (sellerTransactionUserIdColumn != null) sellerTransactionUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (sellerTransactionMethodColumn != null) sellerTransactionMethodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        if (sellerTransactionStatusColumn != null) sellerTransactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup filterOrderStatus ComboBox
        ObservableList<String> orderStatuses = FXCollections.observableArrayList(
                "All Statuses", "SUBMITTED", "UNPAID_AND_CANCELLED", "WAITING_VENDOR",
                "CANCELLED", "FINDING_COURIER", "ON_THE_WAY", "COMPLETED", "ACCEPTED", "REJECTED", "SERVED"
        );
        if (filterOrderStatus != null) {
            filterOrderStatus.setItems(orderStatuses);
            filterOrderStatus.getSelectionModel().selectFirst();
            filterOrderStatus.valueProperty().addListener((obs, oldVal, newVal) -> viewRestaurantOrders());
        }

        // Setup sellerTimePeriodFilter ComboBox
        ObservableList<String> timePeriods = FXCollections.observableArrayList(
                "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 3 Months", "Last 6 Months", "Last Year"
        );
        if (sellerTimePeriodFilter != null) {
            sellerTimePeriodFilter.setItems(timePeriods);
            sellerTimePeriodFilter.getSelectionModel().selectFirst();
            sellerTimePeriodFilter.valueProperty().addListener((obs, oldVal, newVal) -> viewRestaurantOrders());
        }

        if (selectRestaurantForMenu != null) {
            selectRestaurantForMenu.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("(ID: ")) {
                    int idStartIndex = newVal.lastIndexOf("(ID: ") + 5;
                    int idEndIndex = newVal.lastIndexOf(")");
                    selectedRestaurantForMenuId = newVal.substring(idStartIndex, idEndIndex);
                    errorMessageLabel.setText("Selected restaurant for menu: " + selectedRestaurantForMenuId);
                    viewManageMenu();
                } else {
                    selectedRestaurantForMenuId = null;
                    foodItemTable.setItems(FXCollections.emptyObservableList());
                    errorMessageLabel.setText("Please select a valid restaurant");
                }
            });
        }

        if (selectMenuComboBox != null) {
            selectMenuComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    selectedMenuTitle = newVal;
                    errorMessageLabel.setText("Selected menu: " + selectedMenuTitle);
                }
            });
        }

        if (selectRestaurantForOrders != null) {
            selectRestaurantForOrders.valueProperty().addListener((obs, oldVal, newVal) -> {
                viewRestaurantOrders();
            });
        }

        // Initial data loading
        viewMyRestaurants();
        
        // Initialize tab button styles - set first tab as active
        updateTabButtonStyles(restaurantsTab);
    }

    // Tab switching methods
    @FXML
    private void switchToMyRestaurantsTab(ActionEvent event) {
        mainTabPane.getSelectionModel().select(0);
        updateTabButtonStyles(restaurantsTab);
        viewMyRestaurants();
    }

    @FXML
    private void switchToManageMenuTab(ActionEvent event) {
        mainTabPane.getSelectionModel().select(1);
        updateTabButtonStyles(menuTab);
        viewManageMenu();
    }

    @FXML
    private void switchToOrdersTab(ActionEvent event) {
        mainTabPane.getSelectionModel().select(2);
        updateTabButtonStyles(ordersTab);
        viewRestaurantOrders();
    }

    @FXML
    private void switchToProfileTab(ActionEvent event) {
        mainTabPane.getSelectionModel().select(3);
        updateTabButtonStyles(profileTab);
        loadUserProfileView();
    }

    @FXML
    private void switchToTransactionsTab(ActionEvent event) {
        mainTabPane.getSelectionModel().select(4);
        updateTabButtonStyles(transactionsTab);
        viewSellerTransactions();
    }

    private void loadUserProfileView() {
        errorMessageLabel.setText("Loading profile view...");
        executorService.submit(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/UserProfileView.fxml"));
                Parent userProfileView = loader.load();

                Platform.runLater(() -> {
                    if (myProfileContainer != null) {
                        myProfileContainer.getChildren().setAll(userProfileView);
                        errorMessageLabel.setText("Profile view loaded successfully.");
                    } else {
                        errorMessageLabel.setText("Error: My profile container is null in FXML.");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Critical Error loading User Profile View: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewMyRestaurants() {
        errorMessageLabel.setText("Loading your restaurants...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/restaurants/mine", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Restaurant> restaurants = JsonUtil.getObjectMapper().readerForListOf(Restaurant.class).readValue(rootNode);
                                sellerRestaurants = FXCollections.observableArrayList(restaurants);
                                myRestaurantsTable.setItems(sellerRestaurants);

                                ObservableList<String> restaurantNames = FXCollections.observableArrayList();
                                sellerRestaurants.forEach(r -> restaurantNames.add(r.getName() + " (ID: " + r.getId() + ")"));
                                selectRestaurantForOrders.setItems(restaurantNames);
                                selectRestaurantForMenu.setItems(restaurantNames);
                                if (!restaurantNames.isEmpty()) {
                                    selectRestaurantForOrders.getSelectionModel().selectFirst();
                                    selectRestaurantForMenu.getSelectionModel().selectFirst();
                                }

                                errorMessageLabel.setText("Your restaurants loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing restaurant data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing your restaurants: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for restaurants."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching your restaurants: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewRestaurantOrders() {
        errorMessageLabel.setText("Loading restaurant orders...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Restaurant selectedRestaurant = null;
                Integer restaurantId = null;
                if (selectRestaurantForOrders.getSelectionModel().getSelectedItem() != null) {
                    String selectedRestaurantNameId = selectRestaurantForOrders.getSelectionModel().getSelectedItem();
                    String idStr = selectedRestaurantNameId.substring(selectedRestaurantNameId.lastIndexOf("ID: ") + 4, selectedRestaurantNameId.lastIndexOf(")"));
                    try {
                        restaurantId = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        // fallback: leave as null
                    }
                }
                if (restaurantId != null) {
                    final Integer finalRestaurantId = restaurantId;
                    selectedRestaurant = sellerRestaurants.stream()
                            .filter(r -> r.getId() != null && r.getId().equals(finalRestaurantId))
                            .findFirst()
                            .orElse(null);
                }

                if (selectedRestaurant == null) {
                    Platform.runLater(() -> errorMessageLabel.setText("Please select a restaurant to view orders."));
                    return;
                }

                String path = "/restaurants/" + selectedRestaurant.getId() + "/orders";

                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                List<Order> filteredOrders = applySellerOrderFilters(orders);
                                ObservableList<Order> orderObservableList = FXCollections.observableArrayList(filteredOrders);
                                restaurantOrdersTable.setItems(orderObservableList);
                                
                                logger.info("Restaurant orders refreshed - Total: {}, Filtered: {}", orders.size(), filteredOrders.size());
                                // Log status of each order for debugging
                                for (Order order : filteredOrders) {
                                    logger.debug("Order {}: status = {}", order.getId(), order.getStatus());
                                }
                                
                                errorMessageLabel.setText("Restaurant orders loaded successfully. Showing " + filteredOrders.size() + " of " + orders.size() + " orders.");
                            } catch (IOException e) {
                                logger.error("Error parsing orders data", e);
                                errorMessageLabel.setText("Error parsing orders data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            logger.error("Error viewing restaurant orders: {}", errorMessage);
                            errorMessageLabel.setText("Error viewing restaurant orders: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for restaurant orders."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching restaurant orders: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void markOrderAccepted() {
        updateOrderStatus("ACCEPTED");
    }

    @FXML
    private void markOrderRejected() {
        updateOrderStatus("REJECTED");
    }

    @FXML
    private void markOrderServed() {
        updateOrderStatus("SERVED");
    }

    @FXML
    private void handleViewOrderDetails(ActionEvent event) {
        if (restaurantOrdersTable == null) {
            errorMessageLabel.setText("Restaurant orders table not found.");
            return;
        }

        Order selectedOrder = restaurantOrdersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            errorMessageLabel.setText("Please select an order to view details.");
            return;
        }

        errorMessageLabel.setText("Loading order details...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/orders/" + selectedOrder.getId(), token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                Order completeOrder = JsonUtil.getObjectMapper().treeToValue(rootNode, Order.class);
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/OrderDetailsView.fxml"));
                                    Parent orderDetailsView = loader.load();

                                    OrderDetailsController controller = loader.getController();
                                    controller.setOrder(completeOrder);

                                    Stage stage = new Stage();
                                    stage.initModality(Modality.APPLICATION_MODAL);
                                    stage.setTitle("Order Details - Order #" + completeOrder.getId());
                                    Scene scene = new Scene(orderDetailsView);
                                    scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
                                    stage.setScene(scene);
                                    stage.showAndWait();

                                } catch (IOException e) {
                                    errorMessageLabel.setText("Error opening order details dialog: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing order data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error loading order details: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for order details."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while loading order details: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private void updateOrderStatus(String status) {
        Order selectedOrder = restaurantOrdersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            errorMessageLabel.setText("Please select an order to update its status.");
            return;
        }

        logger.info("Updating order {} status from '{}' to '{}'", selectedOrder.getId(), selectedOrder.getStatus(), status);
        errorMessageLabel.setText("Updating order " + selectedOrder.getId() + " status to " + status + "...");

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Map<String, String> statusData = new HashMap<>();
                statusData.put("status", status);
                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(statusData);

                logger.debug("Sending status update request: {}", jsonBody);
                Optional<HttpResponse<String>> responseOpt = ApiClient.patch("/restaurants/orders/" + selectedOrder.getId(), jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    logger.debug("Status update response - Status: {}, Body: {}", response.statusCode(), response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            logger.info("Order status updated successfully on server");
                            errorMessageLabel.setText("Order status updated successfully. Refreshing orders...");
                            
                            // Update the local order object immediately for better UX
                            selectedOrder.setStatus(status);
                            restaurantOrdersTable.refresh();
                            
                            // Then refresh from server
                            viewRestaurantOrders();
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            logger.error("Failed to update order status: {}", errorMessage);
                            errorMessageLabel.setText("Error updating order status: " + errorMessage);
                        }
                    });
                } else {
                    logger.error("No response received for order status update");
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for order status update."));
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Exception during order status update", e);
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred during order status update: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewManageMenu() {
        errorMessageLabel.setText("Loading manage menu ...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Authentication token is missing. Please log in again");
                    });
                    return;
                }
                if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Please select a valid restaurant");
                        foodItemTable.setItems(FXCollections.emptyObservableList());
                    });
                    return;
                }
                String restaurantId = selectedRestaurantForMenuId;

                Optional<HttpResponse<String>> responseOptional = ApiClient.get("/vendors/" + restaurantId, token);

                if (responseOptional.isPresent()) {
                    HttpResponse<String> response = responseOptional.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                    logger.info("rootNode: {}", rootNode);
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                Set<FoodItem> allFoodItemsSet = new java.util.HashSet<>();
                                ObservableList<String> menuTitles = FXCollections.observableArrayList();
                                JsonNode menuTitleNode = rootNode.get("menu_titles");
                                JsonNode menuTitleObject = rootNode.get("menu_title");
                                menuItemsMap.clear();

                                if (menuTitleNode != null && menuTitleNode.isArray()) {
                                    for (JsonNode titleNode : menuTitleNode) {
                                        String menuTitle = titleNode.asText();
                                        menuTitles.add(menuTitle);
                                        JsonNode itemsUnderTitle = menuTitleObject.get(menuTitle);
                                        if (itemsUnderTitle != null && itemsUnderTitle.isArray()) {
                                            List<FoodItem> items = JsonUtil.getObjectMapper().readerForListOf(FoodItem.class).readValue(itemsUnderTitle);
                                            menuItemsMap.put(menuTitle, items);
                                            allFoodItemsSet.addAll(items);
                                        }
                                    }
                                }

                                allFoodItems = FXCollections.observableArrayList(allFoodItemsSet);
                                foodItemTable.setItems(allFoodItems);
                                createMenuFilterButtons(menuTitles);
                                selectMenuComboBox.setItems(menuTitles);
                                if (!menuTitles.isEmpty()) {
                                    selectMenuComboBox.getSelectionModel().selectFirst();
                                }
                                errorMessageLabel.setText("Food item loaded successfully for restaurant ID: " + selectedRestaurantForMenuId);
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing food items data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            Platform.runLater(() -> {
                                errorMessageLabel.setText("Error fetching food items: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                            });
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Field to connect to server for food items");
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching food items: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void addNewRestaurant() {
        errorMessageLabel.setText("");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/RestaurantFormView.fxml"));
            Parent restaurantFormView = fxmlLoader.load();
            RestaurantFormController controller = fxmlLoader.getController();
            controller.setRestaurantId(null);
            controller.setRestaurantEdited(null);
            controller.setRefreshRestaurantCallback(aVoid -> viewManageMenu());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add new restaurant");
            Scene scene = new Scene(restaurantFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            errorMessageLabel.setText("Error opening restaurant form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void addFoodItem(ActionEvent event) {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant to add a food item");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/FoodItemFormView.fxml"));
            Parent foodItemFormView = fxmlLoader.load();
            FoodItemFormController controller = fxmlLoader.getController();
            controller.setRestaurantId(selectedRestaurantForMenuId);
            controller.setRefreshFoodItemCallback(aVoid -> viewManageMenu());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add new food item");
            Scene scene = new Scene(foodItemFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            errorMessageLabel.setText("Error opening food item form" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditRestaurant(ActionEvent event) {
        errorMessageLabel.setText("");
        Restaurant selectedRestaurant = myRestaurantsTable.getSelectionModel().getSelectedItem();
        if (selectedRestaurant == null) {
            errorMessageLabel.setText("Please select a restaurant to edit");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/RestaurantFormView.fxml"));
            Parent RestaurantFormView = fxmlLoader.load();
            RestaurantFormController controller = fxmlLoader.getController();
            controller.setRestaurantEdited(selectedRestaurant);
            controller.setRestaurantId(selectedRestaurant.getId().toString());
            controller.setRefreshRestaurantCallback(aVoid -> viewMyRestaurants());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit restaurant");
            Scene scene = new Scene(RestaurantFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            errorMessageLabel.setText("Error opening restaurant form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void editFoodItem(ActionEvent event) {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant to edit a food item");
            return;
        }
        FoodItem selectedFoodItem = foodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            errorMessageLabel.setText("Please select a food item to edit");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/FoodItemFormView.fxml"));
            Parent foodItemFormView = fxmlLoader.load();
            FoodItemFormController controller = fxmlLoader.getController();
            controller.setRestaurantId(selectedRestaurantForMenuId);
            controller.setFoodItemToEdit(selectedFoodItem);
            controller.setRefreshFoodItemCallback(aVoid -> viewManageMenu());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit food item");
            Scene scene = new Scene(foodItemFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            errorMessageLabel.setText("Error opening food item form" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteFoodItem(ActionEvent event) {
        FoodItem selectedFoodItem = foodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            errorMessageLabel.setText("Please select a food item to delete");
            return;
        }
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant to delete food item");
        }
        errorMessageLabel.setText("removing " + selectedFoodItem.getName() + " from food items");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Authentication token is missing.Please login again");
                    });
                    return;
                }
                Optional<HttpResponse<String>> optionalHttpResponse = ApiClient.delete("/restaurants/" + selectedRestaurantForMenuId + "/item/" + selectedFoodItem.getId(), token);
                if (optionalHttpResponse.isPresent()) {
                    HttpResponse<String> response = optionalHttpResponse.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Food item deleted successfully");
                            viewManageMenu();
                        } else {
                            errorMessageLabel.setText("Error during removing: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Could not connect to server to remove food item");
                    });
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Unexpected error during removing: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void createMenu(ActionEvent event) {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant to creat a menu");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create new menu");
        dialog.setHeaderText("Enter the title for new menu:");
        dialog.setContentText("Menu Title:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String menuTitle = result.get().trim();
            if (menuTitle.isEmpty()) {
                errorMessageLabel.setText("Menu title cannot be empty");
                return;
            }
            errorMessageLabel.setText("Creating menu: " + menuTitle + "...");
            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Authentication token is missing.Please login again");
                        });
                        return;
                    }
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("title", menuTitle);
                    String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);
                    Optional<HttpResponse<String>> responseOptional = ApiClient.post("/restaurants/" + selectedRestaurantForMenuId + "/menu", jsonBody, token);
                    if (responseOptional.isPresent()) {
                        HttpResponse<String> response = responseOptional.get();
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                errorMessageLabel.setText("Menu created successfully");
                            } else {
                                errorMessageLabel.setText("Error creating menu" + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Failed to connect server to create menu");
                        });
                    }
                } catch (IOException | InterruptedException e) {
                    errorMessageLabel.setText("Unexpected error while creating menu: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            errorMessageLabel.setText("Creating menu was cancelled");
        }
    }

    @FXML
    private void deleteMenu() {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant to delete menu");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("delete menu");
        dialog.setHeaderText("Enter title of menu to delete");
        dialog.setContentText("Menu Title: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String menuTitle = result.get().trim();
            if (menuTitle.isEmpty()) {
                errorMessageLabel.setText("Menu Title cannot be empty");
                return;
            }
            errorMessageLabel.setText("Deleting menu: " + menuTitle + "...");
            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Authentication token is missing.Please login again");
                        });
                        return;
                    }
                    Optional<HttpResponse<String>> responseOptional = ApiClient.delete("/restaurants/" + selectedRestaurantForMenuId + "/menu/" + menuTitle, token);
                    if (responseOptional.isPresent()) {
                        HttpResponse<String> response = responseOptional.get();
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Menu deleted successfully");
                            } else {
                                errorMessageLabel.setText("Error deleting menu: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Failed to connect to server to delete menu");
                        });
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Unexpected error occurred: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            });
        } else {
            errorMessageLabel.setText("Menu deletion cancelled");
        }
    }

    @FXML
    private void addItemToMenu() {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant first");
            return;
        }
        FoodItem selectedFoodItem = foodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            errorMessageLabel.setText("Please select a food item to add menu");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add item to menu");
        dialog.setHeaderText("Enter title of menu");
        dialog.setContentText("Menu Title");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String menuTitle = result.get().trim();
            if (menuTitle.isEmpty()) {
                errorMessageLabel.setText("Menu title cannot be empty");
                return;
            }
            errorMessageLabel.setText("Adding food to menu...");
            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Authentication token is missing.Please login again");
                        });
                        return;
                    }
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("item_id", selectedFoodItem.getId());
                    String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(requestBody);
                    Optional<HttpResponse<String>> responseOptional = ApiClient.put("/restaurants/" + selectedRestaurantForMenuId + "/menu/" + menuTitle, jsonBody, token);
                    if (responseOptional.isPresent()) {
                        HttpResponse<String> response = responseOptional.get();
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Food added successfully");
                            } else {
                                errorMessageLabel.setText("Error adding food to menu" + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Failed to connect to server for adding");
                        });
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Unexpected error during Adding: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            });
        } else {
            errorMessageLabel.setText("Add food to menu was cancelled");
        }
    }

    @FXML
    private void deleteItemFromMenu() {
        errorMessageLabel.setText("");
        if (selectedRestaurantForMenuId == null || selectedRestaurantForMenuId.isEmpty()) {
            errorMessageLabel.setText("Please select a restaurant first");
            return;
        }
        FoodItem selectedFood = foodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFood == null) {
            errorMessageLabel.setText("Please select a food item to delete");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deleting food from menu");
        dialog.setHeaderText("Enter title of menu: ");
        dialog.setContentText("Menu Title");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String menuTitle = result.get().trim();
            if (menuTitle.isEmpty()) {
                errorMessageLabel.setText("Menu title cannot be empty");
                return;
            }
            errorMessageLabel.setText("Deleting food from menu: " + selectedFood.getName() + " from " + menuTitle + "...");
            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Authentication token is missing.Please login again");
                        });
                        return;
                    }
                    Optional<HttpResponse<String>> responseOptional = ApiClient.delete("/restaurants/" + selectedRestaurantForMenuId + "/menu/" + menuTitle + "/" + selectedFood.getId(), token);
                    if (responseOptional.isPresent()) {
                        HttpResponse<String> response = responseOptional.get();
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                errorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Food deleted successfully");
                            } else {
                                errorMessageLabel.setText("Error deleting food from menu: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"));
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            errorMessageLabel.setText("Failed to connect to server to delete food from menu");
                        });
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> {
                        errorMessageLabel.setText("Unexpected error occurred: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            });
        } else {
            errorMessageLabel.setText("Deleting food from menu cancelled");
        }
    }

    @FXML
    private void logout() {
        AuthManager.logout();
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) mainTabPane.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Fooody - Login");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                errorMessageLabel.setText("Error navigating to login: " + e.getMessage());
            }
        });
    }

    @FXML
    private void viewSellerTransactions() {
        errorMessageLabel.setText("Loading seller transactions...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/transactions", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Transaction> transactions = JsonUtil.getObjectMapper().readerForListOf(Transaction.class).readValue(rootNode);
                                ObservableList<Transaction> transactionObservableList = FXCollections.observableArrayList(transactions);
                                sellerTransactionsTable.setItems(transactionObservableList);
                                errorMessageLabel.setText("Seller transactions loaded successfully. Found " + transactions.size() + " transactions.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing seller transactions: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing seller transactions: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for seller transactions."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching seller transactions: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private List<Order> applySellerOrderFilters(List<Order> orders) {
        return orders.stream()
                .filter(order -> {
                    if (filterOrderStatus != null && filterOrderStatus.getValue() != null &&
                            !filterOrderStatus.getValue().equals("All Statuses")) {
                        if (!order.getStatus().equalsIgnoreCase(filterOrderStatus.getValue())) {
                            return false;
                        }
                    }

                    if (sellerTimePeriodFilter != null && sellerTimePeriodFilter.getValue() != null &&
                            !sellerTimePeriodFilter.getValue().equals("All Time")) {
                        try {
                            LocalDateTime orderDate = LocalDateTime.parse(order.getCreatedAt());
                            LocalDateTime now = LocalDateTime.now();

                            switch (sellerTimePeriodFilter.getValue()) {
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
                            return true;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private void createMenuFilterButtons(ObservableList<String> menuTitles) {
        if (menuFilterButtonsContainer != null) {
            menuFilterButtonsContainer.getChildren().clear();

            MFXButton allItemsButton = new MFXButton("All Items");
            allItemsButton.getStyleClass().add("login-button");
            allItemsButton.setOnAction(e -> {
                selectedFilterMenu = null;
                foodItemTable.setItems(allFoodItems);
                updateButtonStyles(allItemsButton);
                errorMessageLabel.setText("Showing all food items");
            });
            menuFilterButtonsContainer.getChildren().add(allItemsButton);

            for (String menuTitle : menuTitles) {
                MFXButton menuButton = new MFXButton(menuTitle);
                menuButton.getStyleClass().add("login-button");
                menuButton.setOnAction(e -> {
                    selectedFilterMenu = menuTitle;
                    List<FoodItem> menuItems = menuItemsMap.get(menuTitle);
                    if (menuItems != null) {
                        foodItemTable.setItems(FXCollections.observableArrayList(menuItems));
                        updateButtonStyles(menuButton);
                        errorMessageLabel.setText("Showing items from menu: " + menuTitle);
                    }
                });
                menuFilterButtonsContainer.getChildren().add(menuButton);
            }

            updateButtonStyles(allItemsButton);
        }
    }

    private void updateButtonStyles(MFXButton activeButton) {
        if (menuFilterButtonsContainer != null) {
            for (javafx.scene.Node node : menuFilterButtonsContainer.getChildren()) {
                if (node instanceof MFXButton) {
                    MFXButton button = (MFXButton) node;
                    if (button == activeButton) {
                        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    } else {
                        button.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black;");
                    }
                }
            }
        }
    }

    private void updateTabButtonStyles(MFXButton activeTabButton) {
        // Reset all tab buttons to inactive style
        if (restaurantsTab != null) {
            restaurantsTab.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #6b7280; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 500; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        }
        if (menuTab != null) {
            menuTab.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #6b7280; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 500; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        }
        if (ordersTab != null) {
            ordersTab.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #6b7280; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 500; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        }
        if (profileTab != null) {
            profileTab.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #6b7280; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 500; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        }
        if (transactionsTab != null) {
            transactionsTab.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #6b7280; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 500; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        }

        // Set the active tab button to active style
        if (activeTabButton != null) {
            activeTabButton.setStyle("-fx-background-color: white; -fx-text-fill: #374151; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-border-color: #e2e8f0; -fx-border-width: 1 1 0 1; -fx-background-radius: 8 8 0 0; -fx-border-radius: 8 8 0 0; -fx-cursor: hand;");
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}