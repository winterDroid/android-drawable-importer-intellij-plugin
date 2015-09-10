package de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IIconPackController;
import de.mprengemann.intellij.plugin.androidicons.controllers.androidicons.IAndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.materialicons.IMaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

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
        if (androidIconsController.getId().equals(iconPack)) {
            selectedAsset = androidIconsController.getAssets(selectedAsset.getCategory()).get(0);
        } else if (materialIconsController.getId().equals(iconPack)) {
            selectedAsset = materialIconsController.getAssets(selectedAsset.getCategory()).get(0);
        }
        notifyUpdated();
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
        return getIconPack().getImageFile(asset, color, resolution);
    }

    @Override
    public File getThumbnailFile(ImageAsset asset) {
        if (androidIconsController.getId().equals(asset.getIconPack())) {
            return androidIconsController.getImageFile(asset, "black", Resolution.LDPI);
        } else {
            return materialIconsController.getImageFile(asset, "black", Resolution.MDPI);
        }
    }

    @Override
    public File getSelectedImageFile() {
        return null;
    }

    private IIconPackController getIconPack() {
        if (selectedAsset.getIconPack().equals(androidIconsController.getId())) {
            return androidIconsController;
        } else {
            return materialIconsController;
        }
    }

    @Override
    public List<String> getCategories() {
        return getIconPack().getCategories();
    }

    @Override
    public List<ImageAsset> getAssets() {
        return getIconPack().getAssets(selectedAsset.getCategory());
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
