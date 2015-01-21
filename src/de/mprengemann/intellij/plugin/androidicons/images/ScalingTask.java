package de.mprengemann.intellij.plugin.androidicons.images;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbModeTask;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import de.mprengemann.intellij.plugin.androidicons.util.ResizeAlgorithm;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScalingTask extends DumbModeTask {

    private Project project;
    private final File imageFile;
    private final int targetWidth;
    private final int targetHeight;

    private float toLDPI;
    private float toMDPI;
    private float toHDPI;
    private float toXHDPI;
    private float toXXHDPI;
    private float toXXXHDPI;
    private boolean scaleToLDPI;
    private boolean scaleToMDPI;
    private boolean scaleToHDPI;
    private boolean scaleToXHDPI;
    private boolean scaleToXXHDPI;
    private boolean scaleToXXXHDPI;

    private String path;
    private String exportName;
    private ResizeAlgorithm algorithm;
    private Object method;
    private boolean isNinePatch;

    public ScalingTask(Project project,
                       File imageFile,
                       int targetWidth,
                       int targetHeight,
                       String path,
                       String exportName,
                       ResizeAlgorithm algorithm,
                       Object method,
                       boolean isNinePatch) {
        this.project = project;
        this.imageFile = imageFile;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.path = path;
        this.exportName = exportName;
        this.algorithm = algorithm;
        this.method = method;
        this.isNinePatch = isNinePatch;
    }

    @Override
    public void performInDumbMode(@NotNull final ProgressIndicator progressIndicator) {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                try {
                    scaleImages(progressIndicator);
                } catch (ProcessCanceledException ignored) {
                } catch (IOException ignored) {
                }
            }
        });
    }

    private void scaleImages(ProgressIndicator indicator) throws IOException, ProcessCanceledException {
        indicator.setText("Scale images");
        indicator.setIndeterminate(true);
        indicator.checkCanceled();

        final List<ScalingImageInformation> scalingInformationList = getScalingInformation();

        indicator.setIndeterminate(false);
        for (int i = 0; i < scalingInformationList.size(); i++) {
            ScalingImageInformation information = scalingInformationList.get(i);
            scaleImage(indicator, information);
            indicator.setFraction((i + 1)  / scalingInformationList.size());
        }

        DumbService.getInstance(project).runWhenSmart(new Runnable() {
            @Override
            public void run() {
                try {
                    RefactorHelper.move(project, scalingInformationList);
                } catch (IOException ignored) {
                }
            }
        });
    }

    private void scaleImage(ProgressIndicator indicator, ScalingImageInformation information) {
        indicator.checkCanceled();
        information.setTempImage(exportTempImage(information));
        information.setTargetImage(ImageUtils.getTargetFile(information));
    }

    private List<ScalingImageInformation> getScalingInformation() {
        List<ScalingImageInformation> scalingInformationList = new ArrayList<ScalingImageInformation>();
        if (scaleToLDPI) {
            scalingInformationList.add(new ScalingImageInformation(imageFile, Resolution.LDPI, toLDPI, targetWidth, targetHeight, path, exportName));
        }
        if (scaleToMDPI) {
            scalingInformationList.add(new ScalingImageInformation(imageFile, Resolution.MDPI, toMDPI, targetWidth, targetHeight, path, exportName));
        }
        if (scaleToHDPI) {
            scalingInformationList.add(new ScalingImageInformation(imageFile, Resolution.HDPI, toHDPI, targetWidth, targetHeight, path, exportName));
        }
        if (scaleToXHDPI) {
            scalingInformationList.add(new ScalingImageInformation(imageFile, Resolution.XHDPI, toXHDPI, targetWidth, targetHeight, path, exportName));
        }
        if (scaleToXXHDPI) {
            scalingInformationList.add(new ScalingImageInformation(imageFile, Resolution.XXHDPI, toXXHDPI, targetWidth, targetHeight, path, exportName));
        }
        if (scaleToXXXHDPI) {
            scalingInformationList.add(new ScalingImageInformation(imageFile, Resolution.XXXHDPI, toXXXHDPI, targetWidth, targetHeight, path, exportName));
        }
        return scalingInformationList;
    }

    public void addLDPI(float toLDPI) {
        this.scaleToLDPI = true;
        this.toLDPI = toLDPI;
    }

    public void addMDPI(float toMDPI) {
        this.scaleToMDPI = true;
        this.toMDPI = toMDPI;
    }

    public void addHDPI(float toHDPI) {
        this.scaleToHDPI = true;
        this.toHDPI = toHDPI;
    }

    public void addXHDPI(float toXHDPI) {
        this.scaleToXHDPI = true;
        this.toXHDPI = toXHDPI;
    }

    public void addXXHDPI(float toXXHDPI) {
        this.scaleToXXHDPI = true;
        this.toXXHDPI = toXXHDPI;
    }

    public void addXXXHDPI(float toXXXHDPI) {
        this.scaleToXXXHDPI = true;
        this.toXXXHDPI = toXXXHDPI;
    }

    private File exportTempImage(ScalingImageInformation information) {
        try {
            BufferedImage resizeImageJpg;
            if (isNinePatch) {
                resizeImageJpg = ImageUtils.resizeNinePatchImage(algorithm,
                                                                 method,
                                                                 project,
                                                                 information);
            } else {
                resizeImageJpg = ImageUtils.resizeNormalImage(algorithm,
                                                              method,
                                                              information);
            }

            return ImageUtils.saveImageTempFile(information.getResolution(),
                                                resizeImageJpg,
                                                project,
                                                exportName);

        } catch (Exception ignored) {
        }
        return null;
    }
}
