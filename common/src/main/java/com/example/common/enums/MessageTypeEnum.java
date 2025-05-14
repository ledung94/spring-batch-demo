package com.example.common.enums;

public enum MessageTypeEnum {
    SMS("SMS"),
    NOTIFICATION("NOTI"),
    MAIL("MAIL"),
    ZALO("ZALO");

    private String key;

    private MessageTypeEnum(String key) {
        this.key = key;
    }

    public String key() {
        return this.key.toString();
    }
}
