package de.mprengemann.intellij.plugin.androidicons.controllers.defaults;

import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.util.Set;

public interface IDefaultsController {
    Set<Resolution> getResolutions();
    void setResolutions(Set<Resolution> resolutions);

    ImageAsset getImageAsset();
    void setImageAsset(ImageAsset imageAsset);

    Resolution getSourceResolution();
    void setSourceResolution(Resolution sourceResolution);

    String getSize();
    void setSize(String size);

    String getColor();
    void setColor(String color);

    ResizeAlgorithm getAlgorithm();
    void setAlgorithm(ResizeAlgorithm algorithm);

    String getMethod();
    void setMethod(String method);

    void tearDown();
}
