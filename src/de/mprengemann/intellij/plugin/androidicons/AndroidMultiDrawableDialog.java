package de.mprengemann.intellij.plugin.androidicons;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.ui.AndroidMultiDrawableImporter;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;

/**
 * User: marcprengemann
 * Date: 08.04.14
 * Time: 14:45
 */
public class AndroidMultiDrawableDialog extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = getEventProject(event);
    Module module = event.getData(DataKeys.MODULE);

    AndroidMultiDrawableImporter dialog = new AndroidMultiDrawableImporter(project, module);
    dialog.show();
  }
}
