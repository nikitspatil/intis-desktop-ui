package com.idea.intis.ui.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/*
 ==========================================================
 DashboardView.java

 Dashboard Cards Required:
 1. Total Employees
 2. Active Employees
 3. Pune Branch Employees
 4. Mumbai Branch Employees

 Uses API:
 GET /api/dashboard/summary
 ==========================================================
*/

public class DashboardView {

    public static VBox getView(String user, String pass) {

        /* ---------------- Title ---------------- */
        Label title = new Label("Dashboard");
        title.setStyle(
                "-fx-font-size:24px;" +
                        "-fx-font-weight:bold;"
        );

        /* ---------------- Grid Layout ---------------- */
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        /* ---------------- Cards ---------------- */
        VBox totalCard =
                createCard("Employees");

        VBox activeCard =
                createCard("Active Employees");

        VBox puneCard =
                createCard("Pune Branch");

        VBox mumbaiCard =
                createCard("Mumbai Branch");

        grid.add(totalCard, 0, 0);
        grid.add(activeCard, 1, 0);
        grid.add(puneCard, 0, 1);
        grid.add(mumbaiCard, 1, 1);

        /* First load */
        loadSummary(
                user,
                pass,
                totalCard,
                activeCard,
                puneCard,
                mumbaiCard
        );

        /* Auto refresh every 15 sec */
        Timeline timeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(15),
                                e -> loadSummary(
                                        user,
                                        pass,
                                        totalCard,
                                        activeCard,
                                        puneCard,
                                        mumbaiCard
                                )
                        )
                );

        timeline.setCycleCount(
                Timeline.INDEFINITE
        );

        timeline.play();

        VBox root = new VBox(
                20,
                title,
                grid
        );

        root.setPadding(
                new Insets(20)
        );

        return root;
    }

    /*
     ======================================================
     Create One Dashboard Card
     ======================================================
    */
    private static VBox createCard(
            String titleText) {

        Label label =
                new Label(titleText);

        label.setStyle(
                "-fx-font-size:16px;"
        );

        Label value =
                new Label("0");

        value.setStyle(
                "-fx-font-size:34px;" +
                        "-fx-font-weight:bold;"
        );

        ProgressBar bar =
                new ProgressBar(0);

        bar.setPrefWidth(180);

        VBox card =
                new VBox(
                        10,
                        label,
                        value,
                        bar
                );

        card.setPadding(
                new Insets(20)
        );

        card.setPrefSize(240, 160);

        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:#dbeafe;" +
                        "-fx-border-radius:10;" +
                        "-fx-background-radius:10;"
        );

        return card;
    }

    /*
     ======================================================
     Load Dashboard Summary from Backend
     ======================================================
    */
    private static void loadSummary(
            String user,
            String pass,
            VBox totalCard,
            VBox activeCard,
            VBox puneCard,
            VBox mumbaiCard) {

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
                                    "http://localhost:8080/api/dashboard/summary"))
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
                            response.body()
                    );

            JsonNode data =
                    root.get("data");

            /* ---------------- Main Counts ---------------- */
            int totalEmployees =
                    data.path("totalEmployees")
                            .asInt();

            int activeEmployees =
                    data.path("activeEmployees")
                            .asInt();

            /* ---------------- Branch Counts ---------------- */
            int puneCount = 0;
            int mumbaiCount = 0;

            JsonNode branches =
                    data.path("branchSummary");

            for (JsonNode row : branches) {

                String branchName =
                        row.get(0).isNull()
                                ? ""
                                : row.get(0).asText();

                int count =
                        row.get(1).asInt();

                if (branchName.equalsIgnoreCase("Pune")) {
                    puneCount += count;
                }

                if (branchName.equalsIgnoreCase("Mumbai")) {
                    mumbaiCount += count;
                }
            }

            /* ---------------- Update Cards ---------------- */
            updateCard(
                    totalCard,
                    totalEmployees,
                    1.0
            );

            updateCard(
                    activeCard,
                    activeEmployees,
                    activeEmployees /
                            (double) totalEmployees
            );

            updateCard(
                    puneCard,
                    puneCount,
                    puneCount /
                            (double) totalEmployees
            );

            updateCard(
                    mumbaiCard,
                    mumbaiCount,
                    mumbaiCount /
                            (double) totalEmployees
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
     ======================================================
     Update Card Number + Progress
     ======================================================
    */
    private static void updateCard(
            VBox card,
            int valueNum,
            double progress) {

        Label value =
                (Label) card.getChildren()
                        .get(1);

        ProgressBar bar =
                (ProgressBar) card.getChildren()
                        .get(2);

        value.setText(
                String.valueOf(valueNum)
        );

        bar.setProgress(
                Math.min(progress, 1)
        );
    }
}