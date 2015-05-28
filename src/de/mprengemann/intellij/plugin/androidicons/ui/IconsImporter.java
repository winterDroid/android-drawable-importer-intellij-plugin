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

package de.mprengemann.intellij.plugin.androidicons.ui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import de.mprengemann.intellij.plugin.androidicons.controllers.androidicons.IAndroidIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IIconsImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter.IconsImporterObserver;
import de.mprengemann.intellij.plugin.androidicons.controllers.materialicons.IMaterialIconsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.Asset;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.widgets.ResourceBrowser;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

public class IconsImporter extends DialogWrapper implements IconsImporterObserver {

    private final Project project;
    private IAndroidIconsController androidIconsController;
    private IMaterialIconsController materialIconsController;
    private IIconsImporterController iconImporterController;
    private ISettingsController settingsController;

    private JLabel imageContainer;
    private JComboBox assetSpinner;
    private JComboBox colorSpinner;
    private JComboBox categorySpinner;
    private JComboBox sizeSpinner;

    private JPanel uiContainer;
    private ResourceBrowser resRoot;
    private JTextField resExportName;
    private JCheckBox LDPICheckBox;
    private JCheckBox MDPICheckBox;
    private JCheckBox HDPICheckBox;
    private JCheckBox XHDPICheckBox;
    private JCheckBox XXHDPICheckBox;
    private JCheckBox XXXHDPICheckBox;
    private JComboBox iconPackSpinner;
    private JComboBox searchField;

    public IconsImporter(final Project project, Module module) {
        super(project, true);

        final IconApplication container = ApplicationManager.getApplication().getComponent(IconApplication.class);
        iconImporterController = container.getControllerFactory().getIconImporterController();
        androidIconsController = container.getControllerFactory().getAndroidIconsController();
        materialIconsController = container.getControllerFactory().getMaterialIconsController();
        settingsController = container.getControllerFactory().getSettingsController();
        iconImporterController.addObserver(this);

        this.project = project;

        resRoot.setSelectionListener(new Consumer<File>() {
            @Override
            public void consume(File file) {
                String path;
                if (file == null) {
                    path = "";
                } else {
                    path = file.getPath();
                }
                iconImporterController.setExportRoot(path);
            }
        });
        resRoot.init(project, module, container.getControllerFactory().getSettingsController());

        setTitle("Icons Importer");
        setResizable(false);
        getHelpAction().setEnabled(true);

        iconPackSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                iconImporterController.reset();
                iconImporterController.setSelectedIconPack(IconPack.from((String) iconPackSpinner.getSelectedItem()));
            }
        });
        fillPacks();
        categorySpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                iconImporterController.setSelectedCategory((String) categorySpinner.getSelectedItem());
            }
        });
        fillCategories();
        AssetSpinnerRenderer renderer = new AssetSpinnerRenderer();
        //noinspection GtkPreferredJComboBoxRenderer
        assetSpinner.setRenderer(renderer);
        assetSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                iconImporterController.setSelectedAsset((Asset) assetSpinner.getSelectedItem());
            }
        });
        fillAssets();
        sizeSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                iconImporterController.setSelectedSize((String) sizeSpinner.getSelectedItem());
            }
        });
        fillSizes();
        colorSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                iconImporterController.setSelectedColor((String) colorSpinner.getSelectedItem());
            }
        });
        fillColors();

        resExportName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                iconImporterController.setExportName(resExportName.getText());
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {}

            @Override
            public void keyReleased(KeyEvent keyEvent) {}
        });
        imageContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                updateImage();
            }
        });

        initCheckBox(Resolution.LDPI, LDPICheckBox);
        initCheckBox(Resolution.MDPI, MDPICheckBox);
        initCheckBox(Resolution.HDPI, HDPICheckBox);
        initCheckBox(Resolution.XHDPI, XHDPICheckBox);
        initCheckBox(Resolution.XXHDPI, XXHDPICheckBox);
        initCheckBox(Resolution.XXXHDPI, XXXHDPICheckBox);
        initSearch(searchField);

        init();
    }

    private void initSearch(final JComboBox searchField) {
        final EventList<Asset> assets = GlazedLists.eventList(null);
        assets.addAll(androidIconsController.getAssets());
        assets.addAll(materialIconsController.getAssets());
        for (Asset asset : assets) {
            searchField.addItem(asset);
        }

        TextFilterator<Asset> textFilterator = GlazedLists.textFilterator(
            Asset.class, "name");

        AutoCompleteSupport support = AutoCompleteSupport.install(
            searchField, assets,
            textFilterator, new AssetFormat());
        support.setStrict(true);
        support.setHidesPopupOnFocusLost(true);
        support.setBeepOnStrictViolation(true);
        support.setCorrectsCase(true);

        //noinspection GtkPreferredJComboBoxRenderer
        searchField.setRenderer(new AssetSpinnerRenderer());

        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Asset selectedItem = (Asset) searchField.getSelectedItem();
                if (selectedItem == null) {
                    return;
                }
                iconImporterController.setSelectedIconPack(selectedItem.getIconPack());
                if (selectedItem.getIconPack() == IconPack.MATERIAL_ICONS) {
                    iconImporterController.setSelectedCategory(selectedItem.getCategory());
                }
                iconImporterController.setSelectedAsset(selectedItem);
            }
        });
    }

    private void initCheckBox(final Resolution resolution, final JCheckBox checkBox) {
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                iconImporterController.setExportResolution(resolution, checkBox.isSelected());
            }
        });
        checkBox.setSelected(true);
    }

    private void fillPacks() {
        if (androidIconsController.isInitialized()) {
            iconPackSpinner.addItem(IconPack.ANDROID_ICONS.getName());
        }
        if (materialIconsController.isInitialized()) {
            iconPackSpinner.addItem(IconPack.MATERIAL_ICONS.getName());
        }
    }

    @NotNull
    @Override
    public Action[] createActions() {
        return SystemInfo.isMac ? new Action[] {this.getHelpAction(), this.getCancelAction(), this.getOKAction()}
                                : new Action[] {this.getOKAction(), this.getCancelAction(), this.getHelpAction()};
    }

    private void updateImage() {
        if (imageContainer == null) {
            return;
        }
        ImageUtils.updateImage(imageContainer, iconImporterController.getSelectedImageFile());
    }

    private void fillCategories() {
        categorySpinner.removeAllItems();
        List<String> categories = new ArrayList<String>();
        switch (iconImporterController.getSelectedPack()) {
            case MATERIAL_ICONS:
                categories = materialIconsController.getCategories();
                categorySpinner.setEnabled(true);
                break;
            case ANDROID_ICONS:
                categorySpinner.setEnabled(false);
                break;
        }
        for (String category : categories) {
            categorySpinner.addItem(category);
        }
        if (categories.size() == 0) {
            iconImporterController.setSelectedCategory("");
        }
    }

    private void fillAssets() {
        assetSpinner.removeAllItems();
        List<Asset> assets = new ArrayList<Asset>();
        switch (iconImporterController.getSelectedPack()) {
            case MATERIAL_ICONS:
                assets = materialIconsController.getAssets(iconImporterController);
                break;
            case ANDROID_ICONS:
                assets = androidIconsController.getAssets();
                break;
        }
        for (Asset asset : assets) {
            assetSpinner.addItem(asset);
        }
    }

    private void fillSizes() {
        String lastItem = iconImporterController.getSelectedSize();
        sizeSpinner.removeAllItems();
        List<String> sizes = new ArrayList<String>();
        switch (iconImporterController.getSelectedPack()) {
            case MATERIAL_ICONS:
                sizes = materialIconsController.getSizes(iconImporterController);
                break;
            case ANDROID_ICONS:
                sizes = androidIconsController.getSizes();
                break;
        }
        for (String size : sizes) {
            sizeSpinner.addItem(size);
        }

        if (sizes.contains(lastItem)) {
            sizeSpinner.setSelectedItem(lastItem);
        }
    }

    private void fillColors() {
        String lastItem = iconImporterController.getSelectedColor();
        colorSpinner.removeAllItems();
        List<String> colors = new ArrayList<String>();
        switch (iconImporterController.getSelectedPack()) {
            case MATERIAL_ICONS:
                colors = materialIconsController.getColors(iconImporterController);
                break;
            case ANDROID_ICONS:
                colors = androidIconsController.getColors();
                break;
        }
        for (String color : colors) {
            colorSpinner.addItem(color);
        }

        if (colors.contains(lastItem)) {
            colorSpinner.setSelectedItem(lastItem);
        }
    }

    @Override
    protected void doHelpAction() {
        materialIconsController.openHelp();
    }

    @Override
    protected void doOKAction() {
        importIcons();
        super.doOKAction();
        settingsController.saveResRootForProject(project,
                                                 "file://" + iconImporterController.getExportRoot());
    }

    private void importIcons() {
        RefactoringTask task = iconImporterController.getTask(project);
        ProgressManager.getInstance().run(task);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (StringUtils.isEmpty(iconImporterController.getExportRoot())) {
            return new ValidationInfo("Please select the resources root.", resRoot);
        }

        if (StringUtils.isEmpty(resExportName.getText().trim())) {
            return new ValidationInfo("Please select a name for the drawable.", resExportName);
        } else if (!resExportName.getText().matches("[a-z0-9_.]*")) {
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
    public void onIconPackChanged() {
        fillCategories();
    }

    @Override
    public void onCategoryChanged() {
        fillAssets();
    }

    @Override
    public void onAssetChanged() {
        fillSizes();
    }

    @Override
    public void onSizeChanged() {
        fillColors();
    }

    @Override
    public void onColorChanged() {
        updateImage();
    }

    @Override
    public void onExportNameChanged(String exportName) {
        if (resExportName.getText().equals(exportName)) {
            return;
        }
        resExportName.setText(exportName);
    }

    @Override
    public void onExportRootChanged(String exportRoot) {
        if (resRoot.getText().equals(exportRoot)) {
            return;
        }
        resRoot.setText(exportRoot);
    }

    private class AssetSpinnerRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Asset asset = (Asset) value;
            JLabel label = (JLabel) super.getListCellRendererComponent(list, asset.getName(), index, isSelected, cellHasFocus);
            if (label == null ||
                iconImporterController == null) {
                return label;
            }
            File imageFile = iconImporterController.getImageFile(asset);
            if (imageFile.exists()) {
                label.setIcon(new ImageIcon(imageFile.getAbsolutePath()));
            }
            return label;
        }
    }

    private class AssetFormat extends Format {
        @Override
        public StringBuffer format(Object obj, @NotNull StringBuffer toAppendTo, @NotNull FieldPosition pos) {
            if (obj != null) {
                toAppendTo.append(((Asset) obj).getName());
            }
            return toAppendTo;
        }

        @Override
        public Object parseObject(String s, @NotNull ParsePosition parsePosition) {
            return null;
        }
    }
}