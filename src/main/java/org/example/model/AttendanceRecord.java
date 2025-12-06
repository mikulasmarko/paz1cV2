package org.example.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.LongProperty;

public class AttendanceRecord {
    private final LongProperty idAttendance;
    private final StringProperty name;
    private final StringProperty surname;
    private final StringProperty day;
    private final StringProperty start;
    private final StringProperty end;

    public AttendanceRecord(long idAttendance, String name, String surname, String day, String start, String end) {
        this.idAttendance = new SimpleLongProperty(idAttendance);
        this.name = new SimpleStringProperty(name);
        this.surname = new SimpleStringProperty(surname);
        this.day = new SimpleStringProperty(day);
        this.start = new SimpleStringProperty(start);
        this.end = new SimpleStringProperty(end);
    }

    public long getIdAttendance() {
        return idAttendance.get();
    }

    public LongProperty idAttendanceProperty() {
        return idAttendance;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getSurname() {
        return surname.get();
    }

    public StringProperty surnameProperty() {
        return surname;
    }

    public String getDay() {
        return day.get();
    }

    public StringProperty dayProperty() {
        return day;
    }

    public String getStart() {
        return start.get();
    }

    public StringProperty startProperty() {
        return start;
    }

    public void setStart(String start) {
        this.start.set(start);
    }

    public String getEnd() {
        return end.get();
    }

    public StringProperty endProperty() {
        return end;
    }

    public void setEnd(String end) {
        this.end.set(end);
    }
}
