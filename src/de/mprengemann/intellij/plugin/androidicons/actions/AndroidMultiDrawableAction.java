package de.mprengemann.intellij.plugin.androidicons.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import de.mprengemann.intellij.plugin.androidicons.ui.AndroidMultiDrawableImporter;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidFacetUtils;
import icons.AndroidIcons;

/**
 * User: marcprengemann
 * Date: 08.04.14
 * Time: 14:45
 */
public class AndroidMultiDrawableAction extends AnAction {

  public AndroidMultiDrawableAction() {
    super("Multisource-Drawable", "Imports several drawables into the associated resources folders and renames them.", AndroidIcons.Android);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = getEventProject(event);
    Module module = event.getData(DataKeys.MODULE);

    AndroidMultiDrawableImporter dialog = new AndroidMultiDrawableImporter(project, module);
    dialog.show();
  }

  @Override
  public void update(AnActionEvent e) {
    AndroidFacetUtils.updateActionVisibility(e);
  }
}
