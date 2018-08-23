package de.mprengemann.intellij.plugin.androidicons.dialogs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
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
import de.mprengemann.intellij.plugin.androidicons.model.Destination;
import de.mprengemann.intellij.plugin.androidicons.model.Format;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidFacetUtils;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.widgets.ExportNameField;
import de.mprengemann.intellij.plugin.androidicons.widgets.FileBrowserField;
import de.mprengemann.intellij.plugin.androidicons.widgets.ResolutionButtonModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AddItemBatchScaleDialog extends DialogWrapper implements AddItemBatchScaleDialogObserver {

    private final Project project;
    private final Module module;
    private final BatchScaleImporterController batchScaleController;

    private JPanel uiContainer;
    private JLabel imageContainer;
    private JTextField sourceFile;
    private JCheckBox LDPICheckBox;
    private JCheckBox MDPICheckBox;
    private JCheckBox HDPICheckBox;
    private JCheckBox XHDPICheckBox;
    private JCheckBox XXHDPICheckBox;
    private JCheckBox XXXHDPICheckBox;
    private JCheckBox TVDPICheckBox;
    private JComboBox sourceResolutionSpinner;
    private JFormattedTextField targetWidth;
    private JFormattedTextField targetHeight;
    private ExportNameField targetName;
    private FileBrowserField targetRoot;
    private JComboBox algorithmSpinner;
    private JComboBox methodSpinner;
    private JComboBox formatSpinner;
    private JComboBox destinationSpinner;
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
    private final PropertyChangeListener targetSizeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            final Object source = propertyChangeEvent.getSource();
            if (source == targetHeight) {
                controller.setTargetHeight(((Number) targetHeight.getValue()).intValue());
            } else if (source == targetWidth) {
                controller.setTargetWidth(((Number) targetWidth.getValue()).intValue());
            }
        }
    };
    private final ActionListener formatListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final JComboBox source = (JComboBox) e.getSource();
            final Format selectedItem = (Format) source.getSelectedItem();
            controller.setFormat(selectedItem);
        }
    };

    private final ActionListener destinationListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final JComboBox source = (JComboBox) e.getSource();
            final Destination selectedItem = (Destination) source.getSelectedItem();
            controller.setDestination(selectedItem);
        }
    };

    private ISettingsController settingsController;
    private IDefaultsController defaultsController;

    public AddItemBatchScaleDialog(final Project project,
                                   final Module module,
                                   final BatchScaleImporterController batchScaleImporterController,
                                   final VirtualFile file) {
        super(project);
        this.project = project;
        this.module = module;
        this.batchScaleController = batchScaleImporterController;

        if (file == null) {
            close(0);
            return;
        }

        String path = file.getCanonicalPath();
        if (path == null) {
            close(0);
            return;
        }

        final File realFile = new File(path);
        initRequiredControllers();
        initController(realFile);
        initTargetRoot();
        initInternal();
    }

    public AddItemBatchScaleDialog(final Project project,
                                   final Module module,
                                   final BatchScaleImporterController batchScaleImporterController,
                                   final Resolution sourceResolution,
                                   final List<ImageInformation> information) {
        super(project);
        this.project = project;
        this.module = module;
        this.batchScaleController = batchScaleImporterController;
        initRequiredControllers();
        initController(sourceResolution, information);
        initTargetRoot();
        initInternal();
    }

    private void initInternal() {
        initCheckBoxes();
        initExportName();
        initExportRoot();
        init();
        pack();
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

    private void initExportName() {
        targetName.setText(controller.getExportName());
        targetName.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                controller.setExportName((String) targetName.getValue());
            }
        });
    }

    private void initController(File file) {
        final VirtualFile root = settingsController.getResourceRoot();
        String exportRoot = "";
        if (root != null) {
            exportRoot = root.getCanonicalPath();
        } else {
            exportRoot = AndroidFacetUtils.getResourcesRoot(project, module);
        }
        controller = new AddItemBatchScaleImporterController(defaultsController, exportRoot, file);
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
        final List<ImageInformation> imageInformation = controller.getImageInformation(project);
        batchScaleController.addImage(controller.getSourceResolution(), imageInformation);
        defaultsController.setAlgorithm(controller.getAlgorithm());
        defaultsController.setMethod(controller.getMethod());
        defaultsController.setSourceResolution(controller.getSourceResolution());
        defaultsController.setResolutions(controller.getTargetResolutions());
        if (!controller.isNinePatch()) {
            defaultsController.setFormat(controller.getFormat());
        }
        defaultsController.setDestination(controller.getDestination());
        super.doOKAction();
    }

    @Override
    public void updated() {
        updateSource();
        updateSourceResolution();
        updateTargetSize();
        updateTargetResolutions();
        updateAlgorithms();
        updateAlgorithmMethod();
        updateFormat();
        updateDestination();
        updateImage(controller.getImageFile());
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

    private void updateFormat() {
        formatSpinner.removeActionListener(formatListener);
        formatSpinner.removeAllItems();
        for (Format format : Format.nonVectorValues()) {
            formatSpinner.addItem(format);
        }
        formatSpinner.setSelectedItem(controller.getFormat());
        formatSpinner.setEnabled(!controller.isNinePatch());
        formatSpinner.addActionListener(formatListener);
    }

    private void updateDestination() {
        destinationSpinner.removeActionListener(destinationListener);
        destinationSpinner.removeAllItems();
        for (Destination format : Destination.values()) {
            destinationSpinner.addItem(format);
        }
        destinationSpinner.setSelectedItem(controller.getDestination());
        destinationSpinner.addActionListener(destinationListener);
    }

    private void updateTargetSize() {
        targetHeight.removePropertyChangeListener("value", targetSizeListener);
        targetWidth.removePropertyChangeListener("value", targetSizeListener);

        targetHeight.setValue(controller.getTargetHeight());
        targetWidth.setValue(controller.getTargetWidth());

        targetHeight.addPropertyChangeListener("value", targetSizeListener);
        targetWidth.addPropertyChangeListener("value", targetSizeListener);
    }

    private void updateSourceResolution() {
        sourceResolutionSpinner.removeActionListener(sourceResolutionListener);
        sourceResolutionSpinner.removeAllItems();
        for (Resolution resolution : Resolution.nonVectorValues()) {
            sourceResolutionSpinner.addItem(resolution);
        }
        sourceResolutionSpinner.setSelectedItem(controller.getSourceResolution());
        sourceResolutionSpinner.addActionListener(sourceResolutionListener);
    }

    private void updateSource() {
        sourceFile.setText(controller.getImageFile().getAbsolutePath());
        sourceFile.setEnabled(false);
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
            final int[] sizes = controller.getScaledSize(resolution);
            checkBox.setText(String.format("%s (%d px x %d px)", resolution, sizes[0], sizes[1]));
            checkBox.addActionListener(resolutionActionListener);
        }
    }

    private void updateImage(final File file) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (imageContainer == null) {
                    return;
                }
                if (file == null) {
                    imageContainer.setIcon(null);
                    return;
                }
                ImageUtils.updateImage(imageContainer, file, controller.getFormat());
            }
        });
    }

    private void createUIComponents() {
        NumberFormat format = NumberFormat.getIntegerInstance();
        NumberFormatter numberFormatter = new NumberFormatter(format);
        numberFormatter.setValueClass(Integer.class);
        numberFormatter.setAllowsInvalid(false);
        numberFormatter.setMinimum(1);
        numberFormatter.setCommitsOnValidEdit(true);

        targetHeight = new JFormattedTextField(numberFormatter);
        targetWidth = new JFormattedTextField(numberFormatter);
        targetRoot = new FileBrowserField(FileBrowserField.RESOURCE_DIR_CHOOSER);
    }
}
