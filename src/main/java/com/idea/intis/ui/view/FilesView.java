package com.idea.intis.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idea.intis.ui.config.ApiConfig;
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
 FilesView.java

 Purpose:
 Design Document Revision Module

 This is NOT generic file storage.

 Used For:
 - Design documents
 - API docs
 - UI wireframes
 - Architecture revisions
 - Task linked revisions

 APIs Used:
 POST /api/files
 GET  /api/files/task/{taskId}
 ==========================================================
*/

public class FilesView {

    /*
     ======================================================
     Main Page
     Called from AppShell when Files menu clicked
     ======================================================
    */
    public static VBox getView(String user, String pass) {

        /* ---------------- Title ---------------- */
        Label title =
                new Label("Design Revisions");

        title.setStyle(
                "-fx-font-size:22px;" +
                        "-fx-font-weight:bold;"
        );

        /*
         ------------------------------------------
         Task ID Search Section
         User enters task id to load revisions
         ------------------------------------------
        */
        TextField taskIdField =
                new TextField();

        taskIdField.setPromptText(
                "Enter Task ID"
        );

        taskIdField.setPrefWidth(160);

        Button loadBtn =
                new Button("Load");

        Button addBtn =
                new Button("Add Revision");

        /* ---------------- Table ---------------- */
        TableView<String[]> table =
                new TableView<>();

        table.setPrefHeight(520);

        /*
         Column 1 : Revision ID
        */
        TableColumn<String[], String> idCol =
                new TableColumn<>("ID");

        idCol.setPrefWidth(80);

        idCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[0]
                ));

        /*
         Column 2 : File Name
        */
        TableColumn<String[], String> fileCol =
                new TableColumn<>("File Name");

        fileCol.setPrefWidth(220);

        fileCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[1]
                ));

        /*
         Column 3 : Revision Number
        */
        TableColumn<String[], String> revCol =
                new TableColumn<>("Revision");

        revCol.setPrefWidth(120);

        revCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[2]
                ));

        /*
         Column 4 : Remarks
        */
        TableColumn<String[], String> remarksCol =
                new TableColumn<>("Remarks");

        remarksCol.setPrefWidth(260);

        remarksCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[3]
                ));

        /*
         Column 5 : Uploaded Time
        */
        TableColumn<String[], String> timeCol =
                new TableColumn<>("Uploaded At");

        timeCol.setPrefWidth(220);

        timeCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[4]
                ));

        table.getColumns().addAll(
                idCol,
                fileCol,
                revCol,
                remarksCol,
                timeCol
        );

        /*
         ------------------------------------------
         Load Revisions by Task ID
         ------------------------------------------
        */
        loadBtn.setOnAction(e -> {

            String taskId =
                    taskIdField.getText()
                            .trim();

            if (!taskId.isEmpty()) {

                loadRevisions(
                        table,
                        user,
                        pass,
                        taskId
                );
            }
        });

        /*
         ------------------------------------------
         Add Revision Popup
         ------------------------------------------
        */
        addBtn.setOnAction(e -> {

            String taskId =
                    taskIdField.getText().trim();

            if (taskId.isEmpty()) {

                Alert alert =
                        new Alert(
                                Alert.AlertType.WARNING
                        );

                alert.setTitle("Task Required");
                alert.setHeaderText(null);
                alert.setContentText(
                        "Enter Task ID first."
                );

                alert.show();
                return;
            }

            showAddPopup(
                    user,
                    pass,
                    taskId,
                    table
            );
        });

        HBox toolbar =
                new HBox(
                        10,
                        taskIdField,
                        loadBtn,
                        addBtn
                );

        toolbar.setAlignment(
                Pos.CENTER_LEFT
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
     Add Revision Popup
     ======================================================
    */
    private static void showAddPopup(
            String user,
            String pass,
            String taskId,
            TableView<String[]> table) {

        TextField fileNameField =
                new TextField();

        fileNameField.setPromptText(
                "File Name"
        );

        TextField revisionField =
                new TextField();

        revisionField.setPromptText(
                "Revision No (R1 / V2)"
        );

        TextField pathField =
                new TextField();

        pathField.setPromptText(
                "File Path"
        );

        TextArea remarksField =
                new TextArea();

        remarksField.setPromptText(
                "Remarks"
        );

        remarksField.setPrefHeight(90);

        Button saveBtn =
                new Button("Save Revision");

        Label status =
                new Label();

        /*
         Save Revision Action
        */
        /* ==========================================================
   FILESVIEW.java
   ONLY replace save button action inside showAddPopup()
   ========================================================== */

        saveBtn.setOnAction(e -> {

            try {

                boolean yes =
                        AlertUtil.confirm(
                                "Save new revision?"
                        );

                if (!yes) {
                    return;
                }

                String json =
                        "{"
                                + "\"fileName\":\""
                                + fileNameField.getText()
                                + "\","

                                + "\"revisionNo\":\""
                                + revisionField.getText()
                                + "\","

                                + "\"filePath\":\""
                                + pathField.getText()
                                + "\","

                                + "\"remarks\":\""
                                + remarksField.getText()
                                + "\","

                                + "\"taskId\":"
                                + Long.parseLong(taskId)
                                + "}";

                ApiClient.post(
                        "/api/files",
                        json,
                        user,
                        pass
                );

                AlertUtil.success(
                        "Revision saved successfully."
                );

                loadRevisions(
                        table,
                        user,
                        pass,
                        taskId
                );

            } catch (Exception ex) {

                AlertUtil.error(
                        "Unable to save revision."
                );
            }
        });

        VBox root =
                new VBox(
                        12,
                        fileNameField,
                        revisionField,
                        pathField,
                        remarksField,
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
                "Add Revision"
        );

        popup.setScene(
                new Scene(root, 380, 430)
        );

        popup.show();
    }

    /*
     ======================================================
     Load Revisions by Task ID
     ======================================================
    */
    private static void loadRevisions(
            TableView<String[]> table,
            String user,
            String pass,
            String taskId) {

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
                            .uri(java.net.URI.create(ApiConfig.BASE_URL +
                                    "/api/files/task/"
                                            + taskId))
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
     Convert JSON into table rows
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

        for (JsonNode row : rows) {

            table.getItems().add(
                    new String[]{
                            row.path("id").asText(),
                            row.path("fileName").asText(),
                            row.path("revisionNo").asText(),
                            row.path("remarks").asText(),
                            row.path("uploadedAt").asText()
                    }
            );
        }
    }
}