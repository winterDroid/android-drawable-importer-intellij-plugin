package de.mprengemann.intellij.plugin.androidicons.forms;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ResourcesDialog extends DialogWrapper {
    private final ResourceSelectionListener listener;
    private JPanel container;
    private RadioButtonList<VirtualFile> list;
    private VirtualFile selectedDir = null;

    public ResourcesDialog(Project project, final List<VirtualFile> items, ResourceSelectionListener listener) {
        super(project, true);
        setResizable(false);
        this.listener = listener;

        list.setItems(items, new Function<VirtualFile, String>() {
            @Override
            public String fun(VirtualFile virtualFile) {
                return virtualFile.getCanonicalPath();
            }
        });
        list.setRadioListListener(new RadioListListener() {
            @Override
            public void radioSelectionChanged(int selectedIndex) {
                selectedDir = items.get(selectedIndex);
            }
        });

        init();
    }

    @Override
    protected void doOKAction() {
        if (listener != null) {
            listener.onResourceSelected(selectedDir);
        }
        super.doOKAction();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (selectedDir == null) {
            return new ValidationInfo("Please select a resource dir.", list);
        }
        return super.doValidate();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return container;
    }

    public interface ResourceSelectionListener {
        void onResourceSelected(VirtualFile resDir);
    }
}
