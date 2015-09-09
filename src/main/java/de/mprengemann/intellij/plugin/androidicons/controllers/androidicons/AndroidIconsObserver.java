package de.mprengemann.intellij.plugin.androidicons.controllers.androidicons;

import de.mprengemann.intellij.plugin.androidicons.model.IconPack;

public interface AndroidIconsObserver {
    void updated(IconPack iconPack);
}
