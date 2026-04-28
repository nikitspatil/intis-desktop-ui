package com.idea.intis.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idea.intis.ui.config.ApiConfig;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/*
 ==========================================================
 PresenceView.java

 Responsibility:
 - Show live employee presence screen
 - Display ONLINE / OFFLINE / AWAY users
 - Auto refresh every 10 seconds
 - Manual refresh button
 - Color coded statuses

 API Used:
 GET /api/presence/status
 ==========================================================
*/

public class PresenceView {

    /*
     ======================================================
     Main Presence Page
     Called from AppShell when user clicks Presence
     ======================================================
    */
    public static VBox getView(String user, String pass) {

        /* ---------------- Page Title ---------------- */
        Label title = new Label("Presence Monitor");

        title.setStyle(
                "-fx-font-size:22px;" +
                        "-fx-font-weight:bold;"
        );

        /* ---------------- Table ---------------- */
        TableView<String[]> table =
                new TableView<>();

        table.setPrefHeight(520);

        /*
         ------------------------------------------
         Column 1 : Employee Name
         ------------------------------------------
        */
        TableColumn<String[], String> nameCol =
                new TableColumn<>("Employee");

        nameCol.setPrefWidth(240);

        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[0]
                ));

        /*
         ------------------------------------------
         Column 2 : Status
         ONLINE / OFFLINE / AWAY
         ------------------------------------------
        */
        TableColumn<String[], String> statusCol =
                new TableColumn<>("Status");

        statusCol.setPrefWidth(180);

        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[1]
                ));

        /*
         Color coded status text
        */
        statusCol.setCellFactory(col ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );

                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                            return;
                        }

                        setText(item);

                        if (item.equalsIgnoreCase("ONLINE")) {

                            setStyle(
                                    "-fx-text-fill:green;" +
                                            "-fx-font-weight:bold;"
                            );

                        } else if (item.equalsIgnoreCase("OFFLINE")) {

                            setStyle(
                                    "-fx-text-fill:red;" +
                                            "-fx-font-weight:bold;"
                            );

                        } else {

                            setStyle(
                                    "-fx-text-fill:orange;" +
                                            "-fx-font-weight:bold;"
                            );
                        }
                    }
                });

        /*
         ------------------------------------------
         Column 3 : Last Seen
         ------------------------------------------
        */
        TableColumn<String[], String> lastSeenCol =
                new TableColumn<>("Last Seen");

        lastSeenCol.setPrefWidth(260);

        lastSeenCol.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[2]
                ));

        table.getColumns().addAll(
                nameCol,
                statusCol,
                lastSeenCol
        );

        /* ---------------- Refresh Button ---------------- */
        Button refreshBtn =
                new Button("Refresh");

        refreshBtn.setOnAction(e ->
                loadPresence(
                        table,
                        user,
                        pass
                ));

        /* First Load */
        loadPresence(table, user, pass);

        /*
         ------------------------------------------
         Auto Refresh every 10 seconds
         ------------------------------------------
        */
        Timeline timeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(10),
                                e -> loadPresence(
                                        table,
                                        user,
                                        pass
                                )
                        )
                );

        timeline.setCycleCount(
                Timeline.INDEFINITE
        );

        timeline.play();

        /* ---------------- Final Layout ---------------- */
        VBox root =
                new VBox(
                        12,
                        title,
                        refreshBtn,
                        table
                );

        root.setPadding(
                new Insets(20)
        );

        return root;
    }

    /*
     ======================================================
     Load Presence Data From Backend
     ======================================================
    */
    private static void loadPresence(
            TableView<String[]> table,
            String user,
            String pass) {

        table.getItems().clear();

        try {
            var client =
                    java.net.http.HttpClient
                            .newHttpClient();

            /*
             Basic Authentication
            */
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
                                    "/api/presence/status"))
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
     Convert JSON Response To Table Rows
     Supports multiple name field structures
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

            /*
             --------------------------------------
             Employee Name Fallback Logic
             Different APIs may return different names
             --------------------------------------
            */
            String employeeName =
                    row.path("fullName").asText();

            if (employeeName.isEmpty()) {
                employeeName =
                        row.path("employeeName").asText();
            }

            if (employeeName.isEmpty()) {
                employeeName =
                        row.path("username").asText();
            }

            if (employeeName.isEmpty()) {
                employeeName =
                        row.path("name").asText();
            }

            /*
             Nested Object Support
            */
            if (employeeName.isEmpty()) {
                employeeName =
                        row.path("employee")
                                .path("fullName")
                                .asText();
            }

            if (employeeName.isEmpty()) {
                employeeName =
                        row.path("user")
                                .path("fullName")
                                .asText();
            }

            /* Status */
            String status =
                    row.path("status").asText();

            /* Last Seen */
            String lastSeen =
                    row.path("lastSeen").asText();

            table.getItems().add(
                    new String[]{
                            employeeName,
                            status,
                            lastSeen
                    }
            );
        }
    }
}