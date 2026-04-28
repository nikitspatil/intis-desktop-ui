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
 ==========================================================
 EmployeeView.java

 Responsibility:
 - Show Employees page inside AppShell
 - Load employee list from backend
 - Search employees
 - Add employee popup
 - Controlled Branch dropdown (Pune / Mumbai)

 APIs Used:
 GET  /api/employees
 GET  /api/employees/search?keyword=
 POST /api/employees
 ==========================================================
*/

public class EmployeeView {

    /*
     ======================================================
     Main Employees Page
     Called from AppShell
     ======================================================
    */
    public static VBox getView(String user, String pass) {

        /* ---------------- Page Title ---------------- */
        Label title = new Label("Employee Management");
        title.setStyle(
                "-fx-font-size:22px;" +
                        "-fx-font-weight:bold;"
        );

        /* ---------------- Employee Table ---------------- */
        TableView<String[]> table =
                new TableView<>();

        table.setPrefHeight(520);

        /* Employee ID */
        TableColumn<String[], String> idCol =
                new TableColumn<>("ID");

        idCol.setPrefWidth(80);

        idCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[0]
                ));

        /* Employee Code */
        TableColumn<String[], String> codeCol =
                new TableColumn<>("Code");

        codeCol.setPrefWidth(140);

        codeCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[1]
                ));

        /* Full Name */
        TableColumn<String[], String> nameCol =
                new TableColumn<>("Name");

        nameCol.setPrefWidth(220);

        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[2]
                ));

        /* Department */
        TableColumn<String[], String> deptCol =
                new TableColumn<>("Department");

        deptCol.setPrefWidth(180);

        deptCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[3]
                ));

        /* Branch */
        TableColumn<String[], String> branchCol =
                new TableColumn<>("Branch");

        branchCol.setPrefWidth(150);

        branchCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[4]
                ));

        table.getColumns().addAll(
                idCol,
                codeCol,
                nameCol,
                deptCol,
                branchCol
        );

        /* ---------------- Search Controls ---------------- */
        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search Name / Code"
        );

        searchField.setPrefWidth(220);

        Button searchBtn =
                new Button("Search");

        Button refreshBtn =
                new Button("Refresh");

        Button addBtn =
                new Button("Add Employee");

        /* Search employee */
        searchBtn.setOnAction(e ->
                searchEmployees(
                        table,
                        user,
                        pass,
                        searchField.getText()
                ));

        /* Reload all employees */
        refreshBtn.setOnAction(e ->
                loadEmployees(
                        table,
                        user,
                        pass
                ));

        /* Open Add Employee popup */
        addBtn.setOnAction(e ->
                showAddPopup(
                        user,
                        pass,
                        table
                ));

        HBox toolbar =
                new HBox(
                        10,
                        searchField,
                        searchBtn,
                        refreshBtn,
                        addBtn
                );

        toolbar.setAlignment(
                Pos.CENTER_LEFT
        );

        /* Load employees on page open */
        loadEmployees(table, user, pass);

        /* ---------------- Final Layout ---------------- */
        VBox root =
                new VBox(
                        15,
                        title,
                        toolbar,
                        table
                );

        root.setPadding(
                new Insets(20)
        );

        return root;
    }

    /*
     ======================================================
     Add Employee Popup
     Controlled Branch Dropdown:
     Pune / Mumbai
     ======================================================
    */
    private static void showAddPopup(
            String user,
            String pass,
            TableView<String[]> table) {

        /* Employee Code */
        TextField codeField =
                new TextField();

        codeField.setPromptText(
                "Employee Code"
        );

        /* Full Name */
        TextField nameField =
                new TextField();

        nameField.setPromptText(
                "Full Name"
        );

        /* Department */
        TextField deptField =
                new TextField();

        deptField.setPromptText(
                "Department"
        );

        /*
         ----------------------------------------------
         Controlled Branch Dropdown
         Only allowed values:
         Pune / Mumbai
         ----------------------------------------------
        */
        ComboBox<String> branchBox =
                new ComboBox<>();

        branchBox.getItems().addAll(
                "Pune",
                "Mumbai"
        );

        branchBox.setValue("Pune");
        branchBox.setPrefWidth(260);

        Button saveBtn =
                new Button("Save");

        Label status =
                new Label();

        /*
         Save Employee Button Action
        */
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

                /*
                 JSON body sent to backend
                */
                String json =
                        "{"
                                + "\"employeeCode\":\""
                                + codeField.getText()
                                + "\","

                                + "\"fullName\":\""
                                + nameField.getText()
                                + "\","

                                + "\"department\":\""
                                + deptField.getText()
                                + "\","

                                + "\"branch\":\""
                                + branchBox.getValue()
                                + "\""
                                + "}";

                var request =
                        java.net.http.HttpRequest
                                .newBuilder()
                                .uri(java.net.URI.create(
                                        "http://localhost:8080/api/employees"))
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
                            "Employee Saved"
                    );

                    /* Refresh employee table */
                    loadEmployees(
                            table,
                            user,
                            pass
                    );

                } else {

                    status.setText(
                            "Save Failed"
                    );
                }

            } catch (Exception ex) {

                status.setText(
                        "Error"
                );
            }
        });

        /* Popup Layout */
        VBox root =
                new VBox(
                        12,
                        codeField,
                        nameField,
                        deptField,
                        branchBox,
                        saveBtn,
                        status
                );

        root.setPadding(
                new Insets(20)
        );

        root.setAlignment(
                Pos.CENTER
        );

        Stage popup =
                new Stage();

        popup.setTitle(
                "Add Employee"
        );

        popup.setScene(
                new Scene(root, 360, 380)
        );

        popup.show();
    }

    /*
     ======================================================
     Load All Employees
     ======================================================
    */
    private static void loadEmployees(
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

            fillTable(
                    table,
                    response.body()
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     ======================================================
     Search Employees
     ======================================================
    */
    private static void searchEmployees(
            TableView<String[]> table,
            String user,
            String pass,
            String keyword) {

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
                                    "http://localhost:8080/api/employees/search?keyword="
                                            + keyword))
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
     ======================================================
     Convert JSON response into table rows
     ======================================================
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

        if (rows == null ||
                !rows.isArray()) {
            return;
        }

        for (JsonNode emp : rows) {

            table.getItems().add(
                    new String[]{
                            emp.path("id").asText(),
                            emp.path("employeeCode").asText(),
                            emp.path("fullName").asText(),
                            emp.path("department").asText(),
                            emp.path("branch").asText()
                    }
            );
        }
    }
}