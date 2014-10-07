package csc.vlpol.infret;

public class Utils {
    public static boolean isRussian(char c) {
        return c >= 'а' && c <= 'я' || c >= 'А' && c <= 'Я' || c == 'ё' || c == 'Ё';
    }

    public static boolean isEnglish(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }
}
