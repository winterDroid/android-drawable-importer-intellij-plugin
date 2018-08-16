package de.mprengemann.intellij.plugin.androidicons.controllers.settings;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Base64;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import de.mprengemann.intellij.plugin.androidicons.model.Destination;
import de.mprengemann.intellij.plugin.androidicons.model.Format;
import de.mprengemann.intellij.plugin.androidicons.model.ImageAsset;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SettingsController implements ISettingsController {

    private static final String RES_ROOT = "resourcesRoot";
    private static final String LAST_FOLDER_ROOT = "lastFolderRoot";
    private static final String RESOLUTIONS = "resolutions";
    private static final String SOURCE_RESOLUTION = "sourceResolution";
    private static final String ALGORITHM = "algorithm";
    private static final String METHOD = "method";
    private static final String COLOR = "color";
    private static final String SIZE = "size";
    private static final String ASSET = "asset";
    private static final String FORMAT = "format";
    private static final String DESTINATION = "destination";

    private Set<SettingsObserver> observerSet;
    private Project project;

    public SettingsController() {
        observerSet = new HashSet<SettingsObserver>();
    }

    @Override
    public void addObserver(SettingsObserver observer) {
        observerSet.add(observer);
    }

    @Override
    public void removeObserver(SettingsObserver observer) {
        observerSet.remove(observer);
    }

    @Override
    public void saveResRootForProject(String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(RES_ROOT, fileUrl);
    }

    @Override
    public VirtualFile getResourceRoot() {
        String persistedFile = getResourceRootPath();
        if (persistedFile != null) {
            return VirtualFileManager.getInstance().findFileByUrl(persistedFile);
        } else {
            return null;
        }
    }

    @Override
    public String getResourceRootPath() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        return propertiesComponent.getValue(RES_ROOT);
    }

    @Override
    public String getLastImageFolder() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        return propertiesComponent.getValue(LAST_FOLDER_ROOT);
    }

    @Override
    public void saveLastImageFolder(String fileUrl) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(LAST_FOLDER_ROOT, fileUrl);
    }

    @Override
    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void saveResolutions(Set<Resolution> resolutions) {
        String[] tmp = new String[resolutions.size()];
        int i=0;
        for (Iterator<Resolution> iterator = resolutions.iterator(); iterator.hasNext(); i++) {
            Resolution resolution = iterator.next();
            tmp[i] = resolution.toString();
        }
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValues(RESOLUTIONS, tmp);
    }

    @Override
    public Set<Resolution> getResolutions(Set<Resolution> defaultResolutions) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        String[] values = propertiesComponent.getValues(RESOLUTIONS);
        if (values == null) {
            return defaultResolutions;
        }
        Set<Resolution> resolutions = new HashSet<Resolution>();
        for (String value : values) {
            if (TextUtils.isEmpty(value)) {
                return defaultResolutions;
            }
            final Resolution resolution = Resolution.from(value);
            if (resolution == null) {
                return defaultResolutions;
            }
            resolutions.add(resolution);
        }
        return resolutions;
    }

    @Override
    public void saveSourceResolution(Resolution sourceResolution) {
        updateOrDelete(SOURCE_RESOLUTION, sourceResolution);
    }

    @Override
    public Resolution getSourceResolution(Resolution defaultResolution) {
        return Resolution.from(getOrDefault(SOURCE_RESOLUTION, defaultResolution.toString()));
    }

    @Override
    public void saveAlgorithm(ResizeAlgorithm algorithm) {
        updateOrDelete(ALGORITHM, algorithm);
    }

    @Override
    public ResizeAlgorithm getAlgorithm(ResizeAlgorithm defaultAlgorithm) {
        return ResizeAlgorithm.from(getOrDefault(ALGORITHM, defaultAlgorithm.toString()));
    }

    @Override
    public void saveMethod(String method) {
        updateOrDelete(METHOD, method);
    }

    @Override
    public String getMethod(String defaultMethod) {
        return getOrDefault(METHOD, defaultMethod);
    }

    @Override
    public void saveColor(String color) {
        updateOrDelete(COLOR, color);
    }

    @Override
    public String getColor() {
        return getOrDefault(COLOR, null);
    }

    @Override
    public void saveSize(String size) {
        updateOrDelete(SIZE, size);
    }

    @Override
    public String getSize() {
        return getOrDefault(SIZE, null);
    }

    @Override
    public void saveImageAsset(ImageAsset imageAsset) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        if (imageAsset == null) {
            propertiesComponent.unsetValue(ASSET);
            return;
        }
        String serializedImageAsset = null;
        ByteArrayOutputStream bo = null;
        ObjectOutputStream so = null;
        try {
            bo = new ByteArrayOutputStream();
            so = new ObjectOutputStream(bo);
            so.writeObject(imageAsset);
            so.flush();
            serializedImageAsset = Base64.encode(bo.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            StreamUtil.closeStream(so);
            StreamUtil.closeStream(bo);
        }
        if (serializedImageAsset == null) {
            propertiesComponent.unsetValue(ASSET);
            return;
        }
        propertiesComponent.setValue(ASSET, serializedImageAsset);
    }

    @Override
    public ImageAsset getImageAsset() {
        final String serializedImageAsset = getOrDefault(ASSET, null);
        if (serializedImageAsset == null) {
            return null;
        }
        ByteArrayInputStream bi = null;
        ObjectInputStream si = null;
        try {
            byte b[] = Base64.decode(serializedImageAsset);
            bi = new ByteArrayInputStream(b);
            si = new ObjectInputStream(bi);
            return (ImageAsset) si.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.closeStream(si);
            StreamUtil.closeStream(bi);
        }
        return null;
    }

    @Override
    public void saveFormat(Format format) {
        updateOrDelete(FORMAT, format);
    }

    @Override
    public Format getFormat(Format defaultFormat) {
        return Format.valueOf(getOrDefault(FORMAT, defaultFormat.toString()));
    }

    @Override
    public void saveDestination(Destination destination) {
        updateOrDelete(DESTINATION, destination);
    }

    @Override
    public Destination getDestination(Destination defaultDestination) {
        return Destination.valueOf(getOrDefault(DESTINATION, defaultDestination.toString()));
    }

    private String getOrDefault(String key, String defaultValue) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        if (!propertiesComponent.isValueSet(key)) {
            return defaultValue;
        }
        return propertiesComponent.getValue(key);
    }

    private void updateOrDelete(String key, Object value) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        if (value != null) {
            propertiesComponent.setValue(key, value.toString());
        } else {
            propertiesComponent.unsetValue(key);
        }
    }

    @Override
    public void tearDown() {
        project = null;
        observerSet.clear();
        observerSet = null;
    }
}
