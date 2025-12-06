package org.example.model;

public class Document {
    private long id;
    private String name;
    private String path;
    private String language;
    private String validityFrom;
    private String validityTo;

    public Document(long id, String name, String path, String language, String validityFrom, String validityTo) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.language = language;
        this.validityFrom = validityFrom;
        this.validityTo = validityTo;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getLanguage() {
        return language;
    }

    public String getValidityFrom() {
        return validityFrom;
    }

    public String getValidityTo() {
        return validityTo;
    }
}
