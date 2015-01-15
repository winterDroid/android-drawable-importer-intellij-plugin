package de.mprengemann.intellij.plugin.androidicons.settings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;

public class AndroidIconsSettings implements Configurable {
    private static final String ANDROID_ICONS_URL = "http://www.androidicons.com/";
    private JPanel panel;
    private TextFieldWithBrowseButton textFieldHome;
    private JLabel foundColorsText;
    private JLabel foundAssetsText;
    private JButton openBrowser;

    private VirtualFile selectedFile;
    private String persistedFile;
    private boolean selectionPerformed = false;

    @Nullable
    @Override
    public JComponent createComponent() {
        persistedFile = SettingsHelper.getAssetPathString();
        if (persistedFile != null) {
            VirtualFile loadedFile = VirtualFileManager.getInstance().findFileByUrl(persistedFile);
            if (loadedFile != null) {
                textFieldHome.setText(loadedFile.getCanonicalPath());
                selectedFile = loadedFile;
            }
        }

        FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        String title = "Select res directory";
        workingDirectoryChooserDescriptor.setTitle(title);
        textFieldHome.addBrowseFolderListener(title, null, null, workingDirectoryChooserDescriptor);
        textFieldHome.addBrowseFolderListener(new TextBrowseFolderListener(workingDirectoryChooserDescriptor) {
            @Override
            protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                super.onFileChoosen(chosenFile);
                selectionPerformed = true;
                selectedFile = chosenFile;
                scanForAssets();
            }
        });
        openBrowser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                BrowserUtil.browse(ANDROID_ICONS_URL);
            }
        });

        scanForAssets();
        return panel;
    }

    private void scanForAssets() {
        int colorCount = 0;
        int assetCount = 0;
        if (this.selectedFile != null && this.selectedFile.getCanonicalPath() != null) {
            File assetRoot = new File(this.selectedFile.getCanonicalPath());
            final FilenameFilter systemFileNameFiler = new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return !s.startsWith(".");
                }
            };
            File[] colorDirs = assetRoot.listFiles(systemFileNameFiler);
            if (colorDirs != null) {
                for (File file : colorDirs) {
                    if (file.isDirectory()) {
                        colorCount++;
                    }
                }

                if (colorDirs.length >= 1) {
                    File exColorDir = colorDirs[0];
                    File[] densities = exColorDir.listFiles(systemFileNameFiler);
                    if (densities != null && densities.length >= 1) {
                        File exDensity = densities[0];
                        File[] assets = exDensity.listFiles(systemFileNameFiler);
                        if (assets != null) {
                            for (File asset : assets) {
                                if (!asset.isDirectory()) {
                                    String extension = asset.getName().substring(asset.getName().lastIndexOf(".") + 1);
                                    if (extension.equalsIgnoreCase("png")) {
                                        assetCount++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        foundColorsText.setText(colorCount + " colors");
        foundAssetsText.setText(assetCount + " drawables per color");
    }

    @Override
    public boolean isModified() {
        boolean isModified = false;

        if (selectionPerformed) {
            if (persistedFile == null || !persistedFile.equalsIgnoreCase(selectedFile.getUrl())) {
                isModified = true;
            }
        } else if (!TextUtils.isEmpty(persistedFile) && selectedFile == null) {
            isModified = true;
        }

        return isModified;
    }

    @Override
    public void apply() throws ConfigurationException {
        SettingsHelper.saveAssetPath(selectedFile);
        if (selectedFile != null) {
            persistedFile = selectedFile.getUrl();
            selectionPerformed = false;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void disposeUIResources() {
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Android Icons";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }
}
