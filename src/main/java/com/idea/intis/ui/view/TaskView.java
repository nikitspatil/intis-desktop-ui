package com.idea.intis.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*
 ============================================================
 TaskView.java

 Responsibility:
 - Show Tasks page inside AppShell
 - Load tasks from backend API
 - Display tasks in table
 - Add new task using popup form
 - Load employee dropdown from backend
 - Refresh tasks list

 APIs Used:
 GET  /api/tasks
 POST /api/tasks
 GET  /api/employees
 ============================================================
*/

public class TaskView {

    /*
     ========================================================
     Main Tasks Page
     Called from AppShell when user clicks Tasks menu
     ========================================================
    */
    public static VBox getView(String user, String pass) {

        /* ---------------- Title ---------------- */
        Label title = new Label("Task Management");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold;");

        /* ---------------- Table ---------------- */
        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(520);

        // Column 1 - Task ID
        TableColumn<String[], String> idCol =
                new TableColumn<>("ID");
        idCol.setPrefWidth(80);
        idCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[0]));

        // Column 2 - Task Title
        TableColumn<String[], String> titleCol =
                new TableColumn<>("Title");
        titleCol.setPrefWidth(260);
        titleCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[1]));

        // Column 3 - Employee Name
        TableColumn<String[], String> assignCol =
                new TableColumn<>("Assigned To");
        assignCol.setPrefWidth(220);
        assignCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[2]));

        // Column 4 - Task Status
        TableColumn<String[], String> statusCol =
                new TableColumn<>("Status");
        statusCol.setPrefWidth(180);
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[3]));

        table.getColumns().addAll(
                idCol,
                titleCol,
                assignCol,
                statusCol
        );

        /* ====================================================
           Toolbar Buttons
           ==================================================== */

        Button refreshBtn = new Button("Refresh");
        Button addBtn = new Button("Add Task");
        Button updateBtn = new Button("Update Status");

        // Reload tasks from backend
        refreshBtn.setOnAction(e ->
                loadTasks(table, user, pass));

        // Open Add Task popup
        addBtn.setOnAction(e ->
                showAddPopup(user, pass, table));

        // Open update Task popup
        updateBtn.setOnAction(e ->
                showStatusPopup(user, pass, table));

        HBox toolbar = new HBox(
                10,
                refreshBtn,
                addBtn,
                updateBtn
        );

        toolbar.setAlignment(Pos.CENTER_LEFT);

        /* Load tasks automatically when page opens */
        loadTasks(table, user, pass);

        /* ---------------- Final Page Layout ---------------- */
        VBox root = new VBox(
                15,
                title,
                toolbar,
                table
        );

        root.setPadding(new Insets(20));

        return root;
    }

    /*
     ========================================================
     Add Task Popup Window
     ========================================================
    */
    private static void showAddPopup(
            String user,
            String pass,
            TableView<String[]> table) {

        /* Task title input */
        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        /* Task description input */
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefHeight(90);

        /* Employee dropdown */
        ComboBox<String> employeeBox =
                new ComboBox<>();

        employeeBox.setPrefWidth(280);
        employeeBox.setPromptText("Select Employee");

        // Load employees into dropdown
        loadEmployees(employeeBox, user, pass);

        /* Status dropdown */
        ComboBox<String> statusBox =
                new ComboBox<>();

        statusBox.getItems().addAll(
                "PENDING",
                "IN_PROGRESS",
                "DONE"
        );

        statusBox.setValue("PENDING");

        /* Save button */
        Button saveBtn = new Button("Save Task");

        /* Status label */
        Label status = new Label();

        /*
         ----------------------------------------------------
         Save Button Action
         ----------------------------------------------------
        */
        saveBtn.setOnAction(e -> {

            try {
                String selectedEmployee =
                        employeeBox.getValue();

                // If no employee selected
                if (selectedEmployee == null) {
                    status.setText(
                            "Select employee");
                    return;
                }

                /*
                 Dropdown value format:
                 1 - Rahul
                 Need employee ID only
                */
                String employeeId =
                        selectedEmployee
                                .split(" - ")[0];

                var client =
                        java.net.http.HttpClient
                                .newHttpClient();

                String auth =
                        java.util.Base64
                                .getEncoder()
                                .encodeToString(
                                        (user + ":" + pass)
                                                .getBytes()
                                );

                /*
                 JSON body sent to backend
                */
                String json =
                        "{"
                                + "\"title\":\""
                                + titleField.getText()
                                + "\","

                                + "\"description\":\""
                                + descField.getText()
                                + "\","

                                + "\"employeeId\":"
                                + Long.parseLong(employeeId)
                                + ","

                                + "\"status\":\""
                                + statusBox.getValue()
                                + "\""
                                + "}";

                var request =
                        java.net.http.HttpRequest
                                .newBuilder()
                                .uri(java.net.URI.create(
                                        "http://localhost:8080/api/tasks"))
                                .header(
                                        "Authorization",
                                        "Basic " + auth
                                )
                                .header(
                                        "Content-Type",
                                        "application/json"
                                )
                                .POST(
                                        java.net.http.HttpRequest
                                                .BodyPublishers
                                                .ofString(json)
                                )
                                .build();

                var response =
                        client.send(
                                request,
                                java.net.http.HttpResponse
                                        .BodyHandlers
                                        .ofString()
                        );

                if (response.statusCode() == 200
                        || response.statusCode() == 201) {

                    status.setText(
                            "Task Saved Successfully");

                    // Refresh tasks table
                    loadTasks(table, user, pass);

                } else {
                    status.setText(
                            "Save Failed");
                }

            } catch (Exception ex) {

                status.setText("Error");
            }
        });

        /* Popup Layout */
        VBox root = new VBox(
                12,
                titleField,
                descField,
                employeeBox,
                statusBox,
                saveBtn,
                status
        );

        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Stage popup = new Stage();
        popup.setTitle("Add Task");
        popup.setScene(
                new Scene(root, 380, 430)
        );
        popup.show();
    }

    /*
    =========================================================


    =========================================================
     */
    private static void showStatusPopup(
            String user,
            String pass,
            TableView<String[]> table) {

        String[] selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {
            return;
        }

        ComboBox<String> statusBox =
                new ComboBox<>();

        statusBox.getItems().addAll(
                "PENDING",
                "IN_PROGRESS",
                "DONE"
        );

        statusBox.setValue(selected[3]);

        Button saveBtn =
                new Button("Update");

        Label msg =
                new Label();

        saveBtn.setOnAction(e -> {

            try {
                var client =
                        java.net.http.HttpClient
                                .newHttpClient();

                String auth =
                        java.util.Base64
                                .getEncoder()
                                .encodeToString(
                                        (user + ":" + pass)
                                                .getBytes()
                                );

                String json =
                        "{"
                                + "\"status\":\""
                                + statusBox.getValue()
                                + "\""
                                + "}";

                var request =
                        java.net.http.HttpRequest
                                .newBuilder()
                                .uri(java.net.URI.create(
                                        "http://localhost:8080/api/tasks/"
                                                + selected[0]
                                                + "/status?status="
                                                + statusBox.getValue()
                                ))
                                .header(
                                        "Authorization",
                                        "Basic " + auth
                                )
                                .PUT(
                                        java.net.http.HttpRequest
                                                .BodyPublishers
                                                .noBody()
                                )
                                .build();

                var response =
                        client.send(
                                request,
                                java.net.http.HttpResponse
                                        .BodyHandlers
                                        .ofString()
                        );

                if (response.statusCode() == 200) {

                    msg.setText("Updated");

                    loadTasks(table, user, pass);

                } else {
                    msg.setText("Failed");
                }

            } catch (Exception ex) {
                msg.setText("Error");
            }
        });

        VBox root =
                new VBox(
                        12,
                        statusBox,
                        saveBtn,
                        msg
                );

        root.setPadding(
                new Insets(20)
        );

        Stage popup =
                new Stage();

        popup.setTitle(
                "Update Task Status"
        );

        popup.setScene(
                new Scene(root, 280, 220)
        );

        popup.show();
    }

    /*
     ========================================================
     Load Employees into Dropdown
     Used in Add Task popup
     ========================================================
    */
    private static void loadEmployees(
            ComboBox<String> employeeBox,
            String user,
            String pass) {

        try {
            var client =
                    java.net.http.HttpClient
                            .newHttpClient();

            String auth =
                    java.util.Base64
                            .getEncoder()
                            .encodeToString(
                                    (user + ":" + pass)
                                            .getBytes()
                            );

            var request =
                    java.net.http.HttpRequest
                            .newBuilder()
                            .uri(java.net.URI.create(
                                    "http://localhost:8080/api/employees"))
                            .header(
                                    "Authorization",
                                    "Basic " + auth
                            )
                            .GET()
                            .build();

            var response =
                    client.send(
                            request,
                            java.net.http.HttpResponse
                                    .BodyHandlers
                                    .ofString()
                    );

            ObjectMapper mapper =
                    new ObjectMapper();

            JsonNode root =
                    mapper.readTree(
                            response.body());

            JsonNode rows =
                    root.has("data")
                            ? root.get("data")
                            : root;

            if (rows == null
                    || !rows.isArray()) {
                return;
            }

            for (JsonNode emp : rows) {

                String item =
                        emp.path("id").asText()
                                + " - "
                                + emp.path("fullName")
                                .asText();

                employeeBox
                        .getItems()
                        .add(item);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     ========================================================
     Load Tasks from Backend
     ========================================================
    */
    private static void loadTasks(
            TableView<String[]> table,
            String user,
            String pass) {

        table.getItems().clear();

        try {
            var client =
                    java.net.http.HttpClient
                            .newHttpClient();

            String auth =
                    java.util.Base64
                            .getEncoder()
                            .encodeToString(
                                    (user + ":" + pass)
                                            .getBytes()
                            );

            var request =
                    java.net.http.HttpRequest
                            .newBuilder()
                            .uri(java.net.URI.create(
                                    "http://localhost:8080/api/tasks"))
                            .header(
                                    "Authorization",
                                    "Basic " + auth
                            )
                            .GET()
                            .build();

            var response =
                    client.send(
                            request,
                            java.net.http.HttpResponse
                                    .BodyHandlers
                                    .ofString()
                    );

            fillTable(
                    table,
                    response.body()
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     ========================================================
     Convert JSON Response into Table Rows
     ========================================================
    */
    private static void fillTable(
            TableView<String[]> table,
            String json)
            throws Exception {

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode root =
                mapper.readTree(json);

        JsonNode rows =
                root.has("data")
                        ? root.get("data")
                        : root;

        if (rows == null
                || !rows.isArray()) {
            return;
        }

        for (JsonNode task : rows) {

            String assignee;

            /*
             If backend returns object:
             assignedTo { fullName }
            */
            if (task.path("assignedTo")
                    .isObject()) {

                assignee =
                        task.path("assignedTo")
                                .path("fullName")
                                .asText();

            } else {

                assignee =
                        task.path("assignedTo")
                                .asText();
            }

            table.getItems().add(
                    new String[]{
                            task.path("id")
                                    .asText(),

                            task.path("title")
                                    .asText(),

                            assignee,

                            task.path("status")
                                    .asText()
                    }
            );
        }
    }
}