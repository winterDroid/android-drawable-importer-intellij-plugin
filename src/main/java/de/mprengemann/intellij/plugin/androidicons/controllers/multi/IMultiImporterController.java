package de.mprengemann.intellij.plugin.androidicons.controllers.multi;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;

public interface IMultiImporterController extends IController<MultiImporterObserver> {

    String getTargetRoot();

    void setTargetRoot(File path);

    void addImage(File source, Resolution target);

    RefactoringTask getTask(Project project);

    void setExportName(String exportName);

    String getExportName();

    File getMostRecentImage();

    void setMostRecentImage(Resolution resolution);

}
