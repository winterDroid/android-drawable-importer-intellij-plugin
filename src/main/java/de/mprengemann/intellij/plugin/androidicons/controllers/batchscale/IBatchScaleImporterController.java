package de.mprengemann.intellij.plugin.androidicons.controllers.batchscale;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.util.List;

public interface IBatchScaleImporterController extends IController<BatchScaleImporterObserver> {

    void addImage(Resolution sourceResolution, List<ImageInformation> imageInformation);

    void editImages(Project project, Module module, int[] indices);

    void removeImages(int[] indices);

    void removeImage(String sourcePath);

    int getImageCount();

    ImageInformation getImage(int index);

    List<Resolution> getTargetResolutions(ImageInformation information);

    RefactoringTask getExportTask(Project project);
}
