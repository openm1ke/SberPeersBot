package ru.izpz.s21meet.model;

public enum Status {
    NEW("new"),
    SCHOOL_LOGIN("school_login"),
    SBER_LOGIN("sber_login"),
    SBER_TEAM("sber_team"),
    SBER_ROLE("sber_role"),
    READY("ready");

    private final String displayValue;

    Status(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
