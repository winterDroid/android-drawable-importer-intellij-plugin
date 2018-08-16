package de.mprengemann.intellij.plugin.androidicons.controllers.defaults;

import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.model.Destination;
import de.mprengemann.intellij.plugin.androidicons.model.Format;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DefaultsController implements IDefaultsController {

    public static final HashSet<Resolution> DEFAULT_RESOLUTIONS = new HashSet<Resolution>(Arrays.asList(Resolution.MDPI,
                                                                                                        Resolution.HDPI,
                                                                                                        Resolution.XHDPI,
                                                                                                        Resolution.XXHDPI,
                                                                                                        Resolution.XXXHDPI));
    public static final Resolution DEFAULT_SOURCE_RESOLUTION = Resolution.XHDPI;
    public static final ResizeAlgorithm DEFAULT_ALGORITHM = ResizeAlgorithm.SCALR;
    public static final String DEFAULT_METHOD = DEFAULT_ALGORITHM.getMethods().get(0);
    public static final Format DEFAULT_FORMAT = Format.PNG;
    public static final Destination DEFAULT_DESTINATION = Destination.DRAWABLE;

    private Set<Resolution> resolutions;
    private Resolution sourceResolution;

    private ImageAsset imageAsset;
    private ISettingsController settingsController;

    private ResizeAlgorithm algorithm;
    private String method;
    private Format format;
    private Destination destination;

    private String size;
    private String color;

    public DefaultsController(ISettingsController settingsController) {
        this.settingsController = settingsController;
    }

    @Override
    public Set<Resolution> getResolutions() {
        return resolutions;
    }

    @Override
    public void setResolutions(Set<Resolution> resolutions) {
        this.resolutions = resolutions;
        settingsController.saveResolutions(this.resolutions);
    }

    @Override
    public ImageAsset getImageAsset() {
        return imageAsset;
    }

    @Override
    public void setImageAsset(ImageAsset imageAsset) {
        this.imageAsset = imageAsset;
        settingsController.saveImageAsset(this.imageAsset);
    }

    @Override
    public Resolution getSourceResolution() {
        return sourceResolution;
    }

    @Override
    public void setSourceResolution(Resolution sourceResolution) {
        this.sourceResolution = sourceResolution;
        settingsController.saveSourceResolution(this.sourceResolution);
    }

    @Override
    public String getSize() {
        return size;
    }

    @Override
    public void setSize(String size) {
        this.size = size;
        settingsController.saveSize(this.size);
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public void setColor(String color) {
        this.color = color;
        settingsController.saveColor(this.color);
    }

    @Override
    public ResizeAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public void setAlgorithm(ResizeAlgorithm algorithm) {
        this.algorithm = algorithm;
        settingsController.saveAlgorithm(this.algorithm);
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
        settingsController.saveMethod(this.method);
    }

    @Override
    public Format getFormat() {
        return format;
    }

    @Override
    public void setFormat(Format format) {
        this.format = format;
        settingsController.saveFormat(this.format);
    }

    @Override
    public Destination getDestination() {
        return destination;
    }

    @Override
    public void setDestination(Destination destination) {
        this.destination = destination;
        settingsController.saveDestination(this.destination);
    }

    @Override
    public void restore() {
        imageAsset = settingsController.getImageAsset();
        resolutions = settingsController.getResolutions(DEFAULT_RESOLUTIONS);
        sourceResolution = settingsController.getSourceResolution(DEFAULT_SOURCE_RESOLUTION);
        algorithm = settingsController.getAlgorithm(DEFAULT_ALGORITHM);
        format = settingsController.getFormat(DEFAULT_FORMAT);
        destination = settingsController.getDestination(DEFAULT_DESTINATION);
        method = settingsController.getMethod(DEFAULT_METHOD);
        color = settingsController.getColor();
        size = settingsController.getSize();
    }

    @Override
    public void tearDown() {
        resolutions = null;
        imageAsset = null;
        sourceResolution = null;
        size = null;
        color = null;
        algorithm = null;
        format = null;
        destination = null;
        method = null;
        settingsController = null;
    }
}
