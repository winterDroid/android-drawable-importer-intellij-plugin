package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * User: marcprengemann
 * Date: 04.04.14
 * Time: 16:29
 */
public class WrappedDialog extends DialogWrapper {

  private VirtualFile assetRoot;

  private Project                   project;
  private JLabel                    imageContainer;
  private TextFieldWithBrowseButton resRoot;
  private JLabel                    warningText;
  private JComboBox                 assetSpinner;
  private JComboBox                 colorSpinner;
  private JTextField                resExportName;
  private JCheckBox                 LDPICheckBox;
  private JCheckBox                 MDPICheckBox;
  private JCheckBox                 HDPICheckBox;
  private JCheckBox                 XHDPICheckBox;
  private JCheckBox                 XXHDPICheckBox;
  private JPanel                    container;
  private String                    assetColor;
  private String                    assetName;
  private boolean exportNameChanged = false;
  private File imageFile;

  /**
   * @param project
   */
  public WrappedDialog(@Nullable final Project project) {
    super(project, true);
    this.project = project;

    setTitle("Android Icons Importer");
    setResizable(true);

    FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    String title = "Select res directory";
    workingDirectoryChooserDescriptor.setTitle(title);
    resRoot.addBrowseFolderListener(title, null, project, workingDirectoryChooserDescriptor);

    assetRoot = SettingsHelper.getAssetPath();
    if (assetRoot == null) {
      FileChooserDescriptor assetDirChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
      assetDirChooserDescriptor.setTitle("Select Android Icons Asset Folder");
      FileChooser.chooseFile(assetDirChooserDescriptor, project, null, new Consumer<VirtualFile>() {
        @Override
        public void consume(VirtualFile virtualFile) {
          if (virtualFile != null) {
            SettingsHelper.saveAssetPath(virtualFile);
            assetRoot = virtualFile;
            fillComboBoxes();
          } else {
            Messages.showMessageDialog(
                project,
                "You have to select the Android Icons asset folder!",
                "Error",
                Messages.getWarningIcon());
          }
        }
      });
    } else {
      fillComboBoxes();
    }

    colorSpinner.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        assetColor = (String) colorSpinner.getSelectedItem();
        updateImage();
        checkImageExists();
      }
    });

    assetSpinner.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        assetName = (String) assetSpinner.getSelectedItem();
        updateImage();
        checkImageExists();
      }
    });

    resExportName.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent keyEvent) {
        super.keyTyped(keyEvent);
        if (!exportNameChanged && keyEvent != null && keyEvent.getKeyCode() > -1) {
          exportNameChanged = true;
        }
        checkImageExists();
      }

      @Override
      public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);
      }

      @Override
      public void keyReleased(KeyEvent keyEvent) {
        super.keyReleased(keyEvent);
      }
    });


    init();
  }

  private void checkImageExists() {
    String resRootText = resRoot.getText();
    String drawablePath = resRootText + "/drawable/";
    String drawableLDPIPath = resRootText + "/drawable-lpdi/";
    String drawableMDPIPath = resRootText + "/drawable-mdpi/";
    String drawableHDPIPath = resRootText + "/drawable-hdpi/";
    String drawableXHDPIPath = resRootText + "/drawable-xhdpi/";
    String drawableXXHDPIPath = resRootText + "/drawable-xxhdpi/";

    for (String path : Arrays.asList(drawablePath, drawableLDPIPath, drawableMDPIPath, drawableHDPIPath, drawableXHDPIPath, drawableXXHDPIPath)) {
      if (new File(path + resExportName.getText()).exists()) {
        showWarning();
        break;
      }
    }
    hideWarning();
  }

  private void hideWarning() {
    if (warningText != null) {
      warningText.setVisible(false);
    }
  }

  private void showWarning() {
    if (warningText != null) {
      warningText.setVisible(true);
    }
  }

  private void updateImage() {
    if (imageContainer != null) {
      String path = "/" + assetColor.replace(" ", "_") + "/xxhdpi/ic_action_" + assetName + ".png";
      imageFile = new File(assetRoot.getCanonicalPath() + path);
      if (imageFile.exists()) {
        imageContainer.setIcon(new ImageIcon(imageFile.getAbsolutePath()));
      }
      if (!exportNameChanged) {
        resExportName.setText("ic_action_" + assetName + ".png");
      }
    }
  }

  private void fillComboBoxes() {
    VirtualFile[] colorDirs = assetRoot.getChildren();
    Arrays.sort(colorDirs, new Comparator<VirtualFile>() {
      @Override
      public int compare(VirtualFile virtualFile, VirtualFile virtualFile2) {
        if (virtualFile != null && virtualFile2 != null) {
          return virtualFile.getNameWithoutExtension().compareTo(virtualFile2.getNameWithoutExtension());
        }
        return 0;
      }
    });
    if (colorDirs != null) {
      for (VirtualFile file : colorDirs) {
        if (file.isDirectory()) {
          colorSpinner.addItem(file.getNameWithoutExtension().replace("_", " "));
        }
      }

      if (colorDirs.length > 1) {
        VirtualFile exColorDir = colorDirs[1];
        VirtualFile[] densities = exColorDir.getChildren();
        if (densities != null && densities.length > 1) {
          VirtualFile exDensity = densities[1];
          VirtualFile[] assets = exDensity.getChildren();
          Arrays.sort(assets, new Comparator<VirtualFile>() {
            @Override
            public int compare(VirtualFile virtualFile, VirtualFile virtualFile2) {
              if (virtualFile != null && virtualFile2 != null) {
                return virtualFile.getNameWithoutExtension().compareTo(virtualFile2.getNameWithoutExtension());
              }
              return 0;
            }
          });
          for (VirtualFile asset : assets) {
            if (!asset.isDirectory() && asset.getExtension().equalsIgnoreCase("png")) {
              assetSpinner.addItem(asset.getNameWithoutExtension().replace("ic_action_", ""));
            }
          }

          assetColor = (String) colorSpinner.getSelectedItem();
          assetName = (String) assetSpinner.getSelectedItem();
          updateImage();
        }
      }
    }
  }

  @Override
  protected void doOKAction() {
//      String color = colorTextField.getText();
//
//      String themeSelected = (String)themeComboBox.getSelectedItem();
//      if ("Light".equals(themeSelected)) {
//        themeSelected = "light";
//      }
//      else if ("Dark".equals(themeSelected)) {
//        themeSelected = "dark";
//      }
//      else {
//        themeSelected = "light_dark_action_bar";
//      }
//
//      String compatSelected = (String)compatComboBox.getSelectedItem();
//      if ("None".equals(compatSelected)) {
//        compatSelected = "old";
//      }
//      else if ("Sherlock".equals(compatSelected)) {
//        compatSelected = "abs";
//      }
//      else {
//        compatSelected = "compat";
//      }
//
//      int kitkat = 0;
//      if (kitkatCheckBox.isSelected()) {
//        kitkat = 1;
//      }
//
//      String urlZip = "http://android-holo-colors.com/generate_all.php?color=" +
//                      color.replaceFirst("#", "") +
//                      "&holo=" +
//                      themeSelected +
//                      "&name=" +
//                      nameTextField.getText().replaceAll(" ", "") +
//                      "&kitkat=" + kitkat +
//                      "&minsdk=" + (oldSdkRadio.isSelected() ? "old" : "holo") +
//                      "&compat=" + compatSelected;
//
//      if (editTextCheckBox.isSelected()) {
//        urlZip += "&edittext=true";
//      }
//      if (textHandleCheckBox.isSelected()) {
//        urlZip += "&text_handle=true";
//      }
//      if (autocompleteCheckBox.isSelected()) {
//        urlZip += "&autocomplete=true";
//      }
//      if (buttonCheckBox.isSelected()) {
//        urlZip += "&button=true";
//      }
//      if (coloredButtonCheckBox.isSelected()) {
//        urlZip += "&cbutton=true";
//      }
//      if (checkBoxCheckBox.isSelected()) {
//        urlZip += "&checkbox=true";
//      }
//      if (radioCheckBox.isSelected()) {
//        urlZip += "&radio=true";
//      }
//      if (spinnerCheckBox.isSelected()) {
//        urlZip += "&spinner=true";
//      }
//      if (coloredSpinnerCheckBox.isSelected()) {
//        urlZip += "&cspinner=true";
//      }
//      if (progressBarCheckBox.isSelected()) {
//        urlZip += "&progressbar=true";
//      }
//      if (seekBarCheckBox.isSelected()) {
//        urlZip += "&seekbar=true";
//      }
//      if (toggleCheckBox.isSelected()) {
//        urlZip += "&toggle=true";
//      }
//      if (listSelectorCheckBox.isSelected()) {
//        urlZip += "&list=true";
//      }
//      if (ratingBarCheckBox.isSelected()) {
//        urlZip += "&ratingbar=true";
//      }
//      if (ratingBarSmallCheckBox.isSelected()) {
//        urlZip += "&ratingstarsmall=true";
//      }
//      if (ratingBarBigCheckBox.isSelected()) {
//        urlZip += "&ratingstarbig=true";
//      }
//      if (fastScrollCheckBox.isSelected()) {
//        urlZip += "&fastscroll=true";
//      }
//      if (switchCheckBox.isSelected()) {
//        urlZip += "&switchjb=true";
//      }
//
//      try {
//        loadingDialog.setVisible(true);
//        File zipFile = downloadZip(urlZip);
//        loadingDialog.dispose();
//
//        unzipFile(zipFile);
//        zipFile.delete();
//      }
//      catch (Exception e) {
//        JOptionPane.showMessageDialog(ahcPanel, "Unable to generate your theme, please go to http://android-holo-colors.com");
//      }

    super.doOKAction();
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
//    if (StringUtils.isEmpty(nameTextField.getText().trim())) {
//      return new ValidationInfo("Please select a name for your theme.", nameTextField);
//    }
//
//    if (StringUtils.isEmpty(resPathTextField.getText().trim())) {
//      return new ValidationInfo("Please select res folder in order to unzip the holo colors archive.", resPathTextField);
//    }
//
//    if (StringUtils.isEmpty(colorTextField.getText().trim())) {
//      return new ValidationInfo("Please select a valid color.", colorTextField);
//    }

    return null;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return container;
  }

  // ----------------------------------
  // PRIVATE
  // ----------------------------------

//  private File downloadZip(String urlZip) throws Exception {
//    new File(project.getBaseDir().getPath() + IDEA_FOLDER).mkdirs();
//    File zipFile = new File(project.getBaseDir().getPath() + IDEA_FOLDER, "AndroidHoloColors_" + nameTextField.getText() + ".zip");
//
//    InputStream is = null;
//    FileOutputStream os = null;
//    try {
//      URL url = new URL(urlZip);
//      is = url.openStream();
//
//      os = new FileOutputStream(zipFile);
//      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
//      int n;
//      while (-1 != (n = is.read(buffer))) {
//        os.write(buffer, 0, n);
//      }
//    } finally {
//      if (is != null) {
//        try {
//          is.close();
//        } catch (IOException e) {
//        }
//      }
//      if (os != null) {
//        try {
//          os.close();
//        } catch (IOException e) {
//        }
//      }
//    }
//
//    return zipFile;
//  }
//
//  private void unzipFile(File zipFile) throws Exception {
//    File outputFolder = new File(resPathTextField.getText());
//
//    boolean overwriteAll = false;
//    boolean overwriteNone = false;
//    Object[] overwriteOptions = {"Overwrite this file", "Overwrite all", "Do not overwrite this file", "Do not overwrite any file" };
//
//    ZipInputStream zis = null;
//    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
//    try {
//      zis = new ZipInputStream(new FileInputStream(zipFile));
//      ZipEntry ze = zis.getNextEntry();
//      while (ze != null) {
//        String fileName = ze.getName().replaceFirst("res/", "");
//        File newFile = new File(outputFolder + File.separator + fileName);
//
//        new File(newFile.getParent()).mkdirs();
//
//        boolean overwrite = overwriteAll || (!newFile.exists());
//        if (newFile.exists() && newFile.isFile() && !overwriteAll && !overwriteNone) {
//          int option = JOptionPane
//              .showOptionDialog(ahcPanel, newFile.getName() + " already exists, overwrite ?", "File exists", JOptionPane.YES_NO_CANCEL_OPTION,
//                                JOptionPane.QUESTION_MESSAGE, new ImageIcon(getClass().getResource("/icons/H64.png")), overwriteOptions, overwriteOptions[0]);
//
//          switch (option) {
//            case 0:
//              overwrite = true;
//              break;
//            case 1:
//              overwrite = true;
//              overwriteAll = true;
//              break;
//            case 2:
//              overwrite = false;
//              break;
//            case 3:
//              overwrite = false;
//              overwriteNone = true;
//              break;
//            default:
//              overwrite = false;
//          }
//        }
//
//        if (overwrite && !fileName.endsWith(File.separator)) {
//          FileOutputStream fos = new FileOutputStream(newFile);
//          int len;
//          while ((len = zis.read(buffer)) > 0) {
//            fos.write(buffer, 0, len);
//          }
//          fos.close();
//        }
//        ze = zis.getNextEntry();
//      }
//
//      zis.closeEntry();
//      zis.close();
//    } finally {
//      if (zis != null) {
//        try {
//          zis.close();
//        } catch (IOException e) {
//        }
//      }
//    }
//  }
}