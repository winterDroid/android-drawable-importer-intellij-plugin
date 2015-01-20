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
import de.mprengemann.intellij.plugin.androidicons.util.AndroidResourcesHelper;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import de.mprengemann.intellij.plugin.androidicons.util.SimpleMouseListener;
import org.apache.commons.lang.StringUtils;
import org.intellij.images.fileTypes.ImageFileTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: marcprengemann
 * Date: 08.04.14
 * Time: 14:23
 */
public class AndroidMultiDrawableImporter extends DialogWrapper {

    private final Project project;
    private TextFieldWithBrowseButton resRoot;
    private TextFieldWithBrowseButton ldpiFile;
    private TextFieldWithBrowseButton mdpiFile;
    private TextFieldWithBrowseButton hdpiFile;
    private TextFieldWithBrowseButton xhdpiFile;
    private TextFieldWithBrowseButton xxhdpiFile;
    private TextFieldWithBrowseButton xxxhdpiFile;
    private JLabel imageContainer;
    private JTextField resExportName;
    private JPanel container;

    public AndroidMultiDrawableImporter(@Nullable final Project project, Module module) {
        super(project, true);
        this.project = project;

        setTitle("Android Multi Drawable Importer");
        setResizable(false);

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
        final FileChooserDescriptor imageDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(ImageFileTypeManager.getInstance().getImageFileType());
        String title1 = "Select your " + resolution + " asset";
        imageDescriptor.setTitle(title1);
        browseButton.addBrowseFolderListener(title1, null, project, imageDescriptor);
        browseButton.addBrowseFolderListener(new TextBrowseFolderListener(imageDescriptor) {
            @Override
            @SuppressWarnings("deprecation") // Otherwise not compatible to AndroidStudio
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
        new FileDrop(browseButton.getTextField(), new FileDrop.Target() {
            @Override
            public FileChooserDescriptor getDescriptor() {
                return imageDescriptor;
            }

            @Override
            public boolean isHiddenShown() {
                return false;
            }

            @Override
            public void dropFiles(List<VirtualFile> virtualFiles) {
                if (virtualFiles != null) {
                    if (virtualFiles.size() == 1) {
                        VirtualFile chosenFile = virtualFiles.get(0);
                        browseButton.setText(chosenFile.getCanonicalPath());
                        updateImage(chosenFile.getCanonicalPath());
                        if (StringUtils.isEmpty(resExportName.getText().trim())) {
                            resExportName.setText(chosenFile.getName());
                        }
                    }
                }
            }
        });
    }

    private void updateImage(String fileString) {
        if (fileString != null && !StringUtils.isEmpty(fileString)) {
            File file = new File(fileString);
            ImageUtils.updateImage(imageContainer, file);
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
        List<File> sources = new ArrayList<File>();
        List<File> targets = new ArrayList<File>();

        addDataIfNecessary("ldpi", ldpiFile, sources, targets);
        addDataIfNecessary("mdpi", mdpiFile, sources, targets);
        addDataIfNecessary("hdpi", hdpiFile, sources, targets);
        addDataIfNecessary("xhdpi", xhdpiFile, sources, targets);
        addDataIfNecessary("xxhdpi", xxhdpiFile, sources, targets);
        addDataIfNecessary("xxxhdpi", xxxhdpiFile, sources, targets);

        try {
            RefactorHelper.copy(project, sources, targets);
        } catch (IOException ignored) {
        }

        super.doOKAction();
    }

    private void addDataIfNecessary(String resolution, TextFieldWithBrowseButton browser, List<File> sources, List<File> targets) {
        if (browser != null) {
            String sourceString = browser.getText().trim();
            String targetString = resRoot.getText().trim() + "/drawable-" + resolution + "/" + resExportName.getText().trim();
            if (!StringUtils.isEmpty(sourceString)) {
                File target = new File(targetString);
                File source = new File(sourceString);

                if (source.exists()) {
                    sources.add(source);
                    targets.add(target);
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
