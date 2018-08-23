package de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.additem;

import com.google.common.base.Objects;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.defaults.IDefaultsController;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.model.Destination;
import de.mprengemann.intellij.plugin.androidicons.model.Format;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.ExportNameUtils;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddItemBatchScaleImporterController implements IAddItemBatchScaleImporterController {

    private static final Logger LOGGER = Logger.getInstance(AddItemBatchScaleImporterController.class);
    private Set<AddItemBatchScaleDialogObserver> observers;
    private Set<Resolution> targetResolutions;
    private Resolution sourceResolution;
    private String exportName;
    private File imageFile;
    private int originalImageWidth;
    private int targetWidth;
    private int targetHeight;
    private String exportRoot;
    private ResizeAlgorithm algorithm;
    private String method;
    private float aspectRatio;
    private boolean isNinePatch;
    private Format format;
    private Destination destination;

    public AddItemBatchScaleImporterController(IDefaultsController defaultsController,
                                               String exportRoot,
                                               File file) {
        this.observers = new HashSet<AddItemBatchScaleDialogObserver>();
        this.targetResolutions = defaultsController.getResolutions();
        init(file);

        final String fileName = file.getName();
        exportName = ExportNameUtils.getExportNameFromFilename(fileName);
        sourceResolution = defaultsController.getSourceResolution();
        algorithm = defaultsController.getAlgorithm();
        method = defaultsController.getMethod();
        this.exportRoot = exportRoot;
        isNinePatch = fileName.endsWith(".9.png");
        format = isNinePatch ? Format.PNG : defaultsController.getFormat();
        destination = defaultsController.getDestination();
    }

    public AddItemBatchScaleImporterController(Resolution sourceResolution,
                                               List<ImageInformation> information) {
        this.observers = new HashSet<AddItemBatchScaleDialogObserver>();
        this.targetResolutions = new HashSet<Resolution>();
        for (ImageInformation imageInformation : information) {
            targetResolutions.add(imageInformation.getTargetResolution());
        }
        final ImageInformation baseInformation = information.get(0);
        init(baseInformation.getImageFile());

        this.exportName = baseInformation.getExportName();
        this.sourceResolution = sourceResolution;
        this.algorithm = baseInformation.getAlgorithm();
        this.method = algorithm.getPrettyMethod(baseInformation.getMethod());
        this.exportRoot = baseInformation.getExportPath();
        this.isNinePatch = baseInformation.isNinePatch();
        this.format = baseInformation.getFormat();
        destination = baseInformation.getDestination();

        this.targetHeight = getOriginalTargetSize(sourceResolution, baseInformation.getTargetResolution(), targetHeight, baseInformation.getFactor());
        this.targetWidth = getOriginalTargetSize(sourceResolution, baseInformation.getTargetResolution(), targetWidth, baseInformation.getFactor());
    }

    private void init(File file) {
        try {
            LOGGER.info(String.format("Adding file %s", file));
            BufferedImage image = ImageIO.read(file);
            imageFile = file;
            originalImageWidth = image.getWidth();
            targetWidth = image.getWidth();
            targetHeight = image.getHeight();
            aspectRatio = (float) image.getHeight() / (float) originalImageWidth;
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @Override
    public String getExportName() {
        return exportName;
    }

    @Override
    public String getExportPath() {
        return exportRoot;
    }

    @Override
    public Set<Resolution> getTargetResolutions() {
        return targetResolutions;
    }

    @Override
    public int getTargetWidth() {
        return targetWidth;
    }

    @Override
    public int getTargetHeight() {
        return targetHeight;
    }

    @Override
    public ResizeAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public File getImageFile() {
        return imageFile;
    }

    @Override
    public void setSourceResolution(Resolution resolution) {
        sourceResolution = resolution;
        notifyUpdated();
    }

    @Override
    public Resolution getSourceResolution() {
        return sourceResolution;
    }

    @Override
    public void addTargetResolution(Resolution resolution) {
        if (targetResolutions.contains(resolution)) {
            return;
        }
        targetResolutions.add(resolution);
        notifyUpdated();
    }

    @Override
    public void removeTargetResolution(Resolution resolution) {
        if (!targetResolutions.contains(resolution)) {
            return;
        }
        targetResolutions.remove(resolution);
        notifyUpdated();
    }

    @Override
    public void setTargetHeight(int height) {
        if (targetHeight == height) {
            return;
        }
        targetHeight = height;
        targetWidth = (int) (targetHeight / aspectRatio);
        notifyUpdated();
    }

    @Override
    public void setTargetWidth(int width) {
        if (targetWidth == width) {
            return;
        }
        targetWidth = width;
        targetHeight = (int) (aspectRatio * targetWidth);
        notifyUpdated();
    }

    @Override
    public void setTargetRoot(String path) {
        if (exportRoot.equals(path)) {
            return;
        }
        exportRoot = path;
        notifyUpdated();
    }

    @Override
    public void setExportName(String name) {
        if (exportName.equals(name)) {
            return;
        }
        exportName = name;
        notifyUpdated();
    }

    @Override
    public void setAlgorithm(ResizeAlgorithm algorithm) {
        if (algorithm == this.algorithm) {
            return;
        }
        this.algorithm = algorithm;
        this.method = algorithm.getMethods().get(0);
        notifyUpdated();
    }

    @Override
    public void setMethod(String method) {
        if (Objects.equal(method, this.method)) {
            return;
        }
        this.method = method;
        notifyUpdated();
    }

    @Override
    public List<String> getMethods() {
        return algorithm.getMethods();
    }

    @Override
    public Format getFormat() {
        return format;
    }

    @Override
    public boolean isNinePatch() {
        return isNinePatch;
    }

    @Override
    public void setFormat(Format format) {
        if (this.format == format) {
            return;
        }
        this.format = format;
        notifyUpdated();
    }

    @Override
    public Destination getDestination() {
        return destination;
    }

    @Override
    public void setDestination(Destination destination) {
        if (this.destination == destination) {
            return;
        }
        this.destination = destination;
        notifyUpdated();

    }

    @Override
    public int[] getScaledSize(Resolution resolution) {
        final float scaleFactor = RefactorUtils.getScaleFactor(resolution, sourceResolution);
        return new int[] {(int) (scaleFactor * targetWidth), (int) (scaleFactor * targetHeight)};
    }

    @Override
    public List<ImageInformation> getImageInformation(Project project) {
        final ImageInformation base = ImageInformation.newBuilder()
                                                      .setExportName(exportName)
                                                      .setImageFile(imageFile)
                                                      .setAlgorithm(algorithm)
                                                      .setMethod(algorithm.getMethod(method))
                                                      .setExportPath(exportRoot)
                                                      .setNinePatch(isNinePatch)
                                                      .setFormat(format)
                                                      .setDestination(destination)
                                                      .build();
        final List<ImageInformation> images = new ArrayList<ImageInformation>();
        for (Resolution resolution : targetResolutions) {
            images.add(ImageInformation.newBuilder(base)
                                       .setTargetResolution(resolution)
                                       .setFactor(getRealScaleFactor(resolution))
                                       .build());
        }
        return images;
    }

    @Override
    public List<ImageInformation> getImageInformation(Project project,
                                                      String selectedFile,
                                                      List<ImageInformation> imageInformation,
                                                      Resolution sourceResolution) {
        final ImageInformation base = ImageInformation.newBuilder(imageInformation.get(0))
                                                      .setAlgorithm(algorithm)
                                                      .setMethod(algorithm.getMethod(method))
                                                      .setExportPath(exportRoot)
                                                      .setFormat(format)
                                                      .build();
        final List<ImageInformation> images = new ArrayList<ImageInformation>();
        for (Resolution resolution : targetResolutions) {
            images.add(ImageInformation.newBuilder(base)
                                       .setTargetResolution(resolution)
                                       .setFactor(getRealScaleFactor(resolution))
                                       .build());
        }
        return images;
    }

    private float getRealScaleFactor(Resolution targetResolution) {
        final float resolutionScaleFactor = RefactorUtils.getScaleFactor(targetResolution, sourceResolution);
        final float sizeScaleFactor = (float) targetWidth / (float) originalImageWidth;
        return resolutionScaleFactor * sizeScaleFactor;
    }

    private int getOriginalTargetSize(Resolution sourceResolution, Resolution targetResolution, int size, float factor) {
        final float resolutionScaleFactor = RefactorUtils.getScaleFactor(targetResolution, sourceResolution);
        final float sizeScaleFactor = factor / resolutionScaleFactor;
        return (int) (sizeScaleFactor * size);
    }

    @Override
    public void addObserver(AddItemBatchScaleDialogObserver observer) {
        observers.add(observer);
        notifyUpdated();
    }

    @Override
    public void removeObserver(AddItemBatchScaleDialogObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void tearDown() {
        observers.clear();
    }

    private void notifyUpdated() {
        for (AddItemBatchScaleDialogObserver observer : observers) {
            observer.updated();
        }
    }
}
