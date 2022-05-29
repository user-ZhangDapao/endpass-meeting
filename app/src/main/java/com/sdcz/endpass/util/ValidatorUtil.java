package com.sdcz.endpass.util;

import java.util.regex.Pattern;

public class ValidatorUtil {
    /*匹配特殊字符*/
    public static final String REGEX_SPECIAL_CHAR = "((?=[\\x21-\\x7e]+)[^A-Za-z0-9])";
    /**
     * 校验是否是数字
     */
    public static boolean isNumeric(final String str) {
        for (int index = str.length(); --index >= 0; ) {
            if (!Character.isDigit(str.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSpecialChar(String username) {
        return Pattern.matches(REGEX_SPECIAL_CHAR, username);
    }
}
