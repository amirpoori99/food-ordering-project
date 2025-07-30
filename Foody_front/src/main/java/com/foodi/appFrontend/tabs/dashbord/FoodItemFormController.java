package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.FoodItem;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FoodItemFormController {
    private static final Logger logger = LoggerFactory.getLogger(FoodItemFormController.class);
    
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField supplyField;
    @FXML
    private TextField keywordsField;
    @FXML
    private ImageView foodItemImageView;
    @FXML
    private Label formErrorMessageLabel;

    private String base64ImageString;
    private String restaurantId;
    private FoodItem foodItemToEdit;
    private Consumer<Void> refreshFoodItemsCallback;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com.foodapp.fooody/images/default_food_item.png");
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                if (!defaultImage.isError()) {
                    foodItemImageView.setImage(defaultImage);
                } else {
                    System.err.println("Error loading default image from stream: " + defaultImage.getException().getMessage());
                }
            } else {
                System.err.println("Default food item image resource stream is null");
            }
        } catch (Exception e) {
            System.err.println("Exception loading food item image: " + e.getMessage());
        }
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
        logger.info("Restaurant ID set to: {}", restaurantId);
    }

    public void setFoodItemToEdit(FoodItem foodItem) {
        this.foodItemToEdit = foodItem;
        if (foodItem != null) {
            nameField.setText(foodItem.getName());
            descriptionField.setText(foodItem.getDescription());
            priceField.setText(String.valueOf(foodItem.getPrice()));
            supplyField.setText(String.valueOf(foodItem.getSupply()));
            if (foodItem.getKeywords() != null) {
                keywordsField.setText(String.join(", ", foodItem.getKeywords()));
            }
        }
    }

    public void setRefreshFoodItemCallback(Consumer<Void> refreshFoodItemCallback) {
        this.refreshFoodItemsCallback = refreshFoodItemCallback;
    }

    @FXML
    private void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select food item image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                base64ImageString = Base64.getEncoder().encodeToString(fileContent);
                Image image = new Image(selectedFile.toURI().toString());
                foodItemImageView.setImage(image);
                formErrorMessageLabel.setText("Image selected: " + selectedFile.getName());

            } catch (IOException e) {
                formErrorMessageLabel.setText("Error reading image file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            formErrorMessageLabel.setText("No image selected");
        }

    }

    @FXML
    private void handleSaveFoodItem(ActionEvent event) {
        logger.info("handleSaveFoodItem called - restaurantId: {}", restaurantId);
        String name = nameField.getText();
        String description = descriptionField.getText();
        String priceStr = priceField.getText();
        String supplyStr = supplyField.getText();
        String keywordsStr = keywordsField.getText();
        
        logger.debug("Form data - Name: '{}', Description: '{}', Price: '{}', Supply: '{}', Keywords: '{}'", 
                    name, description, priceStr, supplyStr, keywordsStr);
        
        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || supplyStr.isEmpty()) {
            logger.warn("Validation failed - missing required fields");
            formErrorMessageLabel.setText("Please fill all required fields (Name, Description, Price, Supply)");
            return;
        }
        try {
            Integer price = Integer.parseInt(priceStr);
            Integer supply = Integer.parseInt(supplyStr);

            String[] keywords = keywordsStr.isEmpty() ? new String[0] : keywordsStr.split(",\\s*");
            logger.debug("Parsed values - Price: {}, Supply: {}, Keywords count: {}", price, supply, keywords.length);
            
            if (price <= 0 || supply < 0) {
                logger.warn("Validation failed - Price: {}, Supply: {}", price, supply);
                formErrorMessageLabel.setText("Price must be positive and supply cannot be negative");
                return;
            }
            Map<String, Object> foodItemData = new HashMap<>();
            foodItemData.put("name", name);
            foodItemData.put("description", description);
            foodItemData.put("price", price);
            foodItemData.put("supply", supply);
            foodItemData.put("keywords", keywords);
            // Note: vendor_id is not needed in body since restaurant ID is in URL path
            if (base64ImageString != null && !base64ImageString.isEmpty()) {
                foodItemData.put("imageBase64", base64ImageString);
                logger.debug("Including imageBase64 in request (length: {})", base64ImageString.length());
            } else {
                logger.debug("No image to include in request");
            }
            
            logger.info("Request data map: {}", foodItemData);
            String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(foodItemData);
            logger.info("JSON request body: {}", jsonBody);
            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        logger.error("Authentication token is missing");
                        Platform.runLater(() -> {
                            formErrorMessageLabel.setText("Authentication token is missing");
                        });
                        return;
                    }
                    
                    String endpoint = foodItemToEdit == null ? 
                        "/restaurants/" + restaurantId + "/item" : 
                        "/restaurants/" + restaurantId + "/item/" + foodItemToEdit.getId();
                    String method = foodItemToEdit == null ? "POST" : "PUT";
                    
                    logger.info("Making {} request to endpoint: {}", method, endpoint);
                    
                    Optional<HttpResponse<String>> responseOptional;
                    if (foodItemToEdit == null) {
                        responseOptional = ApiClient.post("/restaurants/" + restaurantId + "/item", jsonBody, token);
                    } else {
                        responseOptional = ApiClient.put("/restaurants/" + restaurantId + "/item/" + foodItemToEdit.getId(), jsonBody, token);
                    }
                    if (responseOptional.isPresent()) {
                        HttpResponse<String> response = responseOptional.get();
                        logger.info("Response received - Status: {}, Body length: {}", response.statusCode(), response.body().length());
                        logger.debug("Response body: {}", response.body());
                        
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                logger.info("Food item saved successfully");
                                formErrorMessageLabel.setText("Food item saved successfully");
                                if (refreshFoodItemsCallback != null) {
                                    refreshFoodItemsCallback.accept(null);
                                }
                                closeForm();
                            } else {
                                String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred";
                                logger.error("Error saving food item - Status: {}, Error: {}", response.statusCode(), errorMessage);
                                formErrorMessageLabel.setText("Error saving food item: " + errorMessage);
                            }
                        });
                    } else {
                        logger.error("No response received from server");
                        Platform.runLater(() -> {
                            formErrorMessageLabel.setText("Failed to connect server");
                        });
                    }

                } catch (IOException | InterruptedException e) {
                    logger.error("Exception during API call", e);
                    Platform.runLater(() -> {
                        formErrorMessageLabel.setText("An unexpected error occurred: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            });
        } catch (NumberFormatException e) {
            logger.error("Number format exception - Price: '{}', Supply: '{}'", priceStr, supplyStr, e);
            formErrorMessageLabel.setText("Price and supply must be valid numbers");
        } catch (JsonProcessingException e) {
            logger.error("JSON processing exception", e);
            formErrorMessageLabel.setText("Error processing Json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
        executorService.shutdown();
    }
}
