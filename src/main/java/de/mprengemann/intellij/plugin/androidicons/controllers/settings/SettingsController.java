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
    private Project project;

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
    public void saveResRootForProject(String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(RES_ROOT, fileUrl);
    }

    @Override
    public VirtualFile getResourceRoot() {
        String persistedFile = getResourceRootPath();
        if (persistedFile != null) {
            return VirtualFileManager.getInstance().findFileByUrl(persistedFile);
        } else {
            return null;
        }
    }

    @Override
    public String getResourceRootPath() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        return propertiesComponent.getValue(RES_ROOT);
    }

    @Override
    public String getLastImageFolder() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        return propertiesComponent.getValue(LAST_FOLDER_ROOT);
    }

    @Override
    public void saveLastImageFolder(String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(LAST_FOLDER_ROOT, fileUrl);
    }

    @Override
    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void tearDown() {
        project = null;
        observerSet.clear();
        observerSet = null;
    }
}
