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

package de.mprengemann.intellij.plugin.androidicons.dialogs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import de.mprengemann.intellij.plugin.androidicons.controllers.multi.IMultiImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.multi.MultiImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.multi.MultiImporterObserver;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.listeners.SimpleMouseListener;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.widgets.ExportNameField;
import de.mprengemann.intellij.plugin.androidicons.widgets.FileBrowserField;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class AndroidMultiDrawableImporter extends DialogWrapper implements MultiImporterObserver {

    private final Project project;
    private final Module module;
    private final IMultiImporterController controller;

    private FileBrowserField resRoot;
    private FileBrowserField ldpiFile;
    private FileBrowserField mdpiFile;
    private FileBrowserField hdpiFile;
    private FileBrowserField xhdpiFile;
    private FileBrowserField xxhdpiFile;
    private FileBrowserField xxxhdpiFile;
    private JLabel imageContainer;
    private ExportNameField resExportName;
    private JPanel uiContainer;
    private final ISettingsController settingsController;

    public AndroidMultiDrawableImporter(final Project project, final Module module) {
        super(project, true);
        this.project = project;
        this.module = module;

        IconApplication container = ApplicationManager.getApplication().getComponent(IconApplication.class);
        settingsController = container.getControllerFactory().getSettingsController();
        this.controller = new MultiImporterController();
        this.controller.addObserver(this);

        initResourceRoot();
        initBrowser(Resolution.LDPI, ldpiFile);
        initBrowser(Resolution.MDPI, mdpiFile);
        initBrowser(Resolution.HDPI, hdpiFile);
        initBrowser(Resolution.XHDPI, xhdpiFile);
        initBrowser(Resolution.XXHDPI, xxhdpiFile);
        initBrowser(Resolution.XXXHDPI, xxxhdpiFile);

        setTitle("Android Multi Drawable Importer");
        setResizable(false);
        init();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (StringUtils.isEmpty(controller.getTargetRoot())) {
            return new ValidationInfo("Please select the resources root.", resRoot);
        }

        if (StringUtils.isEmpty(controller.getExportName())) {
            return new ValidationInfo("Please select a name for the drawable.", resExportName);
        }

        return super.doValidate();
    }

    @Override
    protected void doOKAction() {
        final RefactoringTask task = controller.getTask(project);
        ProgressManager.getInstance().run(task);
        super.doOKAction();
    }

    @Override
    public void updated() {
        updateImage();
        updateTargetRoot();
        updateName();
    }

    private void updateTargetRoot() {
        final String targetRoot = controller.getTargetRoot();
        if (targetRoot == null) {
            return;
        }
        resRoot.setText(targetRoot);
    }

    private void updateName() {
        final String exportName = controller.getExportName();
        if (exportName == null) {
            return;
        }
        resExportName.setText(exportName);
        resExportName.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                controller.setExportName(((String) resExportName.getValue()));
            }
        });
    }

    private void updateImage() {
        final File file = controller.getMostRecentImage();
        if (file == null) {
            return;
        }
        ImageUtils.updateImage(imageContainer, file);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return uiContainer;
    }

    private void initResourceRoot() {
        resRoot.setSelectionListener(new Consumer<File>() {
            @Override
            public void consume(File file) {
                controller.setTargetRoot(file);
            }
        });
        resRoot.initWithResourceRoot(project, module, settingsController);
    }

    private void initBrowser(final Resolution resolution, final FileBrowserField fileBrowser) {
        fileBrowser.init(project, settingsController);
        fileBrowser.setSelectionListener(new Consumer<File>() {
            @Override
            public void consume(File file) {
                controller.addImage(file, resolution);
            }
        });
        fileBrowser.getTextField().addMouseListener(new SimpleMouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                controller.setMostRecentImage(resolution);
            }
        });
    }

    private void createUIComponents() {
        resRoot = new FileBrowserField(FileBrowserField.RESOURCE_DIR_CHOOSER);
        ldpiFile = new FileBrowserField(FileBrowserField.IMAGE_FILE_CHOOSER);
        mdpiFile = new FileBrowserField(FileBrowserField.IMAGE_FILE_CHOOSER);
        hdpiFile = new FileBrowserField(FileBrowserField.IMAGE_FILE_CHOOSER);
        xhdpiFile = new FileBrowserField(FileBrowserField.IMAGE_FILE_CHOOSER);
        xxhdpiFile = new FileBrowserField(FileBrowserField.IMAGE_FILE_CHOOSER);
        xxxhdpiFile = new FileBrowserField(FileBrowserField.IMAGE_FILE_CHOOSER);
    }
}
