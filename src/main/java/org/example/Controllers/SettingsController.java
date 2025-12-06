package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Arrays;

public class SettingsController {

    @FXML
    private VBox rootPane;

    @FXML
    private Button addDocumentButton;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Button backButton;

    @FXML
    void initialize() {
        // Apply theme
        ThemeManager.applyTheme(rootPane, null, Arrays.asList(addDocumentButton, changePasswordButton, backButton),
                null);

        addDocumentButton.setOnAction(event -> switchScene("/org/example/fxml/AddDocument.fxml"));
        changePasswordButton.setOnAction(event -> switchScene("/org/example/fxml/ChangePassword.fxml"));
        backButton.setOnAction(event -> switchScene("/org/example/fxml/MainScreen.fxml"));
    }

    private void switchScene(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
