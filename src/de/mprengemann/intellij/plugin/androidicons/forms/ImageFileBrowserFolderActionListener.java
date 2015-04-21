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

package de.mprengemann.intellij.plugin.androidicons.forms;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImageFileBrowserFolderActionListener extends ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> {

    private final String title;
    private final Project project;
    private final IconApplication container;

    public ImageFileBrowserFolderActionListener(String title,
                                                Project project,
                                                ComponentWithBrowseButton<JTextField> componentWithBrowseButton,
                                                FileChooserDescriptor fileChooserDescriptor) {
        super(title, null, componentWithBrowseButton, project, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        this.title = title;
        this.project = project;
        this.container = ApplicationManager.getApplication().getComponent(IconApplication.class);
    }

    @Nullable
    @Override
    protected VirtualFile getInitialFile() {
        String directoryName = this.getComponentText();
        if (StringUtil.isEmptyOrSpaces(directoryName)) {
            return null;
        } else {
            if (project != null) {
                directoryName = container.getControllerFactory()
                                         .getSettingsController()
                                         .getLastImageFolder(project);
            } else {
                directoryName = FileUtil.toSystemIndependentName(directoryName);
            }

            VirtualFile path;
            for (path = LocalFileSystem.getInstance().findFileByPath(this.expandPath(directoryName));
                 path == null && directoryName.length() > 0;
                 path = LocalFileSystem.getInstance().findFileByPath(directoryName)) {
                int pos = directoryName.lastIndexOf(47);
                if (pos <= 0) {
                    break;
                }
                directoryName = directoryName.substring(0, pos);
            }

            return path;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileChooserDescriptor fileChooserDescriptor = this.myFileChooserDescriptor;
        if (this.title != null) {
            fileChooserDescriptor = (FileChooserDescriptor) this.myFileChooserDescriptor.clone();
            fileChooserDescriptor.setTitle(this.title);
        }

        FileChooser.chooseFile(fileChooserDescriptor, this.getProject(), this.getInitialFile(), new Consumer<VirtualFile>() {
            @SuppressWarnings("deprecation")
            @Override
            public void consume(VirtualFile file) {
                container.getControllerFactory()
                         .getSettingsController()
                         .saveLastImageFolder(project, file.getCanonicalPath());
                onFileChoosen(file);
            }
        });
    }
}
