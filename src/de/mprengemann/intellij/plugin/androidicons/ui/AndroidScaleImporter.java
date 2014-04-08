package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
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

  public AndroidScaleImporter(final Project project) {
    super(project, true);
    this.project = project;

    setTitle("Android Scale Importer");
    setResizable(true);

    VirtualFile lastResRoot = SettingsHelper.getResRootForProject(project);
    if (lastResRoot != null) {
      resRoot.setText(lastResRoot.getCanonicalPath());
    }

    FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    String title = "Select res directory";
    workingDirectoryChooserDescriptor.setTitle(title);
    resRoot.addBrowseFolderListener(title, null, project, workingDirectoryChooserDescriptor);
    resRoot.addBrowseFolderListener(new TextBrowseFolderListener(workingDirectoryChooserDescriptor) {
      @Override
      protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
        super.onFileChoosen(chosenFile);
        SettingsHelper.saveResRootForProject(project, chosenFile.getUrl());
      }
    });

    FileChooserDescriptor imageDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(ImageFileTypeManager.getInstance().getImageFileType());
    String title1 = "Select your asset";
    imageDescriptor.setTitle(title1);
    assetBrowser.addBrowseFolderListener(title1, null, project, imageDescriptor);
    assetBrowser.addBrowseFolderListener(new TextBrowseFolderListener(imageDescriptor) {
      @Override
      protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
        super.onFileChoosen(chosenFile);
        selectedImage = chosenFile;
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
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

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
}
