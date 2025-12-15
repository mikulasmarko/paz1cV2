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
    private Label titleLabel;

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
        ThemeManager.applyTheme(rootPane, Arrays.asList(titleLabel, statusLabel), Arrays.asList(addButton, deleteButton, backButton), null);
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
                ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
                statusLabel.setText(bundle.getString("msg.position_added"));
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
                statusLabel.setText(bundle.getString("error.position_add_failed"));
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    private void handleDelete() {
        String selected = positionListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (dbManager.deletePosition(selected)) {
                loadPositions();
                ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
                statusLabel.setText(bundle.getString("msg.position_deleted"));
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
                statusLabel.setText(bundle.getString("error.position_delete_failed"));
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            statusLabel.setText(bundle.getString("msg.select_position_delete"));
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
