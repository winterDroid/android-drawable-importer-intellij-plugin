package de.mprengemann.intellij.plugin.androidicons;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.components.ApplicationComponent;
import de.mprengemann.intellij.plugin.androidicons.controllers.DefaultControllerFactory;
import de.mprengemann.intellij.plugin.androidicons.controllers.IControllerFactory;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IconApplication implements ApplicationComponent {

    private IControllerFactory controllerFactory;

    @NotNull
    public String getComponentName() {
        return getClass().getName();
    }

    public void initComponent() {
        controllerFactory = new DefaultControllerFactory();

        final URL resourcesFolder = IconApplication.class.getResource("/resources");
        if (resourcesFolder == null) {
            return;
        }
        final File contentFile = new File(resourcesFolder.getFile(), "assets/content.json");
        InputStream contentStream = null;
        try {
            final Reader inputStream = new FileReader(contentFile);
            final Type listType = new TypeToken<ArrayList<IconPack>>() {}.getType();
            final List<IconPack> iconPacks = new Gson().fromJson(inputStream, listType);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void disposeComponent() {
        controllerFactory.tearDown();
        controllerFactory = null;
    }

    public IControllerFactory getControllerFactory() {
        return controllerFactory;
    }
}