package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import org.example.storage.DatabaseManager;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initialize();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("messages", java.util.Locale.getDefault());
        var loader = new FXMLLoader(getClass().getResource("/org/example/fxml/MainScreen.fxml"), bundle);
        Parent rootPane = loader.load();

        var scene = new Scene(rootPane);
        stage.setTitle("JUMPSYS");
        stage.setScene(scene);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.show();
    }

    // WARNING: This main is used for running the app from terminal using `mvn
    // javafx:run`.
    // This main will not work when running from IntelliJ IDEA, hence use
    // IDELauncher.java instead.
    // Explanation: The issue is the Java 9+ module system that JavaFX requires.
    // However, modules are a nightmare to get working with maven, hence we do not
    // use them at this moment.
    // As a dirty but effective hack we can use another main method that calls this
    // one.
    // Hence, you should rather run IDELauncher.java
    public static void main(String[] args) {
        launch(args);
    }
}
