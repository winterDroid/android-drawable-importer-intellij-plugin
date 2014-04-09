package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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
import java.util.Arrays;
import java.util.Collections;

/**
 * User: marcprengemann
 * Date: 07.04.14
 * Time: 11:10
 */
public class AndroidScaleImporter extends DialogWrapper {
  private final Project                   project;
  private       JPanel                    container;
  private       JComboBox                 assetResolutionSpinner;
  private       JComboBox                 targetResolutionSpinner;
  private       JTextField                targetHeight;
  private       JTextField                targetWidth;
  private       TextFieldWithBrowseButton resRoot;
  private       TextFieldWithBrowseButton assetBrowser;
  private       JTextField                resExportName;
  private       JCheckBox                 LDPICheckBox;
  private       JCheckBox                 MDPICheckBox;
  private       JCheckBox                 HDPICheckBox;
  private       JCheckBox                 XHDPICheckBox;
  private       JCheckBox                 XXHDPICheckBox;
  private       JLabel                    imageContainer;
  private       JCheckBox                 XXXHDPICheckBox;
  private       VirtualFile               selectedImage;
  private       File                      imageFile;
  private       float                     toLDPI;
  private       float                     toMDPI;
  private       float                     toHDPI;
  private       float                     toXHDPI;
  private       float                     toXXHDPI;
  private       float                     toXXXHDPI;
  private       int                       imageWidth;
  private       int                       imageHeight;
  private boolean isNinePatch = false;

  public AndroidScaleImporter(final Project project, Module module) {
    super(project, true);
    this.project = project;

    setTitle("Android Scale Importer");
    setResizable(true);

    AndroidResourcesHelper.initResourceBrowser(project, module, "Select res root", resRoot);

    FileChooserDescriptor imageDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(ImageFileTypeManager.getInstance().getImageFileType());
    String title1 = "Select your asset";
    imageDescriptor.setTitle(title1);
    assetBrowser.addBrowseFolderListener(title1, null, project, imageDescriptor);
    assetBrowser.addBrowseFolderListener(new TextBrowseFolderListener(imageDescriptor) {
      @Override
      protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
        super.onFileChoosen(chosenFile);
        selectedImage = chosenFile;
        isNinePatch = chosenFile.getName().endsWith(".9.png");
        updateImage();
        fillImageInformation();
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

        if (!setEnabled) {
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
      public void keyTyped(KeyEvent keyEvent) {
        super.keyTyped(keyEvent);
        updateNewSizes();
      }
    });
    targetWidth.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent keyEvent) {
        super.keyTyped(keyEvent);
        updateNewSizes();
      }
    });

    init();
  }

  private void fillImageInformation() {
    if (selectedImage != null) {
      String canonicalPath = selectedImage.getCanonicalPath();
      if (canonicalPath != null) {
        File file = new File(canonicalPath);
        try {
          BufferedImage image = ImageIO.read(file);
          if (image != null) {
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            targetHeight.setText(String.valueOf(imageHeight));
            targetWidth.setText(String.valueOf(imageWidth));

            resExportName.setText(selectedImage.getName());

            updateScaleFactors();
            updateNewSizes();
          }
        } catch (IOException ignored) {
        }
      }
    }
  }

  private void updateNewSizes() {
    try {
      int targetWidth = Integer.parseInt(this.targetWidth.getText());
      int targetHeight = Integer.parseInt(this.targetHeight.getText());

      LDPICheckBox.setText("LDPI (" + (int) (toLDPI * targetWidth) + "px x " + (int) (toLDPI * targetHeight) + " px)");
      MDPICheckBox.setText("MDPI (" + (int) (toMDPI * targetWidth) + "px x " + (int) (toMDPI * targetHeight) + " px)");
      HDPICheckBox.setText("HDPI (" + (int) (toHDPI * targetWidth) + "px x " + (int) (toHDPI * targetHeight) + " px)");
      XHDPICheckBox.setText("XHDPI (" + (int) (toXHDPI * targetWidth) + "px x " + (int) (toXHDPI * targetHeight) + " px)");
      XXHDPICheckBox.setText("XXHDPI (" + (int) (toXXHDPI * targetWidth) + "px x " + (int) (toXXHDPI * targetHeight) + " px)");
      XXXHDPICheckBox.setText("XXXHDPI (" + (int) (toXXXHDPI * targetWidth) + "px x " + (int) (toXXXHDPI * targetHeight) + " px)");

    } catch (Exception ignored) {
    }
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
    } else if (targetResolution.equalsIgnoreCase("hpdi")) {
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
    if (imageContainer != null && selectedImage != null && selectedImage.getCanonicalPath() != null) {
      imageFile = new File(selectedImage.getCanonicalPath());
      if (imageFile.exists()) {
        imageContainer.setIcon(new ImageIcon(imageFile.getAbsolutePath()));
      }
    }
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
      return new ValidationInfo("Please select a valid name for the drawable. There are just \"[a-z0-9_.]\" allowed.", resExportName);
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
    if (imageFile != null) {
      try {
        if (checkImageExists()) {
          //YES = 0; NO = 1
          int result = Messages.showYesNoDialog(
              "There is at least in one of your drawable folders a drawable with the same name. These will be overwritten. Are you sure to import?",
              "Warning",
              "Yes",
              "Cancel",
              Messages.getWarningIcon());
          if (result == 1) {
            super.doOKAction();
            return;
          }
        }

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
    }

    super.doOKAction();
  }

  private File getTargetFile(String resolution) {
    return new File(resRoot.getText().trim() + "/drawable-" + resolution + "/" + resExportName.getText().trim());
  }

  private File exportTempImage(File imageFile, String resolution, float scaleFactor, int targetWidth, int targetHeight) throws IOException {
    BufferedImage image = ImageIO.read(imageFile);
    int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();

    int newWidth = (int) (scaleFactor * targetWidth);
    int newHeight = (int) (scaleFactor * targetHeight);

    BufferedImage resizeImageJpg = resizeImage(image, newWidth, newHeight, type);

    String exportName = resExportName.getText().trim();
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

  private static BufferedImage resizeImage(BufferedImage originalImage, int newWidth, int newHeight, int type) {
    BufferedImage resizedImage = UIUtil.createImage(newWidth, newHeight, type);
    Graphics2D g = resizedImage.createGraphics();
    g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
    g.dispose();

    return resizedImage;
  }

  private boolean checkImageExists() {
    String resRootText = resRoot.getText();
    String drawableLDPIPath = resRootText + "/drawable-lpdi/";
    String drawableMDPIPath = resRootText + "/drawable-mdpi/";
    String drawableHDPIPath = resRootText + "/drawable-hdpi/";
    String drawableXHDPIPath = resRootText + "/drawable-xhdpi/";
    String drawableXXHDPIPath = resRootText + "/drawable-xxhdpi/";

    for (String path : Arrays.asList(drawableLDPIPath, drawableMDPIPath, drawableHDPIPath, drawableXHDPIPath, drawableXXHDPIPath)) {
      if (new File(path + resExportName.getText()).exists()) {
        return true;
      }
    }
    return false;
  }
}
