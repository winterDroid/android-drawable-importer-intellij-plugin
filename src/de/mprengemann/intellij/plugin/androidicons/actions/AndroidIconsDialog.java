package de.mprengemann.intellij.plugin.androidicons.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.ui.AndroidIconsImporter;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidFacetUtils;

/**
 * User: marcprengemann
 * Date: 04.04.14
 * Time: 09:42
 */
public class AndroidIconsDialog extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = getEventProject(event);
    Module module = event.getData(DataKeys.MODULE);

    AndroidIconsImporter dialog = new AndroidIconsImporter(project, module);
    dialog.show();
  }

  @Override
  public void update(AnActionEvent e) {
    AndroidFacetUtils.updateActionVisibility(e);
  }

}
