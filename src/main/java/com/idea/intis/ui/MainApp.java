package com.idea.intis.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApp extends Application {

    // Modern CSS Styles
    private final String GLASS_BACKGROUND = "-fx-background-color: linear-gradient(to bottom right, #1a1a2e, #16213e);";
    private final String TEXT_STYLE = "-fx-text-fill: white; -fx-font-family: 'Segoe UI', Helvetica, Arial, sans-serif;";

    private final String GLASS_BUTTON =
            "-fx-background-color: rgba(255, 255, 255, 0.1);" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: rgba(255, 255, 255, 0.3);" +
                    "-fx-border-radius: 8;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;" +
                    "-fx-transition: all 0.3s ease-in-out;";

    private final String HOVER_BUTTON =
            "-fx-background-color: rgba(255, 255, 255, 0.2);" +
                    "-fx-border-color: rgba(255, 255, 255, 0.6);";

    private final String INPUT_STYLE =
            "-fx-background-color: rgba(0, 0, 0, 0.3);" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: #aaaaaa;" +
                    "-fx-background-radius: 5;" +
                    "-fx-padding: 10;";

    @Override
    public void start(Stage stage) {

        Label title = new Label("INTIS LOGIN");
        title.setStyle(TEXT_STYLE + "-fx-font-size: 26px; -fx-font-weight: bold; -fx-letter-spacing: 2px;");

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setStyle(INPUT_STYLE);

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setStyle(INPUT_STYLE);

        Button loginBtn = new Button("LOGIN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(GLASS_BUTTON);

        // Hover effects for the glassy button
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(GLASS_BUTTON + HOVER_BUTTON));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(GLASS_BUTTON));

        Label status = new Label();
        status.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");

        loginBtn.setOnAction(e -> {
            try {
                String user = username.getText();
                String pass = password.getText();

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                String auth = java.util.Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());

                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8080/api/auth/me"))
                        .header("Authorization", "Basic " + auth)
                        .GET()
                        .build();

                java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String dashboardText = "Loading...";

                    try {
                        java.net.http.HttpRequest dashRequest = java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://localhost:8080/api/dashboard/summary"))
                                .header("Authorization", "Basic " + auth)
                                .GET()
                                .build();

                        java.net.http.HttpResponse<String> dashResponse = client.send(dashRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
                        if (dashResponse.statusCode() == 200) dashboardText = dashResponse.body();
                    } catch (Exception ex) {
                        dashboardText = "Dashboard API error";
                    }

                    // --- Dashboard UI Transformation ---
                    Label welcome = new Label("INTIS Dashboard");
                    welcome.setStyle(TEXT_STYLE + "-fx-font-size: 28px; -fx-font-weight: bold;");

                    Label total = new Label("Total Employees: Loading...");
                    Label active = new Label("Active Employees: Loading...");

                    if (dashboardText.contains("totalEmployees")) total.setText("Total Employees: Data Loaded");
                    if (dashboardText.contains("activeEmployees")) active.setText("Active Employees: Data Loaded");

                    total.setStyle(TEXT_STYLE + "-fx-font-size: 16px; -fx-opacity: 0.9;");
                    active.setStyle(TEXT_STYLE + "-fx-font-size: 16px; -fx-opacity: 0.9;");

                    // Styled Buttons for Dashboard
                    Button employeesBtn = createModernButton("Employees");
                    employeesBtn.setOnAction(event -> com.idea.intis.ui.view.EmployeeView.show(user, pass));

                    Button tasksBtn = createModernButton("Tasks");
                    Button filesBtn = createModernButton("Files");
                    Button logoutBtn = createModernButton("Logout");
                    logoutBtn.setStyle(GLASS_BUTTON + "-fx-border-color: rgba(255, 100, 100, 0.5);");

                    VBox dashboard = new VBox(20, welcome, total, active, employeesBtn, tasksBtn, filesBtn, logoutBtn);
                    dashboard.setPadding(new Insets(40));
                    dashboard.setAlignment(Pos.CENTER);
                    dashboard.setStyle(GLASS_BACKGROUND);

                    Scene dashboardScene = new Scene(dashboard, 550, 500);
                    stage.setScene(dashboardScene);

                } else {
                    status.setText("Invalid credentials");
                }
            } catch (Exception ex) {
                status.setText("Server not reachable");
            }
        });

        VBox root = new VBox(20, title, username, password, loginBtn, status);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle(GLASS_BACKGROUND);

        Scene scene = new Scene(root, 420, 400);
        stage.setTitle("INTIS System");
        stage.setScene(scene);
        stage.show();
    }

    // Helper to keep dashboard button creation clean
    private Button createModernButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(250);
        btn.setStyle(GLASS_BUTTON);
        btn.setOnMouseEntered(e -> btn.setStyle(GLASS_BUTTON + HOVER_BUTTON));
        btn.setOnMouseExited(e -> btn.setStyle(GLASS_BUTTON));
        return btn;
    }

    public static void main(String[] args) {
        launch();
    }
}