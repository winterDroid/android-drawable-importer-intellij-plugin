package de.mprengemann.intellij.plugin.androidicons.controllers.androidicons;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.mprengemann.intellij.plugin.androidicons.model.Asset;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.ExportNameUtils;
import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AndroidIconsController implements IAndroidIconsController {

    private static final String ANDROID_ICONS_URL = "http://www.androidicons.com/";
    private static final EnumSet<Resolution> SUPPORTED_RESOLUTIONS = EnumSet.of(Resolution.LDPI,
                                                                                Resolution.MDPI,
                                                                                Resolution.HDPI,
                                                                                Resolution.XHDPI,
                                                                                Resolution.XXHDPI);
    private Set<AndroidIconsObserver> observerSet;
    private VirtualFile assetRoot;
    private final FilenameFilter systemFileNameFiler = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return !s.startsWith(".");
        }
    };
    private final Comparator<File> alphabeticalComparator = new Comparator<File>() {
        @Override
        public int compare(File file1, File file2) {
            if (file1 != null && file2 != null) {
                return file1.getName().compareTo(file2.getName());
            }
            return 0;
        }
    };
    private List<File> colorDirs = new ArrayList<File>();
    private List<String> colors = new ArrayList<String>();
    private List<Asset> assets = new ArrayList<Asset>();

    public AndroidIconsController() {
        observerSet = new HashSet<AndroidIconsObserver>();
        restorePath();
        load();
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
        return String.format("assetPath_%s", IconPack.ANDROID_ICONS.toString());
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
        colors.clear();
        colorDirs.clear();
        assets.clear();
        notifyUpdated();
    }

    @Override
    public void addObserver(AndroidIconsObserver observer) {
        observerSet.add(observer);
        notifyUpdated();
    }

    @Override
    public void removeObserver(AndroidIconsObserver observer) {
        observerSet.remove(observer);
    }

    @Override
    public void setPath(VirtualFile file) {
        this.assetRoot = file;
        load();
    }

    @Override
    public String getPath() {
        if (assetRoot != null) {
            return assetRoot.getCanonicalPath();
        }
        return "";
    }

    @Override
    public List<String> getColors() {
        return colors;
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
    public void openBrowser() {
        BrowserUtil.open(ANDROID_ICONS_URL);
    }

    @Override
    public List<String> getSizes() {
        return Collections.singletonList("18dp");
    }

    @Override
    public boolean isInitialized() {
        return !TextUtils.isEmpty(getPath());
    }

    @Override
    public File getImageFile(Asset asset) {
        return getImageFile(asset, "black", Resolution.LDPI);
    }

    @Override
    public boolean isSupprtedResolution(Resolution resolution) {
        return SUPPORTED_RESOLUTIONS.contains(resolution);
    }

    @Override
    public File getImageFile(Asset asset, String color, Resolution resolution) {
        return new File(getPath(),
                        String.format("%s/%s/ic_action_%s.png",
                                      color != null ? color.replace(" ", "_") : "",
                                      resolution.getName(),
                                      asset.getName()));
    }

    public void load() {
        loadColors();
        loadAssets();
        notifyUpdated();
    }

    private void notifyUpdated() {
        for (AndroidIconsObserver observer : observerSet) {
            observer.updated(IconPack.ANDROID_ICONS);
        }
    }

    private void loadAssets() {
        if (colorDirs.size() < 1) {
            return;
        }
        File exColorDir = colorDirs.get(0);
        final FilenameFilter systemFileNameFiler = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return !s.startsWith(".");
            }
        };
        File[] densities = exColorDir.listFiles(systemFileNameFiler);
        if (densities == null || densities.length < 1) {
            return;
        }
        File exDensity = densities[0];
        File[] assetFiles = exDensity.listFiles(systemFileNameFiler);
        if (assetFiles == null ||
            assetFiles.length < 1) {
            return;
        }
        Arrays.sort(assetFiles, alphabeticalComparator);
        for (File asset : assetFiles) {
            if (asset.isDirectory()) {
                continue;
            }
            String extension = asset.getName().substring(asset.getName().lastIndexOf(".") + 1);
            if (!extension.equalsIgnoreCase("png")) {
                continue;
            }
            String assetName = ExportNameUtils.getExportNameFromFilename(asset.getName()).replace("ic_action_", "");
            assets.add(new Asset(IconPack.ANDROID_ICONS, assetName, Resolution.XHDPI));
        }
    }

    private void loadColors() {
        if (!isInitialized()) {
            return;
        }
        final File assetRoot = new File(getPath());
        File[] colorDirs = assetRoot.listFiles(systemFileNameFiler);
        Arrays.sort(colorDirs, alphabeticalComparator);
        for (File file : colorDirs) {
            if (!file.isDirectory()) {
                continue;
            }
            this.colorDirs.add(file);
            this.colors.add(file.getName().replace("_", " "));
        }
    }

    @Override
    public void tearDown() {
        savePath();
        observerSet.clear();
        observerSet = null;
    }
}
