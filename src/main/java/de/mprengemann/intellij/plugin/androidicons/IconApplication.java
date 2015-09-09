package de.mprengemann.intellij.plugin.androidicons;

import com.intellij.openapi.components.ApplicationComponent;
import de.mprengemann.intellij.plugin.androidicons.controllers.DefaultControllerFactory;
import de.mprengemann.intellij.plugin.androidicons.controllers.IControllerFactory;
import org.jetbrains.annotations.NotNull;

public class IconApplication implements ApplicationComponent {

    private IControllerFactory controllerFactory;

    @NotNull
    public String getComponentName() {
        return getClass().getName();
    }

    public void initComponent() {
        controllerFactory = new DefaultControllerFactory();
    }

    public void disposeComponent() {
        controllerFactory.tearDown();
        controllerFactory = null;
    }

    public IControllerFactory getControllerFactory() {
        return controllerFactory;
    }
}