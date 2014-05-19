package de.mprengemann.intellij.plugin.androidicons.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: marcprengemann
 * Date: 09.04.14
 * Time: 09:06
 */
public class RefactorHelper {


    private static int selection;

    public static void copy(Project project, List<File> sources, List<File> targets) throws IOException {
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
                        if (checkFileExist(dir, new int[]{selection}, file, name, "Copy")) {
                            continue;
                        }
                        dir.copyFileFrom(name, file);
                    }
                }
            });
            return;
        }

        throw new IOException("File not found. No idea why.");
    }

    public static boolean checkFileExist(@Nullable PsiDirectory targetDirectory, int[] choice, PsiFile file, String name, String title) {
        if (targetDirectory == null) {
            return false;
        }
        final PsiFile existing = targetDirectory.findFile(name);
        if (existing != null && !existing.equals(file)) {
            int selection;
            if (choice == null || choice[0] == -1) {
                String message = String.format("File '%s' already exists in directory '%s'", name, targetDirectory.getVirtualFile().getPath());
                String[] options = choice == null ? new String[]{"Overwrite", "Skip"}
                        : new String[]{"Overwrite", "Skip", "Overwrite for all", "Skip for all"};
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

    public static File getTempImageFile(Project project, String resolution, String exportName) {
        VirtualFile workspaceFile = project.getWorkspaceFile();
        if (workspaceFile != null) {
            VirtualFile ideaDir = workspaceFile.getParent();
            if (ideaDir != null) {
                return new File(ideaDir.getCanonicalPath() + "/plugin-images/" + resolution + "/" + exportName);
            }
        }
        return null;
    }

    public static void move(Project project, final List<File> sources, final List<File> targets) throws IOException {
        copy(project, sources, targets);
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
        });
    }
}
