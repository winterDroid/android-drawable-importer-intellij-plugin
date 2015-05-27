package de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.androidicons.IAndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.materialicons.IMaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.model.Asset;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IconsImporterController implements IIconsImporterController {

    private IAndroidIconsController androidIconsController;
    private IMaterialIconsController materialIconsController;
    private Set<IconsImporterObserver> observerSet;
    private IconPack iconPack;
    private String category;
    private Asset asset = Asset.NONE;
    private String size;
    private String color;
    private String exportName;
    private String exportRoot;
    private List<Resolution> exportResolutions;

    public IconsImporterController(IAndroidIconsController androidIconsController,
                                   IMaterialIconsController materialIconsController) {
        this.androidIconsController = androidIconsController;
        this.materialIconsController = materialIconsController;
        this.observerSet = new HashSet<IconsImporterObserver>();
        this.exportResolutions = new ArrayList<Resolution>();
    }

    @Override
    public void addObserver(IconsImporterObserver observer) {
        observerSet.add(observer);
    }

    @Override
    public void removeObserver(IconsImporterObserver observer) {
        observerSet.remove(observer);
    }

    @Override
    public void tearDown() {
        observerSet.clear();
        observerSet = null;
    }

    @Override
    public void setExportRoot(String exportRoot) {
        if (this.exportRoot != null &&
            this.exportRoot.equals(exportRoot)) {
            return;
        }
        this.exportRoot = exportRoot;
        notifyExportRootChanged();
    }

    @Override
    public void setSelectedIconPack(IconPack iconPack) {
        if (this.iconPack == iconPack) {
            return;
        }
        this.iconPack = iconPack;
        notifyIconPackChanged();
    }

    @Override
    public void setSelectedCategory(String category) {
        if (this.category != null &&
            this.category.equals(category)) {
            return;
        }
        this.category = category;
        notifyCategoryChanged();
    }

    @Override
    public void setSelectedAsset(Asset asset) {
        if (this.asset != null &&
            this.asset.equals(asset)) {
            return;
        }
        this.asset = asset;
        if (this.asset == null) {
            this.asset = Asset.NONE;
        }
        notifyAssetChanged();
        if (this.asset == null) {
            setExportName("");
            return;
        }
        setExportName(String.format("ic_action_%s", asset));
    }

    @Override
    public void setSelectedSize(String size) {
        if (this.size != null &&
            this.size.equals(size)) {
            return;
        }
        this.size = size;
        notifySizeChanged();
    }

    @Override
    public void setSelectedColor(String color) {
        if (this.color != null &&
            this.color.equals(color)) {
            return;
        }
        this.color = color;
        notifyColorChanged();
    }

    @Override
    public void setExportName(String exportName) {
        if (this.exportName != null &&
            this.exportName.equals(exportName)) {
            return;
        }
        if (isCustomExport()) {
            return;
        }
        this.exportName = exportName;
        notifyExportNameChanged();
    }

    private boolean isCustomExport() {
        if (TextUtils.isEmpty(exportName)) {
            return false;
        }
        final String assetName = exportName.replace("ic_action_", "");
        return !(materialIconsController.getAssets().contains(assetName) ||
                 androidIconsController.getAssets().contains(assetName));
    }

    @Override
    public IconPack getSelectedPack() {
        return iconPack;
    }

    @Override
    public String getExportRoot() {
        return exportRoot;
    }

    @Override
    public String getExportName() {
        return exportName;
    }

    @Override
    public File getImageFile(Asset asset) {
        switch (asset.getIconPack()) {
            case ANDROID_ICONS:
                return androidIconsController.getImageFile(asset);
            case MATERIAL_ICONS:
                return materialIconsController.getImageFile(asset);
        }
        return null;
    }

    @Override
    public File getSelectedImageFile() {
        switch (iconPack) {
            case ANDROID_ICONS:
                return androidIconsController.getImageFile(asset, color, Resolution.XHDPI);
            case MATERIAL_ICONS:
                return materialIconsController.getImageFile(asset, color, size, Resolution.XHDPI);
        }
        return null;
    }

    @Override
    public String getSelectedCategory() {
        return category;
    }

    @Override
    public Asset getSelectedAsset() {
        return asset;
    }

    @Override
    public String getSelectedSize() {
        return size;
    }

    @Override
    public String getSelectedColor() {
        return color;
    }

    @Override
    public void reset() {
        iconPack = null;
        category = null;
        asset = Asset.NONE;
        size = null;
        color = null;
        exportName = null;
    }

    @Override
    public RefactoringTask getTask(Project project) {
        final RefactoringTask task = new RefactoringTask(project);
        ImageInformation baseInformation = ImageInformation.newBuilder()
                                                           .setExportName(exportName)
                                                           .setExportPath(exportRoot)
                                                           .build(project);

        for (Resolution resolution : exportResolutions) {
            ImageInformation.Builder builder = ImageInformation.newBuilder(baseInformation)
                                                               .setResolution(resolution);
            File imageFile;
            switch (iconPack) {
                case ANDROID_ICONS:
                    if (androidIconsController.isSupprtedResolution(resolution)) {
                        imageFile = androidIconsController.getImageFile(asset, color, resolution);
                    } else {
                        imageFile = androidIconsController.getImageFile(asset, color, Resolution.XXHDPI);
                        BufferedImage image;
                        try {
                            image = ImageIO.read(imageFile);
                        } catch (IOException ignored) {
                            continue;
                        }
                        builder.setImageWidth(image.getWidth())
                               .setImageHeight(image.getHeight())
                               .setTargetWidth(image.getWidth())
                               .setTargetHeight(image.getHeight())
                               .setAlgorithm(ResizeAlgorithm.SCALR)
                               .setMethod(Scalr.Method.ULTRA_QUALITY)
                               .setFactor(RefactorHelper.getScaleFactor(resolution, Resolution.XXHDPI));
                    }
                    break;
                case MATERIAL_ICONS:
                    if (materialIconsController.isSupportedResolution(resolution)) {
                        imageFile = materialIconsController.getImageFile(asset, color, size, resolution);
                    } else {
                        imageFile = materialIconsController.getImageFile(asset,
                                                                         color,
                                                                         size,
                                                                         Resolution.MDPI);
                        BufferedImage image;
                        try {
                            image = ImageIO.read(imageFile);
                        } catch (IOException ignored) {
                            continue;
                        }
                        builder.setImageWidth(image.getWidth())
                               .setImageHeight(image.getHeight())
                               .setTargetWidth(image.getWidth())
                               .setTargetHeight(image.getHeight())
                               .setAlgorithm(ResizeAlgorithm.SCALR)
                               .setMethod(Scalr.Method.ULTRA_QUALITY)
                               .setFactor(RefactorHelper.getScaleFactor(resolution, Resolution.MDPI));
                    }
                    break;
                default:
                    continue;
            }
            task.addImage(builder.setImageFile(imageFile)
                                 .build(project));
        }

        return task;
    }

    @Override
    public void setExportResolution(Resolution resolution, boolean export) {
        if (export && !exportResolutions.contains(resolution)) {
            exportResolutions.add(resolution);
        } else if (!export && exportResolutions.contains(resolution)) {
            exportResolutions.remove(resolution);
        }
    }

    private void notifyIconPackChanged() {
        for (IconsImporterObserver observer : observerSet) {
            observer.onIconPackChanged();
        }
    }

    private void notifyCategoryChanged() {
        for (IconsImporterObserver observer : observerSet) {
            observer.onCategoryChanged();
        }
    }

    private void notifyAssetChanged() {
        for (IconsImporterObserver observer : observerSet) {
            observer.onAssetChanged();
        }
    }

    private void notifySizeChanged() {
        for (IconsImporterObserver observer : observerSet) {
            observer.onSizeChanged();
        }
    }

    private void notifyColorChanged() {
        for (IconsImporterObserver observer : observerSet) {
            observer.onColorChanged();
        }
    }

    private void notifyExportNameChanged() {
        for (IconsImporterObserver observer : observerSet) {
            observer.onExportNameChanged(exportName);
        }
    }

    private void notifyExportRootChanged() {
        for (IconsImporterObserver observer : observerSet) {
            observer.onExportRootChanged(exportRoot);
        }
    }
}
