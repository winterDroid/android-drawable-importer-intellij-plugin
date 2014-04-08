package de.mprengemann.intellij.plugin.androidicons.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidUtils;

import java.util.List;

/**
 * User: marcprengemann
 * Date: 08.04.14
 * Time: 14:20
 */
public class AndroidResourcesHelper {

  public static VirtualFile getResRootFile(Project project, Module module) {
    AndroidFacet currentFacet = null;
    VirtualFile resRoot = null;
    if (module != null) {
      List<AndroidFacet> facets = Lists.newArrayList();
      List<AndroidFacet> applicationFacets = AndroidUtils.getApplicationFacets(project);
      for (AndroidFacet facet : applicationFacets) {
        if (!isTestProject(facet)) {
          facets.add(facet);
        }
      }

      for (AndroidFacet facet : facets) {
        if (facet.getModule().getName().equals(module.getName())) {
          currentFacet = facet;
          break;
        }
      }
    }

    if (currentFacet != null) {
      List<VirtualFile> allResourceDirectories = currentFacet.getAllResourceDirectories();
      if (allResourceDirectories.size() >= 1) {
        resRoot = allResourceDirectories.get(0);
      }
    }
    return resRoot;
  }

  private static boolean isTestProject(AndroidFacet facet) {
    return facet.getManifest() != null
           && facet.getManifest().getInstrumentations() != null
           && !facet.getManifest().getInstrumentations().isEmpty();
  }
}
