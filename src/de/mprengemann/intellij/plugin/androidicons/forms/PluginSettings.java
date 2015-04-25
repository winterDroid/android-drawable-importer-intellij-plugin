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
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import de.mprengemann.intellij.plugin.androidicons.controllers.androidicons.AndroidIconsObserver;
import de.mprengemann.intellij.plugin.androidicons.controllers.materialicons.MaterialIconsObserver;
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PluginSettings implements Configurable,
                                       AndroidIconsObserver,
                                       MaterialIconsObserver {
    private JPanel panel;
    private TextFieldWithBrowseButton androidIconsAssetHome;
    private JLabel androidIconsFoundDrawablesText;
    private JLabel androidIconsFoundColorsText;
    private JButton androidIconsOpenBrowser;
    private TextFieldWithBrowseButton materialIconsAssetHome;
    private JLabel materialIconsFoundDrawables;
    private JLabel materialIconsFoundCategories;
    private JButton materialIconsOpenBrowser;
    private IconApplication container;
    private String initalAndroidIconsPath;
    private String initalMaterialIconsPath;

    @Nullable
    @Override
    public JComponent createComponent() {
        this.container = ApplicationManager.getApplication().getComponent(IconApplication.class);
        initalAndroidIconsPath = container.getControllerFactory()
                                          .getAndroidIconsController()
                                          .getPath();
        initalMaterialIconsPath = container.getControllerFactory()
                                           .getMaterialIconsController()
                                           .getPath();

        container.getControllerFactory()
                 .getAndroidIconsController()
                 .addObserver(this);
        container.getControllerFactory()
                 .getMaterialIconsController()
                 .addObserver(this);

        initAndroidIconsSettings();
        initMaterialIconsSettings();

        return panel;
    }

    private void initAndroidIconsSettings() {
        FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        String title = "Select res directory";
        workingDirectoryChooserDescriptor.setTitle(title);
        androidIconsAssetHome.addBrowseFolderListener(title, null, null, workingDirectoryChooserDescriptor);
        androidIconsAssetHome.addBrowseFolderListener(new TextBrowseFolderListener(workingDirectoryChooserDescriptor) {
            @Override
            @SuppressWarnings("deprecation") // Otherwise not compatible to AndroidStudio
            protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                super.onFileChoosen(chosenFile);
                container.getControllerFactory()
                         .getAndroidIconsController()
                         .setPath(chosenFile);
            }
        });
        androidIconsAssetHome.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                VirtualFileManager manager = VirtualFileManager.getInstance();
                VirtualFileSystem fileSystem = manager.getFileSystem("file");
                if (fileSystem == null) {
                    return;
                }
                VirtualFile file = fileSystem.findFileByPath(androidIconsAssetHome.getText());
                container.getControllerFactory()
                         .getAndroidIconsController()
                         .setPath(file);
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });
        androidIconsOpenBrowser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                container.getControllerFactory()
                         .getAndroidIconsController()
                         .openBrowser();
            }
        });
    }

    private void initMaterialIconsSettings() {
        FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        String title = "Select res directory";
        workingDirectoryChooserDescriptor.setTitle(title);
        materialIconsAssetHome.addBrowseFolderListener(title, null, null, workingDirectoryChooserDescriptor);
        materialIconsAssetHome.addBrowseFolderListener(new TextBrowseFolderListener(workingDirectoryChooserDescriptor) {
            @Override
            @SuppressWarnings("deprecation") // Otherwise not compatible to AndroidStudio
            protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                super.onFileChoosen(chosenFile);
                container.getControllerFactory()
                         .getMaterialIconsController()
                         .setPath(chosenFile);
            }
        });
        materialIconsAssetHome.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                VirtualFileManager manager = VirtualFileManager.getInstance();
                VirtualFileSystem fileSystem = manager.getFileSystem("file");
                if (fileSystem == null) {
                    return;
                }
                VirtualFile file = fileSystem.findFileByPath(androidIconsAssetHome.getText());
                container.getControllerFactory()
                         .getMaterialIconsController()
                         .setPath(file);
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });
        materialIconsOpenBrowser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                container.getControllerFactory()
                         .getMaterialIconsController()
                         .openBrowser();
            }
        });
    }

    @Override
    public boolean isModified() {
        return !androidIconsAssetHome.getText().equals(initalAndroidIconsPath) ||
               !materialIconsAssetHome.getText().equals(initalMaterialIconsPath);
    }

    @Override
    public void apply() throws ConfigurationException {
        initalAndroidIconsPath = container.getControllerFactory()
                                          .getAndroidIconsController()
                                          .getPath();
        initalMaterialIconsPath = container.getControllerFactory()
                                           .getMaterialIconsController()
                                           .getPath();

        container.getControllerFactory()
                 .getAndroidIconsController()
                 .savePath();
        container.getControllerFactory()
                 .getMaterialIconsController()
                 .savePath();
    }

    @Override
    public void reset() {
        if (!isModified()) {
            return;
        }
        container.getControllerFactory().getAndroidIconsController().reset();
        container.getControllerFactory().getMaterialIconsController().reset();
    }

    @Override
    public void disposeUIResources() {
        container.getControllerFactory()
                 .getAndroidIconsController()
                 .removeObserver(this);
        container.getControllerFactory()
                 .getMaterialIconsController()
                 .removeObserver(this);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Android Drawable Importer";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void updated(IconPack iconPack) {
        int assetCount;
        switch (iconPack) {
            case ANDROID_ICONS:
                androidIconsAssetHome.setText(container.getControllerFactory()
                                                       .getAndroidIconsController()
                                                       .getPath());
                int colorCount = container.getControllerFactory()
                                          .getAndroidIconsController()
                                          .getColors()
                                          .size();
                androidIconsFoundColorsText.setText(colorCount + " colors");
                assetCount = container.getControllerFactory()
                                      .getAndroidIconsController()
                                      .getAssets()
                                      .size();
                androidIconsFoundDrawablesText.setText(assetCount + " drawables per color");
                break;
            case MATERIAL_ICONS:
                materialIconsAssetHome.setText(container.getControllerFactory()
                                                        .getMaterialIconsController()
                                                        .getPath());
                int categoryCount = container.getControllerFactory()
                                             .getMaterialIconsController()
                                             .getCategories()
                                             .size();
                materialIconsFoundCategories.setText(categoryCount + " categories");
                assetCount = container.getControllerFactory()
                                      .getMaterialIconsController()
                                      .getAssets()
                                      .size();
                materialIconsFoundDrawables.setText(assetCount + " drawables");
                break;
        }
    }
}
