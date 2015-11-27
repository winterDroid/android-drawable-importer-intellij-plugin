package de.mprengemann.intellij.plugin.androidicons.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImageAsset implements Comparable<ImageAsset> {

    private final String name;
    private final String pack;
    private final String category;
    private final List<Resolution> resolutions;
    private final List<String> colors;
    private final List<String> sizes;

    public ImageAsset(String name,
                      String pack,
                      String category,
                      List<Resolution> resolutions,
                      List<String> colors,
                      List<String> sizes) {
        this.name = name;
        this.pack = pack;
        this.category = category;
        this.resolutions = resolutions;
        this.colors = colors;
        this.sizes = sizes;
    }

    public String getName() {
        return name;
    }

    public String getIconPack() {
        return pack;
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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull ImageAsset o) {
        return getName().compareTo(o.getName());
    }
}
