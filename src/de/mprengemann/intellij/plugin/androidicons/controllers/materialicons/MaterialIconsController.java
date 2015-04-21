package de.mprengemann.intellij.plugin.androidicons.controllers.materialicons;

import java.util.HashSet;
import java.util.Set;

public class MaterialIconsController implements IMaterialIconsController {

    private Set<MaterialIconsObserver> observerSet;

    public MaterialIconsController() {
        observerSet = new HashSet<MaterialIconsObserver>();
    }

    @Override
    public void addObserver(MaterialIconsObserver observer) {
        observerSet.add(observer);
    }

    @Override
    public void removeObserver(MaterialIconsObserver observer) {
        observerSet.remove(observer);
    }

    @Override
    public void tearDown() {
        observerSet.clear();
        observerSet = null;
    }
}
