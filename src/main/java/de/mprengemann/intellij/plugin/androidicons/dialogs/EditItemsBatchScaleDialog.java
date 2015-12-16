package de.mprengemann.intellij.plugin.androidicons.dialogs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.IconApplication;
import de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.BatchScaleImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.additem.AddItemBatchScaleDialogObserver;
import de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.additem.AddItemBatchScaleImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.batchscale.additem.IAddItemBatchScaleImporterController;
import de.mprengemann.intellij.plugin.androidicons.controllers.defaults.IDefaultsController;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.listeners.SimpleKeyListener;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.widgets.FileBrowserField;
import de.mprengemann.intellij.plugin.androidicons.widgets.ResolutionButtonModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class EditItemsBatchScaleDialog extends DialogWrapper implements AddItemBatchScaleDialogObserver {

    private final Project project;
    private final Module module;
    private final BatchScaleImporterController batchScaleController;
    private final List<String> selectedFiles;
    private List<Resolution> sourceResolution;
    private final List<List<ImageInformation>> information;

    private JPanel uiContainer;
    private JCheckBox LDPICheckBox;
    private JCheckBox MDPICheckBox;
    private JCheckBox HDPICheckBox;
    private JCheckBox XHDPICheckBox;
    private JCheckBox XXHDPICheckBox;
    private JCheckBox XXXHDPICheckBox;
    private JCheckBox TVDPICheckBox;
    private JComboBox sourceResolutionSpinner;
    private FileBrowserField targetRoot;
    private JComboBox algorithmSpinner;
    private JComboBox methodSpinner;
    private IAddItemBatchScaleImporterController controller;
    private final ActionListener sourceResolutionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            final JComboBox source = (JComboBox) actionEvent.getSource();
            final Resolution selectedItem = (Resolution) source.getSelectedItem();
            controller.setSourceResolution(selectedItem);
        }
    };
    private final ActionListener algorithmListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            final JComboBox source = (JComboBox) actionEvent.getSource();
            final ResizeAlgorithm selectedItem = (ResizeAlgorithm) source.getSelectedItem();
            controller.setAlgorithm(selectedItem);
        }
    };
    private final ActionListener methodListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            final JComboBox source = (JComboBox) actionEvent.getSource();
            final String selectedItem = (String) source.getSelectedItem();
            controller.setMethod(selectedItem);
        }
    };
    private final ActionListener resolutionActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            final JCheckBox source = (JCheckBox) actionEvent.getSource();
            final Resolution resolution = ((ResolutionButtonModel) source.getModel()).getResolution();
            if (source.isSelected()) {
                controller.addTargetResolution(resolution);
            } else {
                controller.removeTargetResolution(resolution);
            }
        }
    };
    private ISettingsController settingsController;
    private IDefaultsController defaultsController;

    public EditItemsBatchScaleDialog(final Project project,
                                     final Module module,
                                     final BatchScaleImporterController batchScaleImporterController,
                                     final List<String> selectedFiles,
                                     final List<Resolution> sourceResolution,
                                     final List<List<ImageInformation>> information) {
        super(project);
        this.project = project;
        this.module = module;
        this.batchScaleController = batchScaleImporterController;
        this.selectedFiles = selectedFiles;
        this.sourceResolution = sourceResolution;
        this.information = information;
        initRequiredControllers();
        initController(sourceResolution.get(0), information.get(0));
        initTargetRoot();
        initInternal();
    }

    private void initInternal() {
        initCheckBoxes();
        initExportRoot();
        initAlgorithms();
        init();
        pack();
        setResizable(false);

        controller.addObserver(this);
    }

    private void initTargetRoot() {
        targetRoot.initWithResourceRoot(project, module, settingsController);
        targetRoot.setSelectionListener(new Consumer<File>() {
            @Override
            public void consume(File file) {
                controller.setTargetRoot(file.getAbsolutePath());
            }
        });
    }

    private void initAlgorithms() {
        for (ResizeAlgorithm algorithm : ResizeAlgorithm.values()) {
            algorithmSpinner.addItem(algorithm);
        }
    }

    private void initCheckBoxes() {
        LDPICheckBox.setModel(new ResolutionButtonModel(Resolution.LDPI));
        MDPICheckBox.setModel(new ResolutionButtonModel(Resolution.MDPI));
        HDPICheckBox.setModel(new ResolutionButtonModel(Resolution.HDPI));
        XHDPICheckBox.setModel(new ResolutionButtonModel(Resolution.XHDPI));
        XXHDPICheckBox.setModel(new ResolutionButtonModel(Resolution.XXHDPI));
        XXXHDPICheckBox.setModel(new ResolutionButtonModel(Resolution.XXXHDPI));
        TVDPICheckBox.setModel(new ResolutionButtonModel(Resolution.TVDPI));
    }

    private void initExportRoot() {
        targetRoot.setText(controller.getExportPath());
        targetRoot.addKeyListener(new SimpleKeyListener() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                controller.setTargetRoot(((JTextField) keyEvent.getSource()).getText());
            }
        });
    }

    private void initRequiredControllers() {
        final IconApplication container = ApplicationManager.getApplication().getComponent(IconApplication.class);
        settingsController = container.getControllerFactory().getSettingsController();
        defaultsController = container.getControllerFactory().getDefaultsController();
    }

    private void initController(Resolution sourceResolution, List<ImageInformation> information) {
        controller = new AddItemBatchScaleImporterController(sourceResolution, information);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return uiContainer;
    }

    @Override
    protected void doOKAction() {
        for (int i = 0; i < selectedFiles.size(); i++) {
            final List<ImageInformation> originalImageInformation = information.get(i);
            final Resolution originalSourceResolution = sourceResolution.get(i);
            final String selectedFile = selectedFiles.get(i);

            final List<ImageInformation> imageInformation = controller.getImageInformation(project,
                                                                                           selectedFile,
                                                                                           originalImageInformation,
                                                                                           originalSourceResolution);
            batchScaleController.addImage(controller.getSourceResolution(), imageInformation);
        }

        defaultsController.setAlgorithm(controller.getAlgorithm());
        defaultsController.setMethod(controller.getMethod());
        defaultsController.setSourceResolution(controller.getSourceResolution());
        defaultsController.setResolutions(controller.getTargetResolutions());
        super.doOKAction();
    }

    @Override
    public void updated() {
        updateSourceResolution();
        updateTargetResolutions();
        updateAlgorithms();
        updateAlgorithmMethod();
    }

    private void updateAlgorithms() {
        algorithmSpinner.removeActionListener(algorithmListener);
        algorithmSpinner.removeAllItems();
        for (ResizeAlgorithm algorithm : ResizeAlgorithm.values()) {
            algorithmSpinner.addItem(algorithm);
        }
        algorithmSpinner.setSelectedItem(controller.getAlgorithm());
        algorithmSpinner.addActionListener(algorithmListener);
    }

    private void updateAlgorithmMethod() {
        methodSpinner.removeActionListener(methodListener);
        methodSpinner.removeAllItems();
        final List<String> methods = controller.getMethods();
        for (String method : methods) {
            methodSpinner.addItem(method);
        }
        methodSpinner.setSelectedItem(controller.getMethod());
        methodSpinner.setEnabled(methods.size() > 1);
        methodSpinner.addActionListener(methodListener);
    }

    private void updateSourceResolution() {
        sourceResolutionSpinner.removeActionListener(sourceResolutionListener);
        sourceResolutionSpinner.removeAllItems();
        for (Resolution resolution : Resolution.values()) {
            sourceResolutionSpinner.addItem(resolution);
        }
        sourceResolutionSpinner.setSelectedItem(controller.getSourceResolution());
        sourceResolutionSpinner.addActionListener(sourceResolutionListener);
    }

    private void updateTargetResolutions() {
        final Set<Resolution> resolutions = controller.getTargetResolutions();
        for (JCheckBox checkBox : Arrays.asList(LDPICheckBox,
                                                MDPICheckBox,
                                                HDPICheckBox,
                                                XHDPICheckBox,
                                                XXHDPICheckBox,
                                                XXXHDPICheckBox,
                                                TVDPICheckBox)) {
            checkBox.removeActionListener(resolutionActionListener);
            final Resolution resolution = ((ResolutionButtonModel) checkBox.getModel()).getResolution();
            checkBox.setSelected(resolutions.contains(resolution));
            checkBox.addActionListener(resolutionActionListener);
        }
    }

    private void createUIComponents() {
        targetRoot = new FileBrowserField(FileBrowserField.RESOURCE_DIR_CHOOSER);
    }
}
