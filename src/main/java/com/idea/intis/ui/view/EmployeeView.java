package com.idea.intis.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EmployeeView {

    public static VBox getView(String user, String pass) {

        Label title = new Label("Employee Management");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold;");

        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(520);

        TableColumn<String[], String> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(80);
        idCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[0]));

        TableColumn<String[], String> codeCol = new TableColumn<>("Code");
        codeCol.setPrefWidth(150);
        codeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[1]));

        TableColumn<String[], String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(250);
        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[2]));

        TableColumn<String[], String> deptCol = new TableColumn<>("Department");
        deptCol.setPrefWidth(180);
        deptCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue()[3]));

        table.getColumns().addAll(idCol, codeCol, nameCol, deptCol);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Name / Code");
        searchField.setPrefWidth(250);

        Button searchBtn = new Button("Search");
        Button refreshBtn = new Button("Refresh");
        Button addBtn = new Button("Add Employee");

        searchBtn.setOnAction(e ->
                searchEmployees(table, user, pass, searchField.getText()));

        refreshBtn.setOnAction(e ->
                loadEmployees(table, user, pass));

        addBtn.setOnAction(e ->
                showAddPopup(user, pass, table));

        HBox toolbar = new HBox(10,
                searchField,
                searchBtn,
                refreshBtn,
                addBtn
        );

        toolbar.setAlignment(Pos.CENTER_LEFT);

        loadEmployees(table, user, pass);

        VBox root = new VBox(15,
                title,
                toolbar,
                table
        );

        root.setPadding(new Insets(20));

        return root;
    }

    private static void showAddPopup(
            String user,
            String pass,
            TableView<String[]> table) {

        TextField code = new TextField();
        code.setPromptText("Employee Code");

        TextField name = new TextField();
        name.setPromptText("Full Name");

        TextField dept = new TextField();
        dept.setPromptText("Department");

        Button save = new Button("Save");
        Label status = new Label();

        save.setOnAction(e -> {

            try {
                var client = java.net.http.HttpClient.newHttpClient();

                String auth = java.util.Base64.getEncoder()
                        .encodeToString((user + ":" + pass).getBytes());

                String json =
                        "{"
                                + "\"employeeCode\":\"" + code.getText() + "\","
                                + "\"fullName\":\"" + name.getText() + "\","
                                + "\"department\":\"" + dept.getText() + "\""
                                + "}";

                var request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8080/api/employees"))
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

                    status.setText("Saved successfully");
                    loadEmployees(table, user, pass);

                } else {
                    status.setText("Save failed");
                }

            } catch (Exception ex) {
                status.setText("Error");
            }
        });

        VBox box = new VBox(10,
                code,
                name,
                dept,
                save,
                status
        );

        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Stage popup = new Stage();
        popup.setTitle("Add Employee");
        popup.setScene(new javafx.scene.Scene(box, 320, 280));
        popup.show();
    }

    private static void loadEmployees(
            TableView<String[]> table,
            String user,
            String pass) {

        table.getItems().clear();

        try {
            var client = java.net.http.HttpClient.newHttpClient();

            String auth = java.util.Base64.getEncoder()
                    .encodeToString((user + ":" + pass).getBytes());

            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/api/employees"))
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

    private static void searchEmployees(
            TableView<String[]> table,
            String user,
            String pass,
            String keyword) {

        table.getItems().clear();

        try {
            var client = java.net.http.HttpClient.newHttpClient();

            String auth = java.util.Base64.getEncoder()
                    .encodeToString((user + ":" + pass).getBytes());

            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(
                            "http://localhost:8080/api/employees/search?keyword=" + keyword))
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

        JsonNode rows = null;

        if (root.isArray()) {
            rows = root;
        } else if (root.has("data")) {
            rows = root.get("data");

            if (rows != null && rows.has("content")) {
                rows = rows.get("content");
            }
        } else if (root.has("content")) {
            rows = root.get("content");
        }

        if (rows == null || !rows.isArray()) {
            return;
        }

        for (JsonNode emp : rows) {

            table.getItems().add(new String[]{
                    emp.path("id").asText(),
                    emp.path("employeeCode").asText(),
                    emp.path("fullName").asText(),
                    emp.path("department").asText()
            });
        }
    }
}