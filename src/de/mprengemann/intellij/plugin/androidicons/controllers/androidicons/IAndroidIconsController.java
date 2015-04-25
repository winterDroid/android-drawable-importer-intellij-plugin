package de.mprengemann.intellij.plugin.androidicons.controllers.androidicons;

import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.Resolution;

import java.io.File;
import java.util.List;

public interface IAndroidIconsController extends IController<AndroidIconsObserver> {
    void restorePath();

    void savePath();

    void reset();

    void setPath(VirtualFile file);

    String getPath();

    List<String> getColors();

    List<String> getAssets();

    VirtualFile getRoot();

    void openBrowser();

    List<String> getSizes();

    boolean isInitialized();

    File getImageFile(String asset);

    File getImageFile(String color, String asset, Resolution resolution);

    boolean isSupprtedResolution(Resolution resolution);
}
