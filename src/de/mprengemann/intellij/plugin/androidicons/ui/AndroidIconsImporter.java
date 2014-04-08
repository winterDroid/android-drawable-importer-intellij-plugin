package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * User: marcprengemann
 * Date: 04.04.14
 * Time: 16:29
 */
public class AndroidIconsImporter extends DialogWrapper {

  private VirtualFile assetRoot;

  private Project                   project;
  private JLabel                    imageContainer;
  private TextFieldWithBrowseButton resRoot;
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

  public AndroidIconsImporter(@Nullable final Project project, Module module) {
    super(project, true);
    this.project = project;

    setTitle("Android Icons Importer");
    setResizable(true);

    AndroidResourcesHelper.initResourceBrowser(project, module, "Select res root", this.resRoot);

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
      }

      @Override
      public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);
      }

      @Override
      public void keyReleased(KeyEvent keyEvent) {
        super.keyReleased(keyEvent);
        checkImageExists();
      }
    });

    init();
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
            if (!asset.isDirectory() && asset.getExtension()!= null && asset.getExtension().equalsIgnoreCase("png")) {
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
    importIcons();
    super.doOKAction();
  }

  private void importIcons() {
    List<FromToPath> paths = new ArrayList<FromToPath>();
    if (LDPICheckBox.isSelected()) {
      paths.add(new FromToPath("ldpi"));
    }
    if (MDPICheckBox.isSelected()) {
      paths.add(new FromToPath("mdpi"));
    }
    if (HDPICheckBox.isSelected()) {
      paths.add(new FromToPath("hdpi"));
    }
    if (XHDPICheckBox.isSelected()) {
      paths.add(new FromToPath("xhdpi"));
    }
    if (XXHDPICheckBox.isSelected()) {
      paths.add(new FromToPath("xxhdpi"));
    }

    for (FromToPath path : paths) {
      if (!copyDrawable(path)) {
        Messages.showErrorDialog(
            project,
            "An error occured while copying the " + path.resolution + " drawable. Please try again.",
            "Import Canceled");
        return;
      }
    }
    project.getBaseDir().refresh(true, true);
  }

  private boolean copyDrawable(FromToPath path) {
    try {
      File source = new File(path.source);
      File target = new File(path.target);

      if (source.exists()) {
        FileUtils.forceMkdir(target.getParentFile());
        FileUtils.copyFile(source, target);
      } else {
        return false;
      }
    } catch (IOException e) {
      return false;
    }
    return true;
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

    return null;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return container;
  }

  private class FromToPath {
    public final String source;
    public final String target;
    public final String resolution;

    private FromToPath(String resolution) {
      this.resolution = resolution;

      String resRootText = resRoot.getText();

      String fromName = "ic_action_" + assetName + ".png";
      String toName = resExportName.getText();

      this.source = assetRoot.getCanonicalPath() + "/" + assetColor.replace(" ", "_") + "/" + resolution + "/" + fromName;
      this.target = resRootText + "/drawable-" + resolution + "/" + toName;
    }
  }
}