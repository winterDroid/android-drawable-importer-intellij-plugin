package de.mprengemann.intellij.plugin.androidicons.controllers.materialicons;

import de.mprengemann.intellij.plugin.androidicons.controllers.IIconPackController;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;

public interface IMaterialIconsController extends IIconPackController {

    void openHelp();

    File getImageFile(ImageAsset asset, String color, String size, Resolution resolution);

    boolean isSupportedResolution(Resolution resolution);
}
