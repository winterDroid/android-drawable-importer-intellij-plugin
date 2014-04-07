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

    assetResolutionSpinner.setSelectedIndex(3);
    targetResolutionSpinner.setSelectedIndex(3);

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
            int width = image.getWidth();
            int height = image.getHeight();

            targetHeight.setText(String.valueOf(height));
            targetWidth.setText(String.valueOf(width));

            resExportName.setText(selectedImage.getName());
          }
        } catch (IOException ignored) {
        }
      }
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
