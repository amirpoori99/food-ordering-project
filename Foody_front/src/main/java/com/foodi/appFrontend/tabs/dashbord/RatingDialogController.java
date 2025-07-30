package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.Order;
import com.foodi.appFrontend.models.FoodItem;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RatingDialogController {

    private static final Logger logger = LoggerFactory.getLogger(RatingDialogController.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    private Label rawPriceLabel;
    @FXML
    private Label taxFeeLabel;
    @FXML
    private Label additionalFeeLabel;
    @FXML
    private Label courierFeeLabel;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private TableView<FoodItemWithQuantity> orderItemsTable;
    @FXML
    private TableColumn<FoodItemWithQuantity, String> itemNameColumn;
    @FXML
    private TableColumn<FoodItemWithQuantity, Integer> itemPriceColumn;
    @FXML
    private TableColumn<FoodItemWithQuantity, String> itemDescriptionColumn;
    @FXML
    private Slider ratingSlider;
    @FXML
    private Label ratingValueLabel;
    @FXML
    private TextArea commentTextArea;
    @FXML
    private VBox imagePreviewContainer;
    @FXML
    private Label noImagesLabel;

    private Order order;
    private Stage dialogStage;
    private boolean ratingSubmitted = false;
    private List<String> imageBase64List = new ArrayList<>();

    @FXML
    public void initialize() {
        // Initialize rating slider
        ratingSlider.setMin(1);
        ratingSlider.setMax(5);
        ratingSlider.setValue(3);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setSnapToTicks(true);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        
        // Update rating value label when slider changes
        ratingSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int rating = newValue.intValue();
            ratingValueLabel.setText(String.valueOf(rating));
        });
        
        // Set initial rating value
        ratingValueLabel.setText("3");
        
        // Setup table columns
        setupTableColumns();
    }
    
    private void setupTableColumns() {
        // Custom cell value factories to avoid reflection issues
        itemNameColumn.setCellValueFactory(cellData -> {
            FoodItemWithQuantity item = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(item.getName());
        });
        
        itemPriceColumn.setCellValueFactory(cellData -> {
            FoodItemWithQuantity item = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(item.getPrice()).asObject();
        });
        
        itemDescriptionColumn.setCellValueFactory(cellData -> {
            FoodItemWithQuantity item = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(item.getDescription());
        });
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            loadOrderDetails();
        }
    }
    
    private void loadOrderDetails() {
        if (order == null) return;
        
        logger.info("Loading order details for order ID: {}", order.getId());
        logger.info("Order item IDs: {}", order.getItemIds());
        
        // First, populate basic order information from the Order object
        populateOrderDetails();
        
        // Then load food items separately
        loadOrderItems();
    }
    
    private void populateOrderDetails() {
        // Populate price breakdown labels
        rawPriceLabel.setText(order.getRawPrice() != null ? "$" + order.getRawPrice() : "$0");
        taxFeeLabel.setText(order.getTaxFee() != null ? "$" + order.getTaxFee() : "$0");
        additionalFeeLabel.setText(order.getAdditionalFee() != null ? "$" + order.getAdditionalFee() : "$0");
        courierFeeLabel.setText(order.getCourierFee() != null ? "$" + order.getCourierFee() : "$0");
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
                ObservableList<FoodItemWithQuantity> items = FXCollections.observableArrayList();
                
                // Get unique item IDs to avoid duplicates
                List<Integer> uniqueItemIds = order.getItemIds().stream().distinct().toList();
                logger.info("Loading {} unique items for order {}", uniqueItemIds.size(), order.getId());
                
                for (Integer itemId : uniqueItemIds) {
                    logger.debug("Loading item with ID: {}", itemId);
                    Optional<HttpResponse<String>> responseOpt = ApiClient.get("/items/" + itemId, token);
                    
                    if (responseOpt.isPresent()) {
                        HttpResponse<String> response = responseOpt.get();
                        
                        if (response.statusCode() == 200) {
                            try {
                                JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                                FoodItem foodItem = JsonUtil.getObjectMapper().treeToValue(rootNode, FoodItem.class);
                                
                                // Count quantity of this item in the order
                                long quantity = order.getItemIds().stream().filter(id -> id.equals(itemId)).count();
                                
                                // Create a custom FoodItem with quantity information
                                FoodItemWithQuantity itemWithQuantity = new FoodItemWithQuantity(foodItem, (int) quantity);
                                items.add(itemWithQuantity);
                                
                                logger.debug("Loaded item: {} with quantity: {}", foodItem.getName(), quantity);
                                
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
    
    // Helper class to store FoodItem with quantity
    private static class FoodItemWithQuantity extends FoodItem {
        private final int quantity;
        
        public FoodItemWithQuantity(FoodItem original, int quantity) {
            this.quantity = quantity;
            // Copy all properties from original FoodItem
            this.setId(original.getId());
            this.setName(original.getName());
            this.setDescription(original.getDescription());
            this.setPrice(original.getPrice());
            this.setKeywords(original.getKeywords());
        }
        
        public int getQuantity() {
            return quantity;
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isRatingSubmitted() {
        return ratingSubmitted;
    }

    @FXML
    private void handleAddPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // Read file and convert to base64
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                String base64String = Base64.getEncoder().encodeToString(fileContent);
                
                // Add to list
                imageBase64List.add(base64String);
                
                // Create image preview
                createImagePreview(selectedFile, base64String);
                
                // Hide "no images" label
                noImagesLabel.setVisible(false);
                
            } catch (IOException e) {
                logger.error("Error reading image file", e);
            }
        }
    }
    
    private void createImagePreview(File imageFile, String base64String) {
        try {
            // Create image view
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            
            // Create remove button
            Button removeButton = new Button("❌");
            removeButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 20; -fx-min-height: 20; -fx-max-width: 20; -fx-max-height: 20; -fx-font-size: 10px;");
            removeButton.setOnAction(e -> removeImage(base64String));
            
            // Create container for image and remove button
            VBox imageContainer = new VBox(5);
            imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
            imageContainer.getChildren().addAll(imageView, removeButton);
            
            // Add to preview container
            imagePreviewContainer.getChildren().add(imageContainer);
            
        } catch (Exception e) {
            logger.error("Error creating image preview", e);
        }
    }
    
    private void removeImage(String base64String) {
        imageBase64List.remove(base64String);
        
        // Remove from preview
        imagePreviewContainer.getChildren().clear();
        
        // Recreate previews for remaining images
        for (String remainingImage : imageBase64List) {
            // This is a simplified approach - in a real app you'd store the original files
            // For now, we'll just show a placeholder
            Label placeholder = new Label("📷 Image");
            placeholder.setStyle("-fx-background-color: #e5e7eb; -fx-padding: 10; -fx-background-radius: 8;");
            imagePreviewContainer.getChildren().add(placeholder);
        }
        
        // Show "no images" label if no images left
        if (imageBase64List.isEmpty()) {
            noImagesLabel.setVisible(true);
        }
    }



    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleSubmitRating() {
        // Get rating from slider (it's always valid since slider has min/max constraints)
        Integer rating = (int) ratingSlider.getValue();
        String comment = commentTextArea.getText();

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    logger.warn("Authentication token missing when submitting rating");
                    return;
                }

                // Prepare rating data
                Map<String, Object> ratingData = new HashMap<>();
                ratingData.put("order_id", order.getId());
                ratingData.put("rating", rating);
                if (comment != null && !comment.trim().isEmpty()) {
                    ratingData.put("comment", comment.trim());
                }
                if (!imageBase64List.isEmpty()) {
                    ratingData.put("imageBase64", imageBase64List);
                }

                String jsonPayload = JsonUtil.getObjectMapper().writeValueAsString(ratingData);

                Optional<HttpResponse<String>> responseOpt = ApiClient.post("/ratings", jsonPayload, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            ratingSubmitted = true;
                            if (dialogStage != null) {
                                dialogStage.close();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "Failed to submit rating";
                            logger.error("Failed to submit rating: {}", errorMessage);
                        }
                    });
                } else {
                    logger.error("Failed to connect to server when submitting rating");
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Error submitting rating", e);
            }
        });
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 