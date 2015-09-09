package de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.Asset;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;

public interface IIconsImporterController extends IController<IconsImporterObserver>{
    void setExportRoot(String exportRoot);

    void setSelectedIconPack(IconPack iconPack);

    void setSelectedCategory(String category);

    void setSelectedAsset(Asset asset);

    void setSelectedSize(String size);

    void setSelectedColor(String color);

    void setExportName(String exportName);

    String getExportName();

    String getExportRoot();

    File getImageFile(Asset asset);

    File getSelectedImageFile();

    Asset getAsset();

    String getSelectedSize();

    String getSelectedColor();

    RefactoringTask getTask(Project project);

    void setExportResolution(Resolution resolution, boolean export);
}
