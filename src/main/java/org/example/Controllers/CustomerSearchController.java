package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.model.Person;
import org.example.storage.DatabaseManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CustomerSearchController {

    @FXML
    private VBox rootPane;

    @FXML
    private Label titleLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Person> resultsTable;

    @FXML
    private TableColumn<Person, Number> idColumn;

    @FXML
    private TableColumn<Person, String> nameColumn;

    @FXML
    private TableColumn<Person, String> surnameColumn;

    @FXML
    private TableColumn<Person, String> emailColumn;

    @FXML
    private Button editButton;

    @FXML
    private Button backButton;

    @FXML
    void initialize() {
        ThemeManager.applyTheme(rootPane, Arrays.asList(titleLabel), Arrays.asList(editButton, backButton), null);

        // Configure Columns
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        surnameColumn.setCellValueFactory(cellData -> cellData.getValue().surnameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch(newValue));

        editButton.setOnAction(event -> handleEdit());
        backButton.setOnAction(event -> switchScene("/org/example/fxml/Settings.fxml"));
    }

    private void handleSearch(String query) {
        DatabaseManager db = new DatabaseManager();
        List<Person> results = db.searchPersons(query);
        ObservableList<Person> data = FXCollections.observableArrayList(results);
        resultsTable.setItems(data);
    }

    private void handleEdit() {
        Person selectedPerson = resultsTable.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            openEditScreen(selectedPerson);
        }
    }

    private void openEditScreen(Person person) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/fxml/EditCustomer.fxml"), bundle);
            Parent root = loader.load();

            EditCustomerController controller = loader.getController();
            controller.setPerson(person);

            Stage stage = (Stage) editButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
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
