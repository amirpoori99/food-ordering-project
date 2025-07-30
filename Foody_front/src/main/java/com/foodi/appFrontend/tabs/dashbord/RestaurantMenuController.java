package com.foodi.appFrontend.tabs.dashbord;

import com.fasterxml.jackson.databind.JsonNode;
import com.foodi.appFrontend.models.FoodItem; // مدل FoodItem
import com.foodi.appFrontend.models.Restaurant; // مدل Restaurant
import com.foodi.appFrontend.models.ItemRating; // برای ratings
import com.foodi.appFrontend.utils.ApiClient; // برای فراخوانی API
import com.foodi.appFrontend.utils.AuthManager; // برای دریافت توکن
import com.foodi.appFrontend.utils.JsonUtil; // برای پردازش JSON
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog; // NEW: برای گرفتن ورودی تعداد
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Node; // NEW: برای گرفتن Window
import javafx.fxml.FXMLLoader; // NEW: برای باز کردن فرم سبد خرید

import java.io.IOException;
import java.io.InputStream; //
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Base64; // NEW: برای Base64
import java.io.ByteArrayInputStream; // NEW: برای Base64 به Image
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.geometry.Insets; // NEW: برای Insets در VBox
import javafx.scene.layout.VBox; // NEW: برای محتوای Tab
import javafx.scene.layout.HBox; // NEW: برای menu categories container
import javafx.stage.Modality; // NEW: برای مودال بودن پنجره سبد خرید


import javafx.scene.Parent;
import javafx.scene.Scene;
import com.foodi.appFrontend.models.Order; // NEW: برای مدل Order
import com.foodi.appFrontend.tabs.dashbord.OrderDetailsController; // NEW: برای کنترلر OrderDetailsView
import com.foodi.appFrontend.tabs.dashbord.FoodItemDetailsController; // NEW: برای کنترلر FoodItemDetailsView


public class RestaurantMenuController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantMenuController.class);

    @FXML
    private ImageView restaurantLogoImageView;
    @FXML
    private Label restaurantNameLabel;
    @FXML
    private Label restaurantAddressLabel;
    @FXML
    private Label restaurantPhoneLabel;
    @FXML
    private TabPane menuTabPane; // برای تب‌های منو (Breakfast, Lunch)
    @FXML
    private Label menuErrorMessageLabel;
    @FXML
    private HBox menuCategoriesContainer;

    private Restaurant currentRestaurant; // رستوران فعلی که منوی آن را مشاهده می‌کنیم
    private Map<FoodItem, Integer> cart = new HashMap<>(); // سبد خرید: FoodItem -> Quantity
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        // تنظیمات اولیه (در حال حاضر نیازی به کد اضافی نیست زیرا setup در setRestaurant انجام می‌شود)
    }

    // متد Setter برای دریافت شیء Restaurant از BuyerDashboard
    public void setRestaurant(Restaurant restaurant) {
        this.currentRestaurant = restaurant;
        if (currentRestaurant != null) {
            restaurantNameLabel.setText(currentRestaurant.getName());
            restaurantAddressLabel.setText(currentRestaurant.getAddress());
            restaurantPhoneLabel.setText(currentRestaurant.getPhone());

            // بارگذاری تصویر لوگو (اگر URL در مدل Restaurant باشد)
            if (currentRestaurant.getLogoBase64() != null && !currentRestaurant.getLogoBase64().isEmpty()) {
                // اگر لوگو به صورت Base64 در مدل Restaurant باشد
                try {
                    byte[] decodedImg = Base64.getDecoder().decode(currentRestaurant.getLogoBase64());
                    Image logoImage = new Image(new ByteArrayInputStream(decodedImg));
                    restaurantLogoImageView.setImage(logoImage);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error decoding Base64 logo: " + e.getMessage());
                    loadDefaultLogo(); // بارگذاری لوگوی پیش فرض در صورت خطا
                }
            } else {
                // بارگذاری لوگوی پیش فرض
                loadDefaultLogo();
            }

            // فراخوانی متد برای بارگذاری منو پس از تنظیم رستوران
            loadRestaurantMenu();
        }
    }

    private void loadDefaultLogo() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/com/foodapp/fooody/images/default_restaurant_logo.png");
            if (imageStream != null) {
                Image defaultLogo = new Image(imageStream);
                if (!defaultLogo.isError()) {
                    restaurantLogoImageView.setImage(defaultLogo);
                } else {
                    System.err.println("Error loading default logo from stream: " + defaultLogo.getException().getMessage());
                }
            } else {
                System.err.println("Default restaurant logo resource stream is null.");
            }
        } catch (Exception e) {
            System.err.println("Exception loading default logo: " + e.getMessage());
        }
    }

    private void loadRestaurantMenu() {
        menuErrorMessageLabel.setText("Loading menu...");
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    Platform.runLater(() -> menuErrorMessageLabel.setText("Authentication token missing. Please log in again."));
                    return;
                }

                // API call to get vendor details and menu items
                Optional<HttpResponse<String>> responseOpt = ApiClient.get("/vendors/" + currentRestaurant.getId(), token); //

                if (responseOpt.isPresent()) {
                    HttpResponse<String> response = responseOpt.get();
                    JsonNode rootNode = JsonUtil.getObjectMapper().readTree(response.body());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                // Clear existing tabs and menu categories before adding new ones
                                menuTabPane.getTabs().clear();
                                menuCategoriesContainer.getChildren().clear();
                                
                                JsonNode menuTitleNode = rootNode.get("menu_titles");
                                JsonNode menuTitleObject = rootNode.get("menu_title"); // ← Add this line
                                if (menuTitleNode != null && menuTitleNode.isArray()) {
                                    for (JsonNode titleNode : menuTitleNode) {
                                        String menuTitle = titleNode.asText();
                                        // Create a Tab for each menu title
                                        Tab menuTab = new Tab(menuTitle);
                                        VBox tabContent = new VBox(10); // Content inside the tab
                                        tabContent.setPadding(new Insets(10));

                                        TableView<FoodItem> foodItemTable = new TableView<>();
                                        // Configure TableColumns dynamically for each tab's table
                                        TableColumn<FoodItem, Integer> idCol = new TableColumn<>("ID");
                                        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
                                        TableColumn<FoodItem, String> nameCol = new TableColumn<>("Name");
                                        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
                                        TableColumn<FoodItem, String> descCol = new TableColumn<>("Description");
                                        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
                                        TableColumn<FoodItem, Integer> priceCol = new TableColumn<>("Price");
                                        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
                                        TableColumn<FoodItem, Integer> supplyCol = new TableColumn<>("Supply");
                                        supplyCol.setCellValueFactory(new PropertyValueFactory<>("supply"));
                                        TableColumn<FoodItem, String> keywordsCol = new TableColumn<>("Keywords");
                                        keywordsCol.setCellValueFactory(cellData -> {
                                            FoodItem item = cellData.getValue();
                                            if (item.getKeywords() != null && item.getKeywords().length > 0) {
                                                return new javafx.beans.property.SimpleStringProperty(String.join(", ", item.getKeywords()));
                                            } else {
                                                return new javafx.beans.property.SimpleStringProperty("");
                                            }
                                        });

                                        TableColumn<FoodItem, String> ratingCol = new TableColumn<>("Rating");
                                        ratingCol.setCellValueFactory(cellData -> {
                                            FoodItem item = cellData.getValue();
                                            if (item != null && item.getRating() != null) {
                                                return new javafx.beans.property.SimpleStringProperty(String.format("%.1f ⭐", item.getRating()));
                                            } else {
                                                return new javafx.beans.property.SimpleStringProperty("No ratings");
                                            }
                                        });

                                        foodItemTable.getColumns().addAll(idCol, nameCol, descCol, priceCol, supplyCol, keywordsCol, ratingCol);
                                        foodItemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                                        // Populate the table with items for this specific menu
                                        JsonNode itemsUnderTitle = menuTitleObject.get(menuTitle); // ← Change this line
                                        if (itemsUnderTitle != null && itemsUnderTitle.isArray()) {
                                            List<FoodItem> items = JsonUtil.getObjectMapper().readerForListOf(FoodItem.class).readValue(itemsUnderTitle);
                                            // No need for HashSet here if items within a single menu are unique by API
                                            // and we are showing them menu-by-menu.
                                            ObservableList<FoodItem> foodItemObservableList = FXCollections.observableArrayList(items);
                                            foodItemTable.setItems(foodItemObservableList);
                                            
                                            // Load ratings for the food items
                                            loadFoodItemRatings(items, foodItemTable);
                                        }

                                        tabContent.getChildren().add(foodItemTable); // Add table to tab content
                                        menuTab.setContent(tabContent); // Set VBox as tab content
                                        menuTabPane.getTabs().add(menuTab); // Add tab to TabPane
                                        
                                        // Create clickable menu title button
                                        io.github.palexdev.materialfx.controls.MFXButton menuTitleButton = new io.github.palexdev.materialfx.controls.MFXButton(menuTitle);
                                        menuTitleButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #374151; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 500; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
                                        
                                        // Store reference to the tab for button click
                                        Tab finalMenuTab = menuTab;
                                        menuTitleButton.setOnAction(event -> {
                                            // Select the corresponding tab when button is clicked
                                            menuTabPane.getSelectionModel().select(finalMenuTab);
                                            
                                            // Update button styles - make clicked button active
                                            updateMenuButtonStyles(menuTitleButton);
                                        });
                                        
                                        menuCategoriesContainer.getChildren().add(menuTitleButton);
                                    }
                                    menuErrorMessageLabel.setText("Menu loaded successfully.");
                                }else{
                                    menuErrorMessageLabel.setText("Error fetching menu: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred."));
                                }

                            }catch(IOException e){
                                menuErrorMessageLabel.setText("Error parsing menu data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }else{
                            menuErrorMessageLabel.setText("Error fetching menu: " + (rootNode.has("error") ? rootNode.get("error").asText() : "An unknown error occurred."));
                        }
                    });
                } else {
                    Platform.runLater(() -> menuErrorMessageLabel.setText("Failed to connect to server to load menu."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    menuErrorMessageLabel.setText("An unexpected error occurred while fetching menu: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        // 1. Get the selected FoodItem from the currently active TableView in menuTabPane.
        Tab selectedTab = menuTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            menuErrorMessageLabel.setText("Please select a menu tab first.");
            return;
        }

        // Get the TableView from the selected tab's content.
        // Assumes the TableView is the first (and only) child of the VBox tab content.
        TableView<FoodItem> activeFoodItemTable = null;
        if (selectedTab.getContent() instanceof VBox) {
            VBox tabContent = (VBox) selectedTab.getContent();
            if (!tabContent.getChildren().isEmpty() && tabContent.getChildren().get(0) instanceof TableView) {
                activeFoodItemTable = (TableView<FoodItem>) tabContent.getChildren().get(0);
            }
        }

        if (activeFoodItemTable == null) {
            menuErrorMessageLabel.setText("Could not find food items table in the selected tab.");
            return;
        }

        FoodItem selectedFoodItem = activeFoodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            menuErrorMessageLabel.setText("Please select a food item to add to cart.");
            return;
        }

        // 2. Getting quantity (e.g., via a dialog)
        TextInputDialog quantityDialog = new TextInputDialog("1");
        quantityDialog.setTitle("Add to Cart");
        quantityDialog.setHeaderText("Enter quantity for " + selectedFoodItem.getName() + ":");
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
                    menuErrorMessageLabel.setText("Quantity must be a positive number.");
                    return;
                }
                
                // Check if quantity exceeds available supply
                if (quantity > selectedFoodItem.getSupply()) {
                    menuErrorMessageLabel.setText("Quantity exceeds available supply. Available: " + selectedFoodItem.getSupply());
                    return;
                }

                // 3. Adding to the 'cart' Map (FoodItem -> Quantity).
                // If item already in cart, update quantity. Otherwise, add new.
                addItemToCart(selectedFoodItem, quantity);

            } catch (NumberFormatException e) {
                menuErrorMessageLabel.setText("Invalid quantity. Please enter a valid number.");
            }
        } else {
            menuErrorMessageLabel.setText("Adding to cart cancelled.");
        }
    }

    /**
     * Add a food item to the cart with the specified quantity
     * @param foodItem The food item to add
     * @param quantity The quantity to add
     */
    public void addItemToCart(FoodItem foodItem, int quantity) {
        if (foodItem == null) {
            menuErrorMessageLabel.setText("No food item provided for adding to cart.");
            return;
        }

        if (quantity <= 0) {
            menuErrorMessageLabel.setText("Quantity must be a positive number.");
            return;
        }

        // Check if quantity exceeds available supply
        if (quantity > foodItem.getSupply()) {
            menuErrorMessageLabel.setText("Quantity exceeds available supply. Available: " + foodItem.getSupply());
            return;
        }

        // Adding to the 'cart' Map (FoodItem -> Quantity).
        // If item already in cart, update quantity. Otherwise, add new.
        cart.put(foodItem, cart.getOrDefault(foodItem, 0) + quantity);

        menuErrorMessageLabel.setText(foodItem.getName() + " x" + quantity + " added to cart. Total items in cart: " + cart.values().stream().mapToInt(Integer::intValue).sum());
    }

    @FXML
    private void handleViewCartAndOrder(ActionEvent event) {
        // Logic to view cart details and finalize order
        logger.info("handleViewCartAndOrder called, cart size: {}", cart.size());
        if (cart.isEmpty()) {
            menuErrorMessageLabel.setText("Your cart is empty. Please add some items first.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/CartView.fxml"));
            Parent cartView = loader.load();

            CartController controller = loader.getController();
            logger.info("CartController loaded: {}", controller != null);
            controller.setCart(cart); // Pass the cart to the CartController
            controller.setCurrentRestaurantId(currentRestaurant.getId().toString()); // Pass the restaurant ID for order submission

            // Set a callback to clear cart if order is successful
            controller.setClearCartCallback(aVoid -> {
                cart.clear(); // Clear the cart in this controller
                menuErrorMessageLabel.setText("Order placed successfully. Cart cleared.");
            });

            // Set a callback to open order details in a new tab
            controller.setOpenOrderDetailsCallback(order -> {
                openOrderDetailsTab(order);
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Your Cart");
            Scene scene = new Scene(cartView);
            // Assuming application.css is general
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait(); // Show the dialog and wait for it to be closed


        } catch (IOException e) {
            menuErrorMessageLabel.setText("Error opening cart view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void openOrderDetailsTab(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/OrderDetailsView.fxml"));
            Parent orderDetailsView = loader.load();
            
            OrderDetailsController controller = loader.getController();
            controller.setOrder(order);
            
            Stage stage = new Stage();
            stage.setTitle("Order Details - Order #" + order.getId());
            Scene scene = new Scene(orderDetailsView);
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            menuErrorMessageLabel.setText("Error opening order details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewItemDetails(ActionEvent event) {
        // 1. Get the selected FoodItem from the currently active TableView in menuTabPane.
        Tab selectedTab = menuTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            menuErrorMessageLabel.setText("Please select a menu tab first.");
            return;
        }

        // Get the TableView from the selected tab's content.
        // Assumes the TableView is the first (and only) child of the VBox tab content.
        TableView<FoodItem> activeFoodItemTable = null;
        if (selectedTab.getContent() instanceof VBox) {
            VBox tabContent = (VBox) selectedTab.getContent();
            if (!tabContent.getChildren().isEmpty() && tabContent.getChildren().get(0) instanceof TableView) {
                activeFoodItemTable = (TableView<FoodItem>) tabContent.getChildren().get(0);
            }
        }

        if (activeFoodItemTable == null) {
            menuErrorMessageLabel.setText("Could not find food items table in the selected tab.");
            return;
        }

        FoodItem selectedFoodItem = activeFoodItemTable.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            menuErrorMessageLabel.setText("Please select a food item to view details.");
            return;
        }

        // Open food item details view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/foodi/appFrontend/view/dashbord/FoodItemDetailsView.fxml"));
            Parent foodItemDetailsView = loader.load();

            FoodItemDetailsController controller = loader.getController();
            controller.setFoodItem(selectedFoodItem);
            controller.setRestaurant(currentRestaurant);
            controller.setRestaurantMenuController(this); // Pass reference to this controller

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Food Item Details - " + selectedFoodItem.getName());
            Scene scene = new Scene(foodItemDetailsView);
            scene.getStylesheets().add(getClass().getResource("/com/foodi/appFrontend/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            menuErrorMessageLabel.setText("Error opening food item details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        // Logic to close the menu view Stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        executorService.shutdown(); // Shutdown executor when form is closed
    }
    
    private void updateMenuButtonStyles(io.github.palexdev.materialfx.controls.MFXButton activeButton) {
        // Reset all buttons to inactive style
        for (javafx.scene.Node node : menuCategoriesContainer.getChildren()) {
            if (node instanceof io.github.palexdev.materialfx.controls.MFXButton) {
                io.github.palexdev.materialfx.controls.MFXButton button = (io.github.palexdev.materialfx.controls.MFXButton) node;
                button.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #374151; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 500; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
            }
        }
        
        // Set active button style
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: white; -fx-text-fill: #374151; -fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-border-color: #e2e8f0; -fx-border-width: 1 1 0 1; -fx-background-radius: 8 8 0 0; -fx-border-radius: 8 8 0 0; -fx-cursor: hand;");
        }
    }

    private void loadFoodItemRatings(List<FoodItem> foodItems, TableView<FoodItem> foodItemTable) {
        executorService.submit(() -> {
            try {
                String token = AuthManager.getJwtToken();
                if (token == null || token.isEmpty()) {
                    logger.warn("Authentication token missing for rating load");
                    return;
                }

                for (FoodItem foodItem : foodItems) {
                    try {
                        String ratingUrl = "/ratings/items/" + foodItem.getId();
                        Optional<HttpResponse<String>> ratingResponseOpt = ApiClient.get(ratingUrl, token);
                        
                        if (ratingResponseOpt.isPresent()) {
                            HttpResponse<String> ratingResponse = ratingResponseOpt.get();
                            
                            if (ratingResponse.statusCode() == 200) {
                                try {
                                    JsonNode ratingRootNode = JsonUtil.getObjectMapper().readTree(ratingResponse.body());
                                    ItemRating itemRating = JsonUtil.getObjectMapper().treeToValue(ratingRootNode, ItemRating.class);
                                    
                                    if (itemRating != null && itemRating.getAvgRating() != null) {
                                        Platform.runLater(() -> {
                                            foodItem.setRating(itemRating.getAvgRating());
                                            if (foodItemTable != null) {
                                                foodItemTable.refresh();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    logger.error("Error parsing rating response for food item {}: {}", foodItem.getId(), e.getMessage());
                                    Platform.runLater(() -> {
                                        foodItem.setRating(null);
                                        if (foodItemTable != null) {
                                            foodItemTable.refresh();
                                        }
                                    });
                                }
                            } else if (ratingResponse.statusCode() == 400 || ratingResponse.statusCode() == 404) {
                                Platform.runLater(() -> {
                                    foodItem.setRating(null);
                                    if (foodItemTable != null) {
                                        foodItemTable.refresh();
                                    }
                                });
                            } else {
                                logger.warn("Failed to load rating for food item {}: {}", foodItem.getId(), ratingResponse.statusCode());
                            }
                        } else {
                            logger.warn("No response received for food item rating {}", foodItem.getId());
                        }
                    } catch (Exception e) {
                        logger.error("Error loading rating for food item {}", foodItem.getId(), e);
                        Platform.runLater(() -> {
                            foodItem.setRating(null);
                            if (foodItemTable != null) {
                                foodItemTable.refresh();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                logger.error("Error in loadFoodItemRatings", e);
            }
        });
    }
}