package de.mprengemann.intellij.plugin.androidicons.controllers;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.defaults.IDefaultsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.androidicons.IAndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.materialicons.IMaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;

public interface IControllerFactory {

    void setProject(Project project);

    IAndroidIconsController getAndroidIconsController();

    IMaterialIconsController getMaterialIconsController();

    IDefaultsController getDefaultsController();

    ISettingsController getSettingsController();

    void tearDown();

}
