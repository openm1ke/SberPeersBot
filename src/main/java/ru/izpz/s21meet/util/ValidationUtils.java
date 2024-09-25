package ru.izpz.s21meet.util;

public class ValidationUtils {

    public static boolean isValidSchoolLogin(String login) {
        return isValidLogin(login) && !login.contains("@");
    }

    public static boolean isValidLogin(String login) {
        return login != null && login.trim().length() > 3 && login.trim().length() <= 16;
    }

    public static boolean isValidText(String login) {
        return login != null && !login.trim().isEmpty();
    }
}
