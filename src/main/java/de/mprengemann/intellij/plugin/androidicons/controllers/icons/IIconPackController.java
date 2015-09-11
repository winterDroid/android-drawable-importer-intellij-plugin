package de.mprengemann.intellij.plugin.androidicons.controllers.icons;

import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;
import java.util.List;

public interface IIconPackController {
    void restorePath();

    void savePath();

    void setPath(VirtualFile file);

    String getPath();

    String getId();

    String getIconPackName();

    List<ImageAsset> getAssets(String category);

    VirtualFile getRoot();

    void openBrowser();

    File getImageFile(ImageAsset asset, String color, Resolution resolution);

    File getImageFile(ImageAsset asset, String color, String size, Resolution resolution);

    void tearDown();

    List<String> getCategories();

    IconPack getIconPack();

    Resolution getThumbnailResolution();
}
