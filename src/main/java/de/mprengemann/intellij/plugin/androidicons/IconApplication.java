package de.mprengemann.intellij.plugin.androidicons;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.components.ApplicationComponent;
import de.mprengemann.intellij.plugin.androidicons.controllers.DefaultControllerFactory;
import de.mprengemann.intellij.plugin.androidicons.controllers.IControllerFactory;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
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

        final InputStream contentStream = getClass().getResourceAsStream("assets/content.json");
        final Reader inputStream = new InputStreamReader(contentStream);
        final Type listType = new TypeToken<ArrayList<IconPack>>() {}.getType();
        final List<IconPack> iconPacks = new Gson().fromJson(inputStream, listType);
    }

    public void disposeComponent() {
        controllerFactory.tearDown();
        controllerFactory = null;
    }

    public IControllerFactory getControllerFactory() {
        return controllerFactory;
    }
}