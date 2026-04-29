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
 TaskView.java

 Responsibility:
 - Show Tasks screen
 - Load tasks
 - Add task popup
 - Update task status popup
 - Uses central ApiClient

 APIs:
 GET  /api/tasks
 GET  /api/employees
 POST /api/tasks
 PUT  /api/tasks/{id}/status?status=
 ==========================================================
*/

public class TaskView {

    /*
     ======================================================
     Main Tasks Page
     ======================================================
    */
    public static VBox getView(
            String user,
            String pass) {

        /* ---------------- Title ---------------- */
        Label title =
                new Label("Task Management");

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
         Column 2 : Title
        */
        TableColumn<String[], String> titleCol =
                new TableColumn<>("Title");

        titleCol.setPrefWidth(220);

        titleCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[1]
                ));

        /*
         Column 3 : Description
        */
        TableColumn<String[], String> descCol =
                new TableColumn<>("Description");

        descCol.setPrefWidth(260);

        descCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[2]
                ));

        /*
         Column 4 : Assigned To
        */
        TableColumn<String[], String> empCol =
                new TableColumn<>("Assigned To");

        empCol.setPrefWidth(180);

        empCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[3]
                ));

        /*
         Column 5 : Status
        */
        TableColumn<String[], String> statusCol =
                new TableColumn<>("Status");

        statusCol.setPrefWidth(150);

        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[4]
                ));

        table.getColumns().addAll(
                idCol,
                titleCol,
                descCol,
                empCol,
                statusCol
        );

        /* ---------------- Buttons ---------------- */
        Button refreshBtn =
                new Button("Refresh");

        Button addBtn =
                new Button("Add Task");

        Button statusBtn =
                new Button("Update Status");

        refreshBtn.setOnAction(e ->
                loadTasks(
                        table,
                        user,
                        pass
                ));

        addBtn.setOnAction(e ->
                showAddPopup(
                        user,
                        pass,
                        table
                ));

        statusBtn.setOnAction(e ->
                showStatusPopup(
                        table,
                        user,
                        pass
                ));

        HBox toolbar =
                new HBox(
                        10,
                        refreshBtn,
                        addBtn,
                        statusBtn
                );

        toolbar.setAlignment(
                Pos.CENTER_LEFT
        );

        /* First Load */
        loadTasks(
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
     Add Task Popup
     ======================================================
    */
    private static void showAddPopup(
            String user,
            String pass,
            TableView<String[]> table) {

        TextField titleField =
                new TextField();

        titleField.setPromptText(
                "Task Title"
        );

        TextField descField =
                new TextField();

        descField.setPromptText(
                "Description"
        );

        /*
         Employee Dropdown
        */
        ComboBox<String> empBox =
                new ComboBox<>();

        loadEmployeesForDropdown(
                empBox,
                user,
                pass
        );

        ComboBox<String> statusBox =
                new ComboBox<>();

        statusBox.getItems().addAll(
                "OPEN",
                "IN_PROGRESS",
                "DONE"
        );

        statusBox.setValue(
                "PENDING"
        );

        Button saveBtn =
                new Button("Save");

        Label status =
                new Label();

        /*
         Save Task
        */
        saveBtn.setOnAction(e -> {

            try {

                if (empBox.getValue() == null) {
                    status.setText(
                            "Select employee"
                    );
                    return;
                }

                Long empId =
                        Long.parseLong(
                                empBox.getValue()
                                        .split(" - ")[0]
                        );

                String json =
                        "{"
                                + "\"title\":\""
                                + titleField.getText()
                                + "\","

                                + "\"description\":\""
                                + descField.getText()
                                + "\","

                                + "\"employeeId\":"
                                + empId
                                + ","

                                + "\"status\":\""
                                + statusBox.getValue()
                                + "\""
                                + "}";

                ApiClient.post(
                        "/api/tasks",
                        json,
                        user,
                        pass
                );

                status.setText(
                        "Task Saved"
                );

                loadTasks(
                        table,
                        user,
                        pass
                );

            } catch (Exception ex) {

                status.setText(
                        "Save Failed"
                );
            }
        });

        VBox root =
                new VBox(
                        12,
                        titleField,
                        descField,
                        empBox,
                        statusBox,
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
                "Add Task"
        );

        popup.setScene(
                new Scene(
                        root,
                        380,
                        420
                )
        );

        popup.show();
    }

    /*
     ======================================================
     Update Task Status Popup
     ======================================================
    */
    private static void showStatusPopup(
            TableView<String[]> table,
            String user,
            String pass) {

        String[] selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {
            return;
        }

        ComboBox<String> statusBox =
                new ComboBox<>();

        statusBox.getItems().addAll(
                "OPEN",
                "IN_PROGRESS",
                "DONE"
        );

        statusBox.setValue(
                selected[4]
        );

        Button updateBtn =
                new Button("Update");

        Label result =
                new Label();

        /* ==========================================================
   TASKVIEW.java
   ONLY replace the update button action inside showStatusPopup()
   ========================================================== */

        updateBtn.setOnAction(e -> {

            try {

                boolean yes =
                        AlertUtil.confirm(
                                "Update task status to "
                                        + statusBox.getValue()
                                        + " ?"
                        );

                if (!yes) {
                    return;
                }

                ApiClient.put(
                        "/api/tasks/"
                                + selected[0]
                                + "/status?status="
                                + statusBox.getValue(),
                        "",
                        user,
                        pass
                );

                AlertUtil.success(
                        "Task status updated."
                );

                loadTasks(
                        table,
                        user,
                        pass
                );

            } catch (Exception ex) {

                AlertUtil.error(
                        "Unable to update task."
                );
            }
        });

        VBox root =
                new VBox(
                        12,
                        statusBox,
                        updateBtn,
                        result
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
                "Update Status"
        );

        popup.setScene(
                new Scene(
                        root,
                        260,
                        220
                )
        );

        popup.show();
    }

    /*
     ======================================================
     Load Tasks
     ======================================================
    */
    private static void loadTasks(
            TableView<String[]> table,
            String user,
            String pass) {

        table.getItems().clear();

        try {

            String json =
                    ApiClient.get(
                            "/api/tasks",
                            user,
                            pass
                    );

            fillTaskTable(
                    table,
                    json
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     ======================================================
     Load Employees for Dropdown
     ======================================================
    */
    private static void loadEmployeesForDropdown(
            ComboBox<String> box,
            String user,
            String pass) {

        try {

            String json =
                    ApiClient.get(
                            "/api/employees",
                            user,
                            pass
                    );

            ObjectMapper mapper =
                    new ObjectMapper();

            JsonNode root =
                    mapper.readTree(json);

            JsonNode rows =
                    root.has("data")
                            ? root.get("data")
                            : root;

            for (JsonNode row : rows) {

                box.getItems().add(
                        row.path("id").asText()
                                + " - "
                                + row.path("fullName")
                                .asText()
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     ======================================================
     Fill Task Table
     ======================================================
    */
    private static void fillTaskTable(
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

        if (rows == null || !rows.isArray()) {
            return;
        }

        for (JsonNode row : rows) {

        /*
         Correct backend field:
         assignedTo.fullName
        */
            String employeeName =
                    row.path("assignedTo")
                            .path("fullName")
                            .asText();

            if (employeeName.isEmpty()) {
                employeeName = "Unassigned";
            }

            table.getItems().add(
                    new String[]{
                            row.path("id").asText(),
                            row.path("title").asText(),
                            row.path("description").asText(),
                            employeeName,
                            row.path("status").asText()
                    }
            );
        }
    }
}