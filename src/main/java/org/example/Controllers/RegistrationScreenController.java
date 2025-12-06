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
    private javafx.scene.control.ComboBox<Integer> dayBox;

    @FXML
    private javafx.scene.control.ComboBox<Integer> monthBox;

    @FXML
    private javafx.scene.control.ComboBox<Integer> yearBox;

    @FXML
    private javafx.scene.control.Label errorLabel;

    @FXML
    void initialize() {
        cancelButton.setOnAction(event -> switchScene("/org/example/fxml/MainScreen.fxml"));
        nextButton.setOnAction(event -> {
            if (validateFields()) {
                switchSceneWithData("/org/example/fxml/DocumentRegistration.fxml");
            }
        });

        // Populate Date Pickers
        javafx.collections.ObservableList<Integer> days = javafx.collections.FXCollections.observableArrayList();
        for (int i = 1; i <= 31; i++)
            days.add(i);
        dayBox.setItems(days);

        javafx.collections.ObservableList<Integer> months = javafx.collections.FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++)
            months.add(i);
        monthBox.setItems(months);

        javafx.collections.ObservableList<Integer> years = javafx.collections.FXCollections.observableArrayList();
        int currentYear = java.time.LocalDate.now().getYear();
        for (int i = currentYear; i >= 1900; i--)
            years.add(i);
        yearBox.setItems(years);

        // Set localized prompts
        ResourceBundle bundle = ResourceBundle.getBundle("messages", java.util.Locale.getDefault());
        if (bundle.containsKey("prompt.day"))
            dayBox.setPromptText(bundle.getString("prompt.day"));
        if (bundle.containsKey("prompt.month"))
            monthBox.setPromptText(bundle.getString("prompt.month"));
        if (bundle.containsKey("prompt.year"))
            yearBox.setPromptText(bundle.getString("prompt.year"));

        java.util.List<Label> labels = java.util.Arrays.asList(registrationLabel);
        java.util.List<Button> buttons = java.util.Arrays.asList(cancelButton, nextButton);
        ThemeManager.applyTheme(rootPane, labels, buttons, null);
    }

    private boolean validateFields() {
        if (firstNameField.getText().isEmpty() ||
                lastNameField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                phoneField.getText().isEmpty() ||
                dayBox.getValue() == null ||
                monthBox.getValue() == null ||
                yearBox.getValue() == null) {

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

        try {
            int d = dayBox.getValue();
            int m = monthBox.getValue();
            int y = yearBox.getValue();
            java.time.LocalDate.of(y, m, d); // Validate date
        } catch (java.time.DateTimeException e) {
            errorLabel.setText("Neplatný dátum.");
            errorLabel.setVisible(true);
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

    private void switchSceneWithData(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();

            int d = dayBox.getValue();
            int m = monthBox.getValue();
            int y = yearBox.getValue();
            java.time.LocalDate birthDate = java.time.LocalDate.of(y, m, d);

            // Pass data to DocumentRegistrationController
            DocumentRegistrationController controller = loader.getController();
            controller.setPersonData(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    birthDate);

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
