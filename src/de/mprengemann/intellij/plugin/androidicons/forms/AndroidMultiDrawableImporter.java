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
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.ex.FileDrop;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import de.mprengemann.intellij.plugin.androidicons.images.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.images.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.images.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import de.mprengemann.intellij.plugin.androidicons.util.ExportNameUtils;
import org.apache.commons.lang.StringUtils;
import org.intellij.images.fileTypes.ImageFileTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class AndroidMultiDrawableImporter extends DialogWrapper {

    private final Project project;
    private final IconApplication container;
    private TextFieldWithBrowseButton resRoot;
    private TextFieldWithBrowseButton ldpiFile;
    private TextFieldWithBrowseButton mdpiFile;
    private TextFieldWithBrowseButton hdpiFile;
    private TextFieldWithBrowseButton xhdpiFile;
    private TextFieldWithBrowseButton xxhdpiFile;
    private TextFieldWithBrowseButton xxxhdpiFile;
    private JLabel imageContainer;
    private JTextField resExportName;
    private JPanel uiContainer;

    public AndroidMultiDrawableImporter(final Project project, Module module) {
        super(project, true);
        this.project = project;
        this.container = ApplicationManager.getApplication().getComponent(IconApplication.class);

        setTitle("Android Multi Drawable Importer");
        setResizable(false);

        AndroidResourcesHelper.initResourceBrowser(project,
                                                   module,
                                                   "Select res root",
                                                   resRoot,
                                                   container.getControllerFactory().getSettingsController());

        initBrowser(Resolution.LDPI, ldpiFile);
        initBrowser(Resolution.MDPI, mdpiFile);
        initBrowser(Resolution.HDPI, hdpiFile);
        initBrowser(Resolution.XHDPI, xhdpiFile);
        initBrowser(Resolution.XXHDPI, xxhdpiFile);
        initBrowser(Resolution.XXXHDPI, xxxhdpiFile);

        init();
    }

    private void initBrowser(Resolution resolution, final TextFieldWithBrowseButton browseButton) {
        final FileChooserDescriptor imageDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(
            ImageFileTypeManager.getInstance().getImageFileType());
        String title1 = "Select your " + resolution.getName() + " asset";
        imageDescriptor.setTitle(title1);
        ImageFileBrowserFolderActionListener actionListener = new ImageFileBrowserFolderActionListener(title1, project, browseButton, imageDescriptor) {
                @Override
                @SuppressWarnings("deprecation") // Otherwise not compatible to AndroidStudio
                protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                    super.onFileChoosen(chosenFile);
                    fillImageInformation(chosenFile);
                }
            };
        browseButton.addBrowseFolderListener(project, actionListener);
        browseButton.getTextField().addMouseListener(new SimpleMouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                updateImage(browseButton.getText());
            }
        });
        new FileDrop(browseButton.getTextField(), new FileDrop.Target() {
            @Override
            public FileChooserDescriptor getDescriptor() {
                return imageDescriptor;
            }

            @Override
            public boolean isHiddenShown() {
                return false;
            }

            @Override
            public void dropFiles(List<VirtualFile> virtualFiles) {
                if (virtualFiles != null) {
                    if (virtualFiles.size() == 1) {
                        VirtualFile chosenFile = virtualFiles.get(0);
                        browseButton.setText(chosenFile.getCanonicalPath());
                        fillImageInformation(chosenFile);
                    }
                }
            }
        });
    }

    private void fillImageInformation(VirtualFile chosenFile) {
        updateImage(chosenFile.getCanonicalPath());
        if (StringUtils.isEmpty(resExportName.getText().trim())) {
            resExportName.setText(ExportNameUtils.getExportNameFromFilename(chosenFile.getName()));
        }
    }

    private void updateImage(String fileString) {
        if (fileString != null && !StringUtils.isEmpty(fileString)) {
            File file = new File(fileString);
            ImageUtils.updateImage(imageContainer, file);
        }
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {

        if (StringUtils.isEmpty(resRoot.getText().trim())) {
            return new ValidationInfo("Please select the resources root.", resRoot);
        }

        if (StringUtils.isEmpty(resExportName.getText().trim())) {
            return new ValidationInfo("Please select a name for the drawable.", resExportName);
        } else if (!resExportName.getText().matches("[a-z0-9_.]*")) {
            return new ValidationInfo(
                "Please select a valid name for the drawable. There are just \"[a-z0-9_.]\" allowed.",
                resExportName);
        }

        return super.doValidate();
    }

    @Override
    protected void doOKAction() {
        ImageInformation baseInformation = ImageInformation.newBuilder()
            .setExportPath(resRoot.getText().trim())
            .setExportName(resExportName.getText().trim())
            .build(project);

        final RefactoringTask task = new RefactoringTask(project);

        task.addImage(getImageInformation(baseInformation, Resolution.LDPI, ldpiFile));
        task.addImage(getImageInformation(baseInformation, Resolution.MDPI, mdpiFile));
        task.addImage(getImageInformation(baseInformation, Resolution.HDPI, hdpiFile));
        task.addImage(getImageInformation(baseInformation, Resolution.XHDPI, xhdpiFile));
        task.addImage(getImageInformation(baseInformation, Resolution.XXHDPI, xxhdpiFile));
        task.addImage(getImageInformation(baseInformation, Resolution.XXXHDPI, xxxhdpiFile));

        ProgressManager.getInstance().run(task);
        
        super.doOKAction();
    }

    private ImageInformation getImageInformation(ImageInformation baseInformation, 
                                                 Resolution resolution,
                                                 TextFieldWithBrowseButton browser) {
        if (browser == null) {
            return null;
        }
        String sourceString = browser.getText().trim();
        if (StringUtils.isEmpty(sourceString)) {
            return null;
        }
        File source = new File(sourceString);
        return ImageInformation.newBuilder(baseInformation)
                               .setImageFile(source)
                               .setResolution(resolution)
                               .build(project);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return uiContainer;
    }
}
