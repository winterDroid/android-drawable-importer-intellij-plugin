package de.mprengemann.intellij.plugin.androidicons.controllers.materialicons;

import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IIconsImporterController;
import de.mprengemann.intellij.plugin.androidicons.model.Asset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;
import java.util.List;

public interface IMaterialIconsController extends IController<MaterialIconsObserver> {
    void restorePath();

    void savePath();

    void reset();

    void setPath(VirtualFile file);

    String getPath();

    List<String> getCategories();

    List<Asset> getAssets();

    List<Asset> getAssets(IIconsImporterController iconImporterController);

    VirtualFile getRoot();

    void openBrowser();

    List<String> getSizes(IIconsImporterController iconImporterController);

    List<String> getColors(IIconsImporterController iconImporterController);

    boolean isInitialized();

    void openHelp();

    File getImageFile(Asset asset);

    File getImageFile(Asset asset, String color, String size, Resolution resolution);

    boolean isSupportedResolution(Resolution resolution);
}
