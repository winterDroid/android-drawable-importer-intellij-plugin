package de.mprengemann.intellij.plugin.androidicons.controllers.multi;

import com.google.common.base.Objects;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.ExportNameUtils;
import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiImporterController implements IMultiImporterController {

    private Set<MultiImporterObserver> observers;
    private Map<Resolution, List<ImageInformation>> zipImageInformationMap;
    private Map<Resolution, ImageInformation> imageInformationMap;
    private String targetRoot;
    private String exportName;
    private Resolution mostRecentResolution;

    public MultiImporterController() {
        this.observers = new HashSet<MultiImporterObserver>();
        this.imageInformationMap = new HashMap<Resolution, ImageInformation>();
        this.zipImageInformationMap = new HashMap<Resolution, List<ImageInformation>>();
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
                                                            .setTargetResolution(resolution)
                                                            .build());
        mostRecentResolution = resolution;
        if (TextUtils.isEmpty(exportName)) {
            exportName = ExportNameUtils.getExportNameFromFilename(source.getName());
        }
        notifyUpdated();
    }

    @Override
    public void addZipImage(File source, Resolution resolution) {
        if (!zipImageInformationMap.containsKey(resolution)) {
            zipImageInformationMap.put(resolution, new ArrayList<ImageInformation>());
        }
        zipImageInformationMap.get(resolution).add(ImageInformation.newBuilder()
                                                                .setImageFile(source)
                                                                .setTargetResolution(resolution)
                                                                .setExportName(FilenameUtils.getBaseName(source.getName()))
                                                                .build());
    }

    @Override
    public Map<Resolution, List<ImageInformation>> getZipImages() {
        return zipImageInformationMap;
    }

    @Override
    public void resetZipInformation() {
        zipImageInformationMap.clear();
    }

    @Override
    public RefactoringTask getTask(Project project) {
        RefactoringTask task = new RefactoringTask(project);
        for (Resolution resolution : imageInformationMap.keySet()) {
            task.addImage(ImageInformation.newBuilder(imageInformationMap.get(resolution))
                                          .setExportPath(targetRoot)
                                          .setExportName(exportName)
                                          .build());
        }
        return task;
    }

    @Override
    public RefactoringTask getZipTask(Project project, File tempDir) {
        RefactoringTask task = new ZipRefactoringTask(project, tempDir);
        for (Resolution resolution : zipImageInformationMap.keySet()) {
            for (ImageInformation imageInformation : zipImageInformationMap.get(resolution)) {
                task.addImage(ImageInformation.newBuilder(imageInformation)
                                              .setExportPath(targetRoot)
                                              .build());
            }
        }
        return task;
    }

    @Override
    public void setExportName(String exportName) {
        if (Objects.equal(exportName, this.exportName)) {
            return;
        }
        this.exportName = ExportNameUtils.getExportNameFromFilename(exportName);
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

    private static class ZipRefactoringTask extends RefactoringTask {
        private File tempDir;

        public ZipRefactoringTask(Project project, File tempDir) {
            super(project);
            this.tempDir = tempDir;
        }

        @Override
        public boolean shouldStartInBackground() {
            return false;
        }

        @Override
        protected void onPostExecute() {
            FileUtils.deleteQuietly(tempDir);
        }
    }
}
