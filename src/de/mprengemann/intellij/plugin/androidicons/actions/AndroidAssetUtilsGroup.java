package de.mprengemann.intellij.plugin.androidicons.actions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidFacetUtils;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: marcprengemann
 * Date: 09.04.14
 * Time: 14:12
 */
public class AndroidAssetUtilsGroup extends DefaultActionGroup {
  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    AndroidFacetUtils.updateActionVisibility(e);
  }
}
