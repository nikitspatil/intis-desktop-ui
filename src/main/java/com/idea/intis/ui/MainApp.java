package com.idea.intis.ui;

import com.idea.intis.ui.view.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

/*
 Main entry point of JavaFX application.

 Responsibility:
 - Launch JavaFX runtime
 - Open first screen (Login page)
*/
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        // Call Login screen and pass main stage(window)
        LoginView.show(stage);

        // Window title shown on top bar
        stage.setTitle("INTIS Management System");

        // Display window
        stage.show();
    }

    public static void main(String[] args) {

        // Starts JavaFX application lifecycle
        launch();
    }
}