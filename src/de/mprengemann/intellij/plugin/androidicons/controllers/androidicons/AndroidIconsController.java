package de.mprengemann.intellij.plugin.androidicons.controllers.androidicons;

import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;
import de.mprengemann.intellij.plugin.androidicons.util.ExportNameUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AndroidIconsController implements IAndroidIconsController {

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
    private List<String> assets = new ArrayList<String>();

    public AndroidIconsController() {
        observerSet = new HashSet<AndroidIconsObserver>();
    }

    @Override
    public void addObserver(AndroidIconsObserver observer) {
        observerSet.add(observer);
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
    public List<String> getAssets() {
        return assets;
    }

    public void load() {
        loadColors();
        loadAssets();
        notifyInitialized();
    }

    private void notifyInitialized() {
        for (AndroidIconsObserver observer : observerSet) {
            observer.onInitialized(IconPack.ANDROID_ICONS);
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
            assets.add(ExportNameUtils.getExportNameFromFilename(asset.getName()).replace("ic_action_", ""));
        }
    }

    private void loadColors() {
        if (this.assetRoot.getCanonicalPath() == null) {
            return;
        }
        final File assetRoot = new File(this.assetRoot.getCanonicalPath());
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
        observerSet.clear();
        observerSet = null;
    }
}
