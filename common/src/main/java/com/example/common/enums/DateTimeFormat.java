package com.example.common.enums;

public enum DateTimeFormat {
    UNIX_TIMESTAMP("EEE MMM dd HH:mm:ss zzz yyyy"),
    TIMESTAMP_MS("yyyyMMddHHmmssSSS");

    private String format;

    DateTimeFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public static String[] getFormats() {
        String[] formats = new String[DateTimeFormat.values().length];
        for (int i = 0; i < DateTimeFormat.values().length; i++) {
            formats[i] = DateTimeFormat.values()[i].getFormat();
        }
        return formats;
    }
}
