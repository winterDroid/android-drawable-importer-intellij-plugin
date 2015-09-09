package de.mprengemann.intellij.plugin.androidicons.controllers.materialicons;

import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;
import java.util.List;

public interface IMaterialIconsController extends IController<MaterialIconsObserver> {
    void restorePath();

    void savePath();

    void reset();

    void setPath(VirtualFile file);

    String getPath();

    List<ImageAsset> getAssets();

    List<ImageAsset> getAssets(String category);

    VirtualFile getRoot();

    void openBrowser();

    boolean isInitialized();

    void openHelp();

    File getImageFile(ImageAsset asset);

    File getImageFile(ImageAsset asset, String color, String size, Resolution resolution);

    boolean isSupportedResolution(Resolution resolution);
}
