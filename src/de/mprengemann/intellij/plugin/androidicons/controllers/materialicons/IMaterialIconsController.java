package de.mprengemann.intellij.plugin.androidicons.controllers.materialicons;

import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IIconsImporterController;
import de.mprengemann.intellij.plugin.androidicons.images.Resolution;

import java.io.File;
import java.util.List;

public interface IMaterialIconsController extends IController<MaterialIconsObserver> {
    void restorePath();

    void savePath();

    void reset();

    void setPath(VirtualFile file);

    String getPath();

    List<String> getCategories();

    List<String> getAssets();

    List<String> getAssets(IIconsImporterController iconImporterController);

    VirtualFile getRoot();

    void openBrowser();

    List<String> getSizes(IIconsImporterController iconImporterController);

    List<String> getColors(IIconsImporterController iconImporterController);

    boolean isInitialized();

    void openHelp();

    File getImageFile(String category, String asset);

    File getImageFile(String category, String asset, String color, String size, Resolution resolution);

    boolean isSupportedResolution(Resolution resolution);
}
