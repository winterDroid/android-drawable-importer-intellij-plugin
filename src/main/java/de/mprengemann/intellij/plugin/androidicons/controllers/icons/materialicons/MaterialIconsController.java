package de.mprengemann.intellij.plugin.androidicons.controllers.icons.materialicons;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.resources.ResourceLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialIconsController implements IMaterialIconsController {

    private IconPack iconPack;
    private Map<String, List<ImageAsset>> categoryMap;

    public MaterialIconsController(IconPack iconPack) {
        this.iconPack = iconPack;
        this.categoryMap = initCategoryMap(iconPack);
    }

    private HashMap<String, List<ImageAsset>> initCategoryMap(IconPack iconPack) {
        final HashMap<String, List<ImageAsset>> categoryMap = new HashMap<String, List<ImageAsset>>();
        for (String category : iconPack.getCategories()) {
            categoryMap.put(category, new ArrayList<ImageAsset>());
        }
        for (ImageAsset asset : iconPack.getAssets()) {
            categoryMap.get(asset.getCategory()).add(asset);
        }
        return categoryMap;
    }

    @Override
    public void restorePath() {

    }

    @Override
    public void savePath() {

    }

    @Override
    public void setPath(VirtualFile file) {

    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getId() {
        return iconPack.getId();
    }

    @Override
    public String getIconPackName() {
        return iconPack.getName();
    }

    @Override
    public IconPack getIconPack() {
        return iconPack;
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
    public VirtualFile getRoot() {
        return null;
    }

    @Override
    public void openBrowser() {

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
        final String localPath = String.format("%s/drawable-%s/%s_%s_%s.png",
                                               asset.getCategory(),
                                               resolution.toString(),
                                               asset.getName(),
                                               color,
                                               size);
        return ResourceLoader.getFile(new File(iconPack.getPath(), localPath).getPath());
    }

    @Override
    public boolean isSupportedResolution(Resolution resolution) {
        return false;
    }

    @Override
    public void tearDown() {
        iconPack = null;
    }
}
