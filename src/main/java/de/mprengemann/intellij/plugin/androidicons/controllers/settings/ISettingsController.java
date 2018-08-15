package de.mprengemann.intellij.plugin.androidicons.controllers.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.controllers.IController;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.model.Destination;
import de.mprengemann.intellij.plugin.androidicons.model.Format;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import java.util.Set;

public interface ISettingsController extends IController<SettingsObserver> {
    void saveResRootForProject(String fileUrl);
    VirtualFile getResourceRoot();
    String getResourceRootPath();

    String getLastImageFolder();
    void saveLastImageFolder(String fileUrl);

    void setProject(Project project);

    void saveResolutions(Set<Resolution> resolutions);
    Set<Resolution> getResolutions(Set<Resolution> defaultResolutions);

    void saveSourceResolution(Resolution sourceResolution);
    Resolution getSourceResolution(Resolution defaultSourceResolution);

    void saveAlgorithm(ResizeAlgorithm algorithm);
    ResizeAlgorithm getAlgorithm(ResizeAlgorithm defaultAlgorithm);

    void saveMethod(String method);
    String getMethod(String defaultMethod);

    void saveColor(String color);
    String getColor();

    void saveSize(String size);
    String getSize();

    void saveImageAsset(ImageAsset imageAsset);
    ImageAsset getImageAsset();

    void saveFormat(Format format);
    Format getFormat(Format defaultFormat);

    void saveDestination(Destination destination);
    Destination getDestination(Destination defaultDestination);

}
