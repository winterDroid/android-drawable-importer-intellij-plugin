package de.mprengemann.intellij.plugin.androidicons.controllers.iconimporter;

public interface IconsImporterObserver {
    void onIconPackChanged();

    void onCategoryChanged();

    void onAssetChanged();

    void onSizeChanged();

    void onColorChanged();

    void onExportNameChanged(String exportName);

    void onExportRootChanged(String exportRoot);
}
