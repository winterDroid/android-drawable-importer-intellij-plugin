package de.mprengemann.intellij.plugin.androidicons.resources;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class ResourceLoader {

    static ResourceLoader rl = new ResourceLoader();

    public static InputStream openStream(String file) {
        return rl.getClass().getResourceAsStream("/assets/" + file);
    }

    public static File getFile(String file) {
        final URL resource = rl.getClass().getResource("/assets/" + file);
        return new File(resource.getFile());
    }
}
