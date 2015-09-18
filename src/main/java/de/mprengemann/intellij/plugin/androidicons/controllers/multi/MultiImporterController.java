package de.mprengemann.intellij.plugin.androidicons.controllers.multi;

import com.google.common.base.Objects;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.ExportNameUtils;
import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiImporterController implements IMultiImporterController {

    private Set<MultiImporterObserver> observers;
    private Map<Resolution, ImageInformation> imageInformationMap;
    private String targetRoot;
    private String exportName;
    private Resolution mostRecentResolution;

    public MultiImporterController() {
        this.observers = new HashSet<MultiImporterObserver>();
        this.imageInformationMap = new HashMap<Resolution, ImageInformation>();
    }

    @Override
    public void addObserver(MultiImporterObserver observer) {
        observers.add(observer);
        observer.updated();
    }

    @Override
    public void removeObserver(MultiImporterObserver observer) {
        observers.remove(observer);
    }

    @Override
    public String getTargetRoot() {
        return targetRoot;
    }

    @Override
    public void setTargetRoot(File path) {
        if (Objects.equal(targetRoot, path.getAbsolutePath())) {
            return;
        }
        targetRoot = path.getAbsolutePath();
        notifyUpdated();
    }

    @Override
    public void addImage(File source, Resolution resolution) {
        imageInformationMap.put(resolution, ImageInformation.newBuilder()
                                                            .setImageFile(source)
                                                            .setResolution(resolution)
                                                            .build());
        mostRecentResolution = resolution;
        if (TextUtils.isEmpty(exportName)) {
            exportName = ExportNameUtils.getExportNameFromFilename(source.getName());
        }
        notifyUpdated();
    }

    @Override
    public RefactoringTask getTask(Project project) {
        RefactoringTask task = new RefactoringTask(project);
        for (ImageInformation imageInformation : imageInformationMap.values()) {
            task.addImage(ImageInformation.newBuilder(imageInformation)
                                          .setExportPath(targetRoot)
                                          .setExportName(exportName)
                                          .build());
        }
        return task;
    }

    @Override
    public void setExportName(String exportName) {
        if (Objects.equal(exportName, this.exportName)) {
            return;
        }
        this.exportName = exportName;
        notifyUpdated();
    }

    @Override
    public String getExportName() {
        return exportName;
    }

    @Override
    public File getMostRecentImage() {
        if (!imageInformationMap.containsKey(mostRecentResolution)) {
            return null;
        }
        return imageInformationMap.get(mostRecentResolution).getImageFile();
    }

    @Override
    public void setMostRecentImage(Resolution resolution) {
        if (resolution == mostRecentResolution) {
            return;
        }
        this.mostRecentResolution = resolution;
        notifyUpdated();
    }

    private void notifyUpdated() {
        for (MultiImporterObserver observer : observers) {
            observer.updated();
        }
    }

    @Override
    public void tearDown() {
        observers.clear();
    }
}
