package de.mprengemann.intellij.plugin.androidicons.model;

import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

public enum Destination {
    DRAWABLE("drawable"),
    MIPMAP("mipmap");


    private final String folderName;

    Destination(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

}
