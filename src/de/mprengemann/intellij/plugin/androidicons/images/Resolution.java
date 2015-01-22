package de.mprengemann.intellij.plugin.androidicons.images;

import java.util.Locale;

public enum Resolution {
    LDPI("ldpi"),
    MDPI("mdpi"),
    HDPI("hdpi"),
    XHDPI("xhdpi"),
    XXHDPI("xxhdpi"),
    XXXHDPI("xxxhdpi");

    private String resolution;

    Resolution(String resolution) {
        this.resolution = resolution;
    }

    public String getName() {
        return resolution.toUpperCase(Locale.US);
    }
    
    @Override
    public String toString() {
        return resolution;
    }

    public static Resolution from(String value) {
        if (value.equalsIgnoreCase(LDPI.toString())) {
            return LDPI;
        } else if (value.equalsIgnoreCase(MDPI.toString())) {
            return MDPI;
        } else if (value.equalsIgnoreCase(HDPI.toString())) {
            return HDPI;
        } else if (value.equalsIgnoreCase(XHDPI.toString())) {
            return XHDPI;
        } else if (value.equalsIgnoreCase(XXHDPI.toString())) {
            return XXHDPI;
        } else if (value.equalsIgnoreCase(XXXHDPI.toString())) {
            return XXXHDPI;
        }
        return null;
    }
}
