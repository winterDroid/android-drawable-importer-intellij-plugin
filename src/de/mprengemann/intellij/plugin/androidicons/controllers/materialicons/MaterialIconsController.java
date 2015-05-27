package de.mprengemann.intellij.plugin.androidicons.controllers.materialicons;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IIconsImporterController;
import de.mprengemann.intellij.plugin.androidicons.model.Asset;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialIconsController implements IMaterialIconsController {

    public static final String DEFAULT_RESOLUTION = "drawable-xhdpi";
    private static final String MATERIAL_ICONS_URL = "https://github.com/google/material-design-icons/releases";
    private static final EnumSet<Resolution> SUPPORTED_RESOLUTIONS = EnumSet.of(Resolution.MDPI,
                                                                                Resolution.HDPI,
                                                                                Resolution.XHDPI,
                                                                                Resolution.XXHDPI,
                                                                                Resolution.XXXHDPI);
    public static final List<String> BLACKLISTED_MATERIAL_ICONS_FOLDER = Arrays.asList("sprites",
                                                                                        "1x_ios",
                                                                                        "1x_web",
                                                                                        "2x_ios",
                                                                                        "2x_web",
                                                                                        "3x_ios",
                                                                                        "svg");
    private final FilenameFilter folderFileNameFiler = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return !s.startsWith(".") &&
                   new File(file, s).isDirectory() &&
                   !BLACKLISTED_MATERIAL_ICONS_FOLDER.contains(FilenameUtils.removeExtension(s));
        }
    };
    private Set<MaterialIconsObserver> observerSet;
    private List<File> categoryDirs = new ArrayList<File>();
    private List<String> categories = new ArrayList<String>();
    private List<Asset> assets = new ArrayList<Asset>();
    private VirtualFile assetRoot;

    public MaterialIconsController() {
        observerSet = new HashSet<MaterialIconsObserver>();
        restorePath();
        load();
    }

    @Override
    public void addObserver(MaterialIconsObserver observer) {
        observerSet.add(observer);
        notifyUpdated();
    }

    @Override
    public void removeObserver(MaterialIconsObserver observer) {
        observerSet.remove(observer);
    }

    @Override
    public void tearDown() {
        savePath();
        observerSet.clear();
        observerSet = null;
    }

    @Override
    public void restorePath() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String persistedFile = propertiesComponent.getValue(getSettingsKey());
        if (persistedFile != null) {
            assetRoot = VirtualFileManager.getInstance().findFileByUrl(persistedFile);
        }
    }

    private String getSettingsKey() {
        return String.format("assetPath_%s", IconPack.MATERIAL_ICONS.toString());
    }

    @Override
    public void savePath() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String assetUrl = assetRoot != null ? assetRoot.getUrl() : null;
        propertiesComponent.setValue(getSettingsKey(), assetUrl);
    }

    @Override
    public void reset() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.unsetValue(getSettingsKey());
        assetRoot = null;
        categoryDirs.clear();
        categories.clear();
        assets.clear();
        notifyUpdated();
    }

    @Override
    public void setPath(VirtualFile file) {
        this.assetRoot = file;
        load();
    }

    private void load() {
        loadCategories();
        loadAssets();
        notifyUpdated();
    }

    private void notifyUpdated() {
        for (MaterialIconsObserver observer : observerSet) {
            observer.updated(IconPack.MATERIAL_ICONS);
        }
    }

    private void loadAssets() {
        if (categoryDirs.size() < 1) {
            return;
        }
        for (File category : categoryDirs) {
            File[] densities = category.listFiles(folderFileNameFiler);
            if (densities == null ||
                densities.length < 1) {
                continue;
            }
            File exDensity = densities[0];
            for (File asset : exDensity.listFiles(getAssetFileNameFilter())) {
                assets.add(new Asset(IconPack.MATERIAL_ICONS, getAssetName(asset), category.getName(), Resolution.XHDPI));
            }
        }
    }

    private String getAssetName(File asset) {
        String assetName = FilenameUtils.removeExtension(asset.getName());
        assetName = assetName.replace("_black_48dp", "");
        assetName = assetName.replace("ic_", "");
        return assetName;
    }

    private void loadCategories() {
        if (assetRoot == null ||
            assetRoot.getCanonicalPath() == null) {
            return;
        }
        File root = new File(assetRoot.getCanonicalPath());
        for (File category : root.listFiles(folderFileNameFiler)) {
            categoryDirs.add(category);
            categories.add(category.getName());
        }
    }

    @Override
    public String getPath() {
        if (assetRoot != null) {
            return assetRoot.getCanonicalPath();
        }
        return "";
    }

    @Override
    public void openBrowser() {
        BrowserUtil.open(MATERIAL_ICONS_URL);
    }

    @Override
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public List<Asset> getAssets() {
        return assets;
    }

    @Override
    public VirtualFile getRoot() {
        return assetRoot;
    }

    @Override
    public List<Asset> getAssets(IIconsImporterController iconImporterController) {
        final String selectedCategory = iconImporterController.getSelectedCategory();
        if (!isInitialized() ||
            selectedCategory == null) {
            return new ArrayList<Asset>();
        }
        File assetRoot = new File(getPath());
        assetRoot = new File(assetRoot, selectedCategory);
        assetRoot = new File(assetRoot, DEFAULT_RESOLUTION);
        File[] assets = assetRoot.listFiles(getAssetFileNameFilter());
        if (assets == null) {
            return new ArrayList<Asset>();
        }
        List<Asset> foundAssets = new ArrayList<Asset>();
        for (File asset : assets) {
            foundAssets.add(new Asset(IconPack.MATERIAL_ICONS, getAssetName(asset), selectedCategory, Resolution.XHDPI));
        }
        return foundAssets;
    }

    @Override
    public List<String> getSizes(IIconsImporterController iconImporterController) {
        if (!isInitialized() ||
            iconImporterController.getSelectedCategory() == null ||
            iconImporterController.getSelectedAsset() == null) {
            return new ArrayList<String>();
        }
        File assetRoot = new File(getPath());
        assetRoot = new File(assetRoot, iconImporterController.getSelectedCategory());
        assetRoot = new File(assetRoot, DEFAULT_RESOLUTION);
        final String assetName = iconImporterController.getSelectedAsset().getName();
        final FilenameFilter drawableFileNameFiler = getAssetFileNameFilter(assetName);
        File[] assets = assetRoot.listFiles(drawableFileNameFiler);
        Set<String> sizes = new HashSet<String>();
        for (File asset : assets) {
            String drawableName = FilenameUtils.removeExtension(asset.getName());
            String[] numbers = drawableName.replaceAll("[^-?0-9]+", " ").trim().split(" ");
            drawableName = numbers[numbers.length - 1].trim() + "dp";
            sizes.add(drawableName);
        }
        List<String> list = new ArrayList<String>();
        list.addAll(sizes);
        Collections.sort(list);
        return list;
    }

    @NotNull
    private FilenameFilter getAssetFileNameFilter() {
        return getAssetFileNameFilter(null);
    }

    @NotNull
    private FilenameFilter getAssetFileNameFilter(final String assetName) {
        return getAssetFileNameFilter(assetName, null);
    }

    @NotNull
    private FilenameFilter getAssetFileNameFilter(final String assetName, final String size) {
        return new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (!FilenameUtils.isExtension(s, "png")) {
                    return false;
                }
                String filename = FilenameUtils.removeExtension(s);
                if (assetName == null) {
                    return filename.startsWith("ic_") &&
                           filename.endsWith("_black_48dp");
                } else {
                    if (size == null) {
                        return filename.startsWith("ic_" + assetName + "_");
                    } else {
                        return filename.startsWith("ic_" + assetName + "_") &&
                               filename.endsWith("_" + size);
                    }
                }
            }
        };
    }

    @Override
    public List<String> getColors(IIconsImporterController iconImporterController) {
        if (!isInitialized() ||
            iconImporterController.getSelectedCategory() == null ||
            iconImporterController.getSelectedAsset() == null ||
            iconImporterController.getSelectedSize() == null) {
            return new ArrayList<String>();
        }
        File assetRoot = new File(getPath());
        assetRoot = new File(assetRoot, iconImporterController.getSelectedCategory());
        assetRoot = new File(assetRoot, DEFAULT_RESOLUTION);
        final String assetName = iconImporterController.getSelectedAsset().getName();
        final String assetSize = iconImporterController.getSelectedSize();
        File[] assets = assetRoot.listFiles(getAssetFileNameFilter(assetName, assetSize));
        Set<String> colors = new HashSet<String>();
        for (File asset : assets) {
            String drawableName = FilenameUtils.removeExtension(asset.getName());
            String[] color = drawableName.split("_");
            drawableName = color[color.length - 2].trim();
            colors.add(drawableName);
        }
        List<String> list = new ArrayList<String>();
        list.addAll(colors);
        Collections.sort(list);
        return list;
    }

    @Override
    public boolean isSupportedResolution(Resolution resolution) {
        return SUPPORTED_RESOLUTIONS.contains(resolution);
    }

    @Override
    public File getImageFile(Asset asset, String color, String size, Resolution resolution) {
        return new File(getPath(),
                        String.format("%s/drawable-%s/ic_%s_%s_%s.png",
                                      asset.getCategory(),
                                      resolution.getName(),
                                      asset.getName(),
                                      color,
                                      size));
    }

    @Override
    public File getImageFile(Asset asset) {
        return getImageFile(asset, "black", "24dp", Resolution.MDPI);
    }

    @Override
    public void openHelp() {
        try {
            BrowserUtil.browse("file://" + new File(getPath(), "index.html").getCanonicalPath());
        } catch (IOException ignored) {
        }
    }

    @Override
    public boolean isInitialized() {
        return !TextUtils.isEmpty(getPath());
    }
}
