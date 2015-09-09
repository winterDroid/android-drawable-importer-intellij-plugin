package de.mprengemann.intellij.plugin.androidicons.controllers.androidicons;

import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;
import java.util.List;

public interface IAndroidIconsController extends IController<AndroidIconsObserver> {
    void restorePath();

    void savePath();

    void setPath(VirtualFile file);

    String getPath();

    List<ImageAsset> getAssets();

    VirtualFile getRoot();

    void openBrowser();

    File getImageFile(ImageAsset asset);

    File getImageFile(ImageAsset asset, String color, Resolution resolution);
}
