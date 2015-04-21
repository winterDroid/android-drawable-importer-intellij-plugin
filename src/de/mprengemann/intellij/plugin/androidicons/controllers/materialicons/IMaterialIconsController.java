package de.mprengemann.intellij.plugin.androidicons.controllers.materialicons;

import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;

import java.util.List;

public interface IMaterialIconsController extends IController<MaterialIconsObserver> {
    void restorePath();

    void savePath();

    void reset();

    void setPath(VirtualFile file);

    String getPath();

    List<String> getCategories();

    List<String> getAssets();

    VirtualFile getRoot();

    void openBrowser();
}
