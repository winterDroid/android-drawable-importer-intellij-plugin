package de.mprengemann.intellij.plugin.androidicons.controllers.settings;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.util.HashSet;
import java.util.Set;

public class SettingsController implements ISettingsController {

    private static final String RES_ROOT = "resourcesRoot";
    private static final String LAST_FOLDER_ROOT = "lastFolderRoot";
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
