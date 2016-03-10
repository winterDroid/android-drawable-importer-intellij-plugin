package de.mprengemann.intellij.plugin.androidicons.model;

import java.util.List;

public class IconPack {

    private final String name;
    private final String id;
    private final String url;
    private final String path;
    private final List<ImageAsset> assets;
    private final List<String> categories;

    public IconPack(String name, String id, String url, String path, List<ImageAsset> assets, List<String> categories) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.path = path;
        this.assets = assets;
        this.categories = categories;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public List<ImageAsset> getAssets() {
        return assets;
    }

    public List<String> getCategories() {
        return categories;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IconPack iconPack = (IconPack) o;

        return id.equals(iconPack.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
