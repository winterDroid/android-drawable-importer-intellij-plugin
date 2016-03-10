package de.mprengemann.intellij.plugin.androidicons.model;

import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

public enum Format {
    JPG,
    PNG,
    XML;

    private static final Format[] NON_VECTOR_VALUES = new Format[] {
        JPG, PNG
    };

    public static Format from(String string) {
        if (TextUtils.isEmpty(string)) {
            throw new IllegalArgumentException();
        }
        if (string.equalsIgnoreCase(JPG.toString())) {
            return JPG;
        } else if (string.equalsIgnoreCase(PNG.toString())) {
            return PNG;
        } else if (string.equalsIgnoreCase(XML.toString())) {
            return XML;
        }
        return null;
    }

    public static Format[] nonVectorValues() {
        return NON_VECTOR_VALUES;
    }
}
