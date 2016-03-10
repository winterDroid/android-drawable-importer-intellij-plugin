package de.mprengemann.intellij.plugin.androidicons.controllers.icons.androidicons;

import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.resources.ResourceLoader;

import java.io.File;
import java.util.List;

public class AndroidIconsController implements IAndroidIconsController {

    private IconPack iconPack;

    public AndroidIconsController(IconPack iconPack) {
        this.iconPack = iconPack;
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
        return false;
    }

    @Override
    public Resolution getThumbnailResolution() {
        return Resolution.LDPI;
    }

    @Override
    public List<ImageAsset> getAssets(String category) {
        return iconPack.getAssets();
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
    public File getImageFile(ImageAsset asset, String color, Resolution resolution) {
        return getImageFile(asset, color, null, resolution);
    }

    @Override
    public File getImageFile(ImageAsset asset, String color, String size, Resolution resolution) {
        if (resolution == Resolution.ANYDPI) {
            throw new IllegalStateException("Vectors not supported by AndroidIcons");
        }
        final String localPath = String.format("%s/%s/%s.png",
                                               color,
                                               resolution.toString().toLowerCase(),
                                               asset.getName());
        return ResourceLoader.getAssetResource(new File(iconPack.getPath(), localPath).getPath());
    }

    @Override
    public void tearDown() {
        iconPack = null;
    }
}
