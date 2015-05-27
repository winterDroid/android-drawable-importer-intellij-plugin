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

package de.mprengemann.intellij.plugin.androidicons.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import de.mprengemann.intellij.plugin.androidicons.model.ImageInformation;
import de.mprengemann.intellij.plugin.androidicons.images.ImageUtils;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import javax.activation.MimetypesFileTypeMap;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RefactorHelper {

    private static int selection;

    public static void copy(Project project, String description, List<File> sources, List<File> targets) throws IOException {
        final PsiManager instance = PsiManager.getInstance(project);
        final List<PsiFile> files = new ArrayList<PsiFile>();
        final List<PsiDirectory> dirs = new ArrayList<PsiDirectory>();
        final List<String> names = new ArrayList<String>();

        for (File source : sources) {
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(source);
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
            RunnableHelper.runWriteCommand(project, new Runnable() {
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

        throw new IOException("File not found. No idea why.");
    }

    public static boolean checkFileExist(@Nullable PsiDirectory targetDirectory,
                                         int[] choice,
                                         PsiFile file,
                                         String name,
                                         String title) {
        if (targetDirectory == null) {
            return false;
        }
        final PsiFile existing = targetDirectory.findFile(name);
        if (existing != null && !existing.equals(file)) {
            int selection;
            if (choice == null || choice[0] == -1) {
                String message = String.format("File '%s' already exists in directory '%s'",
                                               name,
                                               targetDirectory.getVirtualFile().getPath());
                String[] options = choice == null ? new String[] {"Overwrite", "Skip"}
                                                  : new String[] {"Overwrite", "Skip", "Overwrite for all", "Skip for all"};
                selection = Messages.showDialog(message, title, options, 0, Messages.getQuestionIcon());
                if (selection == 2 || selection == 3) {
                    RefactorHelper.selection = selection;
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
        }

        return false;
    }

    public static File getTempImageFile(String tempDir, Resolution resolution, String exportName) {
        return new File(tempDir + "/plugin-images/" + resolution.toString() + "/" + exportName);
    }

    public static void move(Project project, final String description, final List<File> sources, final List<File> targets) throws IOException {
        copy(project, description, sources, targets);
        RunnableHelper.runWriteCommand(project, new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = null;
                    for (File source : sources) {
                        if (dir == null) {
                            dir = source.getParentFile();
                        }
                        FileUtils.forceDelete(source);
                    }

                    if (dir != null) {
                        if (dir.isDirectory()) {
                            File[] images = dir.listFiles(new FilenameFilter() {
                                @Override
                                public boolean accept(File file, String s) {
                                    String mimetype = new MimetypesFileTypeMap().getContentType(file);
                                    String type = mimetype.split("/")[0];
                                    return type.equals("image");
                                }
                            });
                            if (images == null || images.length == 0) {
                                FileUtils.forceDelete(dir);
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }, description);
    }

    public static void move(Project project, List<ImageInformation> scalingInformationList) throws IOException {
        List<File> tempFiles = new ArrayList<File>();
        List<File> targets = new ArrayList<File>();

        for (ImageInformation information : scalingInformationList) {
            tempFiles.add(information.getTempImage());
            targets.add(information.getTargetFile());
        }
        
        String description = ExportNameUtils.getExportDescription(scalingInformationList);

        move(project, description, tempFiles, targets);
    }

    public static float getScaleFactor(Resolution target, Resolution baseLine) {
        switch (baseLine) {
            case MDPI:
                switch (target) {
                    case LDPI:
                        return 0.5f;
                    case MDPI:
                        return 1f;
                    case HDPI:
                        return 1.5f;
                    case XHDPI:
                        return 2f;
                    case XXHDPI:
                        return 3f;
                    case XXXHDPI:
                        return 4f;
                }
                break;
            case LDPI:
                switch (target) {
                    case LDPI:
                        return 2f * 0.5f;
                    case MDPI:
                        return 2f * 1f;
                    case HDPI:
                        return 2f * 1.5f;
                    case XHDPI:
                        return 2f * 2f;
                    case XXHDPI:
                        return 2f * 3f;
                    case XXXHDPI:
                        return 2f * 4f;
                }
                break;
            case HDPI:
                switch (target) {
                    case LDPI:
                        return 2f / 3f * 0.5f;
                    case MDPI:
                        return 2f / 3f * 1f;
                    case HDPI:
                        return 2f / 3f * 1.5f;
                    case XHDPI:
                        return 2f / 3f * 2f;
                    case XXHDPI:
                        return 2f / 3f * 3f;
                    case XXXHDPI:
                        return 2f / 3f * 4f;
                }
                break;
            case XHDPI:
                switch (target) {
                    case LDPI:
                        return 1f / 2f * 0.5f;
                    case MDPI:
                        return 1f / 2f * 1f;
                    case HDPI:
                        return 1f / 2f * 1.5f;
                    case XHDPI:
                        return 1f / 2f * 2f;
                    case XXHDPI:
                        return 1f / 2f * 3f;
                    case XXXHDPI:
                        return 1f / 2f * 4f;
                }
                break;
            case XXHDPI:
                switch (target) {
                    case LDPI:
                        return 1f / 3f * 0.5f;
                    case MDPI:
                        return 1f / 3f * 1f;
                    case HDPI:
                        return 1f / 3f * 1.5f;
                    case XHDPI:
                        return 1f / 3f * 2f;
                    case XXHDPI:
                        return 1f / 3f * 3f;
                    case XXXHDPI:
                        return 1f / 3f * 4f;
                }
                break;
            case XXXHDPI:
                switch (target) {
                    case LDPI:
                        return 1f / 4f * 0.5f;
                    case MDPI:
                        return 1f / 4f * 1f;
                    case HDPI:
                        return 1f / 4f * 1.5f;
                    case XHDPI:
                        return 1f / 4f * 2f;
                    case XXHDPI:
                        return 1f / 4f * 3f;
                    case XXXHDPI:
                        return 1f / 4f * 4f;
                }
                break;
        }
        throw new IllegalArgumentException();
    }

    public static void exportTempImage(Project project, ImageInformation information) {
        try {
            BufferedImage resizeImageJpg;
            if (information.isNinePatch()) {
                resizeImageJpg = ImageUtils.resizeNinePatchImage(project, information);
            } else {
                resizeImageJpg = ImageUtils.resizeNormalImage(information);
            }
            ImageUtils.saveImageTempFile(resizeImageJpg, information);

        } catch (Exception ignored) {
        }
    }
}
