package de.mprengemann.intellij.plugin.androidicons;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.ui.AndroidScaleImporter;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;

/**
 * User: marcprengemann
 * Date: 07.04.14
 * Time: 11:09
 */
public class AndroidScaleDialog extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = getEventProject(event);
    Module module = event.getData(DataKeys.MODULE);

    AndroidScaleImporter dialog = new AndroidScaleImporter(project, module);
    dialog.show();
  }
}
