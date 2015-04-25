/*
 * Copyright 2015 Marc Prengemann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package de.mprengemann.intellij.plugin.androidicons.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import de.mprengemann.intellij.plugin.androidicons.forms.IconsImporter;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidFacetUtils;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;

public class IconsAction extends AnAction {

    private IconApplication container;

    public IconsAction() {
        super("Icons Import",
              "Creates a new Android Asset by the use of Google\'s Material Icons or Android Icons",
              AndroidIcons.Android);
        container = ApplicationManager.getApplication().getComponent(IconApplication.class);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = getEventProject(event);
        if (!container.getControllerFactory().getMaterialIconsController().isInitialized() &&
            !container.getControllerFactory().getAndroidIconsController().isInitialized()) {
            Messages.showMessageDialog(
                project,
                "You have to select the at least on of the Android Icons or Material Icons asset folder in the settings!",
                "Error",
                Messages.getErrorIcon());
            if (project == null) {
                project = ProjectManager.getInstance().getDefaultProject();
            }
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Android Drawable Importer");
            return;
        }

        Module module = event.getData(DataKeys.MODULE);
        IconsImporter dialog = new IconsImporter(project, module);
        dialog.show();
        container.getControllerFactory()
                 .getIconImporterController()
                 .removeObserver(dialog);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        AndroidFacetUtils.updateActionVisibility(e);
    }

}
