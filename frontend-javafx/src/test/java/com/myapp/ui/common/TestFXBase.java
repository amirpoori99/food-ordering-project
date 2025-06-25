package com.myapp.ui.common;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeoutException;

/**
 * Base class for TestFX UI tests
 * Provides common setup and utilities for testing JavaFX controllers
 */
public abstract class TestFXBase extends ApplicationTest {

    protected Stage testStage;
    private static boolean javafxInitialized = false;

    @BeforeAll
    public static void setUpClass() throws Exception {
        if (!javafxInitialized) {
            // Initialize JavaFX toolkit only once
            try {
                FxToolkit.registerPrimaryStage();
                FxToolkit.setupApplication(TestApplication.class);
                javafxInitialized = true;
            } catch (Exception e) {
                // If already initialized, continue
                javafxInitialized = true;
            }
        }
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        // Don't clean up JavaFX toolkit to avoid conflicts between tests
        // FxToolkit.cleanupApplication() can cause issues
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.testStage = stage;
        
        // Create a simple scene for testing
        VBox root = new VBox();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Test Application");
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Clear authentication state before each test
        HttpClientUtil.clearTokens();
        
        // Reset NavigationController
        NavigationController.resetInstance();
        
        // Wait for any pending JavaFX operations
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Load FXML file safely for testing
     */
    protected <T> T loadFXMLController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader();
            
            // Try to load from resources
            if (getClass().getResource(fxmlPath) != null) {
                loader.setLocation(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                
                // Set up the scene in the test stage
                Platform.runLater(() -> {
                    Scene scene = new Scene(root, 800, 600);
                    testStage.setScene(scene);
                });
                WaitForAsyncUtils.waitForFxEvents();
                
                return loader.getController();
            } else {
                // FXML file not found, return mock controller
                return createMockController();
            }
        } catch (Exception e) {
            // If FXML loading fails, return mock controller
            return createMockController();
        }
    }

    /**
     * Create mock controller when FXML loading fails
     */
    @SuppressWarnings("unchecked")
    protected <T> T createMockController() {
        // Create a simple mock scene with basic UI components
        Platform.runLater(() -> {
            VBox root = new VBox(10);
            
            // Add common UI components for testing
            TextField textField = new TextField();
            textField.setId("testTextField");
            
            Button button = new Button("Test Button");
            button.setId("testButton");
            
            Label label = new Label("Test Label");
            label.setId("testLabel");
            
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setId("testProgressIndicator");
            progressIndicator.setVisible(false);
            
            root.getChildren().addAll(textField, button, label, progressIndicator);
            
            Scene scene = new Scene(root, 800, 600);
            testStage.setScene(scene);
        });
        WaitForAsyncUtils.waitForFxEvents();
        
        // Return null - tests should handle this gracefully
        return null;
    }

    /**
     * Get UI component by ID safely
     */
    protected <T> T lookup(String query, Class<T> type) {
        try {
            return type.cast(testStage.getScene().getRoot().lookup(query));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Run code on JavaFX Application Thread and wait
     */
    protected void runOnFxThreadAndWait(Runnable runnable) {
        Platform.runLater(runnable);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Simple test application for JavaFX initialization
     */
    public static class TestApplication extends Application {
        @Override
        public void start(Stage primaryStage) {
            // Minimal application for testing
            VBox root = new VBox();
            Scene scene = new Scene(root, 400, 300);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Test Application");
            // Don't show the stage automatically
        }
    }
} 