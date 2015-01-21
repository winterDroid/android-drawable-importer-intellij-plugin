package de.mprengemann.intellij.plugin.androidicons.images;

import java.io.File;

public class ScalingImageInformation {

    private final File imageFile;
    private final Resolution resolution;
    private final float factor;
    private final int targetWidth;
    private final int targetHeight;
    private final String exportPath;
    private final String exportName;
    private File tempImage;
    private File targetFile;

    public ScalingImageInformation(File imageFile,
                                   Resolution resolution,
                                   float factor,
                                   int targetWidth,
                                   int targetHeight,
                                   String exportPath,
                                   String exportName) {
        this.imageFile = imageFile;
        this.resolution = resolution;
        this.factor = factor;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.exportPath = exportPath;
        this.exportName = exportName;
    }

    public void setTempImage(File tempImage) {
        this.tempImage = tempImage;
    }

    public void setTargetImage(File targetFile) {
        this.targetFile = targetFile;
    }

    public File getTempImage() {
        return tempImage;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public File getImageFile() {
        return imageFile;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public float getFactor() {
        return factor;
    }

    public int getTargetWidth() {
        return targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public String getExportPath() {
        return exportPath;
    }

    public String getExportName() {
        return exportName;
    }
}