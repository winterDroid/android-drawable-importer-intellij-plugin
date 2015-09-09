package de.mprengemann.intellij.plugin.androidicons.model;

import java.util.List;

public class IconPack {

    private final String name;
    private final String url;
    private final String path;
    private final List<ImageAsset> assets;

    public IconPack(String name, String url, String path, List<ImageAsset> assets) {
        this.name = name;
        this.url = url;
        this.path = path;
        this.assets = assets;
    }

    public String getName() {
        return name;
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
}
