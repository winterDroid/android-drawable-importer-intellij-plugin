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
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.ex.FileDrop;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.ui.table.JBTable;
import com.intellij.util.Consumer;
import com.intellij.util.ui.UIUtil;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.BatchScaleImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.BatchScaleImporterObserver;
import de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.IBatchScaleImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.additem.AddItemBatchScaleImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.additem.IAddItemBatchScaleImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.defaults.IDefaultsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.util.MathUtils;
import org.apache.commons.io.FilenameUtils;
import org.intellij.images.fileTypes.ImageFileTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AndroidBatchScaleImporter extends DialogWrapper implements BatchScaleImporterObserver {

    private final FileType imageFileType = ImageFileTypeManager.getInstance().getImageFileType();
    private final FileChooserDescriptor imageDescriptor = new FileChooserDescriptor(true,
                                                                                    true,
                                                                                    false,
                                                                                    false,
                                                                                    false,
                                                                                    true) {
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
            if (file.isDirectory()) {
                return super.isFileVisible(file, showHiddenFiles);
            }
            return file.getFileType().equals(imageFileType);
        }

        public boolean isFileSelectable(VirtualFile file) {
            if (file.isDirectory()) {
                return super.isFileSelectable(file);
            }
            return super.isFileSelectable(file) && file.getFileType().equals(imageFileType);
        }
    };
    private final Project project;
    private final Module module;
    private final IconApplication container;
    private JPanel uiContainer;
    private JTable table;
    private JLabel imageContainer;
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;
    private ImageTableModel tableModel;
    private final BatchScaleImporterController controller;
    private final Consumer<List<VirtualFile>> fileChooserConsumer = new Consumer<List<VirtualFile>>() {
        @Override
        public void consume(final List<VirtualFile> virtualFiles) {
            if (virtualFiles == null ||
                virtualFiles.size() == 0) {
                return;
            }
            final VirtualFile file = virtualFiles.get(0);
            if (file == null) {
                return;
            }
            if (virtualFiles.size() == 1 && !file.isDirectory()) {
                addSingleFile(file);
            } else {
                addMultipleFiles(virtualFiles);
            }
        }
    };

    public AndroidBatchScaleImporter(final Project project, final Module module) {
        super(project);
        this.project = project;
        this.container = ApplicationManager.getApplication().getComponent(IconApplication.class);
        this.controller = new BatchScaleImporterController();
        this.controller.addObserver(this);
        this.module = module;

        setTitle("Batch Drawable Importer");

        initButtons(project);
        initDragDrop();
        initTable();
        init();
        pack();
        setResizable(false);
    }

    private void initButtons(final Project project) {
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FileChooser.chooseFiles(imageDescriptor, project, getInitialFile(), fileChooserConsumer);
            }
        });
        deleteButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                controller.removeImages(table.getSelectedRows());
            }
        });
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                controller.editImages(project, module, table.getSelectedRows());
            }
        });
    }

    private void initDragDrop() {
        new FileDrop(table, new FileDrop.Target() {
            @Override
            public FileChooserDescriptor getDescriptor() {
                return imageDescriptor;
            }

            @Override
            public boolean isHiddenShown() {
                return false;
            }

            @Override
            public void dropFiles(final List<VirtualFile> virtualFiles) {
                fileChooserConsumer.consume(virtualFiles);
            }
        });
    }

    private void addMultipleFiles(List<VirtualFile> virtualFiles) {
        for (final VirtualFile file : virtualFiles) {
            if (file.isDirectory()) {
                VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor() {
                    @Override
                    public boolean visitFile(@NotNull VirtualFile child) {
                        if (file.equals(child)) {
                            return true;
                        }
                        addMultipleFiles(Arrays.asList(child));
                        return true;
                    }
                });
                continue;
            }
            addSingleFileImmediately(file);
        }
    }

    private void addSingleFileImmediately(VirtualFile file) {
        if (!file.getFileType().equals(imageFileType)) {
            return;
        }
        // Hack
        String path = file.getCanonicalPath();
        if (path == null) {
            return;
        }
        final File realFile = new File(path);
        final ISettingsController settingsController = container.getControllerFactory().getSettingsController();
        final IDefaultsController defaultsController = container.getControllerFactory().getDefaultsController();
        final VirtualFile root = settingsController.getResourceRoot();
        final IAddItemBatchScaleImporterController addItemController =
            new AddItemBatchScaleImporterController(defaultsController, root, realFile);
        controller.addImage(addItemController.getSourceResolution(), addItemController.getImageInformation(project));
        addItemController.tearDown();
    }

    private void addSingleFile(VirtualFile file) {
        container.getControllerFactory()
                 .getSettingsController()
                 .saveLastImageFolder(file.getCanonicalPath());
        AddItemBatchScaleDialog addItemBatchScaleDialog =
            new AddItemBatchScaleDialog(project, module, controller, file);
        addItemBatchScaleDialog.show();
    }

    private void initTable() {
        tableModel = new ImageTableModel(controller);
        table.setModel(tableModel);
        
        initRenderers();
        initRowSelection();
        initColumnSizes();
    }

    private void initRenderers() {
        final DefaultTableCellRenderer fileCellRenderer = new DefaultTableCellRenderer() {
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
        fileCellRenderer.setHorizontalTextPosition(DefaultTableCellRenderer.TRAILING);
        table.setDefaultRenderer(File.class, fileCellRenderer);
        table.setDefaultRenderer(ArrayList.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object o) {
                if (o == null) {
                    setText("");
                } else {
                    ArrayList list = (ArrayList) o;
                    Collections.sort(list);
                    StringBuilder buffer = new StringBuilder();
                    Iterator iterator = list.iterator();
                    while (iterator.hasNext()) {
                        Object val = iterator.next();
                        buffer.append(val.toString());
                        if (iterator.hasNext()) {
                            buffer.append(", ");
                        }
                    }
                    setText(buffer.toString());
                }
            }
        });
    }

    private void initRowSelection() {
        table.getColumnModel().setColumnSelectionAllowed(false);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int selectedRow = table.getSelectedRow();
                if (table.getSelectedRowCount() == 1) {
                    updateImage(controller.getImage(selectedRow));
                } else {
                    updateImage(null);
                }
            }
        });
    }

    private void initColumnSizes() {
        table.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                super.componentResized(componentEvent);
                Dimension tableSize = table.getSize();
                final int[] columnSizes = new int[]{ 20, 20, 20, 40 };
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
        if (item == null) {
            imageContainer.setIcon(null);
            return;
        }
        ImageUtils.updateImage(imageContainer, item.getImageFile());
    }

    protected VirtualFile getInitialFile() {
        String directoryName = container.getControllerFactory().getSettingsController().getLastImageFolder();
        VirtualFile path;
        String expandPath = expandPath(directoryName);
        if (expandPath == null) {
            return null;
        }
        for (path = LocalFileSystem.getInstance().findFileByPath(expandPath);
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
        return uiContainer;
    }

    private void createUIComponents() {
        table = new JBTable() {
            @NotNull
            @Override
            public Component prepareRenderer(@NotNull TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setEnabled(isCellEditable(row, column));
                return c;
            }
        };
        addButton = new JButton(getIcon("ic_action_add"));
        editButton = new JButton(getIcon("ic_action_edit"));
        deleteButton = new JButton(getIcon("ic_action_trash"));
    }

    @NotNull
    private Icon getIcon(String name) {
        boolean isDarkTheme = UIUtil.isUnderDarcula();
        final String asset = String.format("/icons/%s%s.png", name, isDarkTheme ? "_dark" : "");
        return IconLoader.getIcon(asset);
    }

    @Override
    protected void doOKAction() {
        RefactoringTask task = controller.getExportTask(project);
        ProgressManager.getInstance().run(task);
        super.doOKAction();
    }

    @Override
    public void updated() {
        updateTable();
    }

    private void updateTable() {
        if (table == null ||
            tableModel == null) {
            return;
        }
        int selectedRow = table.getSelectedRow();
        tableModel.fireTableDataChanged();
        if (table.getRowCount() > 0) {
            selectedRow = MathUtils.clamp(selectedRow, 0, table.getRowCount());
            table.setRowSelectionInterval(selectedRow, selectedRow);
        } else {
            imageContainer.setDisabledIcon(null);
        }
    }

    private static class ImageTableModel extends AbstractTableModel {
        private static final List<String> columnNames = Arrays.asList("Source-File",
                                                                      "Target-Resolutions",
                                                                      "Target-Name",
                                                                      "Target-Root");
        private IBatchScaleImporterController controller;

        public ImageTableModel(IBatchScaleImporterController controller) {
            this.controller = controller;
        }

        @Override
        public int getRowCount() {
            return controller.getImageCount();
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames.get(col);
        }

        @Override
        public Object getValueAt(int row, int col) {
            ImageInformation information = controller.getImage(row);
            switch (col) {
                case 0:
                    return information.getImageFile();
                case 1:
                    return controller.getTargetResolutions(information);
                case 2:
                    return information.getExportName();
                case 3:
                    return information.getExportPath();
            }
            return information;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return getValueAt(0, col).getClass();
        }
    }
}
