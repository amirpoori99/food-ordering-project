package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.FoodItem;
import com.foodi.appFrontend.models.Order;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class OrderDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsController.class);

    @FXML private Label orderIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label deliveryAddressLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;
    @FXML private Label rawPriceLabel;
    @FXML private Label discountLabel;
    @FXML private Label taxFeeLabel;
    @FXML private Label additionalFeeLabel;
    @FXML private Label courierFeeLabel;
    @FXML private Label totalPriceLabel;
    @FXML private TableView<FoodItem> orderItemsTable;
    @FXML private TableColumn<FoodItem, Integer> itemIdColumn;
    @FXML private TableColumn<FoodItem, String> itemNameColumn;
    @FXML private TableColumn<FoodItem, String> itemDescriptionColumn;
    @FXML private TableColumn<FoodItem, Integer> itemPriceColumn;
    @FXML private TableColumn<FoodItem, String> itemKeywordsColumn;

    private Order order;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        logger.debug("Initializing OrderDetailsController");
        setupTableColumns();
        logger.debug("Table columns setup completed");
    }

    private void setupTableColumns() {
        logger.debug("Setting up table columns");
        // Custom cell value factories to avoid reflection issues
        itemIdColumn.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(item.getId()).asObject();
        });
        
        itemNameColumn.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(item.getName());
        });
        
        itemDescriptionColumn.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(item.getDescription());
        });
        
        itemPriceColumn.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(item.getPrice()).asObject();
        });
        
        // Custom cell value factory for keywords
        itemKeywordsColumn.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue();
            String[] keywords = item.getKeywords();
            logger.debug("Keywords cell factory called for item: {}, keywords: {}", 
                item.getName(), keywords != null ? String.join(", ", keywords) : "null");
            if (keywords != null && keywords.length > 0) {
                return new javafx.beans.property.SimpleStringProperty(String.join(", ", keywords));
            } else {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        logger.debug("Keywords column setup completed");
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            populateOrderDetails();
            loadOrderItems();
        }
    }

    private void populateOrderDetails() {
        // Order Information
        orderIdLabel.setText(String.valueOf(order.getId()));
        statusLabel.setText(order.getStatus());
        deliveryAddressLabel.setText(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "N/A");
        createdAtLabel.setText(order.getCreatedAt() != null ? order.getCreatedAt() : "N/A");
        updatedAtLabel.setText(order.getUpdatedAt() != null ? order.getUpdatedAt() : "N/A");

        // Price Breakdown
        rawPriceLabel.setText(order.getRawPrice() != null ? "$" + order.getRawPrice() : "$0");
        taxFeeLabel.setText(order.getTaxFee() != null ? "$" + order.getTaxFee() : "$0");
        additionalFeeLabel.setText(order.getAdditionalFee() != null ? "$" + order.getAdditionalFee() : "$0");
        courierFeeLabel.setText(order.getCourierFee() != null ? "$" + order.getCourierFee() : "$0");
        
        // Calculate discount: raw + tax + additional + courier - total
        int discount = 0;
        int raw = order.getRawPrice() != null ? order.getRawPrice() : 0;
        int tax = order.getTaxFee() != null ? order.getTaxFee() : 0;
        int additional = order.getAdditionalFee() != null ? order.getAdditionalFee() : 0;
        int courier = order.getCourierFee() != null ? order.getCourierFee() : 0;
        int total = order.getPayPrice() != null ? order.getPayPrice() : 0;
        
        discount = raw + tax + additional + courier - total;
        discountLabel.setText("$" + discount);
        
        totalPriceLabel.setText(order.getPayPrice() != null ? "$" + order.getPayPrice() : "$0");
    }

    private void loadOrderItems() {
        if (order.getItemIds() == null || order.getItemIds().isEmpty()) {
            logger.info("No items found for order {}", order.getId());
            return;
        }

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    logger.warn("Authentication token missing when loading order items");
                    return;
                }

                // Load each food item by its ID using /items/{id} endpoint
                ObservableList<FoodItem> items = FXCollections.observableArrayList();
                
                // Get unique item IDs to avoid duplicates
                List<Integer> uniqueItemIds = order.getItemIds().stream().distinct().toList();
                
                for (Integer itemId : uniqueItemIds) {
                    
                    Optional<HttpResponse<String>> responseOpt = ApiClient.get("/items/" + itemId, token);
                    
                    if (responseOpt.isPresent()) {
                        HttpResponse<String> response = responseOpt.get();
                        
                        if (response.statusCode() == 200) {
                            try {
                                logger.debug("API response for item {}: {}", itemId, response.body());
                                JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                                FoodItem foodItem = JsonUtil.getObjectMapper().treeToValue(rootNode, FoodItem.class);
                                items.add(foodItem);
                                logger.debug("Loaded food item: {} with keywords: {}", 
                                    foodItem.getName(), 
                                    foodItem.getKeywords() != null ? String.join(", ", foodItem.getKeywords()) : "null");
                            } catch (IOException e) {
                                logger.error("Error parsing food item data for ID {}", itemId, e);
                            }
                        } else {
                            logger.warn("Failed to load food item with ID {}: {}", itemId, response.statusCode());
                        }
                    } else {
                        logger.warn("No response received for food item with ID {}", itemId);
                    }
                }

                Platform.runLater(() -> {
                    orderItemsTable.setItems(items);
                    logger.info("Loaded {} items for order {}", items.size(), order.getId());
                });

            } catch (IOException | InterruptedException e) {
                logger.error("Exception occurred while loading order items", e);
            }
        });
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) orderIdLabel.getScene().getWindow();
        stage.close();
    }

    public void shutdown() {
        executorService.shutdown();
    }
} 