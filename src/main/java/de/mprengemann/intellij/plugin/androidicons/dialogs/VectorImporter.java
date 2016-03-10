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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.ComboboxSpeedSearch;
import com.intellij.ui.JBColor;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.Consumer;
import com.intellij.util.ui.EmptyIcon;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import de.mprengemann.intellij.plugin.androidicons.controllers.defaults.IDefaultsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IIconsImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IconsImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IconsImporterObserver;
import de.mprengemann.intellij.plugin.androidicons.controllers.icons.materialicons.IMaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.widgets.ExportNameField;
import de.mprengemann.intellij.plugin.androidicons.widgets.FileBrowserField;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VectorImporter extends DialogWrapper implements IconsImporterObserver {

    private final Project project;
    private Module module;
    private final IMaterialIconsController materialIconsController;
    private final ISettingsController settingsController;
    private final IDefaultsController defaultsController;
    private final IIconsImporterController controller;

    private JLabel imageContainer;
    private JComboBox assetSpinner;
    private JComboBox categorySpinner;

    private JPanel uiContainer;
    private FileBrowserField resRoot;
    private ExportNameField resExportName;
    private JComboBox searchField;

    private final ActionListener iconPackActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            final JComboBox source = (JComboBox) actionEvent.getSource();
            final IconPack selectedItem = (IconPack) source.getSelectedItem();
            controller.setSelectedIconPack(selectedItem.getId());
        }
    };
    private final ActionListener categoryActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            final JComboBox source = (JComboBox) actionEvent.getSource();
            final String selectedItem = (String) source.getSelectedItem();
            controller.setSelectedCategory(selectedItem);
        }
    };
    private final ActionListener assetActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            final JComboBox source = (JComboBox) actionEvent.getSource();
            final ImageAsset selectedItem = (ImageAsset) source.getSelectedItem();
            controller.setSelectedAsset(selectedItem);
        }
    };
    private final Consumer<File> resRootListener = new Consumer<File>() {
        @Override
        public void consume(File file) {
            String path;
            if (file == null) {
                path = "";
            } else {
                path = file.getPath();
            }
            controller.setExportRoot(path);
        }
    };
    private final ItemListener searchFieldListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            final ImageAsset selectedItem = (ImageAsset) searchField.getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            controller.setSelectedAsset(selectedItem);
        }
    };
    private ComboboxSpeedSearch comboboxSpeedSearch;

    public VectorImporter(Project project, Module module) {
        super(project, true);
        this.project = project;
        this.module = module;

        final IconApplication container = ApplicationManager.getApplication().getComponent(IconApplication.class);
        materialIconsController = container.getControllerFactory().getMaterialIconsController();
        settingsController = container.getControllerFactory().getSettingsController();
        defaultsController = container.getControllerFactory().getDefaultsController();
        controller = new IconsImporterController(defaultsController, materialIconsController);
        initResRoot();

        setTitle("Vector Drawable Importer");
        getHelpAction().setEnabled(true);

        AssetSpinnerRenderer renderer = new AssetSpinnerRenderer();
        //noinspection GtkPreferredJComboBoxRenderer
        assetSpinner.setRenderer(renderer);
        imageContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                updateImage();
            }
        });

        initSearch();

        controller.addObserver(this);
        init();
        pack();
    }

    private void initSearch() {
        final List<ImageAsset> imageAssets = new ArrayList<ImageAsset>();
        imageAssets.addAll(materialIconsController.getAssets(materialIconsController.getCategories()));
        for (ImageAsset imageAsset : imageAssets) {
            searchField.addItem(imageAsset);
        }
        searchField.setRenderer(new AssetSpinnerRenderer());
        comboboxSpeedSearch = new ComboboxSpeedSearch(searchField) {
            @Override
            protected String getElementText(Object element) {
                return element instanceof ImageAsset ? ((ImageAsset) element).getName() : "";
            }
        };
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                comboboxSpeedSearch.showPopup();
            }
        });
        searchField.addItemListener(searchFieldListener);
    }

    @NotNull
    @Override
    public Action[] createActions() {
        return SystemInfo.isMac ? new Action[] {this.getHelpAction(), this.getCancelAction(), this.getOKAction()}
                                : new Action[] {this.getOKAction(), this.getCancelAction(), this.getHelpAction()};
    }

    private void prepareSpinner(JComboBox comboBox, ActionListener listener) {
        comboBox.removeActionListener(listener);
        comboBox.setSelectedItem(null);
        comboBox.removeAllItems();
    }

    private void initSpinner(JComboBox comboBox, Object selectedItem, ActionListener listener) {
        comboBox.setSelectedItem(selectedItem);
        comboBox.addActionListener(listener);
    }

    private void updateCategories() {
        prepareSpinner(categorySpinner, categoryActionListener);
        for (String category : controller.getCategories()) {
            categorySpinner.addItem(category);
        }
        categorySpinner.setEnabled(categorySpinner.getItemCount() > 1);
        initSpinner(categorySpinner, controller.getSelectedCategory(), categoryActionListener);
    }

    private void updateAssets() {
        prepareSpinner(assetSpinner, assetActionListener);
        List<ImageAsset> assets = controller.getAssets();
        for (ImageAsset asset : assets) {
            assetSpinner.addItem(asset);
        }
        initSpinner(assetSpinner, controller.getSelectedAsset(), assetActionListener);
    }

    private void updateExportName() {
        resExportName.setText(controller.getExportName());
    }

    private void updateSearch() {
        searchField.removeItemListener(searchFieldListener);
        searchField.setSelectedItem(controller.getSelectedAsset());
        searchField.addItemListener(searchFieldListener);
    }

    private void updateImage() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (imageContainer == null) {
                    return;
                }
                ImageUtils.updateImage(imageContainer,
                                       controller.getSelectedImageFile(),
                                       controller.getFormat());
            }
        });
    }

    @Override
    protected void doHelpAction() {
        materialIconsController.openHelp();
    }

    @Override
    protected void doOKAction() {
        importIcons();
        defaultsController.setImageAsset(controller.getSelectedAsset());
        super.doOKAction();
        settingsController.saveResRootForProject("file://" + controller.getExportRoot());
    }

    private void importIcons() {
        controller.getTask(project).queue();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (StringUtils.isEmpty(controller.getExportRoot())) {
            return new ValidationInfo("Please select the resources root.", resRoot);
        }

        if (StringUtils.isEmpty(controller.getExportName()
                                                      .trim())) {
            return new ValidationInfo("Please select a name for the drawable.", resExportName);
        } else if (!controller.getExportName().matches("[a-z0-9_.]*")) {
            return new ValidationInfo(
                "Please select a valid name for the drawable. There are just \"[a-z0-9_.]\" allowed.",
                resExportName);
        }
        return null;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return uiContainer;
    }

    @Override
    public void updated() {
        updateCategories();
        updateAssets();
        updateSearch();
        updateImage();
        updateExportName();
    }

    private void createUIComponents() {
        resRoot = new FileBrowserField(FileBrowserField.RESOURCE_DIR_CHOOSER);
    }

    private void initResRoot() {
        resRoot.setSelectionListener(resRootListener);
        resRoot.initWithResourceRoot(project, module, settingsController);
        resExportName.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                controller.setExportName((String) resExportName.getValue());
            }
        });
    }

    private class AssetSpinnerRenderer extends ListCellRendererWrapper<ImageAsset> {
        private final Icon EMPTY_ICON = EmptyIcon.ICON_18;

        @Override
        public void customize(JList list, ImageAsset imageAsset, int index, boolean selected, boolean hasFocus) {
            LayeredIcon layeredIcon = new LayeredIcon(2);
            File imageFile = controller.getThumbnailFile(imageAsset);
            if (imageFile != null && imageFile.exists()) {
                final ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                layeredIcon.setIcon(icon, 1, (- icon.getIconWidth() + EMPTY_ICON.getIconWidth())/2, (EMPTY_ICON.getIconHeight() - icon.getIconHeight())/2);
            }
            setIcon(layeredIcon);
            final String searchQuery = comboboxSpeedSearch.getEnteredPrefix();
            if (searchQuery != null &&
                searchQuery.trim().length() > 0 &&
                imageAsset.getName().contains(searchQuery)) {
                setBackground(JBColor.YELLOW);
            } else {
                setBackground(null);
            }
        }
    }
}
