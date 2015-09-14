/*
 * Copyright 2015 Marc Prengemann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import de.mprengemann.intellij.plugin.androidicons.listeners.RadioListListener;
import de.mprengemann.intellij.plugin.androidicons.widgets.RadioButtonList;
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
        list.setRadioListListener(new RadioListListener() {
            @Override
            public void radioSelectionChanged(int selectedIndex) {
                selectedDir = items.get(selectedIndex);
            }
        });
        list.setItems(items, new Function<VirtualFile, String>() {
            @Override
            public String fun(VirtualFile virtualFile) {
                return virtualFile.getCanonicalPath();
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
