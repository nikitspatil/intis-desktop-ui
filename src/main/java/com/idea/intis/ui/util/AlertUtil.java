package com.idea.intis.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/*
 ==========================================================
 AlertUtil.java

 Shared popup alerts for entire application.

 Types:
 - success
 - info
 - warning
 - error
 - confirm
 ==========================================================
*/

public class AlertUtil {

    /*
     ======================================================
     Success Message
     ======================================================
    */
    public static void success(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /*
     ======================================================
     Info Message
     ======================================================
    */
    public static void info(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /*
     ======================================================
     Warning Message
     ======================================================
    */
    public static void warning(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /*
     ======================================================
     Error Message
     ======================================================
    */
    public static void error(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /*
     ======================================================
     Confirm Dialog
     Returns true if user clicks OK
     ======================================================
    */
    public static boolean confirm(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle("Confirm");
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result =
                alert.showAndWait();

        return result.isPresent()
                && result.get()
                == ButtonType.OK;
    }
}