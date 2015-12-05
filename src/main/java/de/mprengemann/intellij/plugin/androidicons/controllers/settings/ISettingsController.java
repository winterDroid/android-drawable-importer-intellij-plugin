package de.mprengemann.intellij.plugin.androidicons.controllers.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;

public interface ISettingsController extends IController<SettingsObserver> {
    void saveResRootForProject(String fileUrl);

    VirtualFile getResourceRoot();

    String getResourceRootPath();

    String getLastImageFolder();

    void saveLastImageFolder(String fileUrl);

    void setProject(Project project);
}
