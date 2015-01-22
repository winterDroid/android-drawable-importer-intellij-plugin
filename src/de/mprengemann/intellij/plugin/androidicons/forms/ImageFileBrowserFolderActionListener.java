package de.mprengemann.intellij.plugin.androidicons.forms;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImageFileBrowserFolderActionListener extends ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> {

    private final String title;
    private final Project project;

    public ImageFileBrowserFolderActionListener(String title,
                                                Project project,
                                                ComponentWithBrowseButton<JTextField> componentWithBrowseButton,
                                                FileChooserDescriptor fileChooserDescriptor) {
        super(title, null, componentWithBrowseButton, project, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        this.title = title;
        this.project = project;
    }

    @Nullable
    @Override
    protected VirtualFile getInitialFile() {
        String directoryName = this.getComponentText();
        if (StringUtil.isEmptyOrSpaces(directoryName)) {
            return null;
        } else {
            if (project != null) {
                directoryName = SettingsHelper.getLastImageFolder(project);
            } else {
                directoryName = FileUtil.toSystemIndependentName(directoryName);
            }

            VirtualFile path;
            for (path = LocalFileSystem.getInstance().findFileByPath(this.expandPath(directoryName));
                 path == null && directoryName.length() > 0;
                 path = LocalFileSystem.getInstance().findFileByPath(directoryName)) {
                int pos = directoryName.lastIndexOf(47);
                if (pos <= 0) {
                    break;
                }
                directoryName = directoryName.substring(0, pos);
            }

            return path;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileChooserDescriptor fileChooserDescriptor = this.myFileChooserDescriptor;
        if (this.title != null) {
            fileChooserDescriptor = (FileChooserDescriptor) this.myFileChooserDescriptor.clone();
            fileChooserDescriptor.setTitle(this.title);
        }

        FileChooser.chooseFile(fileChooserDescriptor, this.getProject(), this.getInitialFile(), new Consumer<VirtualFile>() {
            @Override
            public void consume(VirtualFile file) {
                SettingsHelper.saveLastImageFolder(project, file.getCanonicalPath());
                onFileChosen(file);
            }
        });
    }
}
