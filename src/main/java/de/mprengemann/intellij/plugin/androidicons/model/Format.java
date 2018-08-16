package de.mprengemann.intellij.plugin.androidicons.model;

import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

public enum Format {
    JPG,
    PNG,
    XML;

    private static final Format[] NON_VECTOR_VALUES = new Format[] {
        JPG, PNG
    };

    public static Format[] nonVectorValues() {
        return NON_VECTOR_VALUES;
    }
}
