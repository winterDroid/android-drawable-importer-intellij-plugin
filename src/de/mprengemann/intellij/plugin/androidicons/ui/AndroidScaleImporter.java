package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.ex.FileDrop;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import de.mprengemann.intellij.plugin.androidicons.util.ResizeAlgorithm;
import org.apache.commons.lang.StringUtils;
import org.intellij.images.fileTypes.ImageFileTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AndroidScaleImporter extends DialogWrapper {
    private final Project project;
    private JPanel container;
    private JComboBox assetResolutionSpinner;
    private JComboBox targetResolutionSpinner;
    private JTextField targetHeight;
    private JTextField targetWidth;
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
    private VirtualFile selectedImage;
    private File imageFile;
    private float toLDPI;
    private float toMDPI;
    private float toHDPI;
    private float toXHDPI;
    private float toXXHDPI;
    private float toXXXHDPI;
    private boolean isNinePatch = false;
    private int originalImageWidth = -1;
    private int originalImageHeight = -1;

    public AndroidScaleImporter(final Project project, Module module) {
        super(project, true);
        this.project = project;

        setTitle("Android Scale Importer");
        setResizable(false);

        AndroidResourcesHelper.initResourceBrowser(project, module, "Select res root", resRoot);

        final FileChooserDescriptor imageDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(
            ImageFileTypeManager.getInstance().getImageFileType());
        String title1 = "Select your asset";
        imageDescriptor.setTitle(title1);
        assetBrowser.addBrowseFolderListener(title1, null, project, imageDescriptor);
        assetBrowser.addBrowseFolderListener(new TextBrowseFolderListener(imageDescriptor) {

            @Override
            protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                super.onFileChoosen(chosenFile);
                updateImageInformation(chosenFile);
            }
        });
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
                boolean setEnabled = selectedItem.equalsIgnoreCase("other");
                targetResolutionSpinner.setEnabled(setEnabled);
                targetWidth.setEnabled(setEnabled);
                targetHeight.setEnabled(setEnabled);
                aspectRatioLock.setEnabled(setEnabled);

                if (!setEnabled) {
                    aspectRatioLock.setSelected(true);
                    targetHeight.setText(originalImageHeight == -1 ? "" : Integer.toString(originalImageHeight));
                    targetWidth.setText(originalImageWidth == -1 ? "" : Integer.toString(originalImageWidth));
                    updateScaleFactors();
                    updateNewSizes();
                }
            }
        });

        assetResolutionSpinner.setSelectedIndex(3);
        targetResolutionSpinner.setSelectedIndex(3);

        targetResolutionSpinner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateScaleFactors();
                updateNewSizes();
            }
        });
        targetHeight.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                super.keyReleased(keyEvent);
                updateTargetWidth();
                updateNewSizes();
            }
        });
        targetWidth.addKeyListener(new KeyAdapter() {
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
            int targetHeight = Integer.parseInt(this.targetHeight.getText());
            int newTargetWidth = (int) ((float) (originalImageWidth * targetHeight) / (float) originalImageHeight);
            targetWidth.setText(Integer.toString(newTargetWidth));
        } catch (Exception ignored) {
        }
    }

    private void updateTargetHeight() {
        if (!aspectRatioLock.isSelected()) {
            return;
        }
        try {
            int targetWidth = Integer.parseInt(this.targetWidth.getText());
            int newTargetHeight = (int) ((float) (originalImageHeight * targetWidth) / (float) originalImageWidth);
            targetHeight.setText(Integer.toString(newTargetHeight));
        } catch (Exception ignored) {
        }
    }

    private void updateImageInformation(VirtualFile chosenFile) {
        selectedImage = chosenFile;
        isNinePatch = chosenFile.getName().endsWith(".9.png");
        updateImage();
        fillImageInformation();
    }

    private void fillImageInformation() {
        if (selectedImage == null) {
            return;
        }
        String canonicalPath = selectedImage.getCanonicalPath();
        if (canonicalPath == null) {
            return;
        }
        File file = new File(canonicalPath);
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return;
            }
            originalImageWidth = image.getWidth();
            originalImageHeight = image.getHeight();

            if (isNinePatch) {
                originalImageHeight -= 2;
                originalImageWidth -= 2;
            }

            targetHeight.setText(String.valueOf(originalImageHeight));
            targetWidth.setText(String.valueOf(originalImageWidth));

            resExportName.setText(selectedImage.getName());

            updateScaleFactors();
            updateNewSizes();
        } catch (IOException ignored) {
        }
    }

    private void updateNewSizes() {
        try {
            int targetWidth = Integer.parseInt(this.targetWidth.getText());
            int targetHeight = Integer.parseInt(this.targetHeight.getText());
            updateNewSizes(targetWidth, targetHeight);
        } catch (Exception ignored) {
        }
    }

    private void updateNewSizes(int targetWidth, int targetHeight) {
        LDPICheckBox.setText("LDPI (" + (int) (toLDPI * targetWidth) + "px x " + (int) (toLDPI * targetHeight) + " px)");
        MDPICheckBox.setText("MDPI (" + (int) (toMDPI * targetWidth) + "px x " + (int) (toMDPI * targetHeight) + " px)");
        HDPICheckBox.setText("HDPI (" + (int) (toHDPI * targetWidth) + "px x " + (int) (toHDPI * targetHeight) + " px)");
        XHDPICheckBox.setText("XHDPI (" + (int) (toXHDPI * targetWidth) + "px x " + (int) (toXHDPI * targetHeight) + " px)");
        XXHDPICheckBox.setText("XXHDPI (" + (int) (toXXHDPI * targetWidth) + "px x " + (int) (toXXHDPI * targetHeight) + " px)");
        XXXHDPICheckBox.setText("XXXHDPI (" + (int) (toXXXHDPI * targetWidth) + "px x " + (int) (toXXXHDPI * targetHeight) + " px)");
    }

    private void updateScaleFactors() {
        toLDPI = 0f;
        toMDPI = 0f;
        toHDPI = 0f;
        toXHDPI = 0f;
        toXXHDPI = 0f;
        toXXXHDPI = 0f;

        String targetResolution = (String) assetResolutionSpinner.getSelectedItem();
        if (targetResolution.equalsIgnoreCase("other")) {
            targetResolution = (String) targetResolutionSpinner.getSelectedItem();
        }

        if (targetResolution.equalsIgnoreCase("mdpi")) {
            toLDPI = 0.5f;
            toMDPI = 1f;
            toHDPI = 1.5f;
            toXHDPI = 2f;
            toXXHDPI = 3f;
            toXXXHDPI = 4f;
        } else if (targetResolution.equalsIgnoreCase("ldpi")) {
            toLDPI = 2f * 0.5f;
            toMDPI = 2f * 1f;
            toHDPI = 2f * 1.5f;
            toXHDPI = 2f * 2f;
            toXXHDPI = 2f * 3f;
            toXXXHDPI = 2f * 4f;
        } else if (targetResolution.equalsIgnoreCase("hdpi")) {
            toLDPI = 2f / 3f * 0.5f;
            toMDPI = 2f / 3f * 1f;
            toHDPI = 2f / 3f * 1.5f;
            toXHDPI = 2f / 3f * 2f;
            toXXHDPI = 2f / 3f * 3f;
            toXXXHDPI = 2f / 3f * 4f;
        } else if (targetResolution.equalsIgnoreCase("xhdpi")) {
            toLDPI = 1f / 2f * 0.5f;
            toMDPI = 1f / 2f * 1f;
            toHDPI = 1f / 2f * 1.5f;
            toXHDPI = 1f / 2f * 2f;
            toXXHDPI = 1f / 2f * 3f;
            toXXXHDPI = 1f / 2f * 4f;
        } else if (targetResolution.equalsIgnoreCase("xxhdpi")) {
            toLDPI = 1f / 3f * 0.5f;
            toMDPI = 1f / 3f * 1f;
            toHDPI = 1f / 3f * 1.5f;
            toXHDPI = 1f / 3f * 2f;
            toXXHDPI = 1f / 3f * 3f;
            toXXXHDPI = 1f / 3f * 4f;
        } else if (targetResolution.equalsIgnoreCase("xxxhdpi")) {
            toLDPI = 1f / 4f * 0.5f;
            toMDPI = 1f / 4f * 1f;
            toHDPI = 1f / 4f * 1.5f;
            toXHDPI = 1f / 4f * 2f;
            toXXHDPI = 1f / 4f * 3f;
            toXXXHDPI = 1f / 4f * 4f;
        }
    }

    private void updateImage() {
        if (imageContainer == null ||
            selectedImage == null ||
            selectedImage.getCanonicalPath() == null) {
            return;
        }
        imageFile = new File(selectedImage.getCanonicalPath());
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

        if (StringUtils.isEmpty(targetHeight.getText().trim()) || StringUtils.isEmpty(targetWidth.getText().trim())) {
            if (!targetHeight.getText().matches("[0-9.]*") || !targetWidth.getText().matches("[0-9.]*")) {
                return new ValidationInfo("Target height and/or width is not a valid number.", targetWidth);
            }
            return new ValidationInfo("Target height and/or width is not valid.", targetWidth);
        }

        return super.doValidate();
    }

    @Override
    protected void doOKAction() {
        if (imageFile == null) {
            super.doOKAction();
            return;
        }

        try {
            int targetWidth = Integer.parseInt(this.targetWidth.getText());
            int targetHeight = Integer.parseInt(this.targetHeight.getText());

            java.util.List<File> sources = new ArrayList<File>();
            java.util.List<File> targets = new ArrayList<File>();

            if (LDPICheckBox.isSelected()) {
                sources.add(exportTempImage(imageFile, "ldpi", toLDPI, targetWidth, targetHeight));
                targets.add(getTargetFile("ldpi"));
            }
            if (MDPICheckBox.isSelected()) {
                sources.add(exportTempImage(imageFile, "mdpi", toMDPI, targetWidth, targetHeight));
                targets.add(getTargetFile("mdpi"));
            }
            if (HDPICheckBox.isSelected()) {
                sources.add(exportTempImage(imageFile, "hdpi", toHDPI, targetWidth, targetHeight));
                targets.add(getTargetFile("hdpi"));
            }
            if (XHDPICheckBox.isSelected()) {
                sources.add(exportTempImage(imageFile, "xhdpi", toXHDPI, targetWidth, targetHeight));
                targets.add(getTargetFile("xhdpi"));
            }
            if (XXHDPICheckBox.isSelected()) {
                sources.add(exportTempImage(imageFile, "xxhdpi", toXXHDPI, targetWidth, targetHeight));
                targets.add(getTargetFile("xxhdpi"));
            }
            if (XXXHDPICheckBox.isSelected()) {
                sources.add(exportTempImage(imageFile, "xxxhdpi", toXXXHDPI, targetWidth, targetHeight));
                targets.add(getTargetFile("xxxhdpi"));
            }

            RefactorHelper.move(project, sources, targets);
        } catch (Exception ignored) {
        }

        super.doOKAction();
    }

    private File getTargetFile(String resolution) {
        return new File(resRoot.getText().trim() + "/drawable-" + resolution + "/" + resExportName.getText().trim());
    }

    private File exportTempImage(File imageFile,
                                 String resolution,
                                 float scaleFactor,
                                 int targetWidth,
                                 int targetHeight) throws IOException {
        BufferedImage resizeImageJpg;
        String resName = resExportName.getText().trim();
        ResizeAlgorithm algorithm = ResizeAlgorithm.from((String) algorithmSpinner.getSelectedItem());
        Object method = algorithm.getMethod((String) methodSpinner.getSelectedItem());
        if (isNinePatch) {
            resizeImageJpg = ImageUtils.resizeNinePatchImage(algorithm,
                                                             method,
                                                             scaleFactor,
                                                             targetWidth,
                                                             targetHeight,
                                                             imageFile,
                                                             resolution,
                                                             project,
                                                             resName);
        } else {
            resizeImageJpg = ImageUtils.resizeNormalImage(algorithm,
                                                          method,
                                                          imageFile,
                                                          scaleFactor,
                                                          targetWidth,
                                                          targetHeight);
        }

        return ImageUtils.saveImageTempFile(resolution, resizeImageJpg, project, ImageUtils.getExportName("", resName));
    }
}
