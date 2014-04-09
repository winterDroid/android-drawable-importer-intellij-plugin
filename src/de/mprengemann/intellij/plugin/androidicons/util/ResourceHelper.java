package de.mprengemann.intellij.plugin.androidicons.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.copy.CopyFilesOrDirectoriesHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: marcprengemann
 * Date: 09.04.14
 * Time: 09:06
 */
public class ResourceHelper {

  public static void copy(Project project, File source, File target) {
    final List<PsiElement> elements = new ArrayList<PsiElement>();
    final PsiManager instance = PsiManager.getInstance(project);
    final VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(source);
    if (vFile != null) {
      PsiFileSystemItem item = instance.findFile(vFile);
      if (item != null) {
        elements.add(item);
      }
    }

    if (elements.size() > 0) {
      VirtualFile targetDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(target.getParentFile());
      if (targetDir != null) {
        final PsiDirectory dir = instance.findDirectory(targetDir);
        if (dir != null) {
          new CopyFilesOrDirectoriesHandler().doCopy(PsiUtilCore.toPsiElementArray(elements), dir);
        }
      }
    }
  }

}
