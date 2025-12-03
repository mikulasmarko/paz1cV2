package org.example.Controllers;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginSuccessScreenController {

    @FXML
    private Label countdownLabel;

    @FXML
    private Label returnLabel;

    @FXML
    private VBox rootPane;

    @FXML
    private Label successLabel;

    private int secondsRemaining = 5;

    @FXML
    void initialize() {
        java.util.List<Label> labels = java.util.Arrays.asList(countdownLabel, returnLabel, successLabel);
        ThemeManager.applyTheme(rootPane, labels, null, null);
        startCountdown();
    }

    private void switchScene(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCountdown() {
        countdownLabel.setText(String.valueOf(secondsRemaining));

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsRemaining--;
            if (secondsRemaining >= 0) {
                countdownLabel.setText(String.valueOf(secondsRemaining));
            } else {
                switchScene("/org/example/fxml/MainScreen.fxml");
            }
        }));

        timeline.setCycleCount(6); // 5 seconds + 1 to trigger the switch
        timeline.play();
    }

}
