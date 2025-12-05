package org.example.Controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

public class DocumentRegistrationController {

    @FXML
    private Button cancelButton;

    @FXML
    private Button registerButton;

    @FXML
    private CheckBox documentSign;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox rootPane;

    @FXML
    void initialize() {
        registerButton.setOnAction(event -> switchScene("/org/example/fxml/RegistrationSuccess.fxml"));
        cancelButton.setOnAction(event -> switchScene("/org/example/fxml/RegistrationPage.fxml"));
        java.util.List<Button> buttons = java.util.Arrays.asList(cancelButton, registerButton);
        java.util.List<CheckBox> checkBoxes = java.util.Arrays.asList(documentSign);
        ThemeManager.applyTheme(rootPane, null, buttons, checkBoxes);

        try {
            // Load PDF document
            InputStream inputStream = getClass().getResourceAsStream("/org/example/documents/prevadzkovyPoriadok2025.pdf");
            if (inputStream == null) {
                System.err.println("PDF document not found!");
                return;
            }

            PDDocument document = PDDocument.load(inputStream);
            PDFRenderer renderer = new PDFRenderer(document);

            VBox pdfContainer = new VBox(10);
            pdfContainer.setAlignment(Pos.CENTER);
            pdfContainer.setPadding(new Insets(10));

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage bufferedImage = renderer.renderImageWithDPI(i, 200); // Render at 200 DPI
                WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                ImageView imageView = new ImageView(fxImage);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(600);

                pdfContainer.getChildren().add(imageView);
            }

            scrollPane.setContent(pdfContainer);
            document.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchScene(String fxmlPath) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
            Parent root = loader.load();
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
