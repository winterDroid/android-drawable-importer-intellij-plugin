package de.mprengemann.intellij.plugin.androidicons.controllers;

import de.mprengemann.intellij.plugin.androidicons.controllers.defaults.DefaultsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.defaults.IDefaultsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IIconsImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.androidicons.AndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.androidicons.IAndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.materialicons.IMaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.materialicons.MaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.SettingsController;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;

public class DefaultControllerFactory implements IControllerFactory {

    private IAndroidIconsController androidIconsController;
    private IMaterialIconsController materialIconsController;
    private IDefaultsController defaultsController;
    private ISettingsController settingsController;
    private IIconsImporterController iconImporterController;

    public DefaultControllerFactory(IconPack androidIcons,
                                    IconPack materialIcons) {
            androidIconsController = new AndroidIconsController(androidIcons);
            materialIconsController = new MaterialIconsController(materialIcons);
    }

    @Override
    public IAndroidIconsController getAndroidIconsController() {
        return androidIconsController;
    }

    @Override
    public IMaterialIconsController getMaterialIconsController() {
        return materialIconsController;
    }

    @Override
    public IDefaultsController getDefaultsController() {
        if (defaultsController == null) {
            defaultsController = new DefaultsController(getSettingsController());
        }
        return defaultsController;
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

        if (defaultsController != null) {
            defaultsController.tearDown();
            defaultsController = null;
        }

        if (settingsController != null) {
            settingsController.tearDown();
            settingsController = null;
        }

        if (iconImporterController != null) {
            iconImporterController.tearDown();
            iconImporterController = null;
        }
    }
}
