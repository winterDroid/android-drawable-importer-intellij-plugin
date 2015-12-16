package de.mprengemann.intellij.plugin.androidicons.controllers.multi;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.Format;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IMultiImporterController extends IController<MultiImporterObserver> {

    String getTargetRoot();

    void setTargetRoot(File path);

    void addImage(File source, Resolution target);

    RefactoringTask getTask(Project project);

    void addZipImage(File file, Resolution resolution);

    Map<Resolution, List<ImageInformation>> getZipImages();

    void resetZipInformation();

    RefactoringTask getZipTask(Project project, File tempDir);

    void setExportName(String exportName);

    String getExportName();

    File getMostRecentImage();

    void setMostRecentImage(Resolution resolution);

    boolean containsNinePatch();

    Format getFormat();

    void setFormat(Format format);
}
