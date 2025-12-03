package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class RegistrationScreenController {

    @FXML
    private Button cancelButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label registrationLabel;

    @FXML
    private HBox rootPane;

    @FXML
    void initialize() {
        cancelButton.setOnAction(event -> switchScene("/org/example/fxml/MainScreen.fxml"));
        nextButton.setOnAction(event -> switchScene("/org/example/fxml/Document.fxml"));
        java.util.List<Label> labels = java.util.Arrays.asList(registrationLabel);
        java.util.List<Button> buttons = java.util.Arrays.asList(cancelButton, nextButton);
        ThemeManager.applyTheme(rootPane, labels, buttons, null);
    }

    private void switchScene(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
