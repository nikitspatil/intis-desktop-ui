package com.idea.intis.ui.service;

import com.idea.intis.ui.config.ApiConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/*
 ==========================================================
 ApiClient.java

 Shared HTTP client for all screens.

 Supports:
 - GET
 - POST
 - PUT

 Adds:
 - Basic auth
 - timeout
 - central base url
 ==========================================================
*/

public class ApiClient {

    private static final HttpClient client =
            HttpClient.newBuilder()
                    .connectTimeout(
                            Duration.ofSeconds(
                                    ApiConfig.TIMEOUT_SECONDS
                            )
                    )
                    .build();

    /*
     ======================================================
     Build Basic Auth Header
     ======================================================
    */
    private static String auth(
            String user,
            String pass) {

        return "Basic " +
                Base64.getEncoder()
                        .encodeToString(
                                (user + ":" + pass)
                                        .getBytes()
                        );
    }

    /*
     ======================================================
     GET Request
     ======================================================
    */
    public static String get(
            String endpoint,
            String user,
            String pass) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                ApiConfig.BASE_URL +
                                        endpoint
                        ))
                        .timeout(Duration.ofSeconds(
                                ApiConfig.TIMEOUT_SECONDS
                        ))
                        .header(
                                "Authorization",
                                auth(user, pass)
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse
                                .BodyHandlers
                                .ofString()
                );

        return response.body();
    }

    /*
     ======================================================
     POST Request
     ======================================================
    */
    public static String post(
            String endpoint,
            String json,
            String user,
            String pass) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                ApiConfig.BASE_URL +
                                        endpoint
                        ))
                        .timeout(Duration.ofSeconds(
                                ApiConfig.TIMEOUT_SECONDS
                        ))
                        .header(
                                "Authorization",
                                auth(user, pass)
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest
                                        .BodyPublishers
                                        .ofString(json)
                        )
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse
                                .BodyHandlers
                                .ofString()
                );

        return response.body();
    }

    /*
     ======================================================
     PUT Request
     ======================================================
    */
    public static String put(
            String endpoint,
            String json,
            String user,
            String pass) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                ApiConfig.BASE_URL +
                                        endpoint
                        ))
                        .timeout(Duration.ofSeconds(
                                ApiConfig.TIMEOUT_SECONDS
                        ))
                        .header(
                                "Authorization",
                                auth(user, pass)
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .PUT(
                                HttpRequest
                                        .BodyPublishers
                                        .ofString(json)
                        )
                        .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse
                                .BodyHandlers
                                .ofString()
                );

        return response.body();
    }
}