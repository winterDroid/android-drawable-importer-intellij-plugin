package de.mprengemann.intellij.plugin.androidicons.util;

public final class TextUtils {
    public static boolean isEmpty(final CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean isBlank(final CharSequence s) {
        if (s == null) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                continue;
            }
            return false;
        }
        return true;
    }
}
