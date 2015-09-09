package de.mprengemann.intellij.plugin.androidicons.controllers;

public interface IController<T> {
    void addObserver(T observer);

    void removeObserver(T observer);

    void tearDown();
}
