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
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

public class PluginSettings implements Configurable {
    private static final String ANDROID_ICONS_URL = "http://www.androidicons.com/";
    private static final String MATERIAL_ICONS_URL = "https://github.com/google/material-design-icons/releases";
    private static final List<String> BLACKLISTED_MATERIAL_ICONS_FOLDER = Arrays.asList("sprites",
                                                                                        "1x_ios",
                                                                                        "1x_web",
                                                                                        "2x_ios",
                                                                                        "2x_web",
                                                                                        "3x_ios",
                                                                                        "svg");
    private JPanel panel;
    private TextFieldWithBrowseButton androidIconsAssetHome;
    private JLabel androidIconsFoundDrawablesText;
    private JLabel androidIconsFoundColorsText;
    private JButton androidIconsOpenBrowser;
    private TextFieldWithBrowseButton materialIconsAssetHome;
    private JLabel materialIconsFoundDrawables;
    private JLabel materialIconsFoundCategories;
    private JButton materialIconsOpenBrowser;

    private VirtualFile selectedAndroidIconsFile;
    private VirtualFile selectedMaterialIconsFile;
    private String persistedAndroidIconsFile;
    private String persistedMaterialIconsFile;
    private boolean selectionPerformed = false;

    @Nullable
    @Override
    public JComponent createComponent() {
        persistedAndroidIconsFile = SettingsHelper.getAssetPathString(IconPack.ANDROID_ICONS);
        persistedMaterialIconsFile = SettingsHelper.getAssetPathString(IconPack.MATERIAL_ICONS);
        initAndroidIconsSettings();
        initMaterialIconsSettings();
        return panel;
    }

    private void initAndroidIconsSettings() {
        FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        if (persistedAndroidIconsFile != null) {
            VirtualFile loadedFile = VirtualFileManager.getInstance().findFileByUrl(persistedAndroidIconsFile);
            if (loadedFile != null) {
                androidIconsAssetHome.setText(loadedFile.getCanonicalPath());
                selectedAndroidIconsFile = loadedFile;
            }
        }
        String title = "Select res directory";
        workingDirectoryChooserDescriptor.setTitle(title);
        androidIconsAssetHome.addBrowseFolderListener(title, null, null, workingDirectoryChooserDescriptor);
        androidIconsAssetHome.addBrowseFolderListener(new TextBrowseFolderListener(workingDirectoryChooserDescriptor) {
            @Override
            @SuppressWarnings("deprecation") // Otherwise not compatible to AndroidStudio
            protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                super.onFileChoosen(chosenFile);
                selectionPerformed = true;
                selectedAndroidIconsFile = chosenFile;
                scanForAndroidIconsAssets();
            }
        });
        androidIconsOpenBrowser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                BrowserUtil.browse(ANDROID_ICONS_URL);
            }
        });
        scanForAndroidIconsAssets();
    }

    private void initMaterialIconsSettings() {
        FileChooserDescriptor workingDirectoryChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        if (persistedMaterialIconsFile != null) {
            VirtualFile loadedFile = VirtualFileManager.getInstance().findFileByUrl(persistedMaterialIconsFile);
            if (loadedFile != null) {
                materialIconsAssetHome.setText(loadedFile.getCanonicalPath());
                selectedMaterialIconsFile = loadedFile;
            }
        }
        String title = "Select res directory";
        workingDirectoryChooserDescriptor.setTitle(title);
        materialIconsAssetHome.addBrowseFolderListener(title, null, null, workingDirectoryChooserDescriptor);
        materialIconsAssetHome.addBrowseFolderListener(new TextBrowseFolderListener(workingDirectoryChooserDescriptor) {
            @Override
            @SuppressWarnings("deprecation") // Otherwise not compatible to AndroidStudio
            protected void onFileChoosen(@NotNull VirtualFile chosenFile) {
                super.onFileChoosen(chosenFile);
                selectionPerformed = true;
                selectedMaterialIconsFile = chosenFile;
                scanForMaterialIconsAssets();
            }
        });
        materialIconsOpenBrowser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                BrowserUtil.browse(MATERIAL_ICONS_URL);
            }
        });
        scanForMaterialIconsAssets();
    }

    private void scanForAndroidIconsAssets() {
        int colorCount = 0;
        int assetCount = 0;
        if (this.selectedAndroidIconsFile != null && this.selectedAndroidIconsFile.getCanonicalPath() != null) {
            File assetRoot = new File(this.selectedAndroidIconsFile.getCanonicalPath());
            final FilenameFilter folderFilter = new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return !s.startsWith(".") &&
                           new File(file, s).isDirectory() ;
                }
            };
            final FilenameFilter drawableFilter = new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return FilenameUtils.isExtension(s, "png") &&
                           !(new File(file, s).isDirectory());
                }
            };
            File[] colorDirs = assetRoot.listFiles(folderFilter);
            if (colorDirs != null) {
                colorCount = colorDirs.length;
                if (colorDirs.length >= 1) {
                    File exColorDir = colorDirs[0];
                    File[] densities = exColorDir.listFiles(folderFilter);
                    if (densities != null && densities.length >= 1) {
                        File exDensity = densities[0];
                        File[] assets = exDensity.listFiles(drawableFilter);
                        if (assets != null) {
                            assetCount = assets.length;
                        }
                    }
                }
            }
        }
        androidIconsFoundColorsText.setText(colorCount + " colors");
        androidIconsFoundDrawablesText.setText(assetCount + " drawables per color");
    }

    @Override
    public boolean isModified() {
        boolean isModified = false;

        if (selectionPerformed) {
            if (persistedAndroidIconsFile == null || !persistedAndroidIconsFile.equalsIgnoreCase(
                selectedAndroidIconsFile.getUrl())) {
                isModified = true;
            }
            if (persistedMaterialIconsFile == null || !persistedMaterialIconsFile.equalsIgnoreCase(
                selectedMaterialIconsFile.getUrl())) {
                isModified = true;
            }
        } else if (!TextUtils.isEmpty(persistedAndroidIconsFile) && selectedAndroidIconsFile == null) {
            isModified = true;
        } else if (!TextUtils.isEmpty(persistedMaterialIconsFile) && selectedMaterialIconsFile == null) {
            isModified = true;
        }

        return isModified;
    }

    @Override
    public void apply() throws ConfigurationException {
        SettingsHelper.saveAssetPath(IconPack.ANDROID_ICONS, selectedAndroidIconsFile);
        if (selectedAndroidIconsFile != null) {
            persistedAndroidIconsFile = selectedAndroidIconsFile.getUrl();
            selectionPerformed = false;
        }

        SettingsHelper.saveAssetPath(IconPack.MATERIAL_ICONS, selectedMaterialIconsFile);
        if (selectedMaterialIconsFile != null) {
            persistedMaterialIconsFile = selectedMaterialIconsFile.getUrl();
            selectionPerformed = false;
        }
    }

    private void scanForMaterialIconsAssets() {
        int categoriesCount = 0;
        int assetCount = 0;
        if (this.selectedMaterialIconsFile != null && this.selectedMaterialIconsFile.getCanonicalPath() != null) {
            File assetRoot = new File(this.selectedMaterialIconsFile.getCanonicalPath());
            final FilenameFilter drawableFileNameFiler = new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    if (!FilenameUtils.isExtension(s, "png")) {
                        return false;
                    }
                    String filename = FilenameUtils.removeExtension(s);
                    return filename.startsWith("ic_") &&
                           filename.endsWith("_black_48dp");
                }
            };
            final FilenameFilter folderFileNameFiler = new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return !s.startsWith(".") &&
                           new File(file, s).isDirectory() &&
                           !BLACKLISTED_MATERIAL_ICONS_FOLDER.contains(FilenameUtils.removeExtension(s));
                }
            };
            File[] categories = assetRoot.listFiles(folderFileNameFiler);
            if (categories != null) {
                categoriesCount = categories.length;
                if (categories.length >= 1) {
                    for (File category : categories) {
                        File[] densities = category.listFiles(folderFileNameFiler);
                        if (densities != null && densities.length >= 1) {
                            File exDensity = densities[0];
                            File[] assets = exDensity.listFiles(drawableFileNameFiler);
                            assetCount += assets != null ? assets.length : 0;
                        }
                    }
                }
            }
        }
        materialIconsFoundCategories.setText(categoriesCount + " categories");
        materialIconsFoundDrawables.setText(assetCount + " drawables");
    }

    @Override
    public void reset() {
        if (!isModified()) {
            return;
        }
        selectionPerformed = false;
        selectedAndroidIconsFile = null;
        selectedMaterialIconsFile = null;
        persistedAndroidIconsFile = null;
        persistedMaterialIconsFile = null;
        SettingsHelper.clearAssetPath(IconPack.ANDROID_ICONS);
        SettingsHelper.clearAssetPath(IconPack.MATERIAL_ICONS);
        androidIconsAssetHome.setText("");
        materialIconsAssetHome.setText("");
        scanForAndroidIconsAssets();
        scanForMaterialIconsAssets();
    }

    @Override
    public void disposeUIResources() {
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Android Drawable Importer";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }
}
