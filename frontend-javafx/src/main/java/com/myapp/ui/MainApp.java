package com.myapp.ui;

import com.myapp.ui.common.NavigationController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX Application for Food Ordering System
 * Entry point for the frontend application
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize navigation controller
            NavigationController navigationController = NavigationController.getInstance();
            navigationController.initialize(primaryStage);
            
            // Check authentication and redirect to appropriate scene
            navigationController.checkAuthenticationAndRedirect();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        // Cleanup when application is closing
        System.out.println("Food Ordering Application is closing...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
