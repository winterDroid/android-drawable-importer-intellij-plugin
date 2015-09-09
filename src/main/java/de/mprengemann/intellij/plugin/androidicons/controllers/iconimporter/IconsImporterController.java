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
import de.mprengemann.intellij.plugin.androidicons.util.RefactorUtils;
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
    private Asset asset;
    private String exportName;
    private String exportRoot;
    private List<Resolution> exportResolutions;

    private String size;
    private String color;

    public IconsImporterController(IAndroidIconsController androidIconsController,
                                   IMaterialIconsController materialIconsController) {
        this.androidIconsController = androidIconsController;
        this.materialIconsController = materialIconsController;
        this.observerSet = new HashSet<IconsImporterObserver>();
        this.exportResolutions = new ArrayList<Resolution>();
        this.asset = androidIconsController.getAssets().get(0);
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
        notifyUpdated();
    }

    @Override
    public void setSelectedIconPack(IconPack iconPack) {
        switch (iconPack) {
            case ANDROID_ICONS:
                asset = androidIconsController.getAssets().get(0);
                break;
            case MATERIAL_ICONS:
                asset = materialIconsController.getAssets().get(0);
                break;
        }
        notifyUpdated();
    }

    @Override
    public void setSelectedCategory(String category) {
        notifyUpdated();
    }

    @Override
    public void setSelectedAsset(Asset asset) {
        if (this.asset != null &&
            this.asset == asset) {
            return;
        }
        this.asset = asset;
//        if (this.asset == null) {
//            this.asset = Asset.NONE;
//        }
        notifyUpdated();
//        if (this.asset == Asset.NONE) {
//            setExportName("");
//        } else {
            setExportName(String.format("ic_action_%s", asset.getName()));
//        }
    }

    @Override
    public void setSelectedSize(String size) {
        this.size = size;
        notifyUpdated();
    }

    @Override
    public void setSelectedColor(String color) {
        switch (asset.getIconPack()) {
            case ANDROID_ICONS:
                break;
            case MATERIAL_ICONS:
                break;
        }
        notifyUpdated();
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
        notifyUpdated();
    }

    private boolean isCustomExport() {
        if (TextUtils.isEmpty(exportName)) {
            return false;
        }
        final String assetName = exportName.replace("ic_action_", "");
        for (Asset asset : materialIconsController.getAssets()) {
            if (asset.getName().equalsIgnoreCase(assetName)) {
                return false;
            }
        }
        for (Asset asset : androidIconsController.getAssets()) {
            if (asset.getName().equalsIgnoreCase(assetName)) {
                return false;
            }
        }
        return true;
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
        switch (asset.getIconPack()) {
            case ANDROID_ICONS:
                return androidIconsController.getImageFile(asset, color, Resolution.XHDPI);
            case MATERIAL_ICONS:
                return materialIconsController.getImageFile(asset, color, size, Resolution.XHDPI);
        }
        return null;
    }

    @Override
    public Asset getAsset() {
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
            switch (asset.getIconPack()) {
                case ANDROID_ICONS:
                    if (androidIconsController.isSupportedResolution(resolution)) {
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
                               .setFactor(RefactorUtils.getScaleFactor(resolution, Resolution.XXHDPI));
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
                               .setFactor(RefactorUtils.getScaleFactor(resolution, Resolution.MDPI));
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

    private void notifyUpdated() {
        for (IconsImporterObserver observer : observerSet) {
            observer.updated();
        }
    }
}
