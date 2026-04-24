package com.idea.intis.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EmployeeView {

    // Style Constants matching MainApp
    private static final String GLASS_BACKGROUND = "-fx-background-color: linear-gradient(to bottom right, #1a1a2e, #16213e);";
    private static final String TEXT_STYLE = "-fx-text-fill: white; -fx-font-family: 'Segoe UI';";
    private static final String INPUT_STYLE = "-fx-background-color: rgba(0,0,0,0.3); -fx-text-fill: white; -fx-prompt-text-fill: #aaa; -fx-background-radius: 5;";
    private static final String GLASS_BUTTON = "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;";
    private static final String TABLE_STYLE =
            ".table-view { -fx-background-color: transparent; }" +
                    ".table-view .column-header-background { -fx-background-color: rgba(255,255,255,0.05); }" +
                    ".table-view .column-header { -fx-background-color: transparent; }" +
                    ".table-row-cell { -fx-background-color: transparent; -fx-text-background-color: white; -fx-border-color: rgba(255,255,255,0.05); }" +
                    ".table-row-cell:selected { -fx-background-color: rgba(255,255,255,0.1); }";

    public static void show(String user, String pass) {

        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));

        TableColumn<String[], String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));

        TableColumn<String[], String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

        TableColumn<String[], String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));

        table.getColumns().addAll(idCol, codeCol, nameCol, deptCol);
        loadEmployees(table, user, pass);

        // UI Components
        Label viewTitle = new Label("EMPLOYEE MANAGEMENT");
        viewTitle.setStyle(TEXT_STYLE + "-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search Name / Code...");
        searchField.setStyle(INPUT_STYLE + "-fx-pref-height: 35px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = createStyledButton("Search");
        Button resetBtn = createStyledButton("Reset");
        Button addBtn = createStyledButton("Add New Employee");
        Button editBtn = createStyledButton("Edit Selected");

        // Toolbar for search
        HBox searchBar = new HBox(10, searchField, searchBtn, resetBtn);
        searchBar.setAlignment(Pos.CENTER);

        // Action Handlers
        editBtn.setOnAction(e -> showEditPopup(table, user, pass));
        searchBtn.setOnAction(e -> searchEmployees(table, user, pass, searchField.getText()));
        resetBtn.setOnAction(e -> {
            searchField.clear();
            table.getItems().clear();
            loadEmployees(table, user, pass);
        });
        addBtn.setOnAction(e -> showAddPopup(user, pass, table));

        // Sidebar for actions
        VBox sidebar = new VBox(15, viewTitle, new Separator(), addBtn, editBtn);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: rgba(255,255,255,0.03);");

        // Main Content Area
        VBox mainContent = new VBox(15, searchBar, table);
        mainContent.setPadding(new Insets(20));
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        HBox layout = new HBox(sidebar, mainContent);
        layout.setStyle(GLASS_BACKGROUND);

        Stage stage = new Stage();
        stage.setTitle("INTIS - Employee Records");
        Scene scene = new Scene(layout, 950, 550);

        // Injecting table CSS directly
        scene.getStylesheets().add("data:text/css," + TABLE_STYLE.replace(" ", "%20"));

        stage.setScene(scene);
        stage.show();
    }

    private static void showEditPopup(TableView<String[]> table, String user, String pass) {
        String[] selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Stage popup = new Stage();
        VBox root = createBasePopup(popup, "Edit Employee");

        TextField code = createPopupField(selected[1], "Employee Code");
        TextField name = createPopupField(selected[2], "Full Name");
        TextField dept = createPopupField(selected[3], "Department");

        Button save = createStyledButton("Update Record");
        Label status = new Label();
        status.setStyle(TEXT_STYLE);

        save.setOnAction(e -> {
            try {
                var client = java.net.http.HttpClient.newHttpClient();
                String auth = java.util.Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
                String json = String.format("{\"employeeCode\":\"%s\",\"fullName\":\"%s\",\"department\":\"%s\"}",
                        code.getText(), name.getText(), dept.getText());

                var request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8080/api/employees/" + selected[0]))
                        .header("Authorization", "Basic " + auth)
                        .header("Content-Type", "application/json")
                        .PUT(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();

                var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    table.getItems().clear();
                    loadEmployees(table, user, pass);
                    popup.close();
                } else {
                    status.setText("Update failed");
                }
            } catch (Exception ex) { status.setText("Connection Error"); }
        });

        root.getChildren().addAll(new Label("Editing ID: " + selected[0]), code, name, dept, save, status);
        popup.show();
    }

    private static void showAddPopup(String user, String pass, TableView<String[]> table) {
        Stage popup = new Stage();
        VBox root = createBasePopup(popup, "New Employee");

        TextField code = createPopupField("", "Employee Code");
        TextField name = createPopupField("", "Full Name");
        TextField dept = createPopupField("", "Department");

        Button save = createStyledButton("Save Employee");
        Label status = new Label();
        status.setStyle(TEXT_STYLE);

        save.setOnAction(e -> {
            try {
                var client = java.net.http.HttpClient.newHttpClient();
                String auth = java.util.Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
                String json = String.format("{\"employeeCode\":\"%s\",\"fullName\":\"%s\",\"department\":\"%s\"}",
                        code.getText(), name.getText(), dept.getText());

                var request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8080/api/employees"))
                        .header("Authorization", "Basic " + auth)
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();

                var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    table.getItems().clear();
                    loadEmployees(table, user, pass);
                    popup.close();
                } else { status.setText("Save failed"); }
            } catch (Exception ex) { status.setText("Error"); }
        });

        root.getChildren().addAll(code, name, dept, save, status);
        popup.show();
    }

    // --- Modern Helper Methods ---

    private static Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(GLASS_BUTTON);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPadding(new Insets(8, 15, 8, 15));
        btn.setOnMouseEntered(e -> btn.setStyle(GLASS_BUTTON + "-fx-background-color: rgba(255,255,255,0.2);"));
        btn.setOnMouseExited(e -> btn.setStyle(GLASS_BUTTON));
        return btn;
    }

    private static TextField createPopupField(String text, String prompt) {
        TextField tf = new TextField(text);
        tf.setPromptText(prompt);
        tf.setStyle(INPUT_STYLE + "-fx-pref-height: 35px;");
        return tf;
    }

    private static VBox createBasePopup(Stage stage, String title) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle(GLASS_BACKGROUND);
        root.setAlignment(Pos.CENTER);
        stage.setTitle(title);
        stage.setScene(new Scene(root, 350, 400));
        return root;
    }

    private static void searchEmployees(TableView<String[]> table, String user, String pass, String keyword) {
        table.getItems().clear();
        executeFetch(table, user, pass, "http://localhost:8080/api/employees/search?keyword=" + keyword);
    }

    private static void loadEmployees(TableView<String[]> table, String user, String pass) {
        executeFetch(table, user, pass, "http://localhost:8080/api/employees");
    }

    private static void executeFetch(TableView<String[]> table, String user, String pass, String url) {
        try {
            var client = java.net.http.HttpClient.newHttpClient();
            String auth = java.util.Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Basic " + auth)
                    .GET().build();

            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(response.body()).get("data");

            for (JsonNode emp : data) {
                table.getItems().add(new String[]{
                        emp.get("id").asText(),
                        emp.get("employeeCode").asText(),
                        emp.get("fullName").asText(),
                        emp.get("department").asText()
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}