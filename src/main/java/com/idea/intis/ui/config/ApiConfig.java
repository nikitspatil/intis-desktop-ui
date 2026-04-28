package com.idea.intis.ui.config;

/*
 ==========================================================
 ApiConfig.java

 Central place for backend URL and app settings.
 Change here once, applies everywhere.
 ==========================================================
*/

public class ApiConfig {

    /* Backend Base URL */
    public static final String BASE_URL =
            "http://192.168.1.13:8080";

    /* API Timeout Seconds */
    public static final int TIMEOUT_SECONDS = 15;

    private ApiConfig() {
        /* Prevent object creation */
    }
}