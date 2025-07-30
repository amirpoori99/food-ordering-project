package com.foodi.appFrontend.tabs.dashbord;

import com.foodi.appFrontend.models.FoodItem;
import com.foodi.appFrontend.models.Restaurant;
import com.foodi.appFrontend.models.ItemRating;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.http.HttpResponse;

public class FoodItemDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(FoodItemDetailsController.class);

    @FXML private ImageView foodItemImageView;
    @FXML private Label nameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private Label supplyLabel;
    @FXML private Label idLabel;
    @FXML private Label ratingLabel;
    @FXML private VBox keywordsContainer;
    @FXML private Label restaurantNameLabel;
    @FXML private Label restaurantAddressLabel;
    @FXML private Label restaurantPhoneLabel;
    @FXML private VBox reviewsContainer;

    private FoodItem foodItem;
    private Restaurant restaurant;
    private RestaurantMenuController restaurantMenuController; // Reference to parent controller
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        loadDefaultImage();
    }

    private void loadDefaultImage() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com/foodi/appFrontend/images/default_food_item.png");
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                if (!defaultImage.isError()) {
                    foodItemImageView.setImage(defaultImage);
                } else {
                    logger.error("Error loading default image: {}", defaultImage.getException().getMessage());
                }
            } else {
                logger.warn("Default food item image resource stream is null");
            }
        } catch (Exception e) {
            logger.error("Exception loading food item image: {}", e.getMessage());
        }
    }

    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
        if (foodItem != null) {
            populateFoodItemDetails();
        }
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        if (restaurant != null) {
            populateRestaurantDetails();
        }
    }

    public void setRestaurantMenuController(RestaurantMenuController controller) {
        this.restaurantMenuController = controller;
    }

    private void populateFoodItemDetails() {
        if (foodItem == null) return;

        // Set basic information
        nameLabel.setText(foodItem.getName());
        descriptionLabel.setText(foodItem.getDescription());
        priceLabel.setText("$" + foodItem.getPrice());
        supplyLabel.setText(String.valueOf(foodItem.getSupply()));
        idLabel.setText(String.valueOf(foodItem.getId()));

        // Load food item image
        if (foodItem.getImageBase64() != null && !foodItem.getImageBase64().isEmpty()) {
            try {
                byte[] decodedImg = Base64.getDecoder().decode(foodItem.getImageBase64());
                Image foodImage = new Image(new ByteArrayInputStream(decodedImg));
                foodItemImageView.setImage(foodImage);
            } catch (Exception e) {
                logger.error("Error loading food item image: {}", e.getMessage());
            }
        }

        // Load rating for the food item
        loadFoodItemRating();

        // Populate keywords
        populateKeywords();
    }

    private void loadFoodItemRating() {
        if (foodItem == null) return;

        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    logger.warn("Authentication token missing for rating load");
                    return;
                }

                // Call the ratings endpoint for the food item
                String ratingUrl = "/ratings/items/" + foodItem.getId();
                Optional<HttpResponse<String>> ratingResponseOpt = ApiClient.get(ratingUrl, token);

                if (ratingResponseOpt.isPresent()) {
                    HttpResponse<String> ratingResponse = ratingResponseOpt.get();

                    if (ratingResponse.statusCode() == 200) {
                        try {
                            JsonNode ratingRootNode = JsonUtil.getObjectMapper().readTree(ratingResponse.body());
                            ItemRating itemRating = JsonUtil.getObjectMapper().treeToValue(ratingRootNode, ItemRating.class);

                            // Set the rating on the UI
                            if (itemRating != null && itemRating.getAvgRating() != null) {
                                Platform.runLater(() -> {
                                    ratingLabel.setText(String.format("%.1f ⭐", itemRating.getAvgRating()));
                                    // Load and display reviews
                                    populateReviews(itemRating.getComments());
                                });
                            } else {
                                Platform.runLater(() -> {
                                    ratingLabel.setText("No ratings");
                                    // Show no reviews message
                                    showNoReviewsMessage();
                                });
                            }
                        } catch (Exception e) {
                            logger.error("Error parsing rating response for food item {}: {}", foodItem.getId(), e.getMessage());
                            Platform.runLater(() -> {
                                ratingLabel.setText("No ratings");
                                showNoReviewsMessage();
                            });
                        }
                    } else if (ratingResponse.statusCode() == 400 || ratingResponse.statusCode() == 404) {
                        // Item has no ratings yet
                        Platform.runLater(() -> {
                            ratingLabel.setText("No ratings");
                            showNoReviewsMessage();
                        });
                    } else {
                        logger.warn("Failed to load rating for food item {}: {}", foodItem.getId(), ratingResponse.statusCode());
                        Platform.runLater(() -> {
                            ratingLabel.setText("No ratings");
                            showNoReviewsMessage();
                        });
                    }
                } else {
                    logger.warn("No response received for food item rating {}", foodItem.getId());
                    Platform.runLater(() -> {
                        ratingLabel.setText("No ratings");
                        showNoReviewsMessage();
                    });
                }
            } catch (Exception e) {
                logger.error("Error loading rating for food item {}", foodItem.getId(), e);
                Platform.runLater(() -> {
                    ratingLabel.setText("No ratings");
                    showNoReviewsMessage();
                });
            }
        });
    }

    private void populateReviews(List<ItemRating.RatingComment> comments) {
        reviewsContainer.getChildren().clear();
        
        if (comments != null && !comments.isEmpty()) {
            for (ItemRating.RatingComment comment : comments) {
                VBox reviewBox = createReviewBox(comment);
                reviewsContainer.getChildren().add(reviewBox);
            }
        } else {
            showNoReviewsMessage();
        }
    }

    private VBox createReviewBox(ItemRating.RatingComment comment) {
        VBox reviewBox = new VBox(8);
        reviewBox.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 8;");

        // Rating and date header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label ratingLabel = new Label("⭐".repeat(comment.getRating()));
        ratingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f59e0b;");
        
        Label dateLabel = new Label(formatDate(comment.getCreatedAt()));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
        
        headerBox.getChildren().addAll(ratingLabel, dateLabel);

        // Comment text
        Label commentLabel = new Label(comment.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: 500;");

        // User ID
        Label userLabel = new Label("User ID: " + comment.getUserId());
        userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");

        // Add images if available
        if (comment.getImageBase64() != null && !comment.getImageBase64().isEmpty()) {
            HBox imagesBox = new HBox(10);
            imagesBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            imagesBox.setStyle("-fx-padding: 10 0;");
            
            for (String imageBase64 : comment.getImageBase64()) {
                try {
                    byte[] imageData = Base64.getDecoder().decode(imageBase64);
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(100);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-border-width: 1;");
                    
                    imagesBox.getChildren().add(imageView);
                } catch (Exception e) {
                    logger.error("Error loading comment image: {}", e.getMessage());
                }
            }
            
            reviewBox.getChildren().addAll(headerBox, commentLabel, userLabel, imagesBox);
        } else {
            reviewBox.getChildren().addAll(headerBox, commentLabel, userLabel);
        }
        
        return reviewBox;
    }

    private String formatDate(List<Integer> dateArray) {
        if (dateArray == null || dateArray.size() < 6) {
            return "Unknown date";
        }
        
        // Format: [year, month, day, hour, minute, second, nanoseconds]
        int year = dateArray.get(0);
        int month = dateArray.get(1);
        int day = dateArray.get(2);
        int hour = dateArray.get(3);
        int minute = dateArray.get(4);
        
        return String.format("%d/%d/%d at %02d:%02d", month, day, year, hour, minute);
    }

    private void showNoReviewsMessage() {
        reviewsContainer.getChildren().clear();
        Label noReviewsLabel = new Label("No reviews available for this food item yet.");
        noReviewsLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic; -fx-font-size: 14px; -fx-alignment: center;");
        noReviewsLabel.setMaxWidth(Double.MAX_VALUE);
        reviewsContainer.getChildren().add(noReviewsLabel);
    }

    private void populateKeywords() {
        keywordsContainer.getChildren().clear();
        
        if (foodItem.getKeywords() != null && foodItem.getKeywords().length > 0) {
            HBox currentRow = new HBox(8);
            currentRow.setStyle("-fx-alignment: center-left;");
            
            for (int i = 0; i < foodItem.getKeywords().length; i++) {
                String keyword = foodItem.getKeywords()[i];
                Label keywordLabel = new Label(keyword);
                keywordLabel.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: 500;");
                
                // Start a new row every 3 keywords for better layout
                if (i > 0 && i % 3 == 0) {
                    keywordsContainer.getChildren().add(currentRow);
                    currentRow = new HBox(8);
                    currentRow.setStyle("-fx-alignment: center-left;");
                }
                currentRow.getChildren().add(keywordLabel);
            }
            
            // Add the last row if it has any children
            if (!currentRow.getChildren().isEmpty()) {
                keywordsContainer.getChildren().add(currentRow);
            }
        } else {
            Label noKeywordsLabel = new Label("No keywords available");
            noKeywordsLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic;");
            keywordsContainer.getChildren().add(noKeywordsLabel);
        }
    }

    private void populateRestaurantDetails() {
        if (restaurant == null) return;

        restaurantNameLabel.setText(restaurant.getName());
        restaurantAddressLabel.setText(restaurant.getAddress());
        restaurantPhoneLabel.setText(restaurant.getPhone());
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        if (foodItem == null) {
            logger.warn("No food item selected for adding to cart");
            return;
        }

        // Show quantity dialog
        TextInputDialog quantityDialog = new TextInputDialog("1");
        quantityDialog.setTitle("Add to Cart");
        quantityDialog.setHeaderText("Enter quantity for " + foodItem.getName() + ":");
        quantityDialog.setContentText("Quantity:");

        // Apply custom styling to the dialog
        quantityDialog.getDialogPane().setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        quantityDialog.getDialogPane().getButtonTypes().forEach(buttonType -> {
            if (buttonType == javafx.scene.control.ButtonType.OK) {
                quantityDialog.getDialogPane().lookupButton(buttonType).setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16; -fx-font-weight: bold;");
            } else if (buttonType == javafx.scene.control.ButtonType.CANCEL) {
                quantityDialog.getDialogPane().lookupButton(buttonType).setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16; -fx-font-weight: bold;");
            }
        });

        Optional<String> quantityResult = quantityDialog.showAndWait();
        if (quantityResult.isPresent()) {
            try {
                int quantity = Integer.parseInt(quantityResult.get().trim());
                if (quantity <= 0) {
                    logger.warn("Invalid quantity entered: {}", quantity);
                    return;
                }

                if (quantity > foodItem.getSupply()) {
                    logger.warn("Quantity exceeds available supply. Requested: {}, Available: {}", quantity, foodItem.getSupply());
                    return;
                }

                // Add to cart using the restaurant menu controller
                if (restaurantMenuController != null) {
                    restaurantMenuController.addItemToCart(foodItem, quantity);
                    logger.info("Added {} x {} to cart via restaurant menu controller", quantity, foodItem.getName());
                } else {
                    logger.warn("Restaurant menu controller not available for adding to cart");
                }

            } catch (NumberFormatException e) {
                logger.error("Invalid quantity format: {}", quantityResult.get());
            }
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 