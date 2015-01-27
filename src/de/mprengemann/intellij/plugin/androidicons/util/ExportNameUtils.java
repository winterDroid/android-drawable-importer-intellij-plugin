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

import de.mprengemann.intellij.plugin.androidicons.images.ImageInformation;
import org.apache.commons.io.FilenameUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExportNameUtils {
    
    public static String getExportNameFromFilename(String filename) {
        String exportName = FilenameUtils.removeExtension(filename);
        if (exportName.matches("[a-z0-9_.]*")) {
            return exportName;
        }
        exportName = exportName.toLowerCase();
        return exportName.replaceAll("([^(a-z0-9_.)])", "_");
    }

    public static String getExportDescription(List<ImageInformation> scalingInformationList) {
        Set<String> importFileNames = new HashSet<String>();
        for (ImageInformation information : scalingInformationList) {
            importFileNames.add(information.getImageFile().getName());
        }
        StringBuilder builder = new StringBuilder("Import of ");
        // No multi import
        if (importFileNames.size() == 1) {
            builder.append(importFileNames.iterator().next());
            if (scalingInformationList.size() == 1) {
                builder.append(" in resolution ");
            } else {
                builder.append(" in resolutions ");
            }
            for (Iterator<ImageInformation> iterator = scalingInformationList.iterator(); iterator.hasNext(); ) {
                ImageInformation information = iterator.next();
                builder.append(information.getResolution().getName());
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }
        } else {
            for (Iterator<String> iterator = importFileNames.iterator(); iterator.hasNext(); ) {
                String exportName = iterator.next();
                builder.append(exportName);
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(" as ")
                   .append(scalingInformationList.get(0).getExportName());
        }
        return builder.toString();
    }
}
