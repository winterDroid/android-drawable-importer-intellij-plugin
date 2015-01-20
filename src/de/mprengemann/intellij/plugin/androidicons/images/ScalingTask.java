package de.mprengemann.intellij.plugin.androidicons.images;

import com.intellij.openapi.application.ApplicationManager;
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
                } catch (IOException ignored) {
                }
            }
        });
    }

    private void scaleImages(ProgressIndicator indicator) throws IOException {
        indicator.setIndeterminate(true);
        indicator.setText("Scale images");

        final List<File> sources = new ArrayList<File>();
        final List<File> targets = new ArrayList<File>();

        if (scaleToLDPI) {
            try {
                sources.add(exportTempImage(imageFile, "ldpi", toLDPI, targetWidth, targetHeight));
            } catch (IOException e) {
                e.printStackTrace();
            }
            targets.add(getTargetFile("ldpi"));
        }
        if (scaleToMDPI) {
            sources.add(exportTempImage(imageFile, "mdpi", toMDPI, targetWidth, targetHeight));
            targets.add(getTargetFile("mdpi"));
        }
        if (scaleToHDPI) {
            sources.add(exportTempImage(imageFile, "hdpi", toHDPI, targetWidth, targetHeight));
            targets.add(getTargetFile("hdpi"));
        }
        if (scaleToXHDPI) {
            sources.add(exportTempImage(imageFile, "xhdpi", toXHDPI, targetWidth, targetHeight));
            targets.add(getTargetFile("xhdpi"));
        }
        if (scaleToXXHDPI) {
            sources.add(exportTempImage(imageFile, "xxhdpi", toXXHDPI, targetWidth, targetHeight));
            targets.add(getTargetFile("xxhdpi"));
        }
        if (scaleToXXXHDPI) {
            sources.add(exportTempImage(imageFile, "xxxhdpi", toXXXHDPI, targetWidth, targetHeight));
            targets.add(getTargetFile("xxxhdpi"));
        }

        DumbService.getInstance(project).runWhenSmart(new Runnable() {
            @Override
            public void run() {
                try {
                    RefactorHelper.move(project, sources, targets);
                } catch (IOException ignored) {
                }
            }
        });
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


    private File getTargetFile(String resolution) {
        return new File(path + "/drawable-" + resolution + "/" + exportName);
    }

    private File exportTempImage(File imageFile,
                                 String resolution,
                                 float scaleFactor,
                                 int targetWidth,
                                 int targetHeight) throws IOException {
        BufferedImage resizeImageJpg;
        if (isNinePatch) {
            resizeImageJpg = ImageUtils.resizeNinePatchImage(algorithm,
                                                             method,
                                                             scaleFactor,
                                                             targetWidth,
                                                             targetHeight,
                                                             imageFile,
                                                             resolution,
                                                             project,
                                                             path);
        } else {
            resizeImageJpg = ImageUtils.resizeNormalImage(algorithm,
                                                          method,
                                                          imageFile,
                                                          scaleFactor,
                                                          targetWidth,
                                                          targetHeight);
        }

        return ImageUtils.saveImageTempFile(resolution,
                                            resizeImageJpg,
                                            project,
                                            ImageUtils.getExportName("", exportName));
    }
}
