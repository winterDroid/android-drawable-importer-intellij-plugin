package de.mprengemann.intellij.plugin.androidicons.resources;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class ResourceLoader {

    static ResourceLoader rl = new ResourceLoader();

    public static InputStream openStream(String file) {
        return rl.getClass().getResourceAsStream(getAssetPath(file));
    }

    public static File getFile(String file) {
        final URL resource = rl.getClass().getResource(getAssetPath(file));
        return resource != null ? new File(resource.getFile()) : null;
    }

    @NotNull
    private static String getAssetPath(String file) {
        return new File("/assets", file).getAbsolutePath();
    }
}
