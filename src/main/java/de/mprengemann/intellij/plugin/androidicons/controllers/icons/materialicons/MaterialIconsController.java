package de.mprengemann.intellij.plugin.androidicons.controllers.icons.materialicons;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.ide.BrowserUtil;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.resources.ResourceLoader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MaterialIconsController implements IMaterialIconsController {

    private IconPack iconPack;
    private Map<String, List<ImageAsset>> categoryMap;

    public MaterialIconsController(IconPack iconPack) {
        this.iconPack = iconPack;
        this.categoryMap = initCategoryMap(iconPack);
    }

    private HashMap<String, List<ImageAsset>> initCategoryMap(IconPack iconPack) {
        final HashMap<String, List<ImageAsset>> categoryMap = Maps.newHashMap();
        if (iconPack == null) {
            return categoryMap;
        }
        for (String category : iconPack.getCategories()) {
            categoryMap.put(category, Lists.<ImageAsset>newArrayList());
        }
        for (ImageAsset asset : iconPack.getAssets()) {
            categoryMap.get(asset.getCategory()).add(asset);
        }
        return categoryMap;
    }

    @Override
    public String getId() {
        return iconPack.getId();
    }

    @Override
    public IconPack getIconPack() {
        return iconPack;
    }

    @Override
    public boolean supportsVectors() {
        return true;
    }

    @Override
    public Resolution getThumbnailResolution() {
        return Resolution.MDPI;
    }

    @Override
    public List<ImageAsset> getAssets(String category) {
        return categoryMap.get(category);
    }

    @Override
    public List<ImageAsset> getAssets(List<String> categories) {
        return iconPack.getAssets();
    }

    @Override
    public List<String> getCategories() {
        return iconPack.getCategories();
    }

    @Override
    public void openHelp() {
        BrowserUtil.browse(iconPack.getUrl());
    }

    @Override
    public File getImageFile(ImageAsset asset, String color, Resolution resolution) {
        return getImageFile(asset, color, "24dp", resolution);
    }

    @Override
    public File getImageFile(ImageAsset asset, String color, String size, Resolution resolution) {
        final String localPath;
        if (resolution == Resolution.ANYDPI) {
            localPath = getVectorFilePath(asset);
        } else {
            localPath = getImageFilePath(asset, color, size, resolution);
        }
        return ResourceLoader.getAssetResource(new File(iconPack.getPath(), localPath).getPath());
    }

    private String getImageFilePath(ImageAsset asset, String color, String size, Resolution resolution) {
        return String.format("%s/drawable-%s/%s_%s_%s.png",
                                               asset.getCategory(),
                                               resolution.toString().toLowerCase(Locale.ENGLISH),
                                               asset.getName(),
                                               color,
                                               size);
    }

    private String getVectorFilePath(ImageAsset asset) {
        return String.format("%s/drawable-%s-v21/%s_black_24dp.xml",
                             asset.getCategory(),
                             Resolution.ANYDPI.toString().toLowerCase(Locale.ENGLISH),
                             asset.getName());
    }

    @Override
    public void tearDown() {
        iconPack = null;
    }
}
