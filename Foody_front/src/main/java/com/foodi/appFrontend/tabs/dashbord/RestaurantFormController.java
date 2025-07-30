package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.Restaurant;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

public class RestaurantFormController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField taxField;
    @FXML
    private TextField additionalField;
    @FXML
    private ImageView restaurantImageView;
    @FXML
    private Label formErrorMessageLabel;

    private Restaurant restaurantEdited;
    private String base64ImageString;
    private Consumer<Void> refreshRestaurantCallback;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private String restaurantId;

    @FXML
    public void initialize() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com.foodapp.fooody/images/default_food_item.png");
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                if (!defaultImage.isError()) {
                    restaurantImageView.setImage(defaultImage);
                } else {
                    System.err.println("Error loading default image from stream: " + defaultImage.getException().getMessage());
                }
            } else {
                System.err.println("Default restaurant logo resource stream is null");
            }
        } catch (Exception e) {
            System.err.println("Exception loading logo: " + e.getMessage());
        }
    }
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
    public void setRestaurantEdited(Restaurant restaurant) {
        this.restaurantEdited = restaurant;
        if (restaurant != null) {
            nameField.setText(restaurantEdited.getName());
            addressField.setText(restaurantEdited.getAddress());
            phoneField.setText(restaurantEdited.getPhone());
            taxField.setText(String.valueOf(restaurantEdited.getTaxFee()));
            additionalField.setText(String.valueOf(restaurantEdited.getAdditionalFee()));
        }
    }

    public void setRefreshRestaurantCallback(Consumer<Void> refreshRestaurantCallback) {
        this.refreshRestaurantCallback = refreshRestaurantCallback;
    }

    /**
     * Show a message in the error label
     * @param message The message to display
     * @param isError Whether this is an error message (true) or success message (false)
     */
    private void showMessage(String message, boolean isError) {
        if (formErrorMessageLabel != null) {
            formErrorMessageLabel.setText(message);
            formErrorMessageLabel.setVisible(true);
            formErrorMessageLabel.setManaged(true);
            
            // Update styling based on message type
            if (isError) {
                formErrorMessageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #fef2f2; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #fca5a5; -fx-border-width: 1; -fx-border-radius: 8;");
            } else {
                formErrorMessageLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #f0fdf4; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #86efac; -fx-border-width: 1; -fx-border-radius: 8;");
            }
        }
    }

    @FXML
    private void handleLogoUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select restaurant logo");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                base64ImageString = Base64.getEncoder().encodeToString(fileContent);
                Image image = new Image(selectedFile.toURI().toString());
                restaurantImageView.setImage(image);
                showMessage("Logo selected: " + selectedFile.getName(), false);
            } catch (IOException e) {
                showMessage("Error reading logo file: " + e.getMessage(), true);
                e.printStackTrace();
            }
        } else {
            showMessage("No logo selected", false);
        }
    }

    @FXML
    private void handleSaveRestaurant(ActionEvent event) {
        String name = nameField.getText();
        String address = addressField.getText();
        String phone = phoneField.getText();
        String taxFeeStr = taxField.getText();
        String additionalFeeStr = additionalField.getText();
        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            showMessage("Please fill all required fields(Name, Address, phone)", true);
            return;
        }
        try {
            Integer taxFee = Integer.parseInt(taxFeeStr);
            Integer additionalFee = Integer.parseInt(additionalFeeStr);
            if (taxFee < 0 || additionalFee < 0) {
                showMessage("Tax fee and additional fee cannot be negative", true);
                return;
            }
            Map<String, Object> restaurantData = new HashMap<>();
            restaurantData.put("name", name);
            restaurantData.put("address", address);
            restaurantData.put("phone", phone);
            if (base64ImageString != null && !base64ImageString.isEmpty()) {
                restaurantData.put("logoBase64", base64ImageString);
            }
            restaurantData.put("tax_fee", taxFee);
            restaurantData.put("additional_fee", additionalFee);
            String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(restaurantData);
            executorService.submit(() -> {
                try {
                    String token = AuthManager.getJwtToken();
                    if (token == null || token.isEmpty()) {
                        Platform.runLater(() -> {
                            showMessage("Authentication token is missing.Please login again", true);
                        });
                        return;
                    }
                    Optional<HttpResponse<String>> responseOptional;
                    if (restaurantEdited == null){
                        responseOptional = ApiClient.post("/restaurants", jsonBody, token);
                    }else {
                        responseOptional=ApiClient.put("/restaurants/"+restaurantId, jsonBody, token);
                    }
                    if (responseOptional.isPresent()) {
                        HttpResponse<String> response = responseOptional.get();
                        JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                        Platform.runLater(() -> {
                            if (response.statusCode() == 200||response.statusCode()==201) {
                                showMessage(rootNode.has("message") ? rootNode.get("message").asText() : "Restaurant saved successfully", false);
                                if (refreshRestaurantCallback !=null){
                                    refreshRestaurantCallback.accept(null);
                                }
                                closeForm();
                            } else {
                                showMessage("Error saving restaurant: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred"), true);
                            }
                        });

                    } else {
                        Platform.runLater(() -> {
                            showMessage("Failed to connect to server for saving restaurant", true);
                        });
                    }
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> {
                        showMessage("Unexpected error during adding restaurant:" + e.getMessage(), true);
                        e.printStackTrace();
                    });
                }
            });
        } catch (NumberFormatException e) {
            showMessage("Tax fee and additional fee must be valid numbers", true);
        } catch (JsonProcessingException e) {
            showMessage("Error processing json: "+e.getMessage(), true);
        }
    }

    private void closeForm() {
        Stage stage=(Stage) nameField.getScene().getWindow();
        stage.close();
        executorService.shutdown();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeForm();
    }
}
