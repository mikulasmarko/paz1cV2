package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.Label;

public class QRCodeLoginController {

    @FXML
    private Button cancelButton;

    @FXML
    private TextField inputNumber;

    @FXML
    private Button ducumentLoginButton;

    @FXML
    private Button loginButton;

    @FXML
    private Label loginLabel;

    @FXML
    private Label scanQrcodeLabel;

    @FXML
    private javafx.scene.layout.VBox rootPane;

    @FXML
    void initialize() {
        cancelButton.setOnAction(event -> switchScene("/org/example/fxml/MainScreen.fxml"));
        loginButton.setOnAction(event -> handleLogin());

        // Add listener for Enter key on the text field (simulates QR code scanner
        // ending with newline)
        inputNumber.setOnAction(event -> handleLogin());

        java.util.List<Label> labels = java.util.Arrays.asList(loginLabel, scanQrcodeLabel);
        java.util.List<Button> buttons = java.util.Arrays.asList(cancelButton, loginButton);
        ThemeManager.applyTheme(rootPane, labels, buttons, null);

    }

    private void handleLogin() {
        String input = inputNumber.getText();
        if (input != null && !input.trim().isEmpty()) {
            try {
                long personId = Long.parseLong(input.trim());
                org.example.storage.DatabaseManager dbManager = new org.example.storage.DatabaseManager();
                boolean exists = dbManager.personExists(personId);

                if (exists) {
                    System.out.println("User validated: " + personId);
                    inputNumber.clear();

                    java.util.List<String> positions = dbManager.getPersonPositions(personId);
                    if (positions != null && !positions.isEmpty()) {
                        switchScene("/org/example/fxml/attendance.fxml", personId);
                    } else {
                        switchScene("/org/example/fxml/DocumentLogin.fxml");
                    }
                } else {
                    System.out.println("Person with ID " + personId + " not found.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format: " + input);
            }
        }
    }

    private void switchScene(String fxmlPath) {
        switchScene(fxmlPath, -1);
    }

    private void switchScene(String fxmlPath, long personId) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();

            if (personId != -1 && fxmlPath.contains("attendance")) {
                AttendanceController controller = loader.getController();
                controller.setPersonId(personId);
            }

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
