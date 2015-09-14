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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.ui.AndroidMultiDrawableImporter;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidFacetUtils;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;

public class AndroidMultiDrawableAction extends AnAction {

    public AndroidMultiDrawableAction() {
        super("Multisource-Drawable",
              "Imports several drawables into the associated resources folders and renames them.",
              AndroidIcons.Android);
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
