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

package de.mprengemann.intellij.plugin.androidicons.util;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.forms.ResourcesDialog;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AndroidResourcesHelper {

    public static void getResRootFile(Project project, Module module, ResourcesDialog.ResourceSelectionListener listener) {
        AndroidFacet currentFacet = AndroidFacetUtils.getCurrentFacet(project, module);

        if (currentFacet != null) {
            List<VirtualFile> allResourceDirectories = currentFacet.getAllResourceDirectories();
            if (allResourceDirectories.size() == 1) {
                listener.onResourceSelected(allResourceDirectories.get(0));
            } else if (allResourceDirectories.size() > 1) {
                ResourcesDialog dialog = new ResourcesDialog(project, allResourceDirectories, listener);
                dialog.show();
            }
        }
    }

    public static void initResourceBrowser(final Project project, Module module, final String title, @Nullable final TextFieldWithBrowseButton browser) {
        final VirtualFile resRoot = SettingsHelper.getResRootForProject(project);

        if (resRoot == null) {
            getResRootFile(project, module, new ResourcesDialog.ResourceSelectionListener() {
                @Override
                public void onResourceSelected(VirtualFile resDir) {
                    if (browser != null) {
                        browser.setText(resDir.getCanonicalPath());
                    }
                    SettingsHelper.saveResRootForProject(project, resDir.getUrl());
                }
            });
        } else {
            if (browser != null) {
                browser.setText(resRoot.getCanonicalPath());
            }
        }

        if (browser != null) {
            FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            workingDirectoryChooserDescriptor.setTitle(title);
            browser.addBrowseFolderListener(title, null, project, workingDirectoryChooserDescriptor);
            browser.addBrowseFolderListener(new TextBrowseFolderListener(workingDirectoryChooserDescriptor) {
                @Override
                @SuppressWarnings("deprecation") // Otherwise not compatible to AndroidStudio
                protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                    super.onFileChoosen(chosenFile);
                    SettingsHelper.saveResRootForProject(project, chosenFile.getUrl());
                }
            });
        }
    }
}
