package de.mprengemann.intellij.plugin.androidicons;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import de.mprengemann.intellij.plugin.androidicons.ui.AndroidIconsImporter;

/**
 * User: marcprengemann
 * Date: 04.04.14
 * Time: 09:42
 */
public class AndroidIconsDialog extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent event) {
    Module modul = event.getData(DataKeys.MODULE);

    AndroidIconsImporter dialog = new AndroidIconsImporter(getEventProject(event));
    dialog.show();
  }

}
