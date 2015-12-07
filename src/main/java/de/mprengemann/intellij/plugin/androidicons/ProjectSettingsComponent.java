package de.mprengemann.intellij.plugin.androidicons;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import de.mprengemann.intellij.plugin.androidicons.controllers.IControllerFactory;
import org.jetbrains.annotations.NotNull;

public class ProjectSettingsComponent extends AbstractProjectComponent implements IProjectSettingsComponent {
    private IControllerFactory controllerFactory;

    protected ProjectSettingsComponent(Project project) {
        super(project);
    }

    @Override
    public void projectOpened() {
        this.controllerFactory.setProject(myProject);
    }

    @Override
    public void initComponent() {
        super.initComponent();
        final IconApplication iconApplication = ApplicationManager.getApplication().getComponent(IconApplication.class);
        this.controllerFactory = iconApplication.getControllerFactory();
    }

    @Override
    public void disposeComponent() {
        super.disposeComponent();
        this.controllerFactory = null;
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ProjectSettingsComponent";
    }
}
