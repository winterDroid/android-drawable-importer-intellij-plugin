package de.mprengemann.intellij.plugin.androidicons.controllers.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;

public interface ISettingsController extends IController<SettingsObserver> {
    VirtualFile getAssetPath(IconPack iconPack);

    void saveAssetPath(IconPack iconPack, String fileUrl);

    void saveAssetPath(IconPack iconPack, VirtualFile file);

    String getAssetPathString(IconPack iconPack);

    String getAssetPathKey(IconPack iconPack);

    void clearAssetPath(IconPack iconPack);

    void saveResRootForProject(Project project, String fileUrl);

    VirtualFile getResRootForProject(Project project);

    String getResRootStringForProject(Project project);

    String getLastImageFolder(Project project);

    void saveLastImageFolder(Project project, String fileUrl);
}
