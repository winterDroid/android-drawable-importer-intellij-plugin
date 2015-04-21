package de.mprengemann.intellij.plugin.androidicons.controllers.materialicons;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialIconsController implements IMaterialIconsController {

    private static final String MATERIAL_ICONS_URL = "https://github.com/google/material-design-icons/releases";
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
    private final FilenameFilter drawableFileNameFiler = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (!FilenameUtils.isExtension(s, "png")) {
                return false;
            }
            String filename = FilenameUtils.removeExtension(s);
            return filename.startsWith("ic_") &&
                   filename.endsWith("_black_48dp");
        }
    };
    private Set<MaterialIconsObserver> observerSet;
    private List<File> categoryDirs = new ArrayList<File>();
    private List<String> categories = new ArrayList<String>();
    private List<String> assets = new ArrayList<String>();
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
            for (File asset : exDensity.listFiles(drawableFileNameFiler)) {
                String assetName = FilenameUtils.removeExtension(asset.getName());
                assetName = assetName.replace("_black_48dp", "");
                assetName = assetName.replace("ic_", "");
                assets.add(assetName);
            }
        }
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
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public List<String> getAssets() {
        return assets;
    }

    @Override
    public VirtualFile getRoot() {
        return assetRoot;
    }

    @Override
    public void openBrowser() {
        BrowserUtil.open(MATERIAL_ICONS_URL);
    }
}
