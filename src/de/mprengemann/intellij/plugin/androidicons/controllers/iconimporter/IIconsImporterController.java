package de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.images.Resolution;

import java.io.File;

public interface IIconsImporterController extends IController<IconsImporterObserver>{
    void setExportRoot(String exportRoot);

    void setSelectedIconPack(IconPack iconPack);

    void setSelectedCategory(String category);

    void setSelectedAsset(String asset);

    void setSelectedSize(String size);

    void setSelectedColor(String color);

    void setExportName(String exportName);

    IconPack getSelectedPack();

    String getExportName();

    String getExportRoot();

    File getImageFile(String assetName);

    File getSelectedImageFile();

    String getSelectedCategory();

    String getSelectedAsset();

    String getSelectedSize();

    String getSelectedColor();

    void reset();

    RefactoringTask getTask(Project project);

    void setExportResolution(Resolution resolution, boolean export);
}
