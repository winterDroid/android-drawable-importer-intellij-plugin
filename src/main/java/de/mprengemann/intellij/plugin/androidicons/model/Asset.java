package de.mprengemann.intellij.plugin.androidicons.model;

public class Asset {
    private final IconPack iconPack;
    private final String name;
    private final Resolution defaultResolution;
    private final String category;

    public Asset(IconPack iconPack, String name, Resolution defaultResolution) {
        this(iconPack, name, "", defaultResolution);
    }

    public Asset(IconPack iconPack, String name, String category, Resolution defaultResolution) {
        this.iconPack = iconPack;
        this.name = name;
        this.defaultResolution = defaultResolution;
        this.category = category;
    }

    public IconPack getIconPack() {
        return iconPack;
    }

    public String getName() {
        return name;
    }

    public Resolution getDefaultResolution() {
        return defaultResolution;
    }

    public String getCategory() {
        return category;
    }
}
