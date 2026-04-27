package com.idea.intis.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TaskView {

    public static VBox getView(String user, String pass) {

        Label title = new Label("Task Management");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold;");

        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(520);

        TableColumn<String[], String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue()[0]));

        TableColumn<String[], String> taskCol = new TableColumn<>("Title");
        taskCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue()[1]));

        TableColumn<String[], String> assignCol = new TableColumn<>("Assigned To");
        assignCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue()[2]));

        TableColumn<String[], String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue()[3]));

        table.getColumns().addAll(idCol, taskCol, assignCol, statusCol);

        Button refreshBtn = new Button("Refresh");
        Button addBtn = new Button("Add Task");

        refreshBtn.setOnAction(e ->
                loadTasks(table, user, pass));

        addBtn.setOnAction(e ->
                showAddPopup(user, pass, table));

        loadTasks(table, user, pass);

        VBox root = new VBox(
                12,
                title,
                refreshBtn,
                addBtn,
                table
        );

        root.setPadding(new Insets(20));

        return root;
    }

    private static void showAddPopup(
            String user,
            String pass,
            TableView<String[]> table) {

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefHeight(80);

        TextField employeeIdField = new TextField();
        employeeIdField.setPromptText("Employee ID");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(
                "PENDING",
                "IN_PROGRESS",
                "DONE"
        );
        statusBox.setValue("PENDING");

        Button saveBtn = new Button("Save");
        Label status = new Label();

        saveBtn.setOnAction(e -> {

            try {
                var client = java.net.http.HttpClient.newHttpClient();

                String auth = java.util.Base64.getEncoder()
                        .encodeToString((user + ":" + pass).getBytes());

                String empId = employeeIdField.getText().trim();

                if (empId.isEmpty()) {
                    status.setText("Enter Employee ID");
                    return;
                }

                String json =
                        "{"
                                + "\"title\":\"" + titleField.getText() + "\","
                                + "\"description\":\"" + descField.getText() + "\","
                                + "\"employeeId\":" + Long.parseLong(empId) + ","
                                + "\"status\":\"" + statusBox.getValue() + "\""
                                + "}";

                var request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(
                                "http://localhost:8080/api/tasks"))
                        .header("Authorization", "Basic " + auth)
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();

                var response = client.send(
                        request,
                        java.net.http.HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() == 200 ||
                        response.statusCode() == 201) {

                    status.setText("Saved");
                    loadTasks(table, user, pass);

                } else {
                    status.setText("Failed");
                }

            } catch (Exception ex) {
                status.setText("Error");
            }
        });

        VBox root = new VBox(
                10,
                titleField,
                descField,
                employeeIdField,
                statusBox,
                saveBtn,
                status
        );

        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Stage popup = new Stage();
        popup.setTitle("Add Task");
        popup.setScene(new Scene(root, 360, 420));
        popup.show();
    }

    private static void loadTasks(
            TableView<String[]> table,
            String user,
            String pass) {

        table.getItems().clear();

        try {
            var client = java.net.http.HttpClient.newHttpClient();

            String auth = java.util.Base64.getEncoder()
                    .encodeToString((user + ":" + pass).getBytes());

            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(
                            "http://localhost:8080/api/tasks"))
                    .header("Authorization", "Basic " + auth)
                    .GET()
                    .build();

            var response = client.send(
                    request,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            fillTable(table, response.body());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void fillTable(
            TableView<String[]> table,
            String json) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode rows = root.has("data")
                ? root.get("data")
                : root;

        if (rows == null || !rows.isArray()) {
            return;
        }

        for (JsonNode task : rows) {

            String assignee =
                    task.path("assignedTo").isObject()
                            ? task.path("assignedTo")
                              .path("fullName").asText()
                            : task.path("assignedTo").asText();

            table.getItems().add(new String[]{
                    task.path("id").asText(),
                    task.path("title").asText(),
                    assignee,
                    task.path("status").asText()
            });
        }
    }
}