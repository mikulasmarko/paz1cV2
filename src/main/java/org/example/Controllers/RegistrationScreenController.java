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
    private javafx.scene.control.TextField firstNameField;

    @FXML
    private javafx.scene.control.TextField lastNameField;

    @FXML
    private javafx.scene.control.TextField emailField;

    @FXML
    private javafx.scene.control.TextField phoneField;

    @FXML
    private javafx.scene.control.DatePicker birthDateField;

    @FXML
    private javafx.scene.control.Label errorLabel;

    @FXML
    void initialize() {
        cancelButton.setOnAction(event -> switchScene("/org/example/fxml/MainScreen.fxml"));
        nextButton.setOnAction(event -> {
            if (validateFields()) {
                org.example.storage.DatabaseManager dbManager = new org.example.storage.DatabaseManager();
                if (dbManager.registerPerson(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        emailField.getText(),
                        phoneField.getText(),
                        birthDateField.getValue())) {
                    switchScene("/org/example/fxml/DocumentRegistration.fxml");
                } else {
                    showError("error.registration_failed"); // You might need to add this key or reuse an existing one,
                                                            // or just show generic error
                }
            }
        });
        java.util.List<Label> labels = java.util.Arrays.asList(registrationLabel);
        java.util.List<Button> buttons = java.util.Arrays.asList(cancelButton, nextButton);
        ThemeManager.applyTheme(rootPane, labels, buttons, null);
    }

    private boolean validateFields() {
        if (firstNameField.getText().isEmpty() ||
                lastNameField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                phoneField.getText().isEmpty() ||
                birthDateField.getValue() == null) {

            showError("error.all_fields_required");
            return false;
        }

        if (!emailField.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("error.invalid_email");
            return false;
        }

        if (!phoneField.getText().matches("^\\+?[0-9]{10,13}$")) {
            showError("error.invalid_phone");
            return false;
        }

        if (new org.example.storage.DatabaseManager().isEmailExists(emailField.getText())) {
            showError("error.email_exists");
            return false;
        }

        errorLabel.setVisible(false);
        return true;
    }

    private void showError(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        errorLabel.setText(bundle.getString(key));
        errorLabel.setVisible(true);
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
