package com.indexisto.dit.data.logger;

public enum DihLogLevel {

    VERBOSE(0, "verbose"),

    DEBUG(1, "debug"),

    INFO(2, "info");

    private int id;

    private String name;

    private DihLogLevel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static DihLogLevel getById(int id) {
        for (final DihLogLevel result: values()) {
            if (result.getId() == id) return result;
        }
        return null;
    }

    public static DihLogLevel getByName(String name) {
        for (final DihLogLevel result: values()) {
            if (result.getName().equals(name)) return result;
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}