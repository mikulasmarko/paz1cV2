package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.model.Person;
import org.example.storage.DatabaseManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class AddAttendanceController {

    @FXML
    private VBox rootPane;

    @FXML
    private ComboBox<Person> personComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField startField;

    @FXML
    private TextField endField;

    @FXML
    private Button saveButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    private final DatabaseManager dbManager = new DatabaseManager();
    private List<Person> allEmployees;

    @FXML
    void initialize() {
        ThemeManager.applyTheme(rootPane, Arrays.asList(statusLabel), Arrays.asList(saveButton, backButton), null);

        // Load persons
        List<Person> persons = dbManager.searchPersons(""); // Get all
        // Filter only persons with positions (employees)
        allEmployees = persons.stream()
                .filter(p -> p.getPosition() != null && !p.getPosition().isEmpty())
                .toList();

        personComboBox.setItems(FXCollections.observableArrayList(allEmployees));
        personComboBox.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person object) {
                return object == null ? "" : object.getName() + " " + object.getSurname() + " (" + object.getId() + ")";
            }

            @Override
            public Person fromString(String string) {
                return null; // Not needed
            }
        });

        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterEmployees(newValue));

        datePicker.setValue(LocalDate.now());

        saveButton.setOnAction(event -> handleSave());
        backButton.setOnAction(event -> switchScene("/org/example/fxml/AttendanceManagement.fxml"));
    }

    private void handleSave() {
        Person selectedPerson = personComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String start = startField.getText().trim();
        String end = endField.getText().trim();

        if (selectedPerson == null || date == null || start.isEmpty()) {
            statusLabel.setText("Vyplňte povinné polia (Osoba, Dátum, Príchod).");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }

        if (dbManager.addAttendanceRecord(selectedPerson.getId(), date.toString(), start, end.isEmpty() ? null : end)) {
            statusLabel.setText("Záznam uložený.");
            statusLabel.setStyle("-fx-text-fill: green;");
            switchScene("/org/example/fxml/AttendanceManagement.fxml");
        } else {
            statusLabel.setText("Chyba pri ukladaní.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void switchScene(String fxmlPath) {
        try {
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("messages",
                    java.util.Locale.getDefault());
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath), bundle);
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void filterEmployees(String query) {
        if (query == null || query.isEmpty()) {
            personComboBox.setItems(FXCollections.observableArrayList(allEmployees));
        } else {
            String lowerQuery = query.toLowerCase();
            List<Person> filtered = allEmployees.stream()
                    .filter(p -> (p.getName() + " " + p.getSurname()).toLowerCase().contains(lowerQuery))
                    .toList();
            personComboBox.setItems(FXCollections.observableArrayList(filtered));
            personComboBox.getSelectionModel().selectFirst();
            personComboBox.show();
        }
    }
}
