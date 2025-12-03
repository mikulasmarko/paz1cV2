package org.example.Controllers;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.control.CheckBox;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class ThemeManager {

    private static boolean isDarkTheme = false;

    public static void toggleTheme() {
        isDarkTheme = !isDarkTheme;
    }

    public static boolean isDarkTheme() {
        return isDarkTheme;
    }

    public static void applyTheme(Region root, List<Label> labels, List<Button> buttons, List<CheckBox> checkBoxes) {
        String themeFile;
        if (isDarkTheme)
            themeFile = "/theme_dark.properties";
        else
            themeFile = "/theme_light.properties";
        Properties props = new Properties();
        try (InputStream input = ThemeManager.class.getResourceAsStream(themeFile)) {
            if (input == null) {
                System.out.println("Sorry, unable to find " + themeFile);
                return;
            }
            props.load(input);

            String backgroundColor = props.getProperty("background.color");
            String textColor = props.getProperty("text.color");
            String buttonBackground = props.getProperty("button.background");
            String buttonText = props.getProperty("button.text");

            if (root != null) {
                root.setStyle("-fx-background-color: " + backgroundColor + ";");
            }

            String textStyle = "-fx-text-fill: " + textColor + ";";
            if (labels != null) {
                for (Label label : labels) {
                    if (label != null) {
                        label.setStyle(textStyle);
                    }
                }
            }

            String buttonStyle = "-fx-background-color: " + buttonBackground + "; -fx-text-fill: " + buttonText + ";";
            if (buttons != null) {
                for (Button button : buttons) {
                    if (button != null) {
                        button.setStyle(buttonStyle);
                    }
                }
            }

            String checkBoxStyle = "-fx-background-color: " + buttonBackground + "; -fx-text-fill: " + buttonText + ";";
            if (checkBoxes != null) {
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox != null) {
                        checkBox.setStyle(checkBoxStyle);
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
