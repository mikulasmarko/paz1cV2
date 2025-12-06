package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.Document;
import org.example.storage.DatabaseManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class ManageDocumentsController {

    @FXML
    private VBox rootPane;

    @FXML
    private Label titleLabel;

    @FXML
    private TableView<Document> documentsTable;

    @FXML
    private TableColumn<Document, Long> idColumn;

    @FXML
    private TableColumn<Document, String> nameColumn;

    @FXML
    private TableColumn<Document, String> validFromColumn;

    @FXML
    private TableColumn<Document, String> validToColumn;

    @FXML
    private TableColumn<Document, String> languageColumn;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private DatabaseManager dbManager;

    @FXML
    void initialize() {
        dbManager = new DatabaseManager();

        // Setup columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        validFromColumn.setCellValueFactory(new PropertyValueFactory<>("validityFrom"));
        validToColumn.setCellValueFactory(new PropertyValueFactory<>("validityTo"));
        languageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));

        // Buttons
        backButton.setOnAction(event -> switchScene("/org/example/fxml/Settings.fxml"));
        deleteButton.setOnAction(event -> deleteSelectedDocument());

        // Theme
        ThemeManager.applyTheme(rootPane, Arrays.asList(titleLabel), Arrays.asList(backButton, deleteButton), null);
        // Override delete button to red
        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        refreshTable();
    }

    private void refreshTable() {
        documentsTable.getItems().clear();
        documentsTable.getItems().addAll(dbManager.getAllDocuments());
    }

    private void deleteSelectedDocument() {
        Document selected = documentsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dbManager.deleteDocument(selected.getId());
            refreshTable();
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
