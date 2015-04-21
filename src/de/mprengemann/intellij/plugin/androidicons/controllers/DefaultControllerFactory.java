package de.mprengemann.intellij.plugin.androidicons.controllers;

import de.mprengemann.intellij.plugin.androidicons.controllers.androidicons.AndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.androidicons.IAndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.materialicons.IMaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.materialicons.MaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.SettingsController;

public class DefaultControllerFactory implements IControllerFactory {

    private IAndroidIconsController androidIconsController;
    private IMaterialIconsController materialIconsController;
    private ISettingsController settingsController;

    @Override
    public IAndroidIconsController getAndroidIconsController() {
        if (androidIconsController == null) {
            androidIconsController = new AndroidIconsController();
        }
        return androidIconsController;
    }

    @Override
    public IMaterialIconsController getMaterialIconsController() {
        if (materialIconsController == null) {
            materialIconsController = new MaterialIconsController();
        }
        return materialIconsController;
    }

    @Override
    public ISettingsController getSettingsController() {
        if (settingsController == null) {
            settingsController = new SettingsController();
        }
        return settingsController;
    }

    @Override
    public void tearDown() {
        if (materialIconsController != null) {
            materialIconsController.tearDown();
            materialIconsController = null;
        }

        if (androidIconsController != null) {
            androidIconsController.tearDown();
            androidIconsController = null;
        }

        if (settingsController != null) {
            settingsController.tearDown();
            settingsController = null;
        }
    }
}
