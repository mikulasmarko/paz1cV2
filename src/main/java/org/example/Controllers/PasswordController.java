package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class PasswordController {

    @FXML
    private VBox rootPane;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button btnEnter;

    @FXML
    private Button btnBack;

    @FXML
    private Label errorLabel;

    @FXML
    void initialize() {
        ThemeManager.applyTheme(rootPane, null, Arrays.asList(btnEnter, btnBack), null);

        btnEnter.setOnAction(event -> handleLogin());
        btnBack.setOnAction(event -> switchScene("/org/example/fxml/MainScreen.fxml"));

        // Also enter on pressing Enter key in field
        passwordField.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        String password = passwordField.getText();

        org.example.storage.DatabaseManager db = new org.example.storage.DatabaseManager();
        String adminPass = db.getAdminPassword();

        if (adminPass.equals(password)) {
            switchScene("/org/example/fxml/Settings.fxml");
        } else {
            errorLabel.setText("Nesprávne heslo!");
            passwordField.clear();
        }
    }

    private void switchScene(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) btnEnter.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
