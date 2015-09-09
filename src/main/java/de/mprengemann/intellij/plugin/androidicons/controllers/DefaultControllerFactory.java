package de.mprengemann.intellij.plugin.androidicons.controllers;

import de.mprengemann.intellij.plugin.androidicons.controllers.androidicons.AndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.androidicons.IAndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.filepicker.FilePickerController;
import de.mprengemann.intellij.plugin.androidicons.controllers.filepicker.IFilePickerController;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IIconsImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.materialicons.IMaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.materialicons.MaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.SettingsController;

public class DefaultControllerFactory implements IControllerFactory {

    private IAndroidIconsController androidIconsController;
    private IMaterialIconsController materialIconsController;
    private ISettingsController settingsController;
    private IIconsImporterController iconImporterController;
    private IFilePickerController filePickerController;

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
    public IFilePickerController getFilePickerController() {
        if (filePickerController == null) {
            filePickerController = new FilePickerController();
        }
        return filePickerController;
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

        if (iconImporterController != null) {
            iconImporterController.tearDown();
            iconImporterController = null;
        }

        if (filePickerController != null) {
            filePickerController.tearDown();
            filePickerController = null;
        }
    }
}
