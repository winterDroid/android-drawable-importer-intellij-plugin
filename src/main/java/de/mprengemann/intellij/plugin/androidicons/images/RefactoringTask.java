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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ui.UIUtil;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.util.ExportNameUtils;
import de.mprengemann.intellij.plugin.androidicons.util.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.util.RunnableUtils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RefactoringTask extends Task.Backgroundable {

    private static final String TAG = RefactoringTask.class.getSimpleName();
    private static final Logger LOGGER = Logger.getInstance(TAG);
    private Project project;
    private List<ImageInformation> imageInformationList = new ArrayList<ImageInformation>();
    private int selection;
    private ProgressIndicator progressIndicator;

    public RefactoringTask(Project project) {
        super(project, "Importing Images...", true);
        this.project = project;
    }

    private void refactor() throws IOException, ProcessCanceledException {
        progressIndicator.checkCanceled();
        progressIndicator.setIndeterminate(false);
        for (int i = 0; i < imageInformationList.size(); i++) {
            ImageInformation information = imageInformationList.get(i);
            progressIndicator.setText2(information.getExportName());
            progressIndicator.checkCanceled();
            exportTempImage(information);
            progressIndicator.setFraction((float) (i + 1) / (float) imageInformationList.size());
        }

        progressIndicator.setIndeterminate(true);
        progressIndicator.setText2("Finishing");
        UIUtil.invokeLaterIfNeeded(new DumbAwareRunnable() {
            public void run() {
                try {
                    move(project, imageInformationList);
                    LocalFileSystem.getInstance().refresh(true);
                } catch (IOException e) {
                    LOGGER.error(e);
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

    protected void onPreExecute() {}

    @Override
    public void run(@NotNull final ProgressIndicator progressIndicator) {
        if (imageInformationList.size() == 0) {
            return;
        }
        this.progressIndicator = progressIndicator;
        onPreExecute();
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                try {
                    refactor();
                } catch (ProcessCanceledException e) {
                    FileUtils.deleteQuietly(ImageInformation.getTempDir());
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        });
    }

    protected void onPostExecute() {}

    private boolean checkFileExist(@Nullable PsiDirectory targetDirectory,
                                   int[] choice,
                                   PsiFile file,
                                   String name,
                                   String title) {
        if (targetDirectory == null) {
            return false;
        }
        final PsiFile existing = targetDirectory.findFile(name);
        if (existing == null || existing.equals(file)) {
            return false;
        }
        int selection;
        if (choice == null || choice[0] == -1) {
            final String message = String.format("File '%s' already exists in directory '%s'",
                                           name,
                                           targetDirectory.getVirtualFile().getPath());
            String[] options = choice == null ? new String[] {"Overwrite", "Skip"}
                                              : new String[] {"Overwrite", "Skip", "Overwrite for all", "Skip for all"};
            selection = Messages.showDialog(message, title, options, 0, Messages.getQuestionIcon());
            if (selection == 2 || selection == 3) {
                this.selection = selection;
            }
        } else {
            selection = choice[0];
        }

        if (choice != null && selection > 1) {
            choice[0] = selection % 2;
            selection = choice[0];
        }

        if (selection == 0 && file != existing) {
            existing.delete();
        } else {
            return true;
        }

        return false;
    }

    private void copy(Project project,
                      String description,
                      List<File> sources,
                      List<File> targets) throws IOException {
        final PsiManager instance = PsiManager.getInstance(project);
        final List<PsiFile> files = new ArrayList<PsiFile>();
        final List<PsiDirectory> dirs = new ArrayList<PsiDirectory>();
        final List<String> names = new ArrayList<String>();

        for (File source : sources) {
            LocalFileSystem.getInstance().findFileByIoFile(source);
            final VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(source);
            if (vFile != null) {
                PsiFile item = instance.findFile(vFile);
                if (item != null) {
                    files.add(item);
                }
            }
        }

        for (File target : targets) {
            VirtualFile targetDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(target.getParentFile());
            if (targetDir == null) {
                FileUtils.forceMkdir(target.getParentFile());
                targetDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(target.getParentFile());
            }
            if (targetDir != null) {
                final PsiDirectory dir = instance.findDirectory(targetDir);
                if (dir != null) {
                    dirs.add(dir);
                    names.add(target.getName());
                }
            }
        }

        if (files.size() == dirs.size() && files.size() > 0) {
            selection = -1;
            RunnableUtils.runWriteCommand(project, new Runnable() {
                @Override
                public void run() {
                    PsiFile file;
                    PsiDirectory dir;
                    String name;
                    for (int i = 0; i < files.size(); i++) {
                        file = files.get(i);
                        dir = dirs.get(i);
                        name = names.get(i);
                        if (checkFileExist(dir, new int[] {selection}, file, name, "Copy")) {
                            continue;
                        }
                        dir.copyFileFrom(name, file);
                    }
                }
            }, description);
            return;
        }

        throw new FileNotFoundException(sources.toString());
    }

    private void move(Project project, List<ImageInformation> scalingInformationList) throws IOException {
        List<File> tempFiles = new ArrayList<File>();
        List<File> targets = new ArrayList<File>();

        for (ImageInformation information : scalingInformationList) {
            tempFiles.add(information.getTempImage());
            targets.add(information.getTargetFile());
        }

        final String description = ExportNameUtils.getExportDescription(scalingInformationList);
        copy(project, description, tempFiles, targets);
        RunnableUtils.runWriteCommand(project, new Runnable() {
            @Override
            public void run() {
                FileUtils.deleteQuietly(ImageInformation.getTempDir());
                onPostExecute();
            }
        }, description);
    }

    private void exportTempImage(final ImageInformation information) {
        try {
            BufferedImage resizeImageJpg;
            if (information.isNinePatch()) {
                resizeImageJpg = ImageUtils.resizeNinePatchImage(information);
            } else {
                resizeImageJpg = ImageUtils.resizeNormalImage(information);
            }
            ImageUtils.saveImageTempFile(resizeImageJpg, information);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
}
