package de.mprengemann.intellij.plugin.androidicons.controllers.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;

public interface ISettingsController extends IController<SettingsObserver> {
    void saveResRootForProject(Project project, String fileUrl);

    VirtualFile getResRootForProject(Project project);

    String getResRootStringForProject(Project project);

    String getLastImageFolder(Project project);

    void saveLastImageFolder(Project project, String fileUrl);
}
