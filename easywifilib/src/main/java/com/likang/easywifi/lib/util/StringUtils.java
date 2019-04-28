package com.likang.easywifi.lib.util;

import android.text.TextUtils;

/**
 * @author likangren
 */
public class StringUtils {

    private static String TAG = "WifiUtils";

    public static String enclosedInDoubleQuotationMarks(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }

        int lastPos = string.length() - 1;
        if ((string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }

        return "\"" + string + "\"";
    }

    public static String removeQuotationMarks(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        return string.replace("\"", "");
    }


}
