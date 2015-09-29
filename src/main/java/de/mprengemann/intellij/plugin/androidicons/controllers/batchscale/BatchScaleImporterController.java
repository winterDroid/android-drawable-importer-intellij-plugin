package de.mprengemann.intellij.plugin.androidicons.controllers.batchscale;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.dialogs.AddItemBatchScaleDialog;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BatchScaleImporterController implements IBatchScaleImporterController {

    private Set<BatchScaleImporterObserver> observers;
    private Map<String, List<ImageInformation>> images;
    private Map<String, Resolution> sourceResolutions;
    private List<String> sourceFiles;

    public BatchScaleImporterController() {
        this.observers = new HashSet<BatchScaleImporterObserver>();
        this.images = new HashMap<String, List<ImageInformation>>();
        this.sourceResolutions = new HashMap<String, Resolution>();
        this.sourceFiles = new ArrayList<String>() {
            @Override
            public boolean add(String s) {
                if (contains(s)) {
                    return false;
                }
                return super.add(s);
            }
        };
        notifyUpdated();
    }

    @Override
    public void addObserver(BatchScaleImporterObserver observer) {
        observers.add(observer);
        observer.updated();
    }

    @Override
    public void removeObserver(BatchScaleImporterObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void tearDown() {
        observers.clear();
    }

    @Override
    public void addImage(Resolution sourceResolution, List<ImageInformation> imageInformation) {
        if (imageInformation == null ||
            imageInformation.size() == 0) {
            return;
        }
        final String sourcePath = imageInformation.get(0).getImageFile().getAbsolutePath();
        images.put(sourcePath, imageInformation);
        sourceFiles.add(sourcePath);
        sourceResolutions.put(sourcePath, sourceResolution);
        notifyUpdated();
    }

    @Override
    public void editImage(Project project, Module module, int index) {
        if (index >= sourceFiles.size() ||
            0 > index) {
            return;
        }
        final String sourceFile = sourceFiles.get(index);
        final List<ImageInformation> imageInformations = images.get(sourceFile);

        AddItemBatchScaleDialog addItemBatchScaleDialog =
            new AddItemBatchScaleDialog(project,
                                        module,
                                        this,
                                        sourceResolutions.get(sourceFile),
                                        imageInformations);
        addItemBatchScaleDialog.show();
    }

    @Override
    public void removeImage(int index) {
        if (index >= sourceFiles.size() ||
            0 > index) {
            return;
        }
        removeImage(sourceFiles.get(index));
    }

    @Override
    public void removeImage(String sourcePath) {
        if (!images.containsKey(sourcePath) &&
            !sourceFiles.contains(sourcePath)) {
            return;
        }
        images.remove(sourcePath);
        sourceFiles.remove(sourcePath);
        sourceResolutions.remove(sourcePath);
        notifyUpdated();
    }

    @Override
    public int getImageCount() {
        return sourceFiles.size();
    }

    @Override
    public ImageInformation getImage(int index) {
        if (index >= sourceFiles.size() ||
            0 > index) {
            return null;
        }
        final List<ImageInformation> imageInformation = images.get(sourceFiles.get(index));
        return imageInformation.get(0);
    }

    @Override
    public List<Resolution> getTargetResolutions(ImageInformation information) {
        final String sourcePath = information.getImageFile().getAbsolutePath();
        if (!images.containsKey(sourcePath)) {
            return new ArrayList<Resolution>();
        }
        final List<ImageInformation> imageInformations = images.get(sourcePath);
        final List<Resolution> resolutions = new ArrayList<Resolution>();
        for (ImageInformation image : imageInformations) {
            resolutions.add(image.getResolution());
        }
        return resolutions;
    }

    @Override
    public RefactoringTask getExportTask(Project project) {
        RefactoringTask task = new RefactoringTask(project);
        for (String sourceFile : sourceFiles) {
            final List<ImageInformation> informationList = images.get(sourceFile);
            for (ImageInformation imageInformation : informationList) {
                task.addImage(imageInformation);
            }
        }
        return task;
    }

    private void notifyUpdated() {
        for (BatchScaleImporterObserver observer : observers) {
            observer.updated();
        }
    }
}
