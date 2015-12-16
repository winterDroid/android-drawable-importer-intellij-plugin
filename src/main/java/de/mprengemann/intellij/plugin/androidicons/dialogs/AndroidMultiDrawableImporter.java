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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.io.ZipUtil;
import com.intellij.util.ui.UIUtil;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import de.mprengemann.intellij.plugin.androidicons.controllers.multi.IMultiImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.multi.MultiImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.multi.MultiImporterObserver;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.listeners.SimpleMouseListener;
import de.mprengemann.intellij.plugin.androidicons.model.Format;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.widgets.ExportNameField;
import de.mprengemann.intellij.plugin.androidicons.widgets.FileBrowserField;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AndroidMultiDrawableImporter extends DialogWrapper implements MultiImporterObserver {

    private final FileChooserDescriptor archiveDescriptor = new FileChooserDescriptor(true,
                                                                                      false,
                                                                                      false,
                                                                                      false,
                                                                                      false,
                                                                                      false) {
        @Override
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
            return file.isDirectory() || isZip(file.getName());
        }

        @Override
        public boolean isFileSelectable(VirtualFile file) {
            return isZip(file.getName());
        }

        private boolean isZip(String name) {
            return name.contains(".zip");
        }
    };
    private static final Resolution[] RESOLUTIONS = new Resolution[]{
        Resolution.XXXHDPI,
        Resolution.XXHDPI,
        Resolution.XHDPI,
        Resolution.HDPI,
        Resolution.MDPI,
        Resolution.LDPI,
        Resolution.TVDPI
    };
    private static final String TAG = AndroidMultiDrawableImporter.class.getSimpleName();
    private static final Logger LOGGER = Logger.getInstance(TAG);
    private final Project project;
    private final Module module;
    private final IMultiImporterController controller;
    private Action zipImportAction;

    private FileBrowserField resRoot;
    private FileBrowserField ldpiFile;
    private FileBrowserField mdpiFile;
    private FileBrowserField hdpiFile;
    private FileBrowserField xhdpiFile;
    private FileBrowserField xxhdpiFile;
    private FileBrowserField xxxhdpiFile;
    private FileBrowserField tvdpiFile;
    private JLabel imageContainer;
    private ExportNameField resExportName;
    private JPanel uiContainer;
    private JComboBox formatSpinner;
    private final ActionListener formatListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final JComboBox source = (JComboBox) e.getSource();
            final Format selectedItem = (Format) source.getSelectedItem();
            controller.setFormat(selectedItem);
        }
    };
    private final ISettingsController settingsController;
    private final IconApplication container;

    public AndroidMultiDrawableImporter(final Project project, final Module module) {
        super(project, true);
        this.project = project;
        this.module = module;

        container = ApplicationManager.getApplication().getComponent(IconApplication.class);
        settingsController = container.getControllerFactory().getSettingsController();
        this.controller = new MultiImporterController(container.getControllerFactory().getDefaultsController());
        this.controller.addObserver(this);

        initResourceRoot();
        initBrowser(Resolution.LDPI, ldpiFile);
        initBrowser(Resolution.MDPI, mdpiFile);
        initBrowser(Resolution.HDPI, hdpiFile);
        initBrowser(Resolution.XHDPI, xhdpiFile);
        initBrowser(Resolution.XXHDPI, xxhdpiFile);
        initBrowser(Resolution.XXXHDPI, xxxhdpiFile);
        initBrowser(Resolution.TVDPI, tvdpiFile);

        setTitle("Android Multi Drawable Importer");
        init();
        pack();
        setResizable(false);
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
        controller.getTask(project).queue();
        if (!controller.containsNinePatch()) {
            container.getControllerFactory().getDefaultsController().setFormat(controller.getFormat());
        }
        super.doOKAction();
    }

    @Override
    public void updated() {
        updateImage();
        updateTargetRoot();
        updateName();
        updateFormat();
    }

    private void updateFormat() {
        formatSpinner.removeActionListener(formatListener);
        formatSpinner.removeAllItems();
        for (Format format : Format.values()) {
            formatSpinner.addItem(format);
        }
        formatSpinner.setSelectedItem(controller.getFormat());
        formatSpinner.setEnabled(!controller.containsNinePatch());
        formatSpinner.addActionListener(formatListener);
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
        ImageUtils.updateImage(imageContainer, file, controller.getFormat());
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
        tvdpiFile = new FileBrowserField(FileBrowserField.IMAGE_FILE_CHOOSER);
    }

    @Override
    protected void createDefaultActions() {
        super.createDefaultActions();
        this.zipImportAction = new AndroidMultiDrawableImporter.ZipImportAction();
    }

    protected final Action getZipImportAction() {
        return this.zipImportAction;
    }

    private void openZipArchive() {
        FileChooser.chooseFile(archiveDescriptor, project, getInitialFile(), new Consumer<VirtualFile>() {
            @Override
            public void consume(VirtualFile virtualFile) {
                if (virtualFile == null) {
                    return;
                }
                final String filePath = virtualFile.getCanonicalPath();
                if (filePath == null) {
                    return;
                }
                settingsController.saveLastImageFolder(filePath);
                controller.resetZipInformation();
                importZipArchive(virtualFile);
            }
        });
    }

    private void importZipArchive(VirtualFile virtualFile) {
        final String filePath = virtualFile.getCanonicalPath();
        if (filePath == null) {
            return;
        }
        final File tempDir = new File(ImageInformation.getTempDir(), virtualFile.getNameWithoutExtension());
        final String archiveName = virtualFile.getName();
        new Task.Modal(project, "Import Archive", true) {
            @Override
            public void run(@NotNull final ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);
                try {
                    FileUtils.forceMkdir(tempDir);
                    ZipUtil.extract(new File(filePath), tempDir, new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            final String mimeType = new MimetypesFileTypeMap().getContentType(name);
                            final String type = mimeType.split("/")[0];
                            return type.equals("image");
                        }
                    }, true);
                    progressIndicator.checkCanceled();

                    final Path root = Paths.get(tempDir.toURI());
                    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            progressIndicator.checkCanceled();
                            if (!attrs.isRegularFile() || Files.isHidden(file)) {
                                return FileVisitResult.CONTINUE;
                            }
                            final String fileRoot = file.getParent().toString().toUpperCase();
                            final String name = FilenameUtils.getBaseName(file.toString());
                            if (name.startsWith(".") ||
                                fileRoot.contains("__MACOSX")) {
                                return FileVisitResult.CONTINUE;
                            }
                            for (Resolution resolution : RESOLUTIONS) {
                                if (name.toUpperCase().contains("-" + resolution) ||
                                    name.toUpperCase().contains("_" + resolution) ||
                                    fileRoot.contains(resolution.toString())) {
                                    controller.addZipImage(new File(file.toUri()), resolution);
                                    return FileVisitResult.CONTINUE;
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    progressIndicator.checkCanceled();

                    final Map<Resolution, List<ImageInformation>> zipImages = controller.getZipImages();
                    final List<Resolution> foundResolutions = new ArrayList<Resolution>();
                    int foundAssets = 0;
                    for (Resolution resolution : zipImages.keySet()) {
                        final List<ImageInformation> assetInformation = zipImages.get(resolution);
                        if (assetInformation != null && assetInformation.size() > 0) {
                            foundAssets += assetInformation.size();
                            foundResolutions.add(resolution);
                        }
                    }
                    progressIndicator.checkCanceled();

                    final String title = String.format("Import '%s'", archiveName);
                    if (foundResolutions.size() == 0 || foundAssets == 0) {
                        Messages.showErrorDialog("No assets found.", title);
                        FileUtils.deleteQuietly(tempDir);
                        return;
                    }
                    final int finalFoundAssets = foundAssets;
                    UIUtil.invokeLaterIfNeeded(new DumbAwareRunnable() {
                        public void run() {
                            final String[] options = new String[] {"Import", "Cancel"};
                            final String description = String.format("Import %d assets for %s to %s.",
                                                                     finalFoundAssets,
                                                                     StringUtils.join(foundResolutions, ", "),
                                                                     controller.getTargetRoot());
                            final int selection = Messages.showDialog(description,
                                                                      title,
                                                                      options,
                                                                      0,
                                                                      Messages.getQuestionIcon());
                            if (selection == 0) {
                                controller.getZipTask(project, tempDir).queue();
                                close(0);
                            } else {
                                FileUtils.deleteQuietly(tempDir);
                            }
                        }
                    });
                } catch (ProcessCanceledException e) {
                    FileUtils.deleteQuietly(tempDir);
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }.queue();
    }

    private VirtualFile getInitialFile() {
        String directoryName = settingsController.getLastImageFolder();
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

    @Override
    @NotNull
    protected Action[] createActions() {
        final ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(this.getZipImportAction());
        actions.add(this.getCancelAction());
        actions.add(this.getOKAction());
        if (SystemInfo.isMac) {
            Collections.reverse(actions);
        }
        return actions.toArray(new Action[actions.size()]);
    }

    private class ZipImportAction extends AbstractAction {
        public ZipImportAction() {
            this.putValue("Name", "Import Zip Archive");
        }

        public void actionPerformed(ActionEvent e) {
            AndroidMultiDrawableImporter.this.openZipArchive();
        }
    }
}
