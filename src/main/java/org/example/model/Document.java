package org.example.model;

public class Document {
    private final long id;
    private final String name;
    private final String path;
    private final String language;
    private final String validityFrom;
    private final String validityTo;

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
