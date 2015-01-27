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

import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.images.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.images.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.images.Resolution;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import de.mprengemann.intellij.plugin.androidicons.util.ExportNameUtils;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.intellij.images.fileTypes.ImageFileTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndroidBatchScaleImporter extends DialogWrapper {
    public static final String CHECKBOX_TEXT = "%s (%.0f px x %.0f px)";
    
    private final Project project;
    private final Module module;
    private JPanel container;
    private JTable table;
    private JLabel imageContainer;
    private JButton addButton;
    private JButton deleteButton;
    private JCheckBox XXXHDPICheckBox;
    private JCheckBox LDPICheckBox;
    private JCheckBox MDPICheckBox;
    private JCheckBox HDPICheckBox;
    private JCheckBox XHDPICheckBox;
    private JCheckBox XXHDPICheckBox;
    private JCheckBox aspectRatioLock;
    private JComboBox methodSpinner;
    private JComboBox algorithmSpinner;
    private ImageTableModel tableModel;
    private VirtualFile resRoot;

    public AndroidBatchScaleImporter(final Project project, final Module module) {
        super(project);
        this.project = project;
        this.module = module;

        setTitle("Android Scale Importer");
        setResizable(false);

        final FileChooserDescriptor imageDescriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor(ImageFileTypeManager.getInstance().getImageFileType());
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FileChooser.chooseFile(imageDescriptor, project, getInitialFile(), new Consumer<VirtualFile>() {
                    @Override
                    public void consume(final VirtualFile file) {
                        SettingsHelper.saveLastImageFolder(project, file.getCanonicalPath());
                        if (resRoot == null) {
                            AndroidResourcesHelper.getResRootFile(project, module, new ResourcesDialog.ResourceSelectionListener() {
                                                                      @Override
                                                                      public void onResourceSelected(VirtualFile resDir) {
                                                                          resRoot = resDir;
                                                                          SettingsHelper.saveResRootForProject(project, resDir.getUrl());
                                                                          addImageFiles(file);
                                                                      }
                                                                  });
                        } else {
                            addImageFiles(file);
                        }
                    }
                });
            }
        });
        deleteButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    tableModel.removeItem(selectedRow);
                    imageContainer.setIcon(null);
                    updateTargetSizes(null);
                }
            }
        });

        algorithmSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResizeAlgorithm algorithm = ResizeAlgorithm.from((String) algorithmSpinner.getSelectedItem());
                methodSpinner.removeAllItems();
                for (String method : algorithm.getMethods()) {
                    methodSpinner.addItem(method);
                }
            }
        });
        for (ResizeAlgorithm algorithms : ResizeAlgorithm.values()) {
            algorithmSpinner.addItem(algorithms.toString());
        }
        
        initTable();
        init();
    }

    private void addImageFiles(VirtualFile file) {
        if (file == null) {
            return;
        }

        String path = file.getCanonicalPath();
        if (path == null) {
            return;
        }
        addImageFiles(new File(path));
    }

    private void addImageFiles(File file) {
        if (file == null) {
            return;
        }
        
        if (!file.isDirectory()) {
            ImageInformation item = parseImageInformation(file);
            if (item != null) {
                tableModel.addItem(item);
            }
        } else {
            File[] files = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    String mimetype = new MimetypesFileTypeMap().getContentType(file);
                    String type = mimetype.split("/")[0];
                    return type.equals("image");
                }
            });

            for (File foundFile : files) {
                addImageFiles(foundFile);
            }
        }
    }

    private ImageInformation parseImageInformation(@NotNull File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            return ImageInformation.newBuilder()
                                   .setExportName(ExportNameUtils.getExportNameFromFilename(imageFile.getName()))
                                   .setExportPath(resRoot.getCanonicalPath())
                                   .setImageFile(imageFile)
                                   .setNinePatch(imageFile.getName().endsWith(".9.png"))
                                   .setImageWidth(image.getWidth())
                                   .setImageHeight(image.getHeight())
                                   .setTargetWidth(image.getWidth())
                                   .setTargetHeight(image.getHeight())
                                   .build(project);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initTable() {
        tableModel = new ImageTableModel();
        table.setModel(tableModel);
        
        initRenderers();
        initAssetResolutions();
        initTargetResolutions();
        initNumberValidator();
        initExportNameValidator();
        initRowSelection();
        initColumnSizes();
    }

    private void initRenderers() {
        DefaultTableCellRenderer fileCellRenderer = new DefaultTableCellRenderer() {
        @Override
        protected void setValue(Object o) {
            File file = (File) o;
            if (file == null) {
                setText("");
                return;
            }
            if (file.isDirectory()) {
                setText(file.getAbsolutePath());
            } else {
                setText(FilenameUtils.removeExtension(file.getName()));
            }

        }
    };
        fileCellRenderer.setHorizontalTextPosition(DefaultTableCellRenderer.RIGHT);
        table.setDefaultRenderer(File.class, fileCellRenderer);
        table.setDefaultRenderer(Resolution.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object o) {
                if (o == null) {
                    setText("");
                } else {
                    setText(((Resolution) o).getName());
                }
            }
        });
        table.getColumnModel().getColumn(6).setCellEditor(new TextBrowserEditor());
    }

    private void initExportNameValidator() {
        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                boolean result = super.stopCellEditing();
                if (!result) {
                    return false;
                }
                String value = (String) getCellEditorValue();
                value = value.trim();
                if ((StringUtils.isNotEmpty(value) && value.matches("[a-z0-9_.]*"))) {
                    return super.stopCellEditing();
                }
                ((JComponent) this.getComponent()).setBorder(new LineBorder(JBColor.RED));
                return false;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                ((JComponent) this.getComponent()).setBorder(table.getBorder());
                return super.getTableCellEditorComponent(
                    table, value, isSelected, row, column);
            }
        });
    }

    private void initNumberValidator() {
        table.getColumnModel().getColumn(3).setCellEditor(new TargetSizeEditor());
        table.getColumnModel().getColumn(4).setCellEditor(new TargetSizeEditor());
    }

    private void initRowSelection() {
        table.getColumnModel().setColumnSelectionAllowed(false);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    updateImage(tableModel.getItem(selectedRow));
                }
            }
        });
    }

    private void initTargetResolutions() {ComboBox targetResolutionComboBox = new ComboBox();
        targetResolutionComboBox.addItem(Resolution.LDPI.getName());
        targetResolutionComboBox.addItem(Resolution.MDPI.getName());
        targetResolutionComboBox.addItem(Resolution.HDPI.getName());
        targetResolutionComboBox.addItem(Resolution.XHDPI.getName());
        targetResolutionComboBox.addItem(Resolution.XXHDPI.getName());
        targetResolutionComboBox.addItem(Resolution.XXXHDPI.getName());
        targetResolutionComboBox.setSelectedIndex(3);
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(targetResolutionComboBox));
    }

    private void initAssetResolutions() {ComboBox assetResolutionComboBox = new ComboBox();
        assetResolutionComboBox.addItem(Resolution.LDPI.getName());
        assetResolutionComboBox.addItem(Resolution.MDPI.getName());
        assetResolutionComboBox.addItem(Resolution.HDPI.getName());
        assetResolutionComboBox.addItem(Resolution.XHDPI.getName());
        assetResolutionComboBox.addItem(Resolution.XXHDPI.getName());
        assetResolutionComboBox.addItem(Resolution.XXXHDPI.getName());
        assetResolutionComboBox.addItem(Resolution.OTHER.getName());
        assetResolutionComboBox.setSelectedIndex(3);
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(assetResolutionComboBox));
    }

    private void initColumnSizes() {
        table.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                super.componentResized(componentEvent);
                Dimension tableSize = table.getSize();
                final int[] columnSizes = new int[]{ 20, 10, 10, 10, 10, 20, 20};
                for (int i = 0; i < table.getColumnCount(); i++) {
                    TableColumn column = table.getColumnModel().getColumn(i);
                    column.setPreferredWidth((int) (tableSize.width * (columnSizes[i] / 100f)));
                }
            }
        });
    }

    private void updateImage(ImageInformation item) {
        if (imageContainer == null) {
            return;
        }
        updateTargetSizes(item);
        ImageUtils.updateImage(imageContainer, item.getImageFile());
    }

    private void updateTargetSizes(ImageInformation item) {
        if (item == null) {
            LDPICheckBox.setText(Resolution.LDPI.getName());
            MDPICheckBox.setText(Resolution.MDPI.getName());
            HDPICheckBox.setText(Resolution.HDPI.getName());
            XHDPICheckBox.setText(Resolution.XHDPI.getName());
            XXHDPICheckBox.setText(Resolution.XXHDPI.getName());
            XXXHDPICheckBox.setText(Resolution.XXXHDPI.getName());
        } else {
            int targetHeight = item.getTargetHeight();
            int targetWidth = item.getTargetWidth();
            float factor = RefactorHelper.getScaleFactor(Resolution.LDPI, item.getResolution());
            LDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                               Resolution.LDPI.getName(),
                                               factor * targetWidth,
                                               factor * targetHeight));
            factor = RefactorHelper.getScaleFactor(Resolution.MDPI, item.getResolution());
            MDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                               Resolution.MDPI.getName(),
                                               factor * targetWidth,
                                               factor * targetHeight));
            factor = RefactorHelper.getScaleFactor(Resolution.HDPI, item.getResolution());
            HDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                               Resolution.HDPI.getName(),
                                               factor * targetWidth,
                                               factor * targetHeight));
            factor = RefactorHelper.getScaleFactor(Resolution.XHDPI, item.getResolution());
            XHDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                                Resolution.XHDPI.getName(),
                                                factor * targetWidth,
                                                factor * targetHeight));
            factor = RefactorHelper.getScaleFactor(Resolution.XXHDPI, item.getResolution());
            XXHDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                                 Resolution.XXHDPI.getName(),
                                                 factor * targetWidth,
                                                 factor * targetHeight));
            factor = RefactorHelper.getScaleFactor(Resolution.XXXHDPI, item.getResolution());
            XXXHDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                                  Resolution.XXXHDPI.getName(),
                                                  factor * targetWidth,
                                                  factor * targetHeight));
        }
    }

    protected VirtualFile getInitialFile() {
        String directoryName = SettingsHelper.getLastImageFolder(project);
        VirtualFile path;
        for (path = LocalFileSystem.getInstance().findFileByPath(expandPath(directoryName));
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

    private String expandPath(String dirName) {
        if (project != null) {
            dirName = PathMacroManager.getInstance(project).expandPath(dirName);
        }
        if (module != null) {
            dirName = PathMacroManager.getInstance(module).expandPath(dirName);
        }
        return dirName;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return container;
    }

    private void createUIComponents() {
        table = new JBTable() {
            @NotNull
            @Override
            public Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isCellSelected(row, column)) {
                    if (!isCellEditable(row, column)) {
                        c.setBackground(getSelectionBackground().darker());
                    } else {
                        c.setBackground(getSelectionBackground());
                    }
                } else {
                    if (!isCellEditable(row, column)) {
                        c.setBackground(getBackground().darker());
                    } else {
                        c.setBackground(getBackground());
                    }
                }
                return c;
            }
        };
    }

    @Override
    protected void doOKAction() {
        if (table.getModel().getRowCount() == 0) {
            super.doOKAction();
            return;
        }
        
        RefactoringTask task = new RefactoringTask(project);
        List<ImageInformation> imageInformationList = tableModel.imageInformationList;
        for (int i = 0; i < imageInformationList.size(); i++) {
            ImageInformation information = imageInformationList.get(i);
            Resolution resolution = tableModel.resolutionList.get(i);
            if (resolution == Resolution.OTHER) {
                resolution = information.getResolution();
            }
            importSingleImage(information, resolution, task);
        }
        DumbService.getInstance(project).queueTask(task);
        super.doOKAction();
    }

    private void importSingleImage(ImageInformation baseInformation, Resolution targetResolution, RefactoringTask task) {
        float toLDPI = RefactorHelper.getScaleFactor(Resolution.LDPI, targetResolution);
        float toMDPI = RefactorHelper.getScaleFactor(Resolution.MDPI, targetResolution);
        float toHDPI = RefactorHelper.getScaleFactor(Resolution.HDPI, targetResolution);
        float toXHDPI = RefactorHelper.getScaleFactor(Resolution.XHDPI, targetResolution);
        float toXXHDPI = RefactorHelper.getScaleFactor(Resolution.XXHDPI, targetResolution);
        float toXXXHDPI = RefactorHelper.getScaleFactor(Resolution.XXXHDPI, targetResolution);

        task.addImage(getImageInformation(baseInformation, Resolution.LDPI, toLDPI, LDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.MDPI, toMDPI, MDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.HDPI, toHDPI, HDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.XHDPI, toXHDPI, XHDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.XXHDPI, toXXHDPI, XXHDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.XXXHDPI, toXXXHDPI, XXXHDPICheckBox));
    }

    private ImageInformation getImageInformation(ImageInformation baseInformation,
                                                 Resolution resolution,
                                                 float factor,
                                                 JCheckBox checkbox) {
        if (!checkbox.isSelected()) {
            return null;
        }

        ResizeAlgorithm algorithm = ResizeAlgorithm.from((String) algorithmSpinner.getSelectedItem());
        Object algorithmMethod = algorithm.getMethod((String) methodSpinner.getSelectedItem());

        return ImageInformation.newBuilder(baseInformation)
                               .setAlgorithm(algorithm)
                               .setMethod(algorithmMethod)
                               .setResolution(resolution)
                               .setFactor(factor)
                               .build(project);
    }

    class ImageTableModel extends AbstractTableModel {
        List<String> columnNames = Arrays.asList("Asset",
                                                 "Resolution",
                                                 "Target-Resolution",
                                                 "Target-Width",
                                                 "Target-Height",
                                                 "Exportname",
                                                 "Exportpath");
        List<ImageInformation> imageInformationList;
        List<Resolution> resolutionList;

        public ImageTableModel() {
            imageInformationList = new ArrayList<ImageInformation>();
            resolutionList = new ArrayList<Resolution>();
        }

        @Override
        public int getRowCount() {
            return imageInformationList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames.get(col);
        }

        public void addItem(ImageInformation item) {
            imageInformationList.add(item);
            resolutionList.add(Resolution.XHDPI);
            fireTableRowsInserted(imageInformationList.size() - 1, imageInformationList.size() - 1);
            table.setRowSelectionInterval(imageInformationList.size() - 1, imageInformationList.size() - 1);
        }

        public void removeItem(int row) {
            imageInformationList.remove(row);
            resolutionList.remove(Resolution.XHDPI);
            fireTableRowsDeleted(row, row);
        }

        @Override
        public Object getValueAt(int row, int col) {
            ImageInformation information = imageInformationList.get(row);
            switch (col) {
                //Asset
                case 0:
                    return information.getImageFile();
                //Resolution
                case 1:
                    return resolutionList.get(row);
                //Target-Resolution
                case 2:
                    return information.getResolution();
                //Target-Width
                case 3:
                    return information.getTargetWidth();
                //Target-Height
                case 4:
                    return information.getTargetHeight();
                //Exportname
                case 5:
                    return information.getExportName();
                //Exportpath
                case 6:
                    return information.getExportPath();
            }
            return information;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return false;
            }
            if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4) {
                Resolution resolution = (Resolution) getValueAt(rowIndex, 1);
                return resolution == Resolution.OTHER;
            }
            return true;
        }
        
        @Override
        public void setValueAt(Object value, int row, int col) {
            ImageInformation imageInformation = imageInformationList.get(row);
            ImageInformation.Builder builder = ImageInformation.newBuilder(imageInformation);
            switch (col) {
                //Resolution
                case 1:
                    resolutionList.set(row, Resolution.from((String) value));
                    break;
                //Target-Resolution
                case 2:
                    builder.setResolution(Resolution.from((String) value));
                    break;
                //Target-Width
                case 3:
                    if (aspectRatioLock.isSelected()) {
                        try {
                            int targetWidth = (Integer) value;
                            int targetHeight = (int) ((float) (imageInformation.getImageHeight() * targetWidth) / (float) imageInformation.getImageWidth());
                            builder.setTargetHeight(targetHeight)
                                   .setTargetWidth(targetWidth);
                        } catch (Exception ignored) {
                        }
                    } else {
                        builder.setTargetWidth((Integer) value);
                    }
                    updateTargetSizes(builder.build(project));
                    break;
                //Target-Height
                case 4:
                    if (aspectRatioLock.isSelected()) {
                        try {
                            int targetHeight = (Integer) value;
                            int targetWidth = (int) ((float) (imageInformation.getImageWidth() * targetHeight) / (float) imageInformation.getImageHeight());
                            builder.setTargetHeight(targetHeight)
                                   .setTargetWidth(targetWidth);
                        } catch (Exception ignored) {
                        }
                    } else {
                        builder.setTargetHeight((Integer) value);
                    }
                    updateTargetSizes(builder.build(project));
                    break;
                //Exportname
                case 5:
                    builder.setExportName((String) value);
                    break;
                //Exportpath
                case 6:
                    builder.setExportPath((String) value);
                    break;
            }
            imageInformationList.set(row, builder.build(project));
            fireTableRowsUpdated(row, row);
        }


        @Override
        public Class<?> getColumnClass(int col) {
            return getValueAt(0, col).getClass();
        }

        public ImageInformation getItem(int row) {
            return imageInformationList.get(row);
        }
    }

    public class TextBrowserEditor extends AbstractCellEditor implements TableCellEditor {
        TextFieldWithBrowseButton button;

        public TextBrowserEditor() {
            button = new TextFieldWithBrowseButton();
            AndroidResourcesHelper.initResourceBrowser(project, module, "Select res root", button);
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            button.setText((String) value);
            return button;
        }
    }
    
    class TargetSizeEditor extends DefaultCellEditor {
        Class<?>[] argTypes;
        Constructor<?> constructor;
        Object value;

        public TargetSizeEditor() {
            this(new JTextField());
        }

        public TargetSizeEditor(JTextField textField) {
            super(textField);
            ((JTextField) this.getComponent()).setHorizontalAlignment(4);
            this.argTypes = new Class[] {String.class};
            this.getComponent().setName("Table.editor");
        }

        public boolean stopCellEditing() {
            String s = (String) super.getCellEditorValue();
            if ("".equals(s)) {
                if (this.constructor.getDeclaringClass() == String.class) {
                    this.value = s;
                }

                super.stopCellEditing();
            }

            try {
                this.value = this.constructor.newInstance(new Object[] {s});
                Integer value = (Integer) getCellEditorValue();
                if (value > 0) {
                    return super.stopCellEditing();
                } else {
                    throw new IllegalArgumentException();
                }
            } catch (Exception var3) {
                ((JComponent) this.getComponent()).setBorder(new LineBorder(JBColor.RED));
                return false;
            }
        }

        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            this.value = null;
            ((JComponent) this.getComponent()).setBorder(new LineBorder(JBColor.BLACK));

            try {
                Class e = table.getColumnClass(column);
                if (e == Object.class) {
                    e = String.class;
                }

                this.constructor = e.getConstructor(this.argTypes);
            } catch (Exception var7) {
                return null;
            }

            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        public Object getCellEditorValue() {
            return this.value;
        }
    }
}
