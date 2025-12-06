package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.storage.DatabaseManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class AttendanceController {

    @FXML
    private VBox rootPane;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button arrivalButton;

    @FXML
    private Button departureButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label statusLabel;

    private long personId;

    @FXML
    void initialize() {
        ThemeManager.applyTheme(rootPane, Arrays.asList(welcomeLabel),
                Arrays.asList(arrivalButton, departureButton, logoutButton), null);

        arrivalButton.setOnAction(event -> handleArrival());
        departureButton.setOnAction(event -> handleDeparture());
        logoutButton.setOnAction(event -> handleLogout());
    }

    public void setPersonId(long personId) {
        this.personId = personId;
        // Optionally fetch person name to greet properly
        // DatabaseManager db = new DatabaseManager();
        // Person p = db.getPerson(personId); ...
        welcomeLabel.setText("Vitajte, ID: " + personId);
    }

    private void handleArrival() {
        DatabaseManager db = new DatabaseManager();
        if (db.recordAttendance(personId)) {
            statusLabel.setText("Príchod zaznamenaný.");
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText("Chyba pri zázname príchodu.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleDeparture() {
        DatabaseManager db = new DatabaseManager();
        if (db.recordDeparture(personId)) {
            statusLabel.setText("Odchod zaznamenaný.");
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText("Nie je otvorený žiaden príchod.");
            statusLabel.setStyle("-fx-text-fill: orange;");
        }
    }

    private void handleLogout() {
        switchScene("/org/example/fxml/MainScreen.fxml");
    }

    private void switchScene(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
