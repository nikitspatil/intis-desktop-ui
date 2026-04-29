package com.idea.intis.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idea.intis.ui.service.ApiClient;
import com.idea.intis.ui.util.AlertUtil;
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
 - Show Employees screen
 - Load employee list
 - Search employees
 - Add employee popup
 - Controlled branch dropdown
 - Uses central ApiClient

 APIs:
 GET  /api/employees
 GET  /api/employees/search?keyword=
 POST /api/employees
 ==========================================================
*/

public class EmployeeView {

    /*
     ======================================================
     Main Employees Page
     ======================================================
    */
    public static VBox getView(
            String user,
            String pass) {

        /* ---------------- Title ---------------- */
        Label title =
                new Label("Employee Management");

        title.setStyle(
                "-fx-font-size:22px;" +
                        "-fx-font-weight:bold;"
        );

        /* ---------------- Table ---------------- */
        TableView<String[]> table =
                new TableView<>();

        table.setPrefHeight(520);

        /*
         Column 1 : ID
        */
        TableColumn<String[], String> idCol =
                new TableColumn<>("ID");

        idCol.setPrefWidth(70);

        idCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[0]
                ));

        /*
         Column 2 : Code
        */
        TableColumn<String[], String> codeCol =
                new TableColumn<>("Code");

        codeCol.setPrefWidth(130);

        codeCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[1]
                ));

        /*
         Column 3 : Full Name
        */
        TableColumn<String[], String> nameCol =
                new TableColumn<>("Name");

        nameCol.setPrefWidth(220);

        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[2]
                ));

        /*
         Column 4 : Department
        */
        TableColumn<String[], String> deptCol =
                new TableColumn<>("Department");

        deptCol.setPrefWidth(180);

        deptCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[3]
                ));

        /*
         Column 5 : Branch
        */
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

        /*
         Search Employee
        */
        searchBtn.setOnAction(e ->
                searchEmployees(
                        table,
                        user,
                        pass,
                        searchField.getText()
                ));

        /*
         Refresh All Employees
        */
        refreshBtn.setOnAction(e ->
                loadEmployees(
                        table,
                        user,
                        pass
                ));

        /*
         Add Employee Popup
        */
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

        /* First Load */
        loadEmployees(
                table,
                user,
                pass
        );

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
     ======================================================
    */
    private static void showAddPopup(
            String user,
            String pass,
            TableView<String[]> table) {

        TextField codeField =
                new TextField();

        codeField.setPromptText(
                "Employee Code"
        );

        TextField nameField =
                new TextField();

        nameField.setPromptText(
                "Full Name"
        );

        TextField deptField =
                new TextField();

        deptField.setPromptText(
                "Department"
        );

        /*
         Controlled Branch Dropdown
        */
        ComboBox<String> branchBox =
                new ComboBox<>();

        branchBox.getItems().addAll(
                "Pune",
                "Mumbai"
        );

        branchBox.setValue("Pune");
        branchBox.setPrefWidth(250);

        Button saveBtn =
                new Button("Save");

        Label status =
                new Label();

        /*
         Save Action
        */
        /* ==========================================================
   EMPLOYEEVIEW.java
   ONLY replace save button action inside showAddPopup()
   ========================================================== */

        saveBtn.setOnAction(e -> {

            try {

                boolean yes =
                        AlertUtil.confirm(
                                "Create new employee?"
                        );

                if (!yes) {
                    return;
                }

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

                ApiClient.post(
                        "/api/employees",
                        json,
                        user,
                        pass
                );

                AlertUtil.success(
                        "Employee saved successfully."
                );

                loadEmployees(
                        table,
                        user,
                        pass
                );

            } catch (Exception ex) {

                AlertUtil.error(
                        "Unable to save employee."
                );
            }
        });

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
                new Scene(
                        root,
                        360,
                        380
                )
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

            String json =
                    ApiClient.get(
                            "/api/employees",
                            user,
                            pass
                    );

            fillTable(
                    table,
                    json
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

            String json =
                    ApiClient.get(
                            "/api/employees/search?keyword="
                                    + keyword,
                            user,
                            pass
                    );

            fillTable(
                    table,
                    json
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     ======================================================
     Convert JSON Response To Table Rows
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