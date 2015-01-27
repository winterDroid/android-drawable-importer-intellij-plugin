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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.table.JBTable;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.images.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.images.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.images.Resolution;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import de.mprengemann.intellij.plugin.androidicons.util.ExportNameUtils;
import org.apache.commons.io.FilenameUtils;
import org.intellij.images.fileTypes.ImageFileTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndroidBatchScaleImporter extends DialogWrapper {
    private final Project project;
    private final Module module;
    private JPanel container;
    private JTable table;
    private JLabel imageContainer;
    private JButton addButton;
    private JButton deleteButton;
    private ImageTableModel tableModel;
    private VirtualFile resRoot;

    public AndroidBatchScaleImporter(final Project project, final Module module) {
        super(project);
        this.project = project;
        this.module = module;

        setTitle("Android Scale Importer");
//        setResizable(false);

        final FileChooserDescriptor imageDescriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor(
            ImageFileTypeManager.getInstance().getImageFileType());
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
                }
            }
        });
        
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
                                   .setTargetWidth(image.getWidth())
                                   .setTargetHeight(image.getHeight())
                                   .build(project);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initTable() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableModel = new ImageTableModel();
        table.setModel(tableModel);
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
        
        ComboBox assetResolutionComboBox = new ComboBox();
        assetResolutionComboBox.addItem(Resolution.LDPI.getName());
        assetResolutionComboBox.addItem(Resolution.MDPI.getName());
        assetResolutionComboBox.addItem(Resolution.HDPI.getName());
        assetResolutionComboBox.addItem(Resolution.XHDPI.getName());
        assetResolutionComboBox.addItem(Resolution.XXHDPI.getName());
        assetResolutionComboBox.addItem(Resolution.XXXHDPI.getName());
        assetResolutionComboBox.addItem(Resolution.OTHER.getName());
        assetResolutionComboBox.setSelectedIndex(3);
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(assetResolutionComboBox));

        ComboBox targetResolutionComboBox = new ComboBox();
        targetResolutionComboBox.addItem(Resolution.LDPI.getName());
        targetResolutionComboBox.addItem(Resolution.MDPI.getName());
        targetResolutionComboBox.addItem(Resolution.HDPI.getName());
        targetResolutionComboBox.addItem(Resolution.XHDPI.getName());
        targetResolutionComboBox.addItem(Resolution.XXHDPI.getName());
        targetResolutionComboBox.addItem(Resolution.XXXHDPI.getName());
        targetResolutionComboBox.setSelectedIndex(3);
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(targetResolutionComboBox));

        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalTextPosition(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(defaultTableCellRenderer);
        
        table.getColumnModel().getColumn(6).setCellEditor(new TextBrowserEditor());
        
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

    private void updateImage(ImageInformation item) {
        if (imageContainer == null) {
            return;
        }
        ImageUtils.updateImage(imageContainer, item.getImageFile());
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
                    return information.getImageWidth();
                //Target-Height
                case 4:
                    return information.getImageHeight();
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
            ImageInformation.Builder information = ImageInformation.newBuilder(imageInformationList.get(row));
            switch (col) {
                //Resolution
                case 1:
                    resolutionList.set(row, Resolution.from((String) value));
                    break;
                //Target-Resolution
                case 2:
                    information.setResolution(Resolution.from((String) value));
                    break;
                //Target-Width
                case 3:
                    information.setTargetWidth((Integer) value);
                    break;
                //Target-Height
                case 4:
                    information.setTargetHeight((Integer) value);
                    break;
                //Exportname
                case 5:
                    information.setExportName((String) value);
                    break;
                //Exportpath
                case 6:
                    information.setExportPath((String) value);
                    break;
            }
            imageInformationList.set(row, information.build(project));
            fireTableCellUpdated(row, col);
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
}
