package de.mprengemann.intellij.plugin.androidicons.controllers.defaults;

import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DefaultsController implements IDefaultsController {

    private Set<Resolution> resolutions;
    private Resolution sourceResolution;

    private ImageAsset imageAsset;

    private ResizeAlgorithm algorithm;
    private String method;

    private ISettingsController settingsController;
    private String size;
    private String color;

    public DefaultsController(ISettingsController settingsController) {
        this.settingsController = settingsController;
        resolutions = new HashSet<Resolution>(Arrays.asList(Resolution.MDPI,
                                                            Resolution.HDPI,
                                                            Resolution.XHDPI,
                                                            Resolution.XXHDPI,
                                                            Resolution.XXXHDPI));
        sourceResolution = Resolution.XHDPI;
        algorithm = ResizeAlgorithm.SCALR;
        method = this.algorithm.getMethods().get(0);
    }

    @Override
    public Set<Resolution> getResolutions() {
        return resolutions;
    }

    @Override
    public void setResolutions(Set<Resolution> resolutions) {
        this.resolutions = resolutions;
    }

    @Override
    public ImageAsset getImageAsset() {
        return imageAsset;
    }

    @Override
    public void setImageAsset(ImageAsset imageAsset) {
        this.imageAsset = imageAsset;
    }

    @Override
    public Resolution getSourceResolution() {
        return sourceResolution;
    }

    @Override
    public void setSourceResolution(Resolution sourceResolution) {
        this.sourceResolution = sourceResolution;
    }

    @Override
    public String getSize() {
        return size;
    }

    @Override
    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public ResizeAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public void setAlgorithm(ResizeAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public void tearDown() {
        resolutions = null;
        imageAsset = null;
        sourceResolution = null;
        size = null;
        color = null;
        algorithm = null;
        method = null;
        settingsController = null;
    }
}
