package de.mprengemann.intellij.plugin.androidicons.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidUtils;

import java.util.List;

/**
 * User: marcprengemann
 * Date: 09.04.14
 * Time: 08:40
 */
public class AndroidFacetUtils {
  public static AndroidFacet getInstance(Project project, Module module) {
    AndroidFacet currentFacet = null;
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
    return currentFacet;
  }

  public static boolean isTestProject(AndroidFacet facet) {
    return facet.getManifest() != null
           && facet.getManifest().getInstrumentations() != null
           && !facet.getManifest().getInstrumentations().isEmpty();
  }

  public static void updateActionVisibility(AnActionEvent e) {
    Module module = e.getData(LangDataKeys.MODULE);
    PsiElement file = e.getData(LangDataKeys.PSI_ELEMENT);
    boolean visible = false;
    if (module != null && AndroidFacetUtils.getInstance(AnAction.getEventProject(e), module) != null) {
      if (file instanceof PsiDirectory) {
        PsiDirectory dir = (PsiDirectory) file;
        JavaDirectoryService dirService = JavaDirectoryService.getInstance();
        if (dirService != null) {
          visible = true;
        }
      }
    }
    e.getPresentation().setVisible(visible);
  }
}
