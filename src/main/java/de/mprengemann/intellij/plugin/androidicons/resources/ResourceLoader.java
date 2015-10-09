package de.mprengemann.intellij.plugin.androidicons.resources;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;

public class ResourceLoader {

    static ResourceLoader rl = new ResourceLoader();

    public static File getFile(String file) {
        final URL resource = rl.getClass().getResource(getAssetPath(file));
        return resource != null ? new File(resource.getFile()) : null;
    }

    @NotNull
    private static String getAssetPath(String file) {
        return new File("/assets", file).getAbsolutePath();
    }
}
