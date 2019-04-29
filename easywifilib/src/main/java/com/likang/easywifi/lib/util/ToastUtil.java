package com.likang.easywifi.lib.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast统一管理类
 */
public class ToastUtil {

    public static void showLong(Context context, int resId) {
        showLong(context, context.getString(resId));
    }

    public static void showLong(Context context, CharSequence text) {
        show(context, text, Toast.LENGTH_LONG);
    }

    public static void showShort(Context context, int resId) {
        showShort(context, context.getString(resId));
    }

    public static void showShort(Context context, CharSequence text) {
        show(context, text, Toast.LENGTH_SHORT);
    }

    private static void show(Context context, CharSequence text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.setText(text);
        ToastHooker.show(toast);
    }

    private ToastUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

}