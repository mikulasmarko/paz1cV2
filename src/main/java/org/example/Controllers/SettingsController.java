package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Arrays;

public class SettingsController {

    @FXML
    private VBox rootPane;

    @FXML
    private Button addDocumentButton;

    @FXML
    private Button manageDocumentsButton;

    @FXML
    private Button customerVisitsButton;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Button editCustomerButton;

    @FXML
    private Button managePositionsButton;

    @FXML
    private Button attendanceManagementButton;

    @FXML
    private Button exitButton;

    @FXML
    private Button backButton;

    @FXML
    void initialize() {
        // Apply theme
        ThemeManager.applyTheme(rootPane, null,
                Arrays.asList(addDocumentButton, manageDocumentsButton, customerVisitsButton, changePasswordButton,
                        editCustomerButton,
                        managePositionsButton,
                        attendanceManagementButton, backButton, exitButton),
                null);

        // Override style for exit button to be red
        exitButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        addDocumentButton.setOnAction(event -> switchScene("/org/example/fxml/AddDocument.fxml"));
        manageDocumentsButton.setOnAction(event -> switchScene("/org/example/fxml/ManageDocuments.fxml"));
        customerVisitsButton.setOnAction(event -> switchScene("/org/example/fxml/CustomerVisits.fxml"));
        changePasswordButton.setOnAction(event -> switchScene("/org/example/fxml/ChangePassword.fxml"));
        editCustomerButton.setOnAction(event -> switchScene("/org/example/fxml/CustomerSearch.fxml"));
        managePositionsButton.setOnAction(event -> switchScene("/org/example/fxml/ManagePositions.fxml"));
        attendanceManagementButton.setOnAction(event -> switchScene("/org/example/fxml/AttendanceManagement.fxml"));
        exitButton.setOnAction(event -> System.exit(0));
        backButton.setOnAction(event -> switchScene("/org/example/fxml/MainScreen.fxml"));
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
