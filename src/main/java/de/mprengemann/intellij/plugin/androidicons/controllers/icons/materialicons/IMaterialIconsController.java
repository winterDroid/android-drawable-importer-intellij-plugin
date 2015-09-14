package de.mprengemann.intellij.plugin.androidicons.controllers.icons.materialicons;

import de.mprengemann.intellij.plugin.androidicons.controllers.icons.IIconPackController;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;

public interface IMaterialIconsController extends IIconPackController {

    void openHelp();

    boolean isSupportedResolution(Resolution resolution);
}
