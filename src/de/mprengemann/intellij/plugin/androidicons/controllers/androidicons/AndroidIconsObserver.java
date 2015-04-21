package de.mprengemann.intellij.plugin.androidicons.controllers.androidicons;

import de.mprengemann.intellij.plugin.androidicons.images.IconPack;

public interface AndroidIconsObserver {
    void onInitialized(IconPack iconPack);
}
