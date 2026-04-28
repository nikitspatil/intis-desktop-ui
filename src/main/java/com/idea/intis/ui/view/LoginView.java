package com.idea.intis.ui.view;

import com.idea.intis.ui.config.ApiConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*
 LoginView.java

 Responsibility:
 - Show login page UI
 - Accept username/password
 - Call backend authentication API
 - If success -> open AppShell
 - If fail -> show message
*/
public class LoginView {

    /*
     Static method so we can call:

     LoginView.show(stage);

     stage = main application window
    */
    public static void show(Stage stage) {

        /* ------------------------------
           Page Title
           ------------------------------ */
        Label title = new Label("INTIS Login");
        title.setStyle("-fx-font-size:24px; -fx-font-weight:bold;");

        /* Small subtitle under title */
        Label subtitle = new Label("Office Management System");
        subtitle.setStyle("-fx-text-fill:gray;");

        /* ------------------------------
           Username Input Box
           ------------------------------ */
        TextField usernameField = new TextField();

        // Placeholder text inside input
        usernameField.setPromptText("Username");

        // Width of field
        usernameField.setMaxWidth(260);

        /* ------------------------------
           Password Input Box
           ------------------------------ */
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(260);

        /* ------------------------------
           Login Button
           ------------------------------ */
        Button loginBtn = new Button("Login");

        loginBtn.setPrefWidth(260);

        // Basic styling
        loginBtn.setStyle(
                "-fx-background-color:#2563eb;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-weight:bold;"
        );

        /* Label used for success/error message */
        Label status = new Label();

        /* -------------------------------------------------
           What happens when user clicks Login button
           ------------------------------------------------- */
        loginBtn.setOnAction(e -> {

            // Read text entered by user
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();

            // If empty fields, stop login
            if (user.isEmpty() || pass.isEmpty()) {
                status.setText("Enter username and password");
                return;
            }

            status.setText("Checking login...");

            try {
                /* -----------------------------------------
                   Create HTTP client
                   Used to call backend API
                   ----------------------------------------- */
                var client = java.net.http.HttpClient.newHttpClient();

                /* -----------------------------------------
                   Convert username:password to Base64
                   Needed for Basic Authentication
                   ----------------------------------------- */
                String auth = java.util.Base64.getEncoder()
                        .encodeToString((user + ":" + pass).getBytes());

                /* -----------------------------------------
                   Build GET request:
                   http://localhost:8080/api/auth/me
                   ----------------------------------------- */
                var request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(ApiConfig.BASE_URL +
                                "/api/auth/me"))
                        .header("Authorization", "Basic " + auth)
                        .GET()
                        .build();

                /* Send request and wait for response */
                var response = client.send(
                        request,
                        java.net.http.HttpResponse.BodyHandlers.ofString()
                );

                /* -----------------------------------------
                   If status = 200
                   Login success
                   ----------------------------------------- */
                if (response.statusCode() == 200) {

                    // Open main application shell
                    AppShell.show(stage, user, pass);

                }
                /* Unauthorized */
                else if (response.statusCode() == 401) {

                    status.setText("Invalid username or password");

                }
                /* Other response */
                else {

                    status.setText("Login failed");
                }

            } catch (Exception ex) {

                // Backend not running / network issue
                status.setText("Server not reachable");
            }
        });

        /* -----------------------------------------
           Vertical layout for login controls
           ----------------------------------------- */
        VBox root = new VBox(
                12,
                title,
                subtitle,
                usernameField,
                passwordField,
                loginBtn,
                status
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        // Background color
        root.setStyle("-fx-background-color:#f8fafc;");

        /* Create scene (page size) */
        Scene scene = new Scene(root, 430, 340);

        /* Load page into main window */
        stage.setScene(scene);
    }
}