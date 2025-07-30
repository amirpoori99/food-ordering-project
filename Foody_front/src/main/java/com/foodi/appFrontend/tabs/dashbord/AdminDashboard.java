package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.Coupon;
import com.foodi.appFrontend.models.Order;
import com.foodi.appFrontend.models.Transaction;
import com.foodi.appFrontend.models.User;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import io.github.palexdev.materialfx.controls.MFXButton;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminDashboard {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboard.class);
    @FXML private Label errorMessageLabel;

    // Tab Navigation Buttons
    @FXML private MFXButton usersTab;
    @FXML private MFXButton ordersTab;
    @FXML private MFXButton transactionsTab;
    @FXML private MFXButton couponsTab;

    // FXML elements for Users Tab
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userPhoneColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userAddressColumn;
    @FXML private TableColumn<User, String> userStatusColumn;
    @FXML private TableColumn<User, BigDecimal> userWalletBalanceColumn;
    @FXML private ComboBox<String> filterRoleComboBox;

    // The main TabPane to organize sections
    @FXML private TabPane mainTabPane;

    // FXML elements for Orders Tab
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, Integer> orderCustomerIdColumn;
    @FXML private TableColumn<Order, Integer> orderVendorIdColumn;
    @FXML private TableColumn<Order, Integer> orderCourierIdColumn;
    @FXML private TableColumn<Order, String> orderStatusColumn;
    @FXML private TableColumn<Order, Integer> orderPriceColumn;
    @FXML private TableColumn<Order, String> orderAddressColumn;
    @FXML private TableColumn<Order, String> orderCreatedAtColumn;
    @FXML private ComboBox<String> orderStatusFilter;
    @FXML private ComboBox<String> orderTimePeriodFilter;

    // FXML elements for Transactions Tab
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> transactionIdColumn;
    @FXML private TableColumn<Transaction, Integer> transactionOrderIdColumn;
    @FXML private TableColumn<Transaction, Integer> transactionUserIdColumn;
    @FXML private TableColumn<Transaction, String> transactionMethodColumn;
    @FXML private TableColumn<Transaction, String> transactionStatusColumn;
    @FXML private TextField transactionSearchField;
    @FXML private ComboBox<String> transactionUserFilter;
    @FXML private ComboBox<String> transactionMethodFilter;
    @FXML private ComboBox<String> transactionStatusFilter;

    // FXML elements for Coupons Tab
    @FXML private TableView<Coupon> couponsTable;
    @FXML private TableColumn<Coupon, Integer> couponIdColumn;
    @FXML private TableColumn<Coupon, String> couponCodeColumn;
    @FXML private TableColumn<Coupon, String> couponTypeColumn;
    @FXML private TableColumn<Coupon, Double> couponValueColumn;
    @FXML private TableColumn<Coupon, Integer> couponMinPriceColumn;
    @FXML private TableColumn<Coupon, Integer> couponUserCountColumn;
    @FXML private TableColumn<Coupon, String> couponStartDateColumn;
    @FXML private TableColumn<Coupon, String> couponEndDateColumn;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        // Initialize User Table Columns
        if (userIdColumn != null) userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (userNameColumn != null) userNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (userPhoneColumn != null) userPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        if (userEmailColumn != null) userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (userRoleColumn != null) userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (userAddressColumn != null) userAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        if (userStatusColumn != null) userStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (userWalletBalanceColumn != null) userWalletBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("walletBalance"));

        ObservableList<String> roles = FXCollections.observableArrayList("All Roles", "Buyer", "Seller", "Courier", "Admin");
        if (filterRoleComboBox != null) {
            filterRoleComboBox.setItems(roles);
            filterRoleComboBox.getSelectionModel().selectFirst();
            filterRoleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> viewUsers());
        }

        // Initialize Order Table Columns
        if (orderIdColumn != null) orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (orderCustomerIdColumn != null) orderCustomerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        if (orderVendorIdColumn != null) orderVendorIdColumn.setCellValueFactory(new PropertyValueFactory<>("vendorId"));
        if (orderCourierIdColumn != null) orderCourierIdColumn.setCellValueFactory(new PropertyValueFactory<>("courierId"));
        if (orderStatusColumn != null) orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (orderPriceColumn != null) orderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("payPrice"));
        if (orderAddressColumn != null) orderAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        if (orderCreatedAtColumn != null) orderCreatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Initialize Order Status Filter ComboBox
                    ObservableList<String> orderStatuses = FXCollections.observableArrayList(
                "All Statuses", "submitted", "unpaid_and_cancelled", "waiting_vendor",
                "waiting_pickup", "cancelled", "finding_courier", "on_the_way", "completed", "accepted", "rejected", "served"
            );
        if (orderStatusFilter != null) {
            orderStatusFilter.setItems(orderStatuses);
            orderStatusFilter.getSelectionModel().selectFirst();
            orderStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> viewOrders());
        }

        // Initialize Order Time Period Filter ComboBox
        ObservableList<String> timePeriods = FXCollections.observableArrayList(
                "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 3 Months", "Last 6 Months", "Last Year"
        );
        if (orderTimePeriodFilter != null) {
            orderTimePeriodFilter.setItems(timePeriods);
            orderTimePeriodFilter.getSelectionModel().selectFirst();
            orderTimePeriodFilter.valueProperty().addListener((obs, oldVal, newVal) -> viewOrders());
        }

        // Initialize Transaction Table Columns
        if (transactionIdColumn != null) transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (transactionOrderIdColumn != null) transactionOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        if (transactionUserIdColumn != null) transactionUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        if (transactionMethodColumn != null) transactionMethodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        if (transactionStatusColumn != null) transactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Initialize Coupon Table Columns
        if (couponIdColumn != null) couponIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (couponCodeColumn != null) couponCodeColumn.setCellValueFactory(new PropertyValueFactory<>("couponCode"));
        if (couponTypeColumn != null) couponTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (couponValueColumn != null) couponValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        if (couponMinPriceColumn != null) couponMinPriceColumn.setCellValueFactory(new PropertyValueFactory<>("minPrice"));
        if (couponUserCountColumn != null) couponUserCountColumn.setCellValueFactory(new PropertyValueFactory<>("userCount"));
        if (couponStartDateColumn != null) couponStartDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        if (couponEndDateColumn != null) couponEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        // Initialize tab button styles
        updateTabButtonStyles();

        // Initial data loading when dashboard is opened
        viewUsers();
        viewOrders();
        viewTransactions();
        viewCoupons();
    }

    // Tab Navigation Methods
    @FXML
    private void switchToUsersTab() {
        if (mainTabPane != null) mainTabPane.getSelectionModel().select(0);
        updateTabButtonStyles();
        viewUsers();
    }

    @FXML
    private void switchToOrdersTab() {
        if (mainTabPane != null) mainTabPane.getSelectionModel().select(1);
        updateTabButtonStyles();
        viewOrders();
    }

    @FXML
    private void switchToTransactionsTab() {
        if (mainTabPane != null) mainTabPane.getSelectionModel().select(2);
        updateTabButtonStyles();
        viewTransactions();
    }

    @FXML
    private void switchToCouponsTab() {
        if (mainTabPane != null) mainTabPane.getSelectionModel().select(3);
        updateTabButtonStyles();
        viewCoupons();
    }

    private void updateTabButtonStyles() {
        if (mainTabPane == null) return;

        int selectedIndex = mainTabPane.getSelectionModel().getSelectedIndex();

        // Reset all buttons to inactive style
        String inactiveStyle = "-fx-background-color: #f1f5f9; " +
                "-fx-text-fill: #6b7280; " +
                "-fx-padding: 12 24; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 500; " +
                "-fx-border-color: #e2e8f0; " +
                "-fx-border-width: 0 0 1 0; " +
                "-fx-cursor: hand;";

        String activeStyle = "-fx-background-color: white; " +
                "-fx-text-fill: #374151; " +
                "-fx-padding: 12 24; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-border-color: #e2e8f0; " +
                "-fx-border-width: 1 1 0 1; " +
                "-fx-background-radius: 8 8 0 0; " +
                "-fx-border-radius: 8 8 0 0; " +
                "-fx-cursor: hand;";

        // Apply styles based on selection
        if (usersTab != null) usersTab.setStyle(selectedIndex == 0 ? activeStyle : inactiveStyle);
        if (ordersTab != null) ordersTab.setStyle(selectedIndex == 1 ? activeStyle : inactiveStyle);
        if (transactionsTab != null) transactionsTab.setStyle(selectedIndex == 2 ? activeStyle : inactiveStyle);
        if (couponsTab != null) couponsTab.setStyle(selectedIndex == 3 ? activeStyle : inactiveStyle);
    }

    // --- User Management Methods ---
    @FXML
    private void viewUsers() {
        errorMessageLabel.setText("Loading users...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                String selectedRole;
                if (filterRoleComboBox != null && filterRoleComboBox.getSelectionModel().getSelectedItem() != null &&
                        !filterRoleComboBox.getSelectionModel().getSelectedItem().equals("All Roles")) {
                    selectedRole = filterRoleComboBox.getSelectionModel().getSelectedItem();
                } else {
                    selectedRole = null;
                }

                String path = "/admin/users";
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<User> users = JsonUtil.getObjectMapper().readerForListOf(User.class).readValue(rootNode);
                                ObservableList<User> userObservableList = FXCollections.observableArrayList(users);

                                if (selectedRole != null) {
                                    userObservableList = userObservableList.filtered(user ->
                                            user.getRole() != null && user.getRole().equalsIgnoreCase(selectedRole)
                                    );
                                }

                                usersTable.setItems(userObservableList);
                                errorMessageLabel.setText("Users loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing user data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing users: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for users. Please check server status."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching users: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void approveSelectedUsers() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            errorMessageLabel.setText("Please select a user to approve.");
            return;
        }

        if (!"seller".equalsIgnoreCase(selectedUser.getRole()) && !"courier".equalsIgnoreCase(selectedUser.getRole())) {
            errorMessageLabel.setText("Only 'Seller' and 'Courier' roles can be approved/rejected.");
            return;
        }

        System.out.println("Approving user: " + selectedUser.getId());
        updateUserStatus(selectedUser.getId(), "approved");
    }

    @FXML
    private void rejectSelectedUsers() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            errorMessageLabel.setText("Please select a user to reject.");
            return;
        }

        if (!"seller".equalsIgnoreCase(selectedUser.getRole()) && !"courier".equalsIgnoreCase(selectedUser.getRole())) {
            errorMessageLabel.setText("Only 'Seller' and 'Courier' roles can be approved/rejected.");
            return;
        }

        updateUserStatus(selectedUser.getId(), "rejected");
    }

    private void updateUserStatus(String userId, String status) {
        errorMessageLabel.setText("Updating user status to " + status + " for user " + userId + "...");

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

                Optional<HttpResponse<String>> responseOpt = ApiClient.patch("/admin/users/" + userId + "/status", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText("User status updated successfully.");
                            viewUsers(); // Refresh the user list after update
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error updating user status: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for status update."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred during status update: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    // --- Order Management Methods ---
    @FXML
    private void viewOrders() {
        errorMessageLabel.setText("Loading orders...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Map<String, String> queryParams = new HashMap<>();
                if (orderStatusFilter != null && orderStatusFilter.getSelectionModel().getSelectedItem() != null &&
                        !orderStatusFilter.getSelectionModel().getSelectedItem().equals("Filter by Status") &&
                        !orderStatusFilter.getSelectionModel().getSelectedItem().equals("All Statuses")) {
                    queryParams.put("status", orderStatusFilter.getSelectionModel().getSelectedItem());
                }

                String path = "/admin/orders";
                if (!queryParams.isEmpty()) {
                    StringBuilder queryString = new StringBuilder("?");
                    queryParams.forEach((key, value) -> queryString.append(key).append("=").append(value).append("&"));
                    queryString.setLength(queryString.length() - 1);
                    path += queryString.toString();
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Order> orders = JsonUtil.getObjectMapper().readerForListOf(Order.class).readValue(rootNode);
                                
                                // Apply time period filter
                                List<Order> filteredOrders = applyAdminOrderFilters(orders);
                                
                                ObservableList<Order> orderObservableList = FXCollections.observableArrayList(filteredOrders);
                                ordersTable.setItems(orderObservableList);
                                errorMessageLabel.setText("Orders loaded successfully. Showing " + filteredOrders.size() + " of " + orders.size() + " orders.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing orders data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing orders: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for orders. Please check server status."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching orders: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    // --- Transaction Management Methods ---
    @FXML
    private void viewTransactions() {
        errorMessageLabel.setText("Loading transactions...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Map<String, String> queryParams = new HashMap<>();
                if (transactionSearchField != null && !transactionSearchField.getText().isEmpty()) {
                    queryParams.put("search", transactionSearchField.getText());
                }
                if (transactionUserFilter != null && transactionUserFilter.getSelectionModel().getSelectedItem() != null &&
                        !transactionUserFilter.getSelectionModel().getSelectedItem().equals("Filter by User")) {
                    queryParams.put("user", transactionUserFilter.getSelectionModel().getSelectedItem());
                }
                if (transactionMethodFilter != null && transactionMethodFilter.getSelectionModel().getSelectedItem() != null &&
                        !transactionMethodFilter.getSelectionModel().getSelectedItem().equals("Filter by Method")) {
                    queryParams.put("method", transactionMethodFilter.getSelectionModel().getSelectedItem());
                }
                if (transactionStatusFilter != null && transactionStatusFilter.getSelectionModel().getSelectedItem() != null &&
                        !transactionStatusFilter.getSelectionModel().getSelectedItem().equals("Filter by Status")) {
                    queryParams.put("status", transactionStatusFilter.getSelectionModel().getSelectedItem());
                }

                String path = "/admin/transactions";
                if (!queryParams.isEmpty()) {
                    StringBuilder queryString = new StringBuilder("?");
                    queryParams.forEach((key, value) -> queryString.append(key).append("=").append(value).append("&"));
                    queryString.setLength(queryString.length() - 1);
                    path += queryString.toString();
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Transaction> transactions = JsonUtil.getObjectMapper().readerForListOf(Transaction.class).readValue(rootNode);
                                ObservableList<Transaction> transactionObservableList = FXCollections.observableArrayList(transactions);
                                transactionsTable.setItems(transactionObservableList);
                                errorMessageLabel.setText("Transactions loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing transactions data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing transactions: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for transactions. Please check server status."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching transactions: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    // --- Coupon Management Methods ---
    @FXML
    private void viewCoupons() {
        errorMessageLabel.setText("Loading coupons...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                String path = "/admin/coupons";
                Optional<HttpResponse<String>> responseOpt = ApiClient.get(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                    logger.info("Coupons: " + rootNode.toString());
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                List<Coupon> coupons = JsonUtil.getObjectMapper().readerForListOf(Coupon.class).readValue(rootNode);
                                ObservableList<Coupon> couponObservableList = FXCollections.observableArrayList(coupons);
                                couponsTable.setItems(couponObservableList);
                                errorMessageLabel.setText("Coupons loaded successfully.");
                            } catch (IOException e) {
                                errorMessageLabel.setText("Error parsing coupons data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error viewing coupons: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server for coupons. Please check server status."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while fetching coupons: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void addCoupon() {
        openCouponForm(null);
    }

    @FXML
    private void editCoupon() {
        Coupon selectedCoupon = couponsTable.getSelectionModel().getSelectedItem();
        if (selectedCoupon == null) {
            errorMessageLabel.setText("Please select a coupon to edit.");
            return;
        }
        openCouponForm(selectedCoupon);
    }

    @FXML
    private void deleteCoupon() {
        Coupon selectedCoupon = couponsTable.getSelectionModel().getSelectedItem();
        if (selectedCoupon == null) {
            errorMessageLabel.setText("Please select a coupon to delete.");
            return;
        }
        errorMessageLabel.setText("Deleting coupon " + selectedCoupon.getCouponCode() + "...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> errorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                String path = "/admin/coupons/" + selectedCoupon.getId();
                Optional<HttpResponse<String>> responseOpt = ApiClient.delete(path, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorMessageLabel.setText("Coupon deleted successfully.");
                            viewCoupons(); // Refresh the list
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            errorMessageLabel.setText("Error deleting coupon: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server to delete coupon."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("An unexpected error occurred while deleting coupon: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private void openCouponForm(Coupon coupon) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/CouponFormView.fxml"));
            Parent couponFormView = loader.load();
            CouponFormController controller = loader.getController();

            controller.setRefreshCouponsCallback(aVoid -> viewCoupons());

            if (coupon != null) {
                controller.setCouponToEdit(coupon);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(coupon == null ? "Add New Coupon" : "Edit Coupon");
            Scene scene = new Scene(couponFormView);
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            errorMessageLabel.setText("Error opening coupon form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        AuthManager.logout();
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) usersTab.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/login.fxml"));
                Parent root = loader.load();
                Scene loginScene = new Scene(root);
                loginScene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
                stage.setScene(loginScene);
                stage.setTitle("Fooody - Login");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                errorMessageLabel.setText("Error navigating to login: " + e.getMessage());
            }
        });
    }

    private List<Order> applyAdminOrderFilters(List<Order> orders) {
        return orders.stream()
                .filter(order -> {
                    // Apply time period filter
                    if (orderTimePeriodFilter != null && orderTimePeriodFilter.getValue() != null && 
                        !orderTimePeriodFilter.getValue().equals("All Time")) {
                        try {
                            LocalDateTime orderDate = LocalDateTime.parse(order.getCreatedAt());
                            LocalDateTime now = LocalDateTime.now();
                            
                            switch (orderTimePeriodFilter.getValue()) {
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
}