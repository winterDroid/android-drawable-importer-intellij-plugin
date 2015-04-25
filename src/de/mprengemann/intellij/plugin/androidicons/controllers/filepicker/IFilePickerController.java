package de.mprengemann.intellij.plugin.androidicons.controllers.filepicker;

import com.intellij.openapi.vfs.VirtualFile;

public interface IFilePickerController {
    void openFileChooser();

    void openFileChooser(VirtualFile root);

    void tearDown();
}
