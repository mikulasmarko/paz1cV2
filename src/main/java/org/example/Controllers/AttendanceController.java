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
    private Label titleLabel;

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
        ThemeManager.applyTheme(rootPane, Arrays.asList(titleLabel, welcomeLabel, statusLabel),
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
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        welcomeLabel.setText(bundle.getString("label.welcome") + ", ID: " + personId);
    }

    private void handleArrival() {
        DatabaseManager db = new DatabaseManager();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        if (db.recordAttendance(personId)) {
            statusLabel.setText(bundle.getString("msg.arrival_recorded"));
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText(bundle.getString("error.arrival_failed"));
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleDeparture() {
        DatabaseManager db = new DatabaseManager();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        if (db.recordDeparture(personId)) {
            statusLabel.setText(bundle.getString("msg.departure_recorded"));
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText(bundle.getString("error.no_arrival"));
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
