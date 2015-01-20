package de.mprengemann.intellij.plugin.androidicons.util;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by marcprengemann on 20/01/15.
 */
public class ExportNameUtils {
    
    public static String getExportNameFromFilename(String filename) {
        String exportName = FilenameUtils.removeExtension(filename);
        if (exportName.matches("[a-z0-9_.]*")) {
            return exportName;
        }
        exportName = exportName.toLowerCase();
        return exportName.replaceAll("([^(a-z0-9_.)])", "_");
    }
    
}
