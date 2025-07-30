package com.foodi.appFrontend.tabs.signup;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.User;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.foodi.appFrontend.tabs.login.Login; // Ensure this is imported
import javafx.stage.FileChooser; // For file upload
import java.io.File; // For file upload
import java.io.InputStream;
import java.nio.file.Files; // For file to byte[]
import java.util.Base64; // For Base64 encoding
import javafx.scene.image.Image; // For ImageView
import javafx.scene.image.ImageView; // For ImageView

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Signup {

    @FXML
    private MFXTextField nameField;
    @FXML
    private MFXTextField phoneField;
    @FXML
    private MFXTextField emailField;
    @FXML
    private MFXPasswordField passwordField;
    @FXML
    private MFXTextField addressField;
    @FXML
    private MFXTextField bankNameField;
    @FXML
    private MFXTextField accountField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private MFXButton signupButton;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private ImageView profileImageView; // FXML for ImageView

    private String base64ImageString; // To store the Base64 image string

    @FXML
    public void initialize() {
        roleComboBox.setItems(getRoles());
        // Set default profile image programmatically
        try {
            // Use getResourceAsStream to load image from classpath
            InputStream imageStream = getClass().getResourceAsStream("/com/foodi/appFrontend/images/default_profile.jpg");
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                if (!defaultImage.isError()) {
                    profileImageView.setImage(defaultImage);
                } else {
                    System.err.println("Error loading default image from stream: " + defaultImage.getException().getMessage());
                }
            } else {
                System.err.println("Default image resource stream is null. Path might be incorrect or file missing.");
            }
        } catch (Exception e) {
            System.err.println("Exception loading default profile image programmatically: " + e.getMessage());
        }
    }

    public ObservableList<String> getRoles() {
        return FXCollections.observableArrayList("Buyer", "Seller","Courier");
    }

    @FXML
    private void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                base64ImageString = Base64.getEncoder().encodeToString(fileContent);
                // Display selected image in ImageView
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
                errorMessageLabel.setText("Image selected: " + selectedFile.getName());
            } catch (IOException e) {
                errorMessageLabel.setText("Error reading image file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            errorMessageLabel.setText("No image selected.");
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String address = addressField.getText();
        String bankName = bankNameField.getText();
        String accountNumber = accountField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || role == null) {
            errorMessageLabel.setText("Please fill in all fields.");
            return;
        }
        else if(role.equals("Buyer") || role.equals("Seller")) {
            if(address.isEmpty()){
                errorMessageLabel.setText("Please fill in address field.");
                return;
            }
        }

        try {
            Map<String, Object> signupData = new HashMap<>();
            signupData.put("full_name", name);
            signupData.put("phone", phone);
            signupData.put("email", email);
            signupData.put("password", password);
            signupData.put("address", address);

            if ("Seller".equals(role) || "Courier".equals(role)) {
                Map<String, String> bankInfo = new HashMap<>();
                bankInfo.put("bank_name", bankName);
                bankInfo.put("account_number", accountNumber);
                signupData.put("bank_info", bankInfo);
            }
            signupData.put("role", role.toUpperCase());

            if (base64ImageString != null && !base64ImageString.isEmpty()) {
                signupData.put("profileImageBase64", base64ImageString); // Add Base64 image string
            }

            String signupJson = JsonUtil.getObjectMapper().writeValueAsString(signupData);

            Optional<HttpResponse<String>> responseOpt = ApiClient.post("/auth/register", signupJson, null);

            if (responseOpt.isPresent()) {
                HttpResponse<String> response = responseOpt.get();
                JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());
                System.out.println("Response body: " + response.body());

                if (response.statusCode() == 200) {
                    String message = rootNode.has("message") ? rootNode.get("message").asText() : "Sign Up successful!";
                    String token = rootNode.get("access_token").asText();
                    String userId = rootNode.get("user_id").asText();

                    AuthManager.setJwtToken(token);
                    AuthManager.setCurrentUserId(userId);
                    
                    // Since we don't have the full user object in the response,
                    // we'll set the role from what was selected in the form
                    AuthManager.setCurrentUserRole(roleComboBox.getValue().toUpperCase());

                    errorMessageLabel.setText(message);

                    // Create a basic User object with the information we have
                    User user = new User();
                    user.setId(userId);
                    user.setRole(roleComboBox.getValue().toUpperCase());

                    handleUserNavigation(event, user);
                } else {
                    String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                    errorMessageLabel.setText(errorMessage);
                }
            } else {
                errorMessageLabel.setText("Failed to connect to server. Please check server status.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            errorMessageLabel.setText("An unexpected error occurred: " + e.getMessage());
        }
    }
    @FXML
    private void login(ActionEvent event) throws IOException {
        System.out.println(" Login hyperlink clicked!");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/login.fxml"));
        Parent loginView = loader.load();
        Scene loginScene = new Scene(loginView);

        loginScene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.setTitle("Fooody - Login");
        window.show();
    }

    private void handleUserNavigation(ActionEvent event, User user) throws IOException {
        String userRole = user.getRole();
        String userStatus = user.getStatus();

        if ("SELLER".equals(userRole.toUpperCase()) || "COURIER".equals(userRole.toUpperCase())) {
            if ("PENDING_APPROVAL".equals(userStatus) || "REJECTED".equals(userStatus)) {
                errorMessageLabel.setText(""); // Clear message on current screen
                String messageToDisplay = "Your account was successfully registered, but you must wait for the admin to approve it. If you are approved, you can log in.";
                AuthManager.logout();

                System.out.println("Signup Controller: Navigating to Login with propagated message: " + messageToDisplay);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/login.fxml"));
                Parent loginView = loader.load();
                Login loginController = loader.getController();
                loginController.setErrorMessageText(messageToDisplay); // Set message on new Login controller

                Scene loginScene = new Scene(loginView);
                loginScene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
                Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
                window.setScene(loginScene);
                window.setTitle("Fooody - Login");
                window.show();
                System.out.println("Signup Controller: Navigation to Login screen completed.");
                return;
            }
            else { // This handles cases where Seller/Courier is APPROVED
                navigateToDashboard(event, userRole);
            }
        }
        navigateToDashboard(event, userRole); // For Buyer/Admin or approved Seller/Courier
    }

    // This method is no longer used directly for navigation, but might be called elsewhere
    private void navigateToLoginScreen(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/login.fxml"));
        Parent loginView = loader.load();
        Scene loginScene = new Scene(loginView);
        loginScene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(loginScene);
        window.setTitle("Fooody - Login");
        window.show();
    }

    private void navigateToDashboard(ActionEvent event, String role) throws IOException {
        String fxmlPath;
        String title;
        // Map roles to FXML paths
        switch (role.toUpperCase()) {
            case "BUYER":
                fxmlPath = "/com/foodi/appFrontend/view/dashbord/BuyerDashboardView.fxml";
                title = "Fooody - Buyer Dashboard";
                break;
            case "SELLER":
                fxmlPath = "/com/foodi/appFrontend/view/dashbord/SellerDashboardView.fxml";
                title = "Fooody - Seller Dashboard";
                break;
            case "COURIER":
                fxmlPath = "/com/foodi/appFrontend/view/dashbord/CourierDashboardView.fxml";
                title = "Fooody - Courier Dashboard";
                break;
            case "ADMIN":
                System.out.println("وارد بخش ادمین شد");
                fxmlPath = "/com/foodi/appFrontend/view/dashbord/AdminDashboardView.fxml";
                title = "Fooody - Admin Dashboard";
                System.out.println("ادرس درست بود");
                break;
            default:
                errorMessageLabel.setText("Unknown user role. Cannot navigate.");
                return;
        }

        java.net.URL resourceUrl = getClass().getResource(fxmlPath);
        System.out.println("Attempting to load FXML: " + fxmlPath);
        System.out.println("Resource URL found: " + resourceUrl);
        if (resourceUrl == null) {
            System.err.println("ERROR: FXML resource was NOT found at path: " + fxmlPath + ". navigateToDashboard will fail.");
            Platform.runLater(() -> {
                if (errorMessageLabel != null) {
                    errorMessageLabel.setText("CRITICAL ERROR: Dashboard FXML not found at " + fxmlPath + ". Check console for details.");
                } else {
                    System.err.println("ERROR: errorMessageLabel is null. Cannot display error on UI. Is fx:id='errorMessageLabel' correct in login.fxml?");
                }
            });
            return;
        } else {
            System.out.println("SUCCESS: FXML resource URL: " + resourceUrl.toExternalForm());
        }

        FXMLLoader loader = new FXMLLoader(resourceUrl);
        Parent dashboardView = loader.load();
        Scene dashboardScene = new Scene(dashboardView);
        dashboardScene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(dashboardScene);
        window.setTitle(title);
        window.show();
    }
}