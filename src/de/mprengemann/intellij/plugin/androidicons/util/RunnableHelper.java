package de.mprengemann.intellij.plugin.androidicons.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;

public class RunnableHelper {

    public static void runWriteCommand(Project project, Runnable cmd) {
        CommandProcessor.getInstance().executeCommand(project, new WriteAction(cmd), "Import drawables", project.getName());
    }

    public static class WriteAction implements Runnable {
        WriteAction(Runnable cmd) {
            this.cmd = cmd;
        }

        public void run() {
            ApplicationManager.getApplication().runWriteAction(cmd);
        }

        Runnable cmd;
    }

    private RunnableHelper() {
    }
}
