package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class DocumentLoginController {

    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox documentSign;

    @FXML
    private Button ducumentLoginButton;

    @FXML
    private VBox rootPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    void initialize() {
        ducumentLoginButton.setOnAction(event -> switchScene("/org/example/fxml/LoginSuccess.fxml"));
        cancelButton.setOnAction(event -> switchScene("/org/example/fxml/QRCodeLogin.fxml"));
        java.util.List<Button> buttons = java.util.Arrays.asList(cancelButton, ducumentLoginButton);
        java.util.List<CheckBox> checkBoxes = java.util.Arrays.asList(documentSign);
        ThemeManager.applyTheme(rootPane, null, buttons, checkBoxes);
    }

    private void switchScene(String fxmlPath) {
        try {
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("messages", java.util.Locale.getDefault());
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath), bundle);
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) cancelButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }


}
