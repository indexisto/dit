package com.indexisto.dit.data.object;


public enum DihCommandEnum {

    FULL_IMPORT("full-import"),

    DELTA_IMPORT("delta-import");

    private String name;

    private DihCommandEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DihCommandEnum getByName(String name) {
        for (final DihCommandEnum result: values()) {
            if (result.getName().equals(name)) return result;
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}