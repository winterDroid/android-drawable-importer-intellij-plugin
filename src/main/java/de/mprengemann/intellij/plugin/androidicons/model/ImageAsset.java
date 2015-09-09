package de.mprengemann.intellij.plugin.androidicons.model;

import java.util.List;

public class ImageAsset {

    private final String name;
    private final String category;
    private final List<Resolution> resolutions;
    private final List<String> colors;
    private final List<String> sizes;

    public ImageAsset(String name,
                      String category,
                      List<Resolution> resolutions,
                      List<String> colors,
                      List<String> sizes) {
        this.name = name;
        this.category = category;
        this.resolutions = resolutions;
        this.colors = colors;
        this.sizes = sizes;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public List<Resolution> getResolutions() {
        return resolutions;
    }

    public List<String> getColors() {
        return colors;
    }

    public List<String> getSizes() {
        return sizes;
    }
}
