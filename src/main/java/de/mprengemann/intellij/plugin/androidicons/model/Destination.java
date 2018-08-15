package de.mprengemann.intellij.plugin.androidicons.model;

import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

public enum Destination {
    Drawable,
    Mipmap;

    private static final Destination[] DESTINATION_VALUES = new Destination[] {
        Drawable, Mipmap
    };

    public static Destination from(String string) {
        if (TextUtils.isEmpty(string)) {
            throw new IllegalArgumentException();
        }
        if (string.equalsIgnoreCase(Drawable.toString())) {
            return Drawable;
        } else if (string.equalsIgnoreCase(Mipmap.toString())) {
            return Mipmap;
        }
        return null;
    }

    public static Destination[] destinationValues() {
        return DESTINATION_VALUES;
    }
}
