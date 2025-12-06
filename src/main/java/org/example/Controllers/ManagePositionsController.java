package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.storage.DatabaseManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ManagePositionsController {

    @FXML
    private VBox rootPane;

    @FXML
    private ListView<String> positionListView;

    @FXML
    private TextField newPositionField;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    @FXML
    private Label statusLabel;

    private final DatabaseManager dbManager = new DatabaseManager();

    @FXML
    void initialize() {
        ThemeManager.applyTheme(rootPane, null, Arrays.asList(addButton, deleteButton, backButton), null);
        loadPositions();

        addButton.setOnAction(event -> handleAdd());
        deleteButton.setOnAction(event -> handleDelete());
        backButton.setOnAction(event -> switchScene("/org/example/fxml/Settings.fxml"));
    }

    private void loadPositions() {
        List<String> positions = dbManager.getAllPositions();
        positionListView.getItems().setAll(positions);
    }

    private void handleAdd() {
        String newPos = newPositionField.getText();
        if (newPos != null && !newPos.trim().isEmpty()) {
            if (dbManager.addPosition(newPos)) {
                newPositionField.clear();
                loadPositions();
                statusLabel.setText("Pozícia pridaná.");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Chyba pri pridaní (duplikát?).");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void handleDelete() {
        String selected = positionListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (dbManager.deletePosition(selected)) {
                loadPositions();
                statusLabel.setText("Pozícia vymazaná.");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("Nemožno vymazať (asi sa používa).");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            statusLabel.setText("Označte pozíciu na vymazanie.");
            statusLabel.setStyle("-fx-text-fill: orange;");
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
