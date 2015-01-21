package de.mprengemann.intellij.plugin.androidicons.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.forms.AndroidMultiDrawableImporter;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidFacetUtils;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;

public class AndroidMultiDrawableAction extends AnAction {

    public AndroidMultiDrawableAction() {
        super("Multisource-Drawable", "Imports several drawables into the associated resources folders and renames them.", AndroidIcons.Android);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = getEventProject(event);
        Module module = event.getData(DataKeys.MODULE);

        AndroidMultiDrawableImporter dialog = new AndroidMultiDrawableImporter(project, module);
        dialog.show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        AndroidFacetUtils.updateActionVisibility(e);
    }
}
