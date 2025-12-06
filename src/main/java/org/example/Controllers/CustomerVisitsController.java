package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.AttendanceRecord;
import org.example.storage.DatabaseManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class CustomerVisitsController {

    @FXML
    private VBox rootPane;

    @FXML
    private Label titleLabel;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TableView<AttendanceRecord> visitsTable;

    @FXML
    private TableColumn<AttendanceRecord, String> nameColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> surnameColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> startColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> endColumn;

    @FXML
    private Button backButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button addVisitButton;

    private DatabaseManager dbManager;

    @FXML
    void initialize() {
        dbManager = new DatabaseManager();

        // Setup columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));

        startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        startColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        startColumn.setOnEditCommit(event -> {
            String newTime = event.getNewValue();
            if (isValidTime(newTime)) {
                AttendanceRecord record = event.getRowValue();
                record.setStart(newTime);
                if (dbManager.updateAttendance(record.getIdAttendance(), record.getStart(), record.getEnd())) {
                    loadVisits(datePicker.getValue()); // Reload to sort
                }
            } else {
                showInvalidTimeAlert();
                visitsTable.refresh(); // Revert UI
            }
        });

        endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        endColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        endColumn.setOnEditCommit(event -> {
            String newTime = event.getNewValue();
            if (isValidTime(newTime)) {
                AttendanceRecord record = event.getRowValue();
                record.setEnd(newTime);
                if (dbManager.updateAttendance(record.getIdAttendance(), record.getStart(), record.getEnd())) {
                    loadVisits(datePicker.getValue()); // Reload to sort
                }
            } else {
                showInvalidTimeAlert();
                visitsTable.refresh(); // Revert UI
            }
        });

        // Setup date picker
        datePicker.setValue(LocalDate.now());
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> loadVisits(newValue));

        // Buttons
        backButton.setOnAction(event -> switchScene("/org/example/fxml/Settings.fxml"));
        addVisitButton.setOnAction(event -> switchScene("/org/example/fxml/AddCustomerVisit.fxml"));
        refreshButton.setOnAction(event -> loadVisits(datePicker.getValue()));

        // Theme
        ThemeManager.applyTheme(rootPane, Arrays.asList(titleLabel),
                Arrays.asList(backButton, addVisitButton, refreshButton), null);

        // Load initial data
        loadVisits(datePicker.getValue());
    }

    private boolean isValidTime(String time) {
        if (time == null || time.isEmpty())
            return true; // Empty is allowed for End
        // Matches HH:mm or HH:mm:ss
        return time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$");
    }

    private void showInvalidTimeAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Nesprávny formát času. Zadajte čas vo formáte HH:mm.",
                ButtonType.OK);
        alert.showAndWait();
    }

    private void loadVisits(LocalDate date) {
        if (date != null) {
            visitsTable.getItems().clear();
            visitsTable.getItems().addAll(dbManager.getVisitsByDate(date.toString()));
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
