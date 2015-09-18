package de.mprengemann.intellij.plugin.androidicons.controllers.batchscale;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.util.List;

public interface IBatchScaleImporterController extends IController<BatchScaleImporterObserver> {

    void addImage(List<ImageInformation> imageInformation);

    void removeImage(int index);

    void removeImage(String sourcePath);

    int getImageCount();

    ImageInformation getImage(int index);

    List<Resolution> getTargetResolutions(ImageInformation information);

    RefactoringTask getExportTask(Project project);
}
