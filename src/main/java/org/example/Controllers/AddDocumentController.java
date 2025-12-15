package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.storage.DatabaseManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class AddDocumentController {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField nameField;

    @FXML
    private TextField languageField;

    @FXML
    private DatePicker validFromPicker;

    @FXML
    private DatePicker validToPicker;

    @FXML
    private TextField filePathField;

    @FXML
    private Button chooseFileButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label languageLabel;

    @FXML
    private Label validFromLabel;

    @FXML
    private Label validToLabel;

    @FXML
    private Label fileLabel;

    @FXML
    void initialize() {
        ThemeManager.applyTheme(rootPane,
                Arrays.asList(titleLabel, nameLabel, languageLabel, validFromLabel, validToLabel, fileLabel),
                Arrays.asList(chooseFileButton, saveButton, cancelButton),
                null);

        chooseFileButton.setOnAction(event -> chooseFile());
        saveButton.setOnAction(event -> saveDocument());
        cancelButton.setOnAction(event -> switchScene("/org/example/fxml/Settings.fxml"));
    }

    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vyberte PDF dokument");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void saveDocument() {
        String name = nameField.getText();
        String language = languageField.getText();
        String filePath = filePathField.getText();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        if (name.isEmpty() || language.isEmpty() || filePath.isEmpty() || validFromPicker.getValue() == null) {
            errorLabel.setText(bundle.getString("error.missing_fields"));
            return;
        }

        String validFrom = validFromPicker.getValue().toString();
        String validTo = validToPicker.getValue() != null ? validToPicker.getValue().toString() : null;

        DatabaseManager db = new DatabaseManager();
        if (db.insertDocument(name, filePath, language, validFrom, validTo)) {
            // Success - maybe show confirmation or just redirect?
            // Since requirements moved away from popups, let's just redirect.
            System.out.println(bundle.getString("msg.document_saved"));
            switchScene("/org/example/fxml/Settings.fxml");
        } else {
            errorLabel.setText(bundle.getString("error.database_save"));
        }
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
