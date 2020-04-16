package com.eoscode.springapitools.util;

public class StringUtils {

    public static boolean isNumber(String string) {
        return string.matches("^\\d+$");
    }

}
