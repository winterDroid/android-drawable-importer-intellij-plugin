package de.mprengemann.intellij.plugin.androidicons.controllers.batchscale;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import de.mprengemann.intellij.plugin.androidicons.dialogs.AddItemBatchScaleDialog;
import de.mprengemann.intellij.plugin.androidicons.dialogs.EditItemsBatchScaleDialog;
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
                imageInformation.isEmpty()) {
            return;
        }
        final String sourcePath = imageInformation.get(0).getImageFile().getAbsolutePath();
        images.put(sourcePath, imageInformation);
        sourceFiles.add(sourcePath);
        sourceResolutions.put(sourcePath, sourceResolution);
        notifyUpdated();
    }

    @Override
    public void editImages(Project project, Module module, int[] indices) {
        if (indices.length == 0) {
            return;
        }
        DialogWrapper dialogWrapper;
        if (indices.length == 1){
            final int index = indices[0];
            if (index >= sourceFiles.size() || 0 > index) {
                return;
            }
            final String sourceFile = sourceFiles.get(index);
            final List<ImageInformation> imageInformation = images.get(sourceFile);

            dialogWrapper = new AddItemBatchScaleDialog(project,
                                        module,
                                        this,
                                        sourceResolutions.get(sourceFile),
                                        imageInformation);
        } else {
            final List<String> selectedFiles = new ArrayList<String>();
            for (int index : indices) {
                selectedFiles.add(sourceFiles.get(index));
            }
            final List<List<ImageInformation>> imageInformation = new ArrayList<List<ImageInformation>>();
            final List<Resolution> sourceResolution = new ArrayList<Resolution>();
            for (String selectedFile : selectedFiles) {
                imageInformation.add(images.get(selectedFile));
                sourceResolution.add(sourceResolutions.get(selectedFile));
            }
            dialogWrapper = new EditItemsBatchScaleDialog(project,
                                                          module,
                                                          this,
                                                          selectedFiles,
                                                          sourceResolution,
                                                          imageInformation);
        }
        dialogWrapper.show();
    }

    @Override
    public void removeImages(int[] indices) {
        for (int index : indices) {
            if (index >= sourceFiles.size() ||
                0 > index) {
                return;
            }
            removeImage(sourceFiles.get(index));
        }
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
            resolutions.add(image.getTargetResolution());
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
