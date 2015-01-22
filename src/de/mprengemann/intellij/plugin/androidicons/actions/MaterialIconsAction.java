package de.mprengemann.intellij.plugin.androidicons.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.forms.MaterialIconsImporter;
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidFacetUtils;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;

public class MaterialIconsAction extends AnAction {

    public MaterialIconsAction() {
        super("MaterialIcons Import", "Creates a new Android Asset by the use of Google's Material Icons", AndroidIcons.Android);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = getEventProject(event);
        VirtualFile assetRoot = SettingsHelper.getAssetPath(IconPack.ANDROID_ICONS);
        if (assetRoot == null) {
            Messages.showMessageDialog(
                project,
                "You have to select the Material Icons root folder in the settings!",
                "Error",
                Messages.getErrorIcon());
            if (project == null) {
                project = ProjectManager.getInstance().getDefaultProject();
            }
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Android Drawable Import");
        } else {
            Module module = event.getData(DataKeys.MODULE);
            MaterialIconsImporter dialog = new MaterialIconsImporter(project, module);
            dialog.show();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        AndroidFacetUtils.updateActionVisibility(e);
    }

}
