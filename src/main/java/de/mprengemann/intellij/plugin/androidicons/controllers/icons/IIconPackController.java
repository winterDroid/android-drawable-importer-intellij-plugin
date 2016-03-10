package de.mprengemann.intellij.plugin.androidicons.controllers.icons;

import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;
import java.util.List;

public interface IIconPackController {

    String getId();

    List<ImageAsset> getAssets(String category);

    List<ImageAsset> getAssets(List<String> categories);

    File getImageFile(ImageAsset asset, String color, Resolution resolution);

    File getImageFile(ImageAsset asset, String color, String size, Resolution resolution);

    void tearDown();

    List<String> getCategories();

    IconPack getIconPack();

    boolean supportsVectors();

    Resolution getThumbnailResolution();

}
