package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.model.Person;
import org.example.storage.DatabaseManager;
import org.example.QrCode.QrCodeGeneratorDemo;
import org.example.eMail.emailSender;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class EditCustomerController {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField dobField;

    @FXML
    private javafx.scene.control.ComboBox<String> availablePositionsCombo;

    @FXML
    private Button addPositionButton;

    @FXML
    private javafx.scene.control.ListView<String> currentPositionsListView;

    @FXML
    private Button removePositionButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button resendQrButton;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;

    private Person person;

    @FXML
    void initialize() {
        ThemeManager.applyTheme(rootPane, null,
                Arrays.asList(saveButton, resendQrButton, backButton, addPositionButton, removePositionButton), null);

        saveButton.setOnAction(event -> handleSave());
        resendQrButton.setOnAction(event -> handleResendQR());
        backButton.setOnAction(event -> switchScene("/org/example/fxml/CustomerSearch.fxml"));

        addPositionButton.setOnAction(event -> handleAddPosition());
        removePositionButton.setOnAction(event -> handleRemovePosition());

        loadAvailablePositions();
    }

    private void loadAvailablePositions() {
        DatabaseManager db = new DatabaseManager();
        java.util.List<String> positions = db.getAllPositions();
        availablePositionsCombo.getItems().setAll(positions);
    }

    public void setPerson(Person person) {
        this.person = person;
        nameField.setText(person.getName());
        surnameField.setText(person.getSurname());
        emailField.setText(person.getEmail());
        phoneField.setText(person.getPhone());
        dobField.setText(person.getDateOfBirth());
        loadPersonPositions();
    }

    private void loadPersonPositions() {
        if (person == null)
            return;
        DatabaseManager db = new DatabaseManager();
        java.util.List<String> positions = db.getPersonPositions(person.getId());
        currentPositionsListView.getItems().setAll(positions);
    }

    private void handleSave() {
        if (person == null)
            return;

        person.setName(nameField.getText());
        person.setSurname(surnameField.getText());
        person.setEmail(emailField.getText());
        person.setPhone(phoneField.getText());
        person.setDateOfBirth(dobField.getText());
        // Position is handled separately now

        DatabaseManager db = new DatabaseManager();
        if (db.updatePerson(person)) {
            switchScene("/org/example/fxml/CustomerSearch.fxml");
        } else {
            errorLabel.setText("Chyba pri ukladaní údajov.");
        }
    }

    private void handleResendQR() {
        if (person == null)
            return;

        long personId = person.getId();
        String qrPath = "src/main/resources/qrCodesGenerated/" + personId + ".png";
        QrCodeGeneratorDemo.generateQRCode(String.valueOf(personId), qrPath);

        String body = "Dobrý deň " + person.getName() + " " + person.getSurname() + ",\n\n" +
                "Posielame Vám nový QR kód.\n\n" +
                "S pozdravom,\n" +
                "Tím Jump Arena";

        emailSender.sendEmail(person.getEmail(), "Nový QR kód - Jump Arena", body, qrPath);

        File qrFile = new File(qrPath);
        if (qrFile.exists()) {
            qrFile.delete();
        }

        // Visual feedback? For now, maybe change button text temporarily or just update
        // label
        errorLabel.setStyle("-fx-text-fill: green;");
        errorLabel.setText("QR kód bol odoslaný.");
    }

    private void handleAddPosition() {
        if (person == null)
            return;
        String selected = availablePositionsCombo.getValue();
        if (selected != null) {
            DatabaseManager db = new DatabaseManager();
            if (db.addPersonPosition(person.getId(), selected)) {
                loadPersonPositions();
                errorLabel.setText("Pozícia pridaná.");
                errorLabel.setStyle("-fx-text-fill: green;");
            } else {
                errorLabel.setText("Chyba - asi už existuje.");
                errorLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void handleRemovePosition() {
        if (person == null)
            return;
        String selected = currentPositionsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            DatabaseManager db = new DatabaseManager();
            if (db.removePersonPosition(person.getId(), selected)) {
                loadPersonPositions();
                errorLabel.setText("Pozícia odobraná.");
                errorLabel.setStyle("-fx-text-fill: green;");
            } else {
                errorLabel.setText("Chyba pri odoberaní.");
                errorLabel.setStyle("-fx-text-fill: red;");
            }
        }
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
