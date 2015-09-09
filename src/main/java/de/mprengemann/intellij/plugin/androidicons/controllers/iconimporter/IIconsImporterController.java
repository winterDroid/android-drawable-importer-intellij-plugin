package de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;

public interface IIconsImporterController extends IController<IconsImporterObserver>{
    void setExportRoot(String exportRoot);

    void setSelectedIconPack(IconPack iconPack);

    void setSelectedCategory(String category);

    void setSelectedAsset(ImageAsset asset);

    void setSelectedSize(String size);

    void setSelectedColor(String color);

    void setExportName(String exportName);

    String getExportName();

    String getExportRoot();

    File getImageFile(ImageAsset asset);

    File getSelectedImageFile();

    ImageAsset getAsset();

    String getSelectedSize();

    String getSelectedColor();

    RefactoringTask getTask(Project project);

    void setExportResolution(Resolution resolution, boolean export);
}
