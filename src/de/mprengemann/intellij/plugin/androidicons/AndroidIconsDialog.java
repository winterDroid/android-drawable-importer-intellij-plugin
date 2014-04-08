package de.mprengemann.intellij.plugin.androidicons;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.ui.AndroidIconsImporter;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;

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

}
