package de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.additem;

import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface IAddItemBatchScaleImporterController extends IController<AddItemBatchScaleDialogObserver> {
    String getExportName();

    String getExportPath();

    Set<Resolution> getTargetResolutions();

    int getTargetWidth();

    int getTargetHeight();

    ResizeAlgorithm getAlgorithm();

    String getMethod();

    File getImageFile();

    void setSourceResolution(Resolution resolution);

    Resolution getSourceResolution();

    void addTargetResolution(Resolution resolution);

    void removeTargetResolution(Resolution resolution);

    void setTargetHeight(int height);

    void setTargetWidth(int width);

    void setTargetRoot(String path);

    void setTargetName(String name);

    void setAlgorithm(ResizeAlgorithm algorithm);

    void setMethod(String method);

    List<String> getMethods();

    int[] getScaledSize(Resolution resolution);

    List<ImageInformation> getImageInformation(Project project);
}
