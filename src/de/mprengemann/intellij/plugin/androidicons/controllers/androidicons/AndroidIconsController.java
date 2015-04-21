package de.mprengemann.intellij.plugin.androidicons.controllers.androidicons;

import java.util.HashSet;
import java.util.Set;

public class AndroidIconsController implements IAndroidIconsController {

    private Set<AndroidIconsObserver> observerSet;

    public AndroidIconsController() {
        observerSet = new HashSet<AndroidIconsObserver>();
    }

    @Override
    public void addObserver(AndroidIconsObserver observer) {
        observerSet.add(observer);
    }

    @Override
    public void removeObserver(AndroidIconsObserver observer) {
        observerSet.remove(observer);
    }

    @Override
    public void tearDown() {
        observerSet.clear();
        observerSet = null;
    }
}
