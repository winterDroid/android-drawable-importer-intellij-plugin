package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import de.mprengemann.intellij.plugin.androidicons.util.SimpleMouseListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.intellij.images.fileTypes.ImageFileTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * User: marcprengemann
 * Date: 08.04.14
 * Time: 14:23
 */
public class AndroidMultiDrawableImporter extends DialogWrapper {

  private final Project                   project;
  private       TextFieldWithBrowseButton resRoot;
  private       TextFieldWithBrowseButton ldpiFile;
  private       TextFieldWithBrowseButton mdpiFile;
  private       TextFieldWithBrowseButton hdpiFile;
  private       TextFieldWithBrowseButton xhdpiFile;
  private       TextFieldWithBrowseButton xxhdpiFile;
  private       TextFieldWithBrowseButton xxxhdpiFile;
  private       JLabel                    imageContainer;
  private       JTextField                resExportName;
  private       JPanel                    container;

  public AndroidMultiDrawableImporter(@Nullable final Project project, Module module) {
    super(project, true);
    this.project = project;

    setTitle("Android Multi Drawable Importer");
    setResizable(true);

    AndroidResourcesHelper.initResourceBrowser(project, module, "Select res root", resRoot);

    initBrowser("ldpi", ldpiFile);
    initBrowser("mdpi", mdpiFile);
    initBrowser("hdpi", hdpiFile);
    initBrowser("xhdpi", xhdpiFile);
    initBrowser("xxhdpi", xxhdpiFile);
    initBrowser("xxxhdpi", xxxhdpiFile);

    init();
  }

  private void initBrowser(String resolution, final TextFieldWithBrowseButton browseButton) {
    FileChooserDescriptor imageDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(ImageFileTypeManager.getInstance().getImageFileType());
    String title1 = "Select your " + resolution + " asset";
    imageDescriptor.setTitle(title1);
    browseButton.addBrowseFolderListener(title1, null, project, imageDescriptor);
    browseButton.addBrowseFolderListener(new TextBrowseFolderListener(imageDescriptor) {
      @Override
      protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
        super.onFileChoosen(chosenFile);
        updateImage(chosenFile.getCanonicalPath());
        if (StringUtils.isEmpty(resExportName.getText().trim())) {
          resExportName.setText(chosenFile.getName());
        }
      }
    });
    browseButton.getTextField().addMouseListener(new SimpleMouseListener() {
      @Override
      public void mouseClicked(MouseEvent mouseEvent) {
        updateImage(browseButton.getText());
      }
    });
  }

  private void updateImage(String fileString) {
    if (fileString != null && !StringUtils.isEmpty(fileString)) {
      File file = new File(fileString);
      updateImage(file);
    }
  }

  private void updateImage(File file) {
    if (file.exists()) {
      imageContainer.setIcon(new ImageIcon(file.getAbsolutePath()));
    }
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

    return super.doValidate();
  }

  @Override
  protected void doOKAction() {

    exportImage("ldpi", ldpiFile);
    exportImage("mdpi", mdpiFile);
    exportImage("hdpi", hdpiFile);
    exportImage("xhdpi", xhdpiFile);
    exportImage("xxhdpi", xxhdpiFile);
    exportImage("xxxhdpi", xxxhdpiFile);

    super.doOKAction();
  }

  private void exportImage(String resolution, TextFieldWithBrowseButton browser) {
    if (browser != null) {
      String sourceString = browser.getText().trim();
      String targetString = resRoot.getText().trim() + "/drawable-" + resolution + "/" + resExportName.getText().trim();
      if (!StringUtils.isEmpty(sourceString)) {
        File target = new File(targetString);
        File source = new File(sourceString);
        try {
          FileUtils.forceMkdir(target.getParentFile());
          FileUtils.copyFile(source, target);
        } catch (IOException ignored) {
        }
      }
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return container;
  }
}
