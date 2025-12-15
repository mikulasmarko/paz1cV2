package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.model.AttendanceRecord;
import org.example.storage.DatabaseManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AttendanceManagementController {

    @FXML
    private VBox rootPane;

    @FXML
    private Label titleLabel;

    @FXML
    private Label selectMonthLabel;

    @FXML
    private DatePicker monthPicker;

    @FXML
    private Button refreshButton;

    @FXML
    private TableView<AttendanceRecord> attendanceTable;

    @FXML
    private TableColumn<AttendanceRecord, String> nameColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> surnameColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> dayColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> startColumn;

    @FXML
    private TableColumn<AttendanceRecord, String> endColumn;

    @FXML
    private Button generatePdfButton;

    @FXML
    private Button addRecordButton;

    @FXML
    private Button deleteRecordButton;

    @FXML
    private Button backButton;

    @FXML
    private Label statusLabel;

    private final DatabaseManager dbManager = new DatabaseManager();

    @FXML
    void initialize() {
        ThemeManager.applyTheme(rootPane, Arrays.asList(titleLabel, selectMonthLabel, statusLabel),
                Arrays.asList(refreshButton, generatePdfButton, addRecordButton, deleteRecordButton, backButton), null);

        monthPicker.setValue(LocalDate.now());

        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        surnameColumn.setCellValueFactory(cellData -> cellData.getValue().surnameProperty());
        dayColumn.setCellValueFactory(cellData -> cellData.getValue().dayProperty());

        startColumn.setCellValueFactory(cellData -> cellData.getValue().startProperty());
        startColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        startColumn.setOnEditCommit(event -> {
            String newTime = event.getNewValue();
            if (isValidTime(newTime)) {
                AttendanceRecord record = event.getRowValue();
                record.setStart(newTime);
                updateRecord(record);
            } else {
                showInvalidTimeAlert();
                attendanceTable.refresh();
            }
        });

        endColumn.setCellValueFactory(cellData -> cellData.getValue().endProperty());
        endColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        endColumn.setOnEditCommit(event -> {
            String newTime = event.getNewValue();
            if (isValidTime(newTime)) {
                AttendanceRecord record = event.getRowValue();
                record.setEnd(newTime);
                updateRecord(record);
            } else {
                showInvalidTimeAlert();
                attendanceTable.refresh();
            }
        });

        refreshButton.setOnAction(event -> loadData());
        generatePdfButton.setOnAction(event -> generatePdf());
        addRecordButton.setOnAction(event -> openAddDialog());
        deleteRecordButton.setOnAction(event -> deleteSelectedRecord());
        backButton.setOnAction(event -> switchScene("/org/example/fxml/Settings.fxml"));

        loadData();
    }

    private boolean isValidTime(String time) {
        if (time == null || time.isEmpty())
            return true; // Empty is allowed for End
        // Matches HH:mm or HH:mm:ss
        return time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$");
    }

    private void showInvalidTimeAlert() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        new Alert(Alert.AlertType.ERROR, bundle.getString("error.invalid_time_format"), ButtonType.OK)
                .showAndWait();
    }

    private void openAddDialog() {
        switchScene("/org/example/fxml/AddAttendance.fxml");
    }

    private void deleteSelectedRecord() {
        AttendanceRecord selected = attendanceTable.getSelectionModel().getSelectedItem();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        if (selected == null) {
            statusLabel.setText(bundle.getString("msg.select_record_delete"));
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }

        if (dbManager.deleteAttendance(selected.getIdAttendance())) {
            statusLabel.setText(bundle.getString("msg.record_deleted"));
            statusLabel.setStyle("-fx-text-fill: green;");
            loadData();
        } else {
            statusLabel.setText(bundle.getString("error.delete_failed"));
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void loadData() {
        LocalDate selectedDate = monthPicker.getValue();
        if (selectedDate == null)
            return;

        List<AttendanceRecord> records = dbManager.getAttendance(selectedDate.getMonthValue(), selectedDate.getYear());
        ObservableList<AttendanceRecord> data = FXCollections.observableArrayList(records);
        attendanceTable.setItems(data);
    }

    private void updateRecord(AttendanceRecord record) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        if (dbManager.updateAttendance(record.getIdAttendance(), record.getStart(), record.getEnd())) {
            statusLabel.setText(bundle.getString("msg.record_updated"));
            statusLabel.setStyle("-fx-text-fill: green;");
            loadData(); // Sort
        } else {
            statusLabel.setText(bundle.getString("error.update_failed"));
            statusLabel.setStyle("-fx-text-fill: red;");
            loadData(); // Revert on failure
        }
    }

    private void generatePdf() {
        LocalDate selectedDate = monthPicker.getValue();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
        directoryChooser.setTitle("Vyberte priečinok pre uloženie PDF");
        java.io.File selectedDirectory = directoryChooser.showDialog(rootPane.getScene().getWindow());

        if (selectedDirectory == null) {
            return; // User cancelled
        }

        // Group records by person
        java.util.Map<String, List<AttendanceRecord>> recordsByPerson = new java.util.HashMap<>();
        for (AttendanceRecord record : attendanceTable.getItems()) {
            String fullName = record.getName() + " " + record.getSurname();
            recordsByPerson.computeIfAbsent(fullName, k -> new java.util.ArrayList<>()).add(record);
        }

        int successCount = 0;
        StringBuilder errors = new StringBuilder();

        for (java.util.Map.Entry<String, List<AttendanceRecord>> entry : recordsByPerson.entrySet()) {
            String personName = entry.getKey();
            List<AttendanceRecord> personRecords = entry.getValue();

            String safeName = personName.replaceAll("\\s+", "_");
            String filename = selectedDirectory.getAbsolutePath() + java.io.File.separator +
                    "Dochadzka_" + safeName + "_" + selectedDate.getYear() + "_" + selectedDate.getMonthValue()
                    + ".pdf";

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);

                // Load Arial font for CE characters support
                org.apache.pdfbox.pdmodel.font.PDFont font;
                try {
                    font = org.apache.pdfbox.pdmodel.font.PDType0Font.load(doc,
                            new java.io.File("C:\\Windows\\Fonts\\arial.ttf"));
                } catch (IOException e) {
                    font = PDType1Font.HELVETICA;
                }
                final org.apache.pdfbox.pdmodel.font.PDFont mainFont = font;

                // Variables for statistics
                double totalHours = 0;
                double saturdayHours = 0;
                double sundayHours = 0;

                final int MARGIN = 50;
                final int Y_START = 700;
                final int ROW_HEIGHT = 20;
                int[] colWidths = { 150, 100, 70, 70, 70 };
                String[] headers = {
                        bundle.getString("pdf.header.name"),
                        bundle.getString("pdf.header.date"),
                        bundle.getString("pdf.header.arrival"),
                        bundle.getString("pdf.header.departure"),
                        bundle.getString("pdf.header.hours")
                };

                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                    // Title
                    contentStream.setFont(mainFont, 16);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, Y_START + 30);
                    contentStream.showText(bundle.getString("pdf.title") + " " + personName + " - " + selectedDate.getMonth() + " "
                            + selectedDate.getYear());
                    contentStream.endText();

                    int y = Y_START;

                    // Draw Table Header
                    drawRow(contentStream, y, MARGIN, colWidths, headers, true, mainFont);
                    y -= ROW_HEIGHT;

                    contentStream.setFont(mainFont, 10);

                    for (AttendanceRecord record : personRecords) {
                        if (y < MARGIN + 50) {
                            break;
                        }

                        String start = record.getStart();
                        String end = record.getEnd();
                        String dateStr = record.getDay();

                        double hours = 0;
                        if (start != null && end != null && !start.isEmpty() && !end.isEmpty()) {
                            hours = calculateHours(start, end);
                        }

                        totalHours += hours;
                        if (isWeekend(dateStr)) {
                            LocalDate date = LocalDate.parse(dateStr);
                            if (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
                                saturdayHours += hours;
                            } else if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                                sundayHours += hours;
                            }
                        }

                        String[] rowData = {
                                record.getName() + " " + record.getSurname(),
                                dateStr,
                                start != null ? start : "",
                                end != null ? end : "",
                                formatDuration(hours)
                        };

                        drawRow(contentStream, y, MARGIN, colWidths, rowData, false, mainFont);
                        y -= ROW_HEIGHT;
                    }

                    // Summary
                    y -= 20;
                    contentStream.setFont(mainFont, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, y);
                    contentStream.showText(bundle.getString("pdf.summary"));
                    contentStream.endText();
                    y -= 15;
                    contentStream.setFont(mainFont, 12);

                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, y);
                    contentStream.showText(bundle.getString("pdf.total_hours") + " " + formatDuration(totalHours));
                    contentStream.endText();
                    y -= 15;

                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, y);
                    contentStream.showText(bundle.getString("pdf.saturday_hours") + " " + formatDuration(saturdayHours));
                    contentStream.endText();
                    y -= 15;

                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, y);
                    contentStream.showText(bundle.getString("pdf.sunday_hours") + " " + formatDuration(sundayHours));
                    contentStream.endText();
                }

                doc.save(filename);
                successCount++;

            } catch (IOException e) {
                e.printStackTrace();
                errors.append(personName).append(": ").append(e.getMessage()).append("\n");
            }
        }

        if (errors.length() > 0) {
            statusLabel.setText("Chyby pri generovaní.");
            statusLabel.setStyle("-fx-text-fill: red;");
            new Alert(Alert.AlertType.ERROR, bundle.getString("error.pdf_generation_failed") + errors.toString())
                    .showAndWait();
        } else {
            java.text.MessageFormat formatter = new java.text.MessageFormat(bundle.getString("msg.pdf_generated"));
            statusLabel.setText(formatter.format(new Object[]{successCount}));
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    private void drawRow(PDPageContentStream contentStream, int y, int margin, int[] colWidths, String[] data,
            boolean isHeader, org.apache.pdfbox.pdmodel.font.PDFont font) throws IOException {
        int x = margin;

        contentStream.setFont(font, 10);

        // Draw horizontal line top
        contentStream.moveTo(margin, y + 15);
        contentStream.lineTo(margin + sum(colWidths), y + 15);
        contentStream.stroke();

        for (int i = 0; i < data.length; i++) {
            // Draw text
            contentStream.beginText();
            contentStream.newLineAtOffset(x + 5, y + 5);
            contentStream.showText(data[i]);
            contentStream.endText();

            // Draw vertical line
            contentStream.moveTo(x, y + 15);
            contentStream.lineTo(x, y - 5);
            contentStream.stroke();

            x += colWidths[i];
        }
        // Final vertical line
        contentStream.moveTo(x, y + 15);
        contentStream.lineTo(x, y - 5);
        contentStream.stroke();

        // Draw horizontal line bottom
        contentStream.moveTo(margin, y - 5);
        contentStream.lineTo(margin + sum(colWidths), y - 5);
        contentStream.stroke();
    }

    private String formatDuration(double hours) {
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60);
        return String.format("%d:%02d", h, m);
    }

    private int sum(int[] array) {
        int sum = 0;
        for (int i : array)
            sum += i;
        return sum;
    }

    private double calculateHours(String start, String end) {
        try {
            String[] sParts = start.split(":");
            String[] eParts = end.split(":");
            double s = Integer.parseInt(sParts[0]) + Integer.parseInt(sParts[1]) / 60.0;
            double e = Integer.parseInt(eParts[0]) + Integer.parseInt(eParts[1]) / 60.0;
            return Math.max(0, e - s);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isWeekend(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            java.time.DayOfWeek dow = date.getDayOfWeek();
            return dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY;
        } catch (Exception e) {
            return false;
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
            new Alert(Alert.AlertType.ERROR, "Failed to load scene: " + e.getMessage()).showAndWait();
        }
    }
}
