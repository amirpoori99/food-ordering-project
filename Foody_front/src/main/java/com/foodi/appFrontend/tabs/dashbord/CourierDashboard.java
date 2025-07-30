package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.Order;
import com.foodi.appFrontend.models.Transaction;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane; // اضافه شده

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CourierDashboard {

    @FXML private Label errorMessageLabel;
    @FXML private TabPane mainTabPane;

    @FXML private TableView<Order> availableDeliveriesTable;
    @FXML private TableColumn<Order, Integer> availableOrderIdColumn;
    @FXML private TableColumn<Order, Integer> availableVendorIdColumn;
    @FXML private TableColumn<Order, String> availableDeliveryAddressColumn;
    @FXML private TableColumn<Order, String> availableStatusColumn;

    @FXML private TableView<Order> deliveryHistoryTable;
    @FXML private TableColumn<Order, Integer> historyOrderIdColumn;
    @FXML private TableColumn<Order, Integer> historyVendorIdColumn;
    @FXML private TableColumn<Order, String> historyDeliveryAddressColumn;
    @FXML private TableColumn<Order, String> historyStatusColumn;
    @FXML private TableColumn<Order, String> historyUpdatedAtColumn;

    @FXML private TableView<Transaction> courierTransactionsTable;
    @FXML private TableColumn<Transaction, Integer> transactionIdColumn;
    @FXML private TableColumn<Transaction, Integer> transactionOrderIdColumn;
    @FXML private TableColumn<Transaction, Integer> transactionUserIdColumn;
    @FXML private TableColumn<Transaction, String> transactionMethodColumn;
    @FXML private TableColumn<Transaction, String> transactionStatusColumn;

    // Delivery History Filters
    @FXML private ComboBox<String> courierTimePeriodFilter;
    @FXML private ComboBox<String> courierOrderStatusFilter;

    // FXML for included UserProfileView (این فیلد ممکن است دیگر ضروری نباشد اگر به صورت دستی کنترلر را فراخوانی می‌کنید)
    @FXML private UserProfileController userProfileViewController;

    @FXML private AnchorPane myProfileContainer; // اضافه شده: کانتینر برای بارگذاری پروفایل کاربر

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        // Initialize tab navigation
        setupTabNavigation();

        // Initialize Available Deliveries Table Columns
        if (availableOrderIdColumn != null) availableOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (availableVendorIdColumn != null) availableVendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (availableDeliveryAddressColumn != null) availableDeliveryAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (availableStatusColumn != null) availableStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize Delivery History Table Columns
        if (historyOrderIdColumn != null) historyOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (historyVendorIdColumn != null) historyVendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (historyDeliveryAddressColumn != null) historyDeliveryAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (historyStatusColumn != null) historyStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (historyUpdatedAtColumn != null) historyUpdatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        // Initialize Courier Transactions Table Columns
        if (transactionIdColumn != null) transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (transactionOrderIdColumn != null) transactionOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        if (transactionUserIdColumn != null) transactionUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (transactionMethodColumn != null) transactionMethodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        if (transactionStatusColumn != null) transactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize Delivery History Filters
        if (courierTimePeriodFilter != null) {
            ObservableList<String> timePeriods = FXCollections.observableArrayList(
                "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 3 Months", "Last 6 Months", "Last Year"
            );
            courierTimePeriodFilter.setItems(timePeriods);
            courierTimePeriodFilter.getSelectionModel().selectFirst();
            courierTimePeriodFilter.valueProperty().addListener((obs, oldVal, newVal) -> viewDeliveryHistory());
        }

        if (courierOrderStatusFilter != null) {
            ObservableList<String> orderStatuses = FXCollections.observableArrayList(
                "All Statuses", "submitted", "unpaid_and_cancelled", "waiting_vendor",
                "cancelled", "finding_courier", "on_the_way", "completed", "accepted", "rejected", "served"
            );
            courierOrderStatusFilter.setItems(orderStatuses);
            courierOrderStatusFilter.getSelectionModel().selectFirst();
            courierOrderStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> viewDeliveryHistory());
        }

        // Load initial data
        viewAvailableDeliveries();
        viewDeliveryHistory();
        viewCourierTransactions();
    }



    // متد جدید برای بارگذاری پویا UserProfileView.fxml
    private void loadUserProfileView() {
        errorMessageLabel.setText("Loading profile view...");
        executorService.submit(() -> {
            try {
                // این بارگذاری به صورت مستقیم از classpath است
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/UserProfileView.fxml"));
                Parent userProfileView = loader.load(); // تلاش برای بارگذاری FXML

                Platform.runLater(() -> {
                    if (myProfileContainer != null) {
                        myProfileContainer.getChildren().setAll(userProfileView); // تزریق محتوای بارگذاری شده به کانتینر
                        errorMessageLabel.setText("Profile view loaded successfully.");

                        // اگر UserProfileController نیاز به مقداردهی اولیه یا بارگذاری داده دارد،
                        // می‌توانید کنترلر را دریافت کرده و متدهای آن را فراخوانی کنید.
                        // UserProfileController userProfileControllerInstance = loader.getController();
                        // if (userProfileControllerInstance != null) {
                        //     userProfileControllerInstance.loadUserProfile(); // فرض بر وجود چنین متدی در UserProfileController
                        // }
                    } else {
                        errorMessageLabel.setText("Error: My profile container (myProfileContainer) is null in FXML.");
                    }
                });
            } catch (IOException e) {
                // اگر بارگذاری در اینجا هم با شکست مواجه شود، خطای دقیق‌تری خواهیم داشت
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Critical Error loading User Profile View dynamically: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewAvailableDeliveries() {
        errorMessageLabel.setText("Loading available deliveries...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/deliveries/available", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                ObservableList<Order> orderObservableList = FXCollections.observableArrayList(orders);
                                availableDeliveriesTable.setItems(orderObservableList);
                                errorMessageLabel.setText("Available deliveries loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing available deliveries: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing available deliveries: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for available deliveries."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching available deliveries: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void acceptDelivery() {
        updateDeliveryStatus("accepted", availableDeliveriesTable);
    }

    @FXML
    private void markDeliveryReceived() {
        updateDeliveryStatus("received", deliveryHistoryTable);
    }

    @FXML
    private void markDeliveryDelivered() {
        updateDeliveryStatus("delivered", deliveryHistoryTable);
    }

    @FXML
    private void handleViewOrderDetailsAvailable(ActionEvent event) {
        if (availableDeliveriesTable == null) {
            errorMessageLabel.setText("Available deliveries table not found.");
            return;
        }

        Order selectedOrder = availableDeliveriesTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            errorMessageLabel.setText("Please select an order to view details.");
            return;
        }

        // Load complete order details from /orders/{id} endpoint
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
                                // Parse the complete order data from the API response
                                Order completeOrder = JsonUtil.getObjectMapper().treeToValue(rootNode, Order.class);
                                
                                // Open the order details dialog with complete order data
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

    @FXML
    private void handleViewOrderDetailsHistory(ActionEvent event) {
        if (deliveryHistoryTable == null) {
            errorMessageLabel.setText("Delivery history table not found.");
            return;
        }

        Order selectedOrder = deliveryHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            errorMessageLabel.setText("Please select an order to view details.");
            return;
        }

        // Load complete order details from /orders/{id} endpoint
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
                                // Parse the complete order data from the API response
                                Order completeOrder = JsonUtil.getObjectMapper().treeToValue(rootNode, Order.class);
                                
                                // Open the order details dialog with complete order data
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

    private void updateDeliveryStatus(String status, TableView<Order> table) {
        Order selectedOrder = table.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            errorMessageLabel.setText("Please select a delivery to update its status.");
            return;
        }

        errorMessageLabel.setText("Updating delivery " + selectedOrder.getId() + " status to " + status + "...");

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

                Optional<HttpResponse<String>> responseOpt = ApiClient.patch("/deliveries/" + selectedOrder.getId(), jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText("Delivery status updated successfully.");
                            viewAvailableDeliveries(); // Refresh available deliveries
                            viewDeliveryHistory(); // Refresh history as well
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error updating delivery status: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for delivery status update."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while saving delivery: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewDeliveryHistory() {
        errorMessageLabel.setText("Loading delivery history...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/deliveries/history", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                
                                // Apply filters
                                List<Order> filteredOrders = applyCourierOrderFilters(orders);
                                
                                ObservableList<Order> orderObservableList = FXCollections.observableArrayList(filteredOrders);
                                deliveryHistoryTable.setItems(orderObservableList);
                                errorMessageLabel.setText("Delivery history loaded successfully. Showing " + filteredOrders.size() + " of " + orders.size() + " orders.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing delivery history: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing delivery history: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for delivery history."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching delivery history: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void viewCourierTransactions() {
        errorMessageLabel.setText("Loading courier transactions...");
        System.out.println("Loading courier transactions...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                System.out.println("Fetching courier transactions from API...");
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/transactions", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    System.out.println("API Response Status: " + response.statusCode());
                    System.out.println("API Response Body: " + response.body());
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Transaction> transactions = JsonUtil.getObjectMapper().readerForListOf(Transaction.class).readValue(rootNode);
                                ObservableList<Transaction> transactionObservableList = FXCollections.observableArrayList(transactions);
                                courierTransactionsTable.setItems(transactionObservableList);
                                errorMessageLabel.setText("Courier transactions loaded successfully. Found " + transactions.size() + " transactions.");
                                System.out.println("Loaded " + transactions.size() + " courier transactions");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing courier transactions: " + e.getMessage());
                                System.out.println("Error parsing courier transactions: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing courier transactions: " + errorMessage);
                            System.out.println("Error viewing courier transactions: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for courier transactions."));
                    System.out.println("Failed to connect to server for courier transactions.");
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching courier transactions: " + e.getMessage());
                    System.out.println("An unexpected error occurred while fetching courier transactions: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void logout() {
        AuthManager.logout();
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) errorMessageLabel.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/login.fxml"));
                Parent root = loader.load();
                Scene loginScene = new Scene(root);
                loginScene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
                stage.setScene(loginScene);
                stage.setTitle("Fooody - Login");
                stage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
                errorMessageLabel.setText("Error navigating to login: " + e.getMessage());
            }
        });
    }

    private List<Order> applyCourierOrderFilters(List<Order> orders) {
        return orders.stream()
                .filter(order -> {
                    // Apply status filter
                    if (courierOrderStatusFilter != null && courierOrderStatusFilter.getValue() != null && 
                        !courierOrderStatusFilter.getValue().equals("All Statuses")) {
                        if (!order.getStatus().equalsIgnoreCase(courierOrderStatusFilter.getValue())) {
                            return false;
                        }
                    }
                    
                    // Apply time period filter
                    if (courierTimePeriodFilter != null && courierTimePeriodFilter.getValue() != null && 
                        !courierTimePeriodFilter.getValue().equals("All Time")) {
                        try {
                            LocalDateTime orderDate = LocalDateTime.parse(order.getUpdatedAt());
                            LocalDateTime now = LocalDateTime.now();
                            
                            switch (courierTimePeriodFilter.getValue()) {
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
        executorService.shutdown();
    }

    // Tab Navigation Methods
    private void setupTabNavigation() {
        // This method can be used for any tab navigation setup if needed
    }

    @FXML
    private void switchToAvailableDeliveriesTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(0); // First tab
        }
    }

    @FXML
    private void switchToDeliveryHistoryTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(1); // Second tab
        }
    }

    @FXML
    private void switchToTransactionsTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(2); // Third tab
        }
    }

    @FXML
    private void switchToProfileTab() {
        if (mainTabPane != null) {
            mainTabPane.getSelectionModel().select(3); // Fourth tab
            loadUserProfileView(); // Load the profile content
        }
    }
}