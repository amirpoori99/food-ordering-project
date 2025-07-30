module com.foodapp.fooody {
    // Requires statements for JavaFX modules
    requires javafx.web;
    requires javafx.swing;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Requires statements for Jackson (JSON processing)
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.annotation;

    // Requires statements for other UI libraries
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.jfoenix;
    requires java.net.http;
    requires MaterialFX;
    requires org.slf4j;

    // Export the main package so LauncherImpl can access FooodyApp
    exports com.foodi.appFrontend;

    // Exports for controllers (needed for instantiation)
    exports com.foodi.appFrontend.tabs.dashbord to javafx.fxml;

    // Opens statements for FXML controllers and models for Jackson
    // This allows JavaFX's FXMLLoader to access FXML files within these packages
    opens com.foodi.appFrontend.tabs.login to javafx.fxml;
    opens com.foodi.appFrontend.tabs.signup to javafx.fxml;
    opens com.foodi.appFrontend.tabs.dashbord to javafx.fxml, javafx.base;
    opens com.foodi.appFrontend.models to com.fasterxml.jackson.databind, javafx.base;
    opens com.foodi.appFrontend.utils to com.fasterxml.jackson.databind;
    opens com.foodi.appFrontend.css to javafx.fxml;
    opens com.foodi.appFrontend.images to javafx.fxml;

    // FIX: ADD THIS LINE to open the 'view.dashbord' package for FXML includes
    opens com.foodi.appFrontend.view.dashbord to javafx.fxml;
}