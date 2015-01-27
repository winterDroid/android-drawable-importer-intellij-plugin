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

package de.mprengemann.intellij.plugin.androidicons.settings;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import de.mprengemann.intellij.plugin.androidicons.images.IconPack;

public class SettingsHelper {
    private static final String PATH = "assetPath_%s";
    private static final String RES_ROOT = "resourcesRoot";
    private static final String LAST_FOLDER_ROOT = "lastFolderRoot";
    public static final String LEGACY_PATH = "assetPath";

    public static VirtualFile getAssetPath(IconPack iconPack) {
        String persistedFile = getAssetPathString(iconPack);
        if (persistedFile != null) {
            return VirtualFileManager.getInstance().findFileByUrl(persistedFile);
        } else {
            return null;
        }
    }

    public static void saveAssetPath(IconPack iconPack, String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(getAssetPathKey(iconPack), fileUrl);
    }

    public static void saveAssetPath(IconPack iconPack, VirtualFile file) {
        saveAssetPath(iconPack, file != null ? file.getUrl() : null);
    }

    public static String getAssetPathString(IconPack iconPack) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        if (propertiesComponent.getValue(LEGACY_PATH) != null && iconPack == IconPack.ANDROID_ICONS) {
            // Legacy support
            String file = propertiesComponent.getValue(LEGACY_PATH);
            propertiesComponent.unsetValue(getAssetPathKey(iconPack));
            saveAssetPath(IconPack.ANDROID_ICONS, file);
            return file;
        }
        return propertiesComponent.getValue(getAssetPathKey(iconPack));
    }

    private static String getAssetPathKey(IconPack iconPack) {
        return String.format(PATH, iconPack.toString());
    }

    public static void clearAssetPath(IconPack iconPack) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.unsetValue(getAssetPathKey(iconPack));
    }

    public static void saveResRootForProject(Project project, String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(RES_ROOT, fileUrl);
    }

    public static VirtualFile getResRootForProject(Project project) {
        String persistedFile = getResRootStringForProject(project);
        if (persistedFile != null) {
            return VirtualFileManager.getInstance().findFileByUrl(persistedFile);
        } else {
            return null;
        }
    }

    public static String getResRootStringForProject(Project project) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        return propertiesComponent.getValue(RES_ROOT);
    }

    public static String getLastImageFolder(Project project) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        return propertiesComponent.getValue(LAST_FOLDER_ROOT);
    }

    public static void saveLastImageFolder(Project project, String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(LAST_FOLDER_ROOT, fileUrl);
    }
}
