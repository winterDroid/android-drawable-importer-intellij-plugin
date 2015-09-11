package de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.IIconPackController;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.androidicons.IAndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.materialicons.IMaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IconsImporterController implements IIconsImporterController {

    private final Set<IconsImporterObserver> observerSet;
    private final IAndroidIconsController androidIconsController;
    private final IMaterialIconsController materialIconsController;

    private ImageAsset selectedAsset;
    private String selectedSize;
    private String selectedColor;
    private String exportName;

    public IconsImporterController(IAndroidIconsController androidIconsController,
                                   IMaterialIconsController materialIconsController) {
        this.androidIconsController = androidIconsController;
        this.materialIconsController = materialIconsController;
        this.observerSet = new HashSet<IconsImporterObserver>();

        final String category = androidIconsController.getCategories().get(0);
        this.selectedAsset = androidIconsController.getAssets(category).get(0);
        this.selectedSize = selectedAsset.getSizes().get(0);
        this.selectedColor = selectedAsset.getColors().get(0);
        this.exportName = null;
    }

    @Override
    public void addObserver(IconsImporterObserver observer) {
        observerSet.add(observer);
        notifyUpdated();
    }

    @Override
    public void removeObserver(IconsImporterObserver observer) {
        observerSet.remove(observer);
    }

    @Override
    public void tearDown() {
        observerSet.clear();
    }

    @Override
    public void setExportRoot(String exportRoot) {

    }

    @Override
    public void setSelectedIconPack(String iconPack) {
        if (selectedAsset.getIconPack().equals(iconPack)) {
            return;
        }
        final IIconPackController controller = getControllerForIconPackId(iconPack);
        final String category = controller.getCategories().get(0);
        selectedAsset = controller.getAssets(category).get(0);
        notifyUpdated();
    }

    @NotNull
    private IIconPackController getControllerForIconPackId(String iconPack) {
        IIconPackController controller;
        if (androidIconsController.getId().equals(iconPack)) {
            controller = androidIconsController;
        } else if (materialIconsController.getId().equals(iconPack)) {
            controller = materialIconsController;
        } else {
            throw new IllegalArgumentException(iconPack + " not found!");
        }
        return controller;
    }

    @Override
    public void setSelectedCategory(String category) {
        if (selectedAsset.getCategory().equals(category)) {
            return;
        }
        selectedAsset = materialIconsController.getAssets(category).get(0);
        notifyUpdated();
    }

    @Override
    public void setSelectedAsset(ImageAsset asset) {
        selectedAsset = asset;
        notifyUpdated();
    }

    @Override
    public void setSelectedSize(String size) {
        if (selectedSize.equals(size) &&
            selectedAsset.getSizes().contains(size)) {
            return;
        }
        selectedSize = size;
        if (!selectedAsset.getSizes().contains(selectedSize)) {
            selectedSize = selectedAsset.getSizes().get(0);
        }
        notifyUpdated();
    }

    @Override
    public void setSelectedColor(String color) {
        if (selectedColor.equals(color) &&
            selectedAsset.getColors().contains(color)) {
            return;
        }
        selectedColor = color;
        if (!selectedAsset.getColors().contains(selectedColor)) {
            selectedSize = selectedAsset.getColors().get(0);
        }
        notifyUpdated();
    }

    @Override
    public void setExportName(String exportName) {

    }

    @Override
    public String getExportName() {
        return exportName != null ? exportName : selectedAsset.getName();
    }

    @Override
    public String getExportRoot() {
        return null;
    }

    @Override
    public File getImageFile(ImageAsset asset, String color, Resolution resolution) {
        return getSelectedIconPack().getImageFile(asset, color, resolution);
    }

    @Override
    public File getThumbnailFile(ImageAsset asset) {
        final IIconPackController iconPackController = getControllerForIconPackId(asset.getIconPack());
        return iconPackController.getImageFile(asset, "black", iconPackController.getThumbnailResolution());
    }

    @Override
    public File getSelectedImageFile() {
        final IIconPackController iconPackController = getSelectedIconPack();
        return iconPackController.getImageFile(selectedAsset, selectedColor, selectedSize, Resolution.XHDPI);
    }

    @Override
    public List<String> getCategories() {
        return getSelectedIconPack().getCategories();
    }

    @Override
    public List<ImageAsset> getAssets() {
        return getSelectedIconPack().getAssets(selectedAsset.getCategory());
    }

    @Override
    public List<String> getSizes() {
        return selectedAsset.getSizes();
    }

    @Override
    public List<String> getColors() {
        return selectedAsset.getColors();
    }

    @Override
    public ImageAsset getSelectedAsset() {
        return selectedAsset;
    }

    @Override
    public String getSelectedSize() {
        return selectedSize;
    }

    @Override
    public String getSelectedColor() {
        return selectedColor;
    }

    @Override
    public IIconPackController getSelectedIconPack() {
        return getControllerForIconPackId(selectedAsset.getIconPack());
    }

    @Override
    public String getSelectedCategory() {
        return selectedAsset.getCategory();
    }

    @Override
    public RefactoringTask getTask(Project project) {
        return null;
    }

    @Override
    public void setExportResolution(Resolution resolution, boolean export) {

    }

    private void notifyUpdated() {
        for (IconsImporterObserver observer : observerSet) {
            observer.updated();
        }
    }
}
