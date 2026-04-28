package com.idea.intis.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idea.intis.ui.config.ApiConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class AuditView {

    public static VBox getView(String user, String pass) {

        Label title = new Label("Audit Logs");
        title.setStyle(
                "-fx-font-size:22px;" +
                        "-fx-font-weight:bold;"
        );

        TableView<String[]> table =
                new TableView<>();

        table.setPrefHeight(520);

        TableColumn<String[], String> userCol =
                new TableColumn<>("User");

        userCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue()[0]));

        TableColumn<String[], String> actionCol =
                new TableColumn<>("Action");

        actionCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue()[1]));

        TableColumn<String[], String> moduleCol =
                new TableColumn<>("Module");

        moduleCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue()[2]));

        TableColumn<String[], String> timeCol =
                new TableColumn<>("Time");

        timeCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue()[3]));

        table.getColumns().addAll(
                userCol,
                actionCol,
                moduleCol,
                timeCol
        );

        Button refreshBtn =
                new Button("Refresh");

        refreshBtn.setOnAction(e ->
                loadLogs(table, user, pass));

        loadLogs(table, user, pass);

        VBox root = new VBox(
                12,
                title,
                refreshBtn,
                table
        );

        root.setPadding(new Insets(20));

        return root;
    }

    private static void loadLogs(
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
                            .uri(java.net.URI.create(ApiConfig.BASE_URL +
                                    "/api/audit/logs"))
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

    private static void fillTable(
            TableView<String[]> table,
            String json) throws Exception {

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
                            row.path("username").asText(),
                            row.path("action").asText(),
                            row.path("module").asText(),
                            row.path("createdAt").asText()
                    }
            );
        }
    }
}