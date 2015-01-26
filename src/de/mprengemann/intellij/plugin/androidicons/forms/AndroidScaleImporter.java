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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.ex.FileDrop;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.images.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.images.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.images.RefactoringTask;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.images.Resolution;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AndroidScaleImporter extends DialogWrapper {
    public static final String CHECKBOX_TEXT = "%s (%.0f px x %.0f px)";
    
    private final static String[] FILE_RESOLUTIONS = new String[] {
        Resolution.LDPI.getName(),
        Resolution.MDPI.getName(),
        Resolution.HDPI.getName(),
        Resolution.XHDPI.getName(),
        Resolution.XXHDPI.getName(),
        Resolution.XXXHDPI.getName(),
        "Other"
    };
    private final static String[] FOLDER_RESOLUTIONS = new String[] {
        Resolution.LDPI.getName(),
        Resolution.MDPI.getName(),
        Resolution.HDPI.getName(),
        Resolution.XHDPI.getName(),
        Resolution.XXHDPI.getName(),
        Resolution.XXXHDPI.getName()
    };
    
    private final Project project;
    private JPanel container;
    private JComboBox assetResolutionSpinner;
    private JComboBox targetResolutionSpinner;
    private JTextField imageHeight;
    private JTextField imageWidth;
    private TextFieldWithBrowseButton resRoot;
    private TextFieldWithBrowseButton assetBrowser;
    private JTextField resExportName;
    private JCheckBox LDPICheckBox;
    private JCheckBox MDPICheckBox;
    private JCheckBox HDPICheckBox;
    private JCheckBox XHDPICheckBox;
    private JCheckBox XXHDPICheckBox;
    private JLabel imageContainer;
    private JCheckBox XXXHDPICheckBox;
    private JCheckBox aspectRatioLock;
    private JComboBox methodSpinner;
    private JComboBox algorithmSpinner;
    private VirtualFile selectedAsset;
    private float toLDPI;
    private float toMDPI;
    private float toHDPI;
    private float toXHDPI;
    private float toXXHDPI;
    private float toXXXHDPI;
    private int originalImageWidth = -1;
    private int originalImageHeight = -1;

    public AndroidScaleImporter(final Project project, Module module) {
        super(project, true);
        this.project = project;

        setTitle("Android Scale Importer");
        setResizable(false);

        AndroidResourcesHelper.initResourceBrowser(project, module, "Select res root", resRoot);

        final FileChooserDescriptor imageDescriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor(
            ImageFileTypeManager.getInstance().getImageFileType());
        String title1 = "Select your asset(s) or a folder";
        ImageFileBrowserFolderActionListener actionListener = new ImageFileBrowserFolderActionListener(title1, project, assetBrowser, imageDescriptor) {
            @Override
            @SuppressWarnings("deprecation") // Otherwise not compatible to AndroidStudio
            protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                super.onFileChoosen(chosenFile);
                updateImageInformation(chosenFile);
            }
        };
        assetBrowser.addBrowseFolderListener(project, actionListener);
        new FileDrop(assetBrowser.getTextField(), new FileDrop.Target() {
            @Override
            public FileChooserDescriptor getDescriptor() {
                return imageDescriptor;
            }

            @Override
            public boolean isHiddenShown() {
                return false;
            }

            @Override
            public void dropFiles(java.util.List<VirtualFile> virtualFiles) {
                if (virtualFiles != null) {
                    if (virtualFiles.size() == 1) {
                        VirtualFile chosenFile = virtualFiles.get(0);
                        assetBrowser.setText(chosenFile.getCanonicalPath());
                        updateImageInformation(chosenFile);
                    }
                }
            }
        });


        assetResolutionSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String selectedItem = (String) assetResolutionSpinner.getSelectedItem();
                boolean setEnabled = "other".equalsIgnoreCase(selectedItem);
                targetResolutionSpinner.setEnabled(setEnabled);
                imageWidth.setEnabled(setEnabled);
                imageHeight.setEnabled(setEnabled);
                aspectRatioLock.setEnabled(setEnabled);

                imageHeight.setText(originalImageHeight == -1 ? "" : Integer.toString(originalImageHeight));
                imageWidth.setText(originalImageWidth == -1 ? "" : Integer.toString(originalImageWidth));
                if (!setEnabled && selectedItem != null) {
                    aspectRatioLock.setSelected(true);
                    updateScaleFactors();
                    updateNewSizes();
                }
            }
        });

        for (String resolutions : FILE_RESOLUTIONS) {
            assetResolutionSpinner.addItem(resolutions);
        }
        assetResolutionSpinner.setSelectedIndex(3);
        targetResolutionSpinner.setSelectedIndex(3);

        targetResolutionSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateScaleFactors();
                updateNewSizes();
            }
        });
        imageHeight.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                super.keyReleased(keyEvent);
                updateTargetWidth();
                updateNewSizes();
            }
        });
        imageWidth.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                super.keyReleased(keyEvent);
                updateTargetHeight();
                updateNewSizes();
            }
        });

        aspectRatioLock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTargetHeight();
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

        init();
    }

    private void updateTargetWidth() {
        if (!aspectRatioLock.isSelected()) {
            return;
        }
        try {
            int targetHeight = Integer.parseInt(this.imageHeight.getText());
            int newTargetWidth = (int) ((float) (originalImageWidth * targetHeight) / (float) originalImageHeight);
            imageWidth.setText(Integer.toString(newTargetWidth));
        } catch (Exception ignored) {
        }
    }

    private void updateTargetHeight() {
        if (!aspectRatioLock.isSelected()) {
            return;
        }
        try {
            int targetWidth = Integer.parseInt(this.imageWidth.getText());
            int newTargetHeight = (int) ((float) (originalImageHeight * targetWidth) / (float) originalImageWidth);
            imageHeight.setText(Integer.toString(newTargetHeight));
        } catch (Exception ignored) {
        }
    }

    private void updateImageInformation(VirtualFile chosenFile) {
        selectedAsset = chosenFile;
        resExportName.setText("");
        resExportName.setEnabled(!selectedAsset.isDirectory());
        updateImage();
        fillImageInformation();
    }

    private void fillImageInformation() {
        if (selectedAsset == null) {
            return;
        }
        String canonicalPath = selectedAsset.getCanonicalPath();
        if (canonicalPath == null) {
            return;
        }
        File file = new File(canonicalPath);
        try {
            if (selectedAsset.isDirectory()) {
                originalImageHeight = -1;
                originalImageWidth = -1;

                imageHeight.setText("");
                imageWidth.setText("");
            } else {
                BufferedImage image = ImageIO.read(file);
                if (image == null) {
                    return;
                }
                originalImageWidth = image.getWidth();
                originalImageHeight = image.getHeight();

                if (selectedAsset.getName().endsWith(".9.png")) {
                    originalImageHeight -= 2;
                    originalImageWidth -= 2;
                }

                imageHeight.setText(String.valueOf(originalImageHeight));
                imageWidth.setText(String.valueOf(originalImageWidth));
            }

            resExportName.setText(ExportNameUtils.getExportNameFromFilename(selectedAsset.getName()));

            updateImageAssets(selectedAsset.isDirectory());
            updateScaleFactors();
            updateNewSizes();
        } catch (IOException ignored) {
        }
    }

    private void updateImageAssets(boolean directory) {
        assetResolutionSpinner.removeAllItems();
        String res = (String) assetResolutionSpinner.getSelectedItem();
        List<String> resolutions = Arrays.asList(directory ? FOLDER_RESOLUTIONS
                                                           : FILE_RESOLUTIONS);
        for (String resolution : resolutions) {
            assetResolutionSpinner.addItem(resolution);
        }
        int selectedIndex = 3;
        if (resolutions.contains(res)) {
            selectedIndex = resolutions.indexOf(res);
        }
        assetResolutionSpinner.setSelectedIndex(selectedIndex);
    }

    private void updateNewSizes() {
        try {
            int targetWidth = -1;
            int targetHeight = -1;
            if (selectedAsset != null &&
                !selectedAsset.isDirectory()) {
                targetWidth = Integer.parseInt(this.imageWidth.getText());
                targetHeight = Integer.parseInt(this.imageHeight.getText());
            }
            updateNewSizes(targetWidth, targetHeight);
        } catch (Exception ignored) {
        }
    }

    private void updateNewSizes(int targetWidth, int targetHeight) {
        if (targetHeight == -1 || targetWidth == -1) {
            LDPICheckBox.setText(Resolution.LDPI.getName());
            MDPICheckBox.setText(Resolution.MDPI.getName());
            HDPICheckBox.setText(Resolution.HDPI.getName());
            XHDPICheckBox.setText(Resolution.XHDPI.getName());
            XXHDPICheckBox.setText(Resolution.XXHDPI.getName());
            XXXHDPICheckBox.setText(Resolution.XXXHDPI.getName());
        } else {
            LDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                               Resolution.LDPI.getName(),
                                               toLDPI * targetWidth,
                                               toLDPI * targetHeight));
            MDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                               Resolution.MDPI.getName(),
                                               toMDPI * targetWidth,
                                               toMDPI * targetHeight));
            HDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                               Resolution.HDPI.getName(),
                                               toHDPI * targetWidth,
                                               toHDPI * targetHeight));
            XHDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                                Resolution.XHDPI.getName(),
                                                toXHDPI * targetWidth,
                                                toXHDPI * targetHeight));
            XXHDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                                 Resolution.XXHDPI.getName(),
                                                 toXXHDPI * targetWidth,
                                                 toXXHDPI * targetHeight));
            XXXHDPICheckBox.setText(String.format(CHECKBOX_TEXT,
                                                  Resolution.XXXHDPI.getName(),
                                                  toXXXHDPI * targetWidth,
                                                  toXXXHDPI * targetHeight));
        }
    }

    private void updateScaleFactors() {
        Resolution targetResolution = Resolution.from((String) assetResolutionSpinner.getSelectedItem());
        if (targetResolution == null) {
            targetResolution = Resolution.from((String) targetResolutionSpinner.getSelectedItem());
        }
        
        toLDPI = RefactorHelper.getScaleFactor(Resolution.LDPI, targetResolution);
        toMDPI = RefactorHelper.getScaleFactor(Resolution.MDPI, targetResolution);
        toHDPI = RefactorHelper.getScaleFactor(Resolution.HDPI, targetResolution);
        toXHDPI = RefactorHelper.getScaleFactor(Resolution.XHDPI, targetResolution);
        toXXHDPI = RefactorHelper.getScaleFactor(Resolution.XXHDPI, targetResolution);
        toXXXHDPI = RefactorHelper.getScaleFactor(Resolution.XXXHDPI, targetResolution);
    }

    private void updateImage() {
        if (imageContainer == null ||
            selectedAsset == null ||
            selectedAsset.getCanonicalPath() == null ||
            selectedAsset.isDirectory()) {
            return;
        }
        File imageFile = new File(selectedAsset.getCanonicalPath());
        ImageUtils.updateImage(imageContainer, imageFile);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return container;
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

        if (StringUtils.isEmpty(assetBrowser.getText().trim())) {
            return new ValidationInfo("Please select an image.", assetBrowser);
        }

        if (selectedAsset == null || !selectedAsset.isDirectory()) {
            if (StringUtils.isEmpty(imageHeight.getText().trim()) || StringUtils.isEmpty(imageWidth.getText().trim())) {
                if (!imageHeight.getText().matches("[0-9.]*") || !imageWidth.getText().matches("[0-9.]*")) {
                    return new ValidationInfo("Target height and/or width is not a valid number.", imageWidth);
                }
                return new ValidationInfo("Target height and/or width is not valid.", imageWidth);
            }
        }

        return super.doValidate();
    }

    @Override
    protected void doOKAction() {
        if (selectedAsset == null || selectedAsset.getCanonicalPath() == null) {
            super.doOKAction();
            return;
        }

        try {
            ResizeAlgorithm algorithm = ResizeAlgorithm.from((String) algorithmSpinner.getSelectedItem());
            Object algorithmMethod = algorithm.getMethod((String) methodSpinner.getSelectedItem());
            RefactoringTask task = new RefactoringTask(project);
            String exportPath = resRoot.getText().trim();
            String exportName = resExportName.getText().trim();
            final File file = new File(selectedAsset.getCanonicalPath());
            if (!selectedAsset.isDirectory()) {
                final int targetWidth = Integer.parseInt(this.imageWidth.getText());
                final int targetHeight = Integer.parseInt(this.imageHeight.getText());
                importSingleImage(algorithm,
                                  algorithmMethod,
                                  task,
                                  exportPath,
                                  exportName,
                                  file,
                                  targetWidth,
                                  targetHeight);
            } else {
                importImagesFromFolder(file,
                                       algorithm,
                                       algorithmMethod,
                                       task,
                                       exportPath);
            }
            DumbService.getInstance(project).queueTask(task);
        } catch (Exception e) {
            Logger.getInstance(AndroidScaleImporter.class).error("doOK", e);
        }

        super.doOKAction();
    }

    private void importSingleImage(ResizeAlgorithm algorithm,
                                   Object algorithmMethod,
                                   RefactoringTask task,
                                   String exportPath,
                                   String exportName,
                                   File imageFile,
                                   int targetWidth,
                                   int targetHeight) {
        ImageInformation baseInformation = ImageInformation.newBuilder()
                                                           .setImageFile(imageFile)
                                                           .setAlgorithm(algorithm)
                                                           .setMethod(algorithmMethod)
                                                           .setNinePatch(selectedAsset.getName().endsWith(".9.png"))
                                                           .setExportName(exportName)
                                                           .setExportPath(exportPath)
                                                           .setTargetWidth(targetWidth)
                                                           .setTargetHeight(targetHeight)
                                                           .build(project);

        task.addImage(getImageInformation(baseInformation, Resolution.LDPI, toLDPI, LDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.MDPI, toMDPI, MDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.HDPI, toHDPI, HDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.XHDPI, toXHDPI, XHDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.XXHDPI, toXXHDPI, XXHDPICheckBox));
        task.addImage(getImageInformation(baseInformation, Resolution.XXXHDPI, toXXXHDPI, XXXHDPICheckBox));
    }

    private void importImagesFromFolder(File file,
                                        ResizeAlgorithm algorithm,
                                        Object algorithmMethod,
                                        RefactoringTask task, 
                                        String exportPath) {
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
            if (foundFile.isDirectory()) {
                importImagesFromFolder(foundFile, algorithm, algorithmMethod, task, exportPath);
            } else {
                try {
                    BufferedImage image = ImageIO.read(foundFile);
                    importSingleImage(algorithm,
                                      algorithmMethod,
                                      task,
                                      exportPath,
                                      FilenameUtils.removeExtension(foundFile.getName()),
                                      foundFile,
                                      image.getWidth(),
                                      image.getHeight());
                } catch (IOException ignored) {
                }
            }
        }
    }

    private ImageInformation getImageInformation(ImageInformation baseInformation,
                                                 Resolution resolution,
                                                 float factor,
                                                 JCheckBox checkbox) {
        if (!checkbox.isSelected()) {
            return null;
        }

        return ImageInformation.newBuilder(baseInformation)
                               .setResolution(resolution)
                               .setFactor(factor)
                               .build(project);
    }
}
