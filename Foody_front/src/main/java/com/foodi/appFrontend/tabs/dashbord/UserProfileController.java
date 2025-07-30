package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.User;
import com.foodi.appFrontend.utils.ApiClient;
import com.foodi.appFrontend.utils.AuthManager;
import com.foodi.appFrontend.utils.JsonUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

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
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @FXML private ImageView profileImageView;
    @FXML private MFXTextField fullNameField;
    @FXML private MFXTextField phoneField;
    @FXML private MFXTextField emailField;
    @FXML private MFXTextField addressField;
    @FXML private MFXTextField roleField;
    @FXML private MFXTextField walletBalanceField;
    @FXML private Label profileErrorMessageLabel;
    @FXML private MFXButton saveButton;
    @FXML private MFXButton cancelButton;
    @FXML private MFXButton editProfileButton;
    @FXML private io.github.palexdev.materialfx.controls.MFXTextField bankNameField;
    @FXML private io.github.palexdev.materialfx.controls.MFXTextField accountNumberField;
    @FXML private Label bankNameLabel;
    @FXML private Label accountNumberLabel;


    private User currentUser;
    private String base64ImageString; // To store the Base64 image string for upload
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        logger.info("UserProfileController initialized");
        logger.debug("ProfileImageView initialized: {}", profileImageView != null);
        
        setFieldsEditable(false); // Initially non-editable
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        editProfileButton.setVisible(true); // Ensure Edit Profile button is visible
        loadUserProfile(); // Load user profile data when the view is initialized
    }

    private void setFieldsEditable(boolean editable) {
        fullNameField.setEditable(editable);
        phoneField.setEditable(editable);
        emailField.setEditable(editable);
        addressField.setEditable(editable);
        
        // Only make bank fields editable for non-buyer roles
        if (currentUser != null && !"buyer".equals(currentUser.getRole())) {
            bankNameField.setEditable(editable);
            accountNumberField.setEditable(editable);
        }
        // roleField and walletBalanceField remain non-editable
    }

    private void loadUserProfile() {
        profileErrorMessageLabel.setText("Loading profile...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> profileErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/auth/profile", token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                logger.info("Profile response received: {}", rootNode.toString());
                                currentUser = JsonUtil.getObjectMapper().treeToValue(rootNode, User.class);
                                logger.info("User profile parsed - ProfileImageBase64: {}", 
                                          currentUser.getProfileImageBase64() != null ? "present" : "null");
                                populateProfileFields(currentUser);
                                profileErrorMessageLabel.setText("Profile loaded successfully.");
                            } catch (IOException e) {
                                logger.error("Error parsing profile data", e);
                                profileErrorMessageLabel.setText("Error parsing profile data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            logger.warn("Profile loading failed - Status: {}, Error: {}", response.statusCode(), errorMessage);
                            profileErrorMessageLabel.setText("Error loading profile: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> profileErrorMessageLabel.setText("Failed to connect to server to load profile."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    profileErrorMessageLabel.setText("An unexpected error occurred while loading profile: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    private void populateProfileFields(User user) {
        if (user != null) {
            fullNameField.setText(user.getFullName());
            phoneField.setText(user.getPhone());
            emailField.setText(user.getEmail());
            addressField.setText(user.getAddress());
            roleField.setText(user.getRole());
            
            // Load wallet balance using the new API endpoint
            loadWalletBalance(user.getId());

            // Load profile image
            logger.info("Loading profile image - ProfileImageBase64: {}", 
                      user.getProfileImageBase64() != null ? "present" : "null");
            
            // Try base64 (as per API spec)
            if (user.getProfileImageBase64() != null && !user.getProfileImageBase64().isEmpty()) {
                logger.info("Attempting to load image from base64 data");
                logger.debug("Base64 data length: {}", user.getProfileImageBase64().length());
                logger.debug("Base64 data starts with: {}", user.getProfileImageBase64().substring(0, Math.min(50, user.getProfileImageBase64().length())));
                
                try {
                    byte[] imageData = Base64.getDecoder().decode(user.getProfileImageBase64());
                    logger.debug("Decoded image data size: {} bytes", imageData.length);
                    
                    Image profileImage = new Image(new java.io.ByteArrayInputStream(imageData));
                    profileImageView.setImage(profileImage);
                    logger.info("Profile image loaded successfully from base64");
                    logger.debug("ImageView image set - Width: {}, Height: {}, Error: {}", 
                              profileImage.getWidth(), profileImage.getHeight(), profileImage.getException());
                } catch (Exception e) {
                    logger.error("Error loading profile image from base64: {}", e.getMessage(), e);
                    loadDefaultProfileImage(); // Fallback to default if base64 loading fails
                }
            } else {
                logger.info("No profile image data available, loading default image");
                loadDefaultProfileImage(); // Load default if no image data is set
            }

            // Show bank info only for non-buyer roles (courier, seller, admin)
            if (!"buyer".equals(user.getRole())) {
                if (user.getBankInfo() != null) {
                    bankNameField.setText(user.getBankInfo().getBankName() != null ? user.getBankInfo().getBankName() : "");
                    accountNumberField.setText(user.getBankInfo().getAccountNumber() != null ? user.getBankInfo().getAccountNumber() : "");
                } else {
                    bankNameField.setText("");
                    accountNumberField.setText("");
                }
                // Show bank info fields and labels
                bankNameField.setVisible(true);
                accountNumberField.setVisible(true);
                bankNameLabel.setVisible(true);
                accountNumberLabel.setVisible(true);
            } else {
                // Hide bank info fields and labels for buyers
                bankNameField.setVisible(false);
                accountNumberField.setVisible(false);
                bankNameLabel.setVisible(false);
                accountNumberLabel.setVisible(false);
            }
        }
    }

    private void loadDefaultProfileImage() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com/foodi/appFrontend/images/default_profile.jpg");
            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                profileImageView.setImage(defaultImage);
                logger.info("Default profile image loaded successfully");
                logger.debug("Default image - Width: {}, Height: {}, Error: {}", 
                          defaultImage.getWidth(), defaultImage.getHeight(), defaultImage.getException());
            } else {
                logger.error("Default image resource stream is null for default loading.");
            }
        } catch (Exception e) {
            logger.error("Exception loading default profile image for fallback: {}", e.getMessage(), e);
        }
    }

    @FXML
    private void handleChangePicture(ActionEvent event) {
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
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
                profileErrorMessageLabel.setText("New image selected. Click 'Save Changes' to upload.");
                editProfileButton.setVisible(false);
                saveButton.setVisible(true);
                cancelButton.setVisible(true);
                setFieldsEditable(true); // Allow other fields to be edited too
            } catch (IOException e) {
                profileErrorMessageLabel.setText("Error reading image file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        setFieldsEditable(true);
        editProfileButton.setVisible(false);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
        profileErrorMessageLabel.setText("You can now edit your profile.");
    }

    @FXML
    private void handleSaveChanges(ActionEvent event) {
        profileErrorMessageLabel.setText("Saving changes...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> profileErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                Map<String, Object> profileData = new HashMap<>();
                profileData.put("full_name", fullNameField.getText());
                profileData.put("phone", phoneField.getText());
                profileData.put("email", emailField.getText());
                profileData.put("address", addressField.getText());

                // Add bank info only for non-buyer roles
                if (currentUser != null && !"buyer".equals(currentUser.getRole())) {
                    Map<String, String> bankInfo = new HashMap<>();
                    if (bankNameField.getText() != null && !bankNameField.getText().trim().isEmpty()) {
                        bankInfo.put("bank_name", bankNameField.getText().trim());
                    }
                    if (accountNumberField.getText() != null && !accountNumberField.getText().trim().isEmpty()) {
                        bankInfo.put("account_number", accountNumberField.getText().trim());
                    }
                    if (!bankInfo.isEmpty()) {
                        profileData.put("bank_info", bankInfo);
                    }
                }

                if (base64ImageString != null) {
                    profileData.put("profileImageBase64", base64ImageString);
                }

                String jsonBody = JsonUtil.getObjectMapper().writeValueAsString(profileData);

                Optional<HttpResponse<String>> responseOpt = ApiClient.put("/auth/profile", jsonBody, token);

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            profileErrorMessageLabel.setText(rootNode.has("message") ? rootNode.get("message").asText() : "Profile updated successfully.");
                            setFieldsEditable(false);
                            saveButton.setVisible(false);
                            cancelButton.setVisible(false);
                            editProfileButton.setVisible(true);
                            base64ImageString = null;
                            loadUserProfile();
                        } else {
                            String errorMessage = rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred.";
                            profileErrorMessageLabel.setText("Error updating profile: " + errorMessage);
                        }
                    });
                } else {
                    Platform.runLater(() -> profileErrorMessageLabel.setText("Failed to connect to server to update profile."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    profileErrorMessageLabel.setText("An unexpected error occurred while saving profile: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleCancelEdit(ActionEvent event) {
        // Reset fields to original values
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
            phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            addressField.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
            
            // Reset bank info to original values only for non-buyer roles
            if (!"buyer".equals(currentUser.getRole())) {
                if (currentUser.getBankInfo() != null) {
                    bankNameField.setText(currentUser.getBankInfo().getBankName() != null ? currentUser.getBankInfo().getBankName() : "");
                    accountNumberField.setText(currentUser.getBankInfo().getAccountNumber() != null ? currentUser.getBankInfo().getAccountNumber() : "");
                } else {
                    bankNameField.setText("");
                    accountNumberField.setText("");
                }
            }
        }
        
        // Reset image if changed
        if (base64ImageString != null) {
            base64ImageString = null;
            loadUserProfile(); // Reload original image
        }
        
        setFieldsEditable(false);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        editProfileButton.setVisible(true);
        profileErrorMessageLabel.setText("Edit cancelled.");
    }

    public void shutdown() {
        executorService.shutdown();
    }
    
    // Load wallet balance using the new API endpoint
    private void loadWalletBalance(String userId) {
        logger.info("Loading wallet balance for user ID: {}", userId);
        
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    logger.warn("Authentication token missing when loading wallet balance");
                    Platform.runLater(() -> profileErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }
                
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
                                
                                String balanceText = balance.toPlainString();
                                logger.info("Wallet balance loaded: {}", balanceText);
                                walletBalanceField.setText(balanceText);
                                
                            } catch (Exception e) {
                                logger.error("Error parsing balance data", e);
                                profileErrorMessageLabel.setText("Error parsing balance data: " + e.getMessage());
                            }
                        } else {
                            String errorMessage = balanceRootNode.has("error") ? 
                                balanceRootNode.get("error").asText() : "An unknown error occurred.";
                            logger.error("Error loading wallet balance: {}", errorMessage);
                            profileErrorMessageLabel.setText("Error loading wallet balance: " + errorMessage);
                        }
                    });
                } else {
                    logger.error("No response received when loading wallet balance");
                    Platform.runLater(() -> {
                        profileErrorMessageLabel.setText("Failed to connect to server for wallet balance.");
                    });
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Exception occurred while loading wallet balance", e);
                Platform.runLater(() -> {
                    profileErrorMessageLabel.setText("An unexpected error occurred while loading wallet balance: " + e.getMessage());
                });
            }
        });
    }
}