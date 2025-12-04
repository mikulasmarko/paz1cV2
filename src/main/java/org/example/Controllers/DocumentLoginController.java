package org.example.Controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public class DocumentLoginController {

    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox documentSign;

    @FXML
    private Button ducumentLoginButton;

    @FXML
    private VBox rootPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    void initialize() {
        ducumentLoginButton.setOnAction(event -> switchScene("/org/example/fxml/LoginSuccess.fxml"));
        cancelButton.setOnAction(event -> switchScene("/org/example/fxml/QRCodeLogin.fxml"));
        java.util.List<Button> buttons = java.util.Arrays.asList(cancelButton, ducumentLoginButton);
        java.util.List<CheckBox> checkBoxes = java.util.Arrays.asList(documentSign);
        ThemeManager.applyTheme(rootPane, null, buttons, checkBoxes);

        try {
            // Load PDF document
            InputStream inputStream = getClass().getResourceAsStream("/org/example/documents/document.pdf");
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
                BufferedImage bufferedImage = renderer.renderImageWithDPI(i, 200);
                WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                ImageView imageView = new ImageView(fxImage);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(600);

                pdfContainer.getChildren().add(imageView);
            }

            scrollPane.setContent(pdfContainer);
            document.close();

        } catch (Exception e) {

        }
    }

    private void switchScene(String fxmlPath) {
        try {
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("messages",
                    java.util.Locale.getDefault());
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath), bundle);
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) cancelButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setFullScreen(true);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

}
