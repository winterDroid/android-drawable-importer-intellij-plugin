package de.mprengemann.intellij.plugin.androidicons.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.settings.SettingsHelper;
import de.mprengemann.intellij.plugin.androidicons.ui.ResourcesDialog;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * User: marcprengemann
 * Date: 08.04.14
 * Time: 14:20
 */
public class AndroidResourcesHelper {

  public static void getResRootFile(Project project, Module module, ResourcesDialog.ResourceSelectionListener listener) {
    AndroidFacet currentFacet = AndroidFacetUtils.getInstance(project, module);

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

  public static void initResourceBrowser(final Project project, Module module, final String title, final TextFieldWithBrowseButton browser) {
    final VirtualFile resRoot = SettingsHelper.getResRootForProject(project);

    if (resRoot == null) {
      getResRootFile(project, module, new ResourcesDialog.ResourceSelectionListener() {
        @Override
        public void onResourceSelected(VirtualFile resDir) {
          browser.setText(resDir.getCanonicalPath());
          SettingsHelper.saveResRootForProject(project, resDir.getUrl());
        }
      });
    } else {
      browser.setText(resRoot.getCanonicalPath());
    }

    FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    workingDirectoryChooserDescriptor.setTitle(title);
    browser.addBrowseFolderListener(title, null, project, workingDirectoryChooserDescriptor);
    browser.addBrowseFolderListener(new TextBrowseFolderListener(workingDirectoryChooserDescriptor) {
      @Override
      protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
        super.onFileChoosen(chosenFile);
        SettingsHelper.saveResRootForProject(project, chosenFile.getUrl());
      }
    });
  }
}
