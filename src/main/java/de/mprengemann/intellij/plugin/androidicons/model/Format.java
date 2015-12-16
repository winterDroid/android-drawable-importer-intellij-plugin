package de.mprengemann.intellij.plugin.androidicons.model;

import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

public enum Format {
    JPG,
    PNG;

    public static Format from(String string) {
        if (TextUtils.isEmpty(string)) {
            throw new IllegalArgumentException();
        }
        if (string.equalsIgnoreCase(JPG.toString())) {
            return JPG;
        } else if (string.equalsIgnoreCase(PNG.toString())) {
            return PNG;
        }
        return null;
    }
}
