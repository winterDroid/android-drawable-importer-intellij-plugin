package de.mprengemann.intellij.plugin.androidicons.ui;

import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

import javax.swing.*;

public class ResolutionButtonModel extends JToggleButton.ToggleButtonModel {

    private Resolution resolution;

    public ResolutionButtonModel(Resolution resolution) {
        this.resolution = resolution;
    }

    public Resolution getResolution() {
        return resolution;
    }
}
