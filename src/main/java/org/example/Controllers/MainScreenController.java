package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;

public class MainScreenController {

    @FXML
    private Button loginButton;

    @FXML
    private Button registrationButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button languageButton;

    @FXML
    private Button themeButton;

    @FXML
    private VBox rootPane;

    @FXML
    private Label labelOperatingRules;

    @FXML
    private Label labelJumpArena;

    @FXML
    void initialize() {
        loginButton.setOnAction(event -> switchScene("/org/example/fxml/QRCodeLogin.fxml"));
        registrationButton.setOnAction(event -> switchScene("/org/example/fxml/RegistrationPage.fxml"));
        languageButton.setOnAction(event -> toggleLanguage());
        themeButton.setOnAction(event -> toggleTheme());
        applyTheme();
        updateLanguageButtonIcon();
    }

    private void switchScene(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toggleLanguage() {
        Locale currentLocale = Locale.getDefault();
        if (currentLocale.getLanguage().equals("sk")) {
            Locale.setDefault(Locale.ENGLISH);
        } else {
            Locale.setDefault(Locale.forLanguageTag("sk"));
        }
        // Reload current scene to apply language change
        switchScene("/org/example/fxml/MainScreen.fxml");
    }

    private void updateLanguageButtonIcon() {
        Locale currentLocale = Locale.getDefault();
        String imagePath;
        if (currentLocale.getLanguage().equals("sk")) {
            imagePath = "/org/example/flags/ukFlag.png";
        } else {
            imagePath = "/org/example/flags/skFlag.png";
        }

        try {
            Image flagImage = new Image(getClass().getResourceAsStream(imagePath));
            ImageView flagImageView = new ImageView(flagImage);
            flagImageView.setFitWidth(40);
            flagImageView.setFitHeight(40);
            languageButton.setGraphic(flagImageView);
            languageButton.setText(""); // Remove text to only show the icon
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to text if image loading fails
            languageButton.setText(currentLocale.getLanguage());
        }
    }

    private void toggleTheme() {
        ThemeManager.toggleTheme();
        applyTheme();
    }

    private void applyTheme() {
        java.util.List<Label> labels = java.util.Arrays.asList(labelOperatingRules, labelJumpArena);
        java.util.List<Button> buttons = java.util.Arrays.asList(loginButton, registrationButton, settingsButton,
                languageButton, themeButton);
        ThemeManager.applyTheme(rootPane, labels, buttons, null);
    }
}
