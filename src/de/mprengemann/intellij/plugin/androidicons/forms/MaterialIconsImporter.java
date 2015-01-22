package de.mprengemann.intellij.plugin.androidicons.forms;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;
import de.mprengemann.intellij.plugin.androidicons.images.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.images.Resolution;
import de.mprengemann.intellij.plugin.androidicons.settings.PluginSettings;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialIconsImporter extends DialogWrapper {

    private static final String DEFAULT_RESOLUTION = "drawable-xhdpi";
    private VirtualFile assetRoot;

    private Project project;
    private JLabel imageContainer;
    private TextFieldWithBrowseButton resRoot;
    private JComboBox assetSpinner;
    private JComboBox colorSpinner;
    private JComboBox categorySpinner;
    private JComboBox sizeSpinner;
    
    private JTextField resExportName;
    private JCheckBox MDPICheckBox;
    private JCheckBox HDPICheckBox;
    private JCheckBox XHDPICheckBox;
    private JCheckBox XXHDPICheckBox;
    private JCheckBox XXXHDPICheckBox;
    private JPanel container;
    private boolean exportNameChanged = false;
    private final Comparator<File> alphabeticalComparator = new Comparator<File>() {
        @Override
        public int compare(File file1, File file2) {
            if (file1 != null && file2 != null) {
                return file1.getName().compareTo(file2.getName());
            }
            return 0;
        }
    };

    public MaterialIconsImporter(@Nullable final Project project, Module module) {
        super(project, true);
        this.project = project;
        
        setTitle("Material Icons Importer");
        setResizable(false);

        AndroidResourcesHelper.initResourceBrowser(project, module, "Select res root", this.resRoot);
        assetRoot = SettingsHelper.getAssetPath(IconPack.MATERIAL_ICONS);
        
        getHelpAction().setEnabled(true);

        fillCategories();
        fillAssets();
        fillSizes();
        fillColors();

        categorySpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fillAssets();
                updateImage();
            }
        });
        AssetSpinnerRenderer renderer = new AssetSpinnerRenderer();
        //noinspection GtkPreferredJComboBoxRenderer
        assetSpinner.setRenderer(renderer);
        assetSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                fillSizes();
                updateImage();
            }
        });
        sizeSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fillColors();
                updateImage();
            }
        });
        colorSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateImage();
            }
        });

        resExportName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                super.keyTyped(keyEvent);
                if (!exportNameChanged && keyEvent != null && keyEvent.getKeyCode() > -1) {
                    exportNameChanged = true;
                }
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                super.keyPressed(keyEvent);
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                super.keyReleased(keyEvent);
            }
        });
        imageContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                updateImage();
            }
        });

        init();
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
        String assetColor = (String) colorSpinner.getSelectedItem();
        String assetName = (String) assetSpinner.getSelectedItem();
        String assetCategory = (String) categorySpinner.getSelectedItem();
        String assetSize = (String) sizeSpinner.getSelectedItem();

        if (assetColor == null ||
            assetName == null ||
            assetCategory == null ||
            assetSize == null) {
            return;
        }

        String path = assetCategory + "/" + DEFAULT_RESOLUTION + "/ic_" + assetName + "_" + assetColor + "_" + assetSize + ".png";
        File imageFile = new File(assetRoot.getCanonicalPath(), path);
        ImageUtils.updateImage(imageContainer, imageFile);
        if (!exportNameChanged) {
            resExportName.setText("ic_action_" + assetName);
        }
    }
    
    private void fillCategories() {
        categorySpinner.removeAllItems();
        if (this.assetRoot.getCanonicalPath() == null) {
            return;
        }
        File assetRoot = new File(this.assetRoot.getCanonicalPath());
        final FilenameFilter folderFileNameFiler = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return !s.startsWith(".") &&
                       new File(file, s).isDirectory() &&
                       !PluginSettings.BLACKLISTED_MATERIAL_ICONS_FOLDER
                           .contains(FilenameUtils.removeExtension(s));
            }
        };
        File[] categories = assetRoot.listFiles(folderFileNameFiler);
        Arrays.sort(categories, alphabeticalComparator);
        for (File file : categories) {
            categorySpinner.addItem(file.getName());
        }
    }
    
    private void fillAssets() {
        assetSpinner.removeAllItems();
        if (this.assetRoot.getCanonicalPath() == null) {
            return;
        }
        File assetRoot = new File(this.assetRoot.getCanonicalPath());
        assetRoot = new File(assetRoot, (String) categorySpinner.getSelectedItem());
        assetRoot = new File(assetRoot, DEFAULT_RESOLUTION);
        final FilenameFilter drawableFileNameFiler = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (!FilenameUtils.isExtension(s, "png")) {
                    return false;
                }
                String filename = FilenameUtils.removeExtension(s);
                return filename.startsWith("ic_") &&
                       filename.endsWith("_black_48dp");
            }
        };
        File[] assets = assetRoot.listFiles(drawableFileNameFiler);
        for (File asset : assets) {
            String assetName = FilenameUtils.removeExtension(asset.getName());
            assetName = assetName.replace("_black_48dp", "");
            assetName = assetName.replace("ic_", "");
            assetSpinner.addItem(assetName);
        }
    }
    
    private void fillSizes() {
        final String lastSelection = (String) sizeSpinner.getSelectedItem();
        sizeSpinner.removeAllItems();
        if (this.assetRoot.getCanonicalPath() == null) {
            return;
        }
        File assetRoot = new File(this.assetRoot.getCanonicalPath());
        assetRoot = new File(assetRoot, (String) categorySpinner.getSelectedItem());
        assetRoot = new File(assetRoot, DEFAULT_RESOLUTION);
        final String assetName = (String) assetSpinner.getSelectedItem();
        final FilenameFilter drawableFileNameFiler = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (!FilenameUtils.isExtension(s, "png")) {
                    return false;
                }
                String filename = FilenameUtils.removeExtension(s);
                return filename.startsWith("ic_" + assetName + "_");
            }
        };
        File[] assets = assetRoot.listFiles(drawableFileNameFiler);
        Set<String> sizes = new HashSet<String>();
        for (File asset : assets) {
            String drawableName = FilenameUtils.removeExtension(asset.getName());
            String[] numbers = drawableName.replaceAll("[^-?0-9]+", " ").trim().split(" ");
            drawableName = numbers[numbers.length - 1].trim() + "dp";
            sizes.add(drawableName);
        }
        List<String> list = new ArrayList<String>();
        list.addAll(sizes);
        Collections.sort(list);
        for (String size : list) {
            sizeSpinner.addItem(size);
        }
        if (list.contains(lastSelection)) {
            sizeSpinner.setSelectedIndex(list.indexOf(lastSelection));
        }
    }
    
    private void fillColors() {
        final String lastSelection = (String) colorSpinner.getSelectedItem();
        colorSpinner.removeAllItems();
        if (this.assetRoot.getCanonicalPath() == null) {
            return;
        }
        File assetRoot = new File(this.assetRoot.getCanonicalPath());
        assetRoot = new File(assetRoot, (String) categorySpinner.getSelectedItem());
        assetRoot = new File(assetRoot, DEFAULT_RESOLUTION);
        final String assetName = (String) assetSpinner.getSelectedItem();
        final String assetSize = (String) sizeSpinner.getSelectedItem();
        final FilenameFilter drawableFileNameFiler = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (!FilenameUtils.isExtension(s, "png")) {
                    return false;
                }
                String filename = FilenameUtils.removeExtension(s);
                return filename.startsWith("ic_" + assetName + "_") &&
                       filename.endsWith("_" + assetSize);
            }
        };
        File[] assets = assetRoot.listFiles(drawableFileNameFiler);
        Set<String> colors = new HashSet<String>();
        for (File asset : assets) {
            String drawableName = FilenameUtils.removeExtension(asset.getName());
            String[] color = drawableName.split("_");
            drawableName = color[color.length - 2].trim();
            colors.add(drawableName);
        }
        List<String> list = new ArrayList<String>();
        list.addAll(colors);
        Collections.sort(list);
        for (String size : list) {
            colorSpinner.addItem(size);
        }
        if (list.contains(lastSelection)) {
            colorSpinner.setSelectedIndex(list.indexOf(lastSelection));
        }
    }

    @Override
    protected void doHelpAction() {
        try {
            BrowserUtil.browse("file://" + new File(assetRoot.getCanonicalPath(), "index.html").getCanonicalPath());
        } catch (IOException ignored) {
        }
    }

    @Override
    protected void doOKAction() {
        importIcons();
        super.doOKAction();
    }

    private void importIcons() {
        List<FromToPath> paths = new ArrayList<FromToPath>();
        if (MDPICheckBox.isSelected()) {
            paths.add(new FromToPath(Resolution.MDPI));
        }
        if (HDPICheckBox.isSelected()) {
            paths.add(new FromToPath(Resolution.HDPI));
        }
        if (XHDPICheckBox.isSelected()) {
            paths.add(new FromToPath(Resolution.XHDPI));
        }
        if (XXHDPICheckBox.isSelected()) {
            paths.add(new FromToPath(Resolution.XXHDPI));
        }
        if (XXXHDPICheckBox.isSelected()) {
            paths.add(new FromToPath(Resolution.XXXHDPI));
        }
        copyDrawables(paths);
    }

    private boolean copyDrawables(List<FromToPath> paths) {
        try {
            if (paths != null && paths.size() > 0) {
                List<File> sources = new ArrayList<File>();
                List<File> targets = new ArrayList<File>();
                for (FromToPath path : paths) {
                    if (path.source.exists()) {
                        sources.add(path.source);
                        targets.add(path.target);
                    }
                }
                RefactorHelper.copy(project, sources, targets);
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return true;
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

        return null;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return container;
    }

    private class FromToPath {
        public final File source;
        public final File target;
        public final Resolution resolution;

        private FromToPath(Resolution resolution) {
            this.resolution = resolution;

            String resRootText = resRoot.getText();

            String assetCategory = (String) categorySpinner.getSelectedItem();
            String assetName = (String) assetSpinner.getSelectedItem();
            String assetColor = (String) colorSpinner.getSelectedItem();
            String assetSize = (String) sizeSpinner.getSelectedItem();
            String fromName = "ic_" + assetName + "_" + assetColor + "_" + assetSize + ".png";
            String toName = resExportName.getText();

            this.source = new File(assetRoot.getCanonicalPath(), assetCategory + "/drawable-" + resolution + "/" + fromName);
            this.target = ImageUtils.getTargetFile(resRootText, resolution, toName);
        }
    }

    private class AssetSpinnerRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b2) {
            JLabel label = (JLabel) super.getListCellRendererComponent(jList, o, i, b, b2);
            if (label != null) {
                String name = (String) assetSpinner.getItemAt(i);
                String path = categorySpinner.getSelectedItem() + "/" + DEFAULT_RESOLUTION + "/ic_" + name + "_black_24dp.png";
                File imageFile = new File(assetRoot.getCanonicalPath(), path);
                if (imageFile.exists()) {
                    label.setIcon(new ImageIcon(imageFile.getAbsolutePath()));
                }
            }
            return label;
        }
    }
}