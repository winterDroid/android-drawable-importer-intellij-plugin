package de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;
import java.util.List;

public interface IIconsImporterController extends IController<IconsImporterObserver>{
    void setExportRoot(String exportRoot);

    void setSelectedIconPack(String iconPack);

    void setSelectedCategory(String category);

    void setSelectedAsset(ImageAsset asset);

    void setSelectedSize(String size);

    void setSelectedColor(String color);

    void setExportName(String exportName);

    String getExportName();

    String getExportRoot();

    File getImageFile(ImageAsset asset, String color, Resolution resolution);

    File getThumbnailFile(ImageAsset asset);

    File getSelectedImageFile();

    ImageAsset getSelectedAsset();

    List<String> getCategories();

    List<ImageAsset> getAssets();

    List<String> getSizes();

    List<String> getColors();

    String getSelectedSize();

    String getSelectedColor();

    RefactoringTask getTask(Project project);

    void setExportResolution(Resolution resolution, boolean export);

}
