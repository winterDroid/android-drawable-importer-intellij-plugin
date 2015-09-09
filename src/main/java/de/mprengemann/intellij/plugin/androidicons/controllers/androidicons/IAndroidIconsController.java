package de.mprengemann.intellij.plugin.androidicons.controllers.androidicons;

import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.model.Asset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;
import java.util.List;

public interface IAndroidIconsController extends IController<AndroidIconsObserver> {
    void restorePath();

    void savePath();

    void reset();

    void setPath(VirtualFile file);

    String getPath();

    List<String> getColors();

    List<Asset> getAssets();

    VirtualFile getRoot();

    void openBrowser();

    List<String> getSizes();

    boolean isInitialized();

    File getImageFile(Asset asset);

    File getImageFile(Asset asset, String color, Resolution resolution);

    boolean isSupportedResolution(Resolution resolution);
}
