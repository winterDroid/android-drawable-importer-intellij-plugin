package de.mprengemann.intellij.plugin.androidicons.widgets;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import de.mprengemann.intellij.plugin.androidicons.controllers.settings.ISettingsController;
import de.mprengemann.intellij.plugin.androidicons.ui.ResourcesDialog;
import de.mprengemann.intellij.plugin.androidicons.util.AndroidFacetUtils;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

public class ResourceBrowser extends TextFieldWithBrowseButton implements KeyListener {

    private Project project;
    private Module module;
    private ISettingsController settingsController;
    private Consumer<File> listener;

    public ResourceBrowser() {
        super();
        getTextField().addKeyListener(this);
    }

    public void init(Project project,
                     Module module,
                     ISettingsController settingsController) {
        this.project = project;
        this.module = module;
        this.settingsController = settingsController;
        initInternal();
    }

    private void initInternal() {
        final VirtualFile resourceDir = settingsController.getResRootForProject(project);
        if (resourceDir == null) {
            getResRootFile(new ResourcesDialog.ResourceSelectionListener() {
                @Override
                public void onResourceSelected(VirtualFile resDir) {
                    setText(resDir.getCanonicalPath());
                }
            });
        } else {
            setText(resourceDir.getCanonicalPath());
        }

        FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        workingDirectoryChooserDescriptor.setTitle("Select res root");
        addBrowseFolderListener("Select res root", null, project, workingDirectoryChooserDescriptor);
        addBrowseFolderListener(new TextBrowseFolderListener(workingDirectoryChooserDescriptor) {
            @Override
            @SuppressWarnings("deprecation") // Otherwise not compatible to AndroidStudio
            protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                super.onFileChoosen(chosenFile);
                setText(chosenFile.getCanonicalPath());
            }
        });
    }

    private void getResRootFile(ResourcesDialog.ResourceSelectionListener listener) {
        AndroidFacet currentFacet = AndroidFacetUtils.getCurrentFacet(project, module);

        if (currentFacet != null) {
            List<VirtualFile> allResourceDirectories = currentFacet.getAllResourceDirectories();
            if (allResourceDirectories.size() == 1) {
                listener.onResourceSelected(allResourceDirectories.get(0));
            } else if (allResourceDirectories.size() > 1) {
                ResourcesDialog dialog = new ResourcesDialog(project, allResourceDirectories, listener);
                dialog.show();
            }
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        notifyListener(text);
    }

    private void notifyListener(String filePath) {
        if (listener != null) {
            listener.consume(new File(filePath));
        }
    }

    public void setSelectionListener(Consumer<File> fileConsumer) {
        this.listener = fileConsumer;
        notifyListener(getText());
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        notifyListener(getText());
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}
