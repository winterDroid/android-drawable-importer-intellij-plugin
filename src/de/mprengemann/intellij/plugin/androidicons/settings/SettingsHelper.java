package de.mprengemann.intellij.plugin.androidicons.settings;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

/**
 * User: marcprengemann
 * Date: 04.04.14
 * Time: 13:23
 */
public class SettingsHelper {
  private static final String PATH     = "assetPath";
  private static final String RES_ROOT = "resourcesRoot";

  public static VirtualFile getAssetPath() {
    String persistedFile = getAssetPathString();
    if (persistedFile != null) {
      return VirtualFileManager.getInstance().findFileByUrl(persistedFile);
    } else {
      return null;
    }
  }

  public static void saveAssetPath(String fileUrl) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(PATH, fileUrl);
  }

  public static void saveAssetPath(VirtualFile file) {
    saveAssetPath(file != null ? file.getUrl() : null);
  }

  public static String getAssetPathString() {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    return propertiesComponent.getValue(PATH);
  }

  public static void saveResRootForProject(Project project, String fileUrl) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    propertiesComponent.setValue(RES_ROOT, fileUrl);
  }

  public static VirtualFile getResRootForProject(Project project) {
    String persistedFile = getResRootStringForProject(project);
    if (persistedFile != null) {
      return VirtualFileManager.getInstance().findFileByUrl(persistedFile);
    } else {
      return null;
    }
  }

  public static String getResRootStringForProject(Project project) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    return propertiesComponent.getValue(RES_ROOT);
  }
}
