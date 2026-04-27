package com.idea.intis.ui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * AppShell acts as the main layout container (Shell) for the application after login.
 * It manages the Sidebar navigation, Top bar, and the dynamic Content area.
 */
public class AppShell {

    public static void show(Stage stage, String username, String password) {

        // --- SECTION 1: SIDEBAR NAVIGATION ---
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(180);
        // Dark slate background for a professional "Night" theme
        sidebar.setStyle("-fx-background-color:#0f172a;");

        // Menu Items
        Button dashboardBtn = createMenuButton("Dashboard");
        Button employeesBtn = createMenuButton("Employees");
        Button tasksBtn = createMenuButton("Tasks");
        Button filesBtn = createMenuButton("Files");
        Button presenceBtn = createMenuButton("Presence");
        Button auditBtn = createMenuButton("Audit");

        // Spacer: This pushes the Logout button to the bottom of the sidebar
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createMenuButton("Logout");
        // Optional: Red highlight for logout
        logoutBtn.setStyle(logoutBtn.getStyle() + "-fx-border-color: #ef4444; -fx-border-width: 0 0 0 4;");

        sidebar.getChildren().addAll(
                dashboardBtn,
                employeesBtn,
                tasksBtn,
                filesBtn,
                presenceBtn,
                auditBtn,
                spacer, // Pushes everything below this point to the bottom
                logoutBtn
        );

        // --- SECTION 2: TOP HEADER BAR ---
        Label header = new Label("Welcome, " + username);
        header.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill: #1e293b;");
        header.setPadding(new Insets(15));

        VBox topBar = new VBox(header);
        topBar.setStyle("-fx-background-color:white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        // --- SECTION 3: DYNAMIC CONTENT AREA ---
        // This StackPane changes its children based on sidebar navigation
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color:#f8fafc;");
        content.setPadding(new Insets(20));
        content.getChildren().add(new Label("Dashboard Page Overview")); // Default view

        // --- SECTION 4: MAIN LAYOUT ASSEMBLY ---
        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setTop(topBar);
        root.setCenter(content);

        // --- SECTION 5: NAVIGATION LOGIC (EVENT HANDLERS) ---

        dashboardBtn.setOnAction(e ->
                content.getChildren().setAll(new Label("Dashboard Page Content")));

        employeesBtn.setOnAction(e ->
                content.getChildren().setAll(
                        EmployeeView.getView(username, password)
                ));

        tasksBtn.setOnAction(e ->
                content.getChildren().setAll(
                        TaskView.getView(username, password)
                ));

        filesBtn.setOnAction(e ->
                content.getChildren().setAll(new Label("Files Management Page")));

        presenceBtn.setOnAction(e ->
                content.getChildren().setAll(new Label("Employee Presence/Attendance")));

        auditBtn.setOnAction(e ->
                content.getChildren().setAll(new Label("System Audit Logs")));

        logoutBtn.setOnAction(e ->
                stage.close()); // Logic to close and potentially return to LoginView

        // --- SECTION 6: SCENE RENDER ---
        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("INTIS Management System");
        stage.setScene(scene);
    }

    /**
     * Helper method to create styled buttons for the sidebar to ensure consistency.
     */
    private static Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE); // Ensures button fills sidebar width
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setStyle(
                "-fx-background-color:#1e293b;" +
                        "-fx-text-fill:#cbd5e1;" +
                        "-fx-font-size:14px;" +
                        "-fx-padding:12;" +
                        "-fx-background-radius: 5;"
        );

        // Simple hover effect logic could be added here via setOnMouseEntered
        return btn;
    }
}