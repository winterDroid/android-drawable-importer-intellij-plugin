package de.mprengemann.intellij.plugin.androidicons.controllers.settings;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;

import java.util.HashSet;
import java.util.Set;

public class SettingsController implements ISettingsController {

    private static final String PATH = "assetPath_%s";
    private static final String RES_ROOT = "resourcesRoot";
    private static final String LAST_FOLDER_ROOT = "lastFolderRoot";
    public static final String LEGACY_PATH = "assetPath";
    private Set<SettingsObserver> observerSet;

    public SettingsController() {
        observerSet = new HashSet<SettingsObserver>();
    }

    @Override
    public void addObserver(SettingsObserver observer) {
        observerSet.add(observer);
    }

    @Override
    public void removeObserver(SettingsObserver observer) {
        observerSet.remove(observer);
    }

    @Override
    public VirtualFile getAssetPath(IconPack iconPack) {
        String persistedFile = getAssetPathString(iconPack);
        if (persistedFile != null) {
            return VirtualFileManager.getInstance().findFileByUrl(persistedFile);
        } else {
            return null;
        }
    }

    @Override
    public void saveAssetPath(IconPack iconPack, String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(getAssetPathKey(iconPack), fileUrl);
    }

    @Override
    public void saveAssetPath(IconPack iconPack, VirtualFile file) {
        saveAssetPath(iconPack, file != null ? file.getUrl() : null);
    }

    @Override
    public String getAssetPathString(IconPack iconPack) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        if (propertiesComponent.getValue(LEGACY_PATH) != null && iconPack == IconPack.ANDROID_ICONS) {
            // Legacy support
            String file = propertiesComponent.getValue(LEGACY_PATH);
            propertiesComponent.unsetValue(getAssetPathKey(iconPack));
            saveAssetPath(IconPack.ANDROID_ICONS, file);
            return file;
        }
        return propertiesComponent.getValue(getAssetPathKey(iconPack));
    }

    @Override
    public String getAssetPathKey(IconPack iconPack) {
        return String.format(PATH, iconPack.toString());
    }

    @Override
    public void clearAssetPath(IconPack iconPack) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.unsetValue(getAssetPathKey(iconPack));
    }

    @Override
    public void saveResRootForProject(Project project, String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(RES_ROOT, fileUrl);
    }

    @Override
    public VirtualFile getResRootForProject(Project project) {
        String persistedFile = getResRootStringForProject(project);
        if (persistedFile != null) {
            return VirtualFileManager.getInstance().findFileByUrl(persistedFile);
        } else {
            return null;
        }
    }

    @Override
    public String getResRootStringForProject(Project project) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        return propertiesComponent.getValue(RES_ROOT);
    }

    @Override
    public String getLastImageFolder(Project project) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        return propertiesComponent.getValue(LAST_FOLDER_ROOT);
    }

    @Override
    public void saveLastImageFolder(Project project, String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(LAST_FOLDER_ROOT, fileUrl);
    }

    @Override
    public void tearDown() {
        observerSet.clear();
        observerSet = null;
    }
}
