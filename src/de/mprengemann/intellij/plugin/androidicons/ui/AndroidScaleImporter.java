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
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.imgscalr.Scalr;
import org.intellij.images.fileTypes.ImageFileTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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

        aspectRatioLock.setIcon(new ImageIcon(getClass().getResource("/icons/unlocked.png")));
        aspectRatioLock.setSelectedIcon(new ImageIcon(getClass().getResource("/icons/locked.png")));
        aspectRatioLock.setDisabledIcon(new ImageIcon(getClass().getResource("/icons/unlocked_disabled.png")));
        aspectRatioLock.setDisabledSelectedIcon(new ImageIcon(getClass().getResource("/icons/locked_disabled.png")));
        aspectRatioLock.setSelected(true);
        aspectRatioLock.setEnabled(false);
        aspectRatioLock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTargetHeight();
            }
        });

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
        if (isNinePatch) {
            resizeImageJpg = resizeNinePatchImage(scaleFactor, targetWidth, targetHeight, imageFile, resolution);
        } else {
            resizeImageJpg = resizeNormalImage(scaleFactor, targetWidth, targetHeight, imageFile);
        }

        return saveImageTempFile(resolution, resizeImageJpg, "");
    }

    private File saveImageTempFile(String resolution, BufferedImage resizeImageJpg, String prefix) throws IOException {
        String exportName;
        if (!StringUtils.isEmpty(prefix.trim())) {
            exportName = prefix + "_" + resExportName.getText().trim();
        } else {
            exportName = resExportName.getText().trim();
        }
        File exportFile = RefactorHelper.getTempImageFile(project, resolution, exportName);
        if (exportFile != null) {
            if (!exportFile.getParentFile().exists()) {
                FileUtils.forceMkdir(exportFile.getParentFile());
            }
            ImageIO.write(resizeImageJpg, "png", exportFile);
            return exportFile;
        } else {
            throw new IOException("Couldn't find .idea path.");
        }
    }

    private BufferedImage resizeNinePatchImage(float scaleFactor,
                                               int targetWidth,
                                               int targetHeight,
                                               File imageFile,
                                               String resolution) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        int type = BufferedImage.TYPE_INT_ARGB;

        int newWidth = (int) (scaleFactor * (targetWidth + 2));
        int newHeight = (int) (scaleFactor * (targetHeight + 2));

        BufferedImage trimedImage = trim9PBorder(image, type);
        saveImageTempFile(resolution, trimedImage, "trimmed");
        trimedImage = Scalr.resize(trimedImage, newWidth, newHeight);
        saveImageTempFile(resolution, trimedImage, "trimmedResized");

        BufferedImage borderImage;

        int w = trimedImage.getWidth();
        int h = trimedImage.getHeight();

        try {
            borderImage = generateBordersImage(image, w, h, type);
        } catch (Exception e) {
            return null;
        }

        int[] rgbArray = new int[w * h];
        trimedImage.getRGB(0, 0, w, h, rgbArray, 0, w);
        borderImage.setRGB(1, 1, w, h, rgbArray, 0, w);

        return borderImage;
    }

    private BufferedImage trim9PBorder(BufferedImage inputImage, int type) {
        BufferedImage trimedImage = UIUtil.createImage(inputImage.getWidth() - 2, inputImage.getHeight() - 2, type);
        Graphics2D g = trimedImage.createGraphics();
        g.drawImage(inputImage, 0, 0, trimedImage.getWidth(),
                    trimedImage.getHeight(), 1, 1, inputImage.getWidth() - 1,
                    inputImage.getHeight() - 1, null);
        g.dispose();
        return trimedImage;
    }

    private BufferedImage generateBordersImage(BufferedImage source,
                                               int trimedWidth,
                                               int trimedHeight,
                                               int type) throws Wrong9PatchException {
        BufferedImage finalBorder = UIUtil.createImage(trimedWidth + 2, trimedHeight + 2, type);
        int cutW = source.getWidth() - 2;
        int cutH = source.getHeight() - 2;

        // left border
        BufferedImage leftBorder = UIUtil.createImage(1, cutH, type);
        leftBorder.setRGB(0, 0, 1, cutH, source.getRGB(0, 1, 1, cutH, null, 0, 1), 0, 1);
        verifyBorderImage(leftBorder);
        leftBorder = resizeBorder(leftBorder, 1, trimedHeight, type);
        finalBorder.setRGB(0, 1, 1, trimedHeight, leftBorder.getRGB(0, 0, 1, trimedHeight, null, 0, 1), 0, 1);

        // right border
        BufferedImage rightBorder = UIUtil.createImage(1, cutH, type);
        rightBorder.setRGB(0, 0, 1, cutH, source.getRGB(cutW + 1, 1, 1, cutH, null, 0, 1), 0, 1);
        verifyBorderImage(rightBorder);
        rightBorder = resizeBorder(rightBorder, 1, trimedHeight, type);
        finalBorder.setRGB(trimedWidth + 1,
                           1,
                           1,
                           trimedHeight,
                           rightBorder.getRGB(0, 0, 1, trimedHeight, null, 0, 1),
                           0,
                           1);

        // top border
        BufferedImage topBorder = UIUtil.createImage(cutW, 1, type);
        topBorder.setRGB(0, 0, cutW, 1, source.getRGB(1, 0, cutW, 1, null, 0, cutW), 0, cutW);
        verifyBorderImage(topBorder);
        topBorder = resizeBorder(topBorder, trimedWidth, 1, type);
        finalBorder.setRGB(1,
                           0,
                           trimedWidth,
                           1,
                           topBorder.getRGB(0, 0, trimedWidth, 1, null, 0, trimedWidth),
                           0,
                           trimedWidth);

        // bottom border
        BufferedImage bottomBorder = UIUtil.createImage(cutW, 1, type);
        bottomBorder.setRGB(0, 0, cutW, 1, source.getRGB(1, cutH + 1, cutW, 1, null, 0, cutW), 0, cutW);
        verifyBorderImage(bottomBorder);
        bottomBorder = resizeBorder(bottomBorder, trimedWidth, 1, type);
        finalBorder.setRGB(1,
                           trimedHeight + 1,
                           trimedWidth,
                           1,
                           bottomBorder.getRGB(0, 0, trimedWidth, 1, null, 0, trimedWidth),
                           0,
                           trimedWidth);

        return finalBorder;
    }

    private BufferedImage resizeBorder(final BufferedImage border, int targetWidth, int targetHeight, int type) {
        if (targetWidth > border.getWidth() || targetHeight > border.getHeight()) {
            BufferedImage endImage = Scalr.resize(border, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
            this.enforceBorderColors(endImage);
            return endImage;
        }

        int w = border.getWidth();
        int h = border.getHeight();
        int[] data = border.getRGB(0, 0, w, h, null, 0, w);
        int[] newData = new int[targetWidth * targetHeight];

        float widthRatio = (float) Math.max(targetWidth - 1, 1) / (float) Math.max(w - 1, 1);
        float heightRatio = (float) Math.max(targetHeight - 1, 1) / (float) Math.max(h - 1, 1);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((0xff000000 & data[y * w + x]) != 0) {
                    int newX = Math.min(Math.round(x * widthRatio), targetWidth - 1);
                    int newY = Math.min(Math.round(y * heightRatio), targetHeight - 1);

                    newData[newY * targetWidth + newX] = data[y * w + x];
                }
            }
        }

        BufferedImage img = UIUtil.createImage(targetWidth, targetHeight, type);
        img.setRGB(0, 0, targetWidth, targetHeight, newData, 0, targetWidth);

        return img;
    }

    private void verifyBorderImage(BufferedImage border) throws Wrong9PatchException {
        int[] rgb = border.getRGB(0, 0, border.getWidth(), border.getHeight(), null, 0, border.getWidth());
        for (int aRgb : rgb) {
            if ((0xff000000 & aRgb) != 0) {
                if (aRgb != 0xff000000 && aRgb != 0xffff0000) {
                    throw new Wrong9PatchException();
                }
            }
        }
    }

    private void enforceBorderColors(BufferedImage inputImage) {
        Graphics2D g = inputImage.createGraphics();
        g.setBackground(new JBColor(new Color(0, 0, 0, 0), null));
        g.clearRect(1, 1, inputImage.getWidth() - 2, inputImage.getHeight() - 2);
        g.dispose();
        int w = inputImage.getWidth();
        int h = inputImage.getHeight();
        int[] rgb = new int[w * h];

        inputImage.getRGB(0, 0, w, h, rgb, 0, w);

        for (int i = 0; i < rgb.length; i++) {
            if ((0xff000000 & rgb[i]) != 0) {
                rgb[i] = 0xff000000;
            }
        }
        inputImage.setRGB(0, 0, w, h, rgb, 0, w);
        inputImage.setRGB(0, 0, 0x0);
        inputImage.setRGB(0, h - 1, 0x0);
        inputImage.setRGB(w - 1, h - 1, 0x0);
        inputImage.setRGB(w - 1, 0, 0x0);
    }

    private BufferedImage resizeNormalImage(float scaleFactor,
                                            int targetWidth,
                                            int targetHeight,
                                            File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        int newWidth = (int) (scaleFactor * targetWidth);
        int newHeight = (int) (scaleFactor * targetHeight);
        return Scalr.resize(image, newWidth, newHeight, Scalr.OP_ANTIALIAS);
    }
}
