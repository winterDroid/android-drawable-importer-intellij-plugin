package de.mprengemann.intellij.plugin.androidicons;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import de.mprengemann.intellij.plugin.androidicons.ui.AndroidIconsImporter;
import de.mprengemann.intellij.plugin.androidicons.ui.AndroidScaleImporter;

/**
 * User: marcprengemann
 * Date: 07.04.14
 * Time: 11:09
 */
public class AndroidScaleDialog extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent event) {
    AndroidScaleImporter dialog = new AndroidScaleImporter(getEventProject(event));
    dialog.show();
  }
}
