package de.mprengemann.intellij.plugin.androidicons.resources;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class ResourceLoader {

    private static final String TAG = ResourceLoader.class.getSimpleName();
    private static final Logger LOGGER = Logger.getInstance(TAG);

    static ResourceLoader rl = new ResourceLoader();

    public static File getExportPath() {
        final String exportPath = PathManager.getSystemPath();
        return new File(exportPath, "android-drawable-importer-intellij-plugin");
    }

    public static File getAssetResource(String file) {
        return new File(getExportPath(), file);
    }

    public static File getBundledResource(String file) {
        final URL resource = rl.getClass().getResource(getAssetPath(file));
        if (resource == null) {
            return null;
        }
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            LOGGER.error(e);
            return new File(resource.getPath());
        }
    }

    @NotNull
    private static String getAssetPath(String file) {
        return String.format("/assets/%s", file);
    }
}
