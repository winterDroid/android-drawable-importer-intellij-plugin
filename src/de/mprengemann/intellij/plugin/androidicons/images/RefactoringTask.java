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

package de.mprengemann.intellij.plugin.androidicons.images;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.util.ui.UIUtil;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RefactoringTask extends Task.Backgroundable {

    private Project project;
    private List<ImageInformation> imageInformationList = new ArrayList<ImageInformation>();

    public RefactoringTask(Project project) {
        super(project, "Import images", false);
        this.project = project;
    }

    private void refactor(ProgressIndicator indicator) throws IOException, ProcessCanceledException {
        indicator.setText("Import images");
        indicator.checkCanceled();
        indicator.setIndeterminate(false);
        for (int i = 0; i < imageInformationList.size(); i++) {
            ImageInformation information = imageInformationList.get(i);
            indicator.checkCanceled();
            RefactorHelper.exportTempImage(project, information);
            indicator.setFraction((float) (i + 1) / (float) imageInformationList.size());
        }

        UIUtil.invokeLaterIfNeeded(new DumbAwareRunnable() {
            public void run() {
                try {
                    RefactorHelper.move(project, imageInformationList);
                    LocalFileSystem.getInstance().refresh(true);
                } catch (IOException ignored) {
                }
            }
        });
    }

    public void addImage(ImageInformation imageInformation) {
        if (imageInformation == null) {
            return;
        }
        imageInformationList.add(imageInformation);
    }

    @Override
    public void run(@NotNull final ProgressIndicator progressIndicator) {
        if (imageInformationList.size() == 0) {
            return;
        }
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                try {
                    refactor(progressIndicator);
                } catch (ProcessCanceledException ignored) {
                } catch (IOException ignored) {
                }
            }
        });
    }
}
