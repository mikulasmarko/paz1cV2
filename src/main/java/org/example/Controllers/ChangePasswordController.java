package org.example.Controllers;

import javafx.fxml.FXML;
// import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.storage.DatabaseManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class ChangePasswordController {

    @FXML
    private VBox rootPane;

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label titleLabel;

    @FXML
    private Label oldPasswordLabel;

    @FXML
    private Label newPasswordLabel;

    @FXML
    private Label confirmPasswordLabel;

    @FXML
    private Label errorLabel;

    @FXML
    void initialize() {
        ThemeManager.applyTheme(rootPane, Arrays.asList(titleLabel, oldPasswordLabel, newPasswordLabel, confirmPasswordLabel, errorLabel),
                Arrays.asList(saveButton, cancelButton), null);

        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> switchScene("/org/example/fxml/Settings.fxml"));
    }

    private void handleSave() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        DatabaseManager db = new DatabaseManager();
        String currentAdminPass = db.getAdminPassword();

        if (!currentAdminPass.equals(oldPass)) {
            showError(bundle.getString("error.old_password_incorrect"));
            return;
        }

        if (newPass.isEmpty()) {
            showError(bundle.getString("error.new_password_empty"));
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showError(bundle.getString("error.passwords_do_not_match"));
            return;
        }

        if (db.setAdminPassword(newPass)) {
            switchScene("/org/example/fxml/Settings.fxml");
        } else {
            showError(bundle.getString("error.password_save_failed"));
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void switchScene(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
