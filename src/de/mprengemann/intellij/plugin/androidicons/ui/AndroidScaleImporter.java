package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: marcprengemann
 * Date: 07.04.14
 * Time: 11:10
 */
public class AndroidScaleImporter extends DialogWrapper{
  private final Project                   project;
  private       JPanel                    container;
  private       JComboBox                 assetResolutionSpinner;
  private       JComboBox                 targetResolutionSpinner;
  private       JTextField                targetHeight;
  private       JTextField                targetWidth;
  private       TextFieldWithBrowseButton resRoot;
  private       TextFieldWithBrowseButton assetSpinner;
  private       JTextField                resExportName;
  private       JCheckBox                 LDPICheckBox;
  private       JCheckBox                 MDPICheckBox;
  private       JCheckBox                 HDPICheckBox;
  private       JCheckBox                 XHDPICheckBox;
  private       JCheckBox                 XXHDPICheckBox;
  private JLabel imageContainer;

  public AndroidScaleImporter(Project project) {
    super(project, true);
    this.project = project;

    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return container;
  }
}
