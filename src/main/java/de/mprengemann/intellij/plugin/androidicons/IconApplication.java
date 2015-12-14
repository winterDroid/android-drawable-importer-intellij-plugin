package de.mprengemann.intellij.plugin.androidicons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.util.io.ZipUtil;
import de.mprengemann.intellij.plugin.androidicons.controllers.DefaultControllerFactory;
import de.mprengemann.intellij.plugin.androidicons.controllers.IControllerFactory;
import de.mprengemann.intellij.plugin.androidicons.model.IconPack;
import de.mprengemann.intellij.plugin.androidicons.model.Resolution;
import de.mprengemann.intellij.plugin.androidicons.resources.ResourceLoader;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class IconApplication implements ApplicationComponent {

    private static final String TAG = IconApplication.class.getSimpleName();
    private static final Logger LOGGER = Logger.getInstance(TAG);
    private IControllerFactory controllerFactory;

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getName();
    }

    @Override
    public void initComponent() {
        IconPack androidIcons = null;
        IconPack materialIcons = null;
        try {
            final InputStream bundledIconPackPropertiesFile = ResourceLoader.getBundledResourceStream("icon_packs.properties");
            final Properties bundledIconPackProperties = new Properties();
            bundledIconPackProperties.load(bundledIconPackPropertiesFile);

            final boolean export;
            final File localIconPackPropertiesFile = new File(ResourceLoader.getExportPath(), "icon_packs.properties");
            if (!localIconPackPropertiesFile.exists()) {
                export = true;
            } else {
                final Properties localIconPackProperties = new Properties();
                localIconPackProperties.load(FileUtils.openInputStream(localIconPackPropertiesFile));

                export = Integer.parseInt(bundledIconPackProperties.getProperty("version")) !=
                         Integer.parseInt(localIconPackProperties.getProperty("version"));
            }

            final File contentFile = ResourceLoader.getBundledResource("content.json");
            assert contentFile != null;
            final FileReader fileReader = new FileReader(contentFile);
            final Type listType = new TypeToken<ArrayList<IconPack>>() {}.getType();
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Resolution.class, new Resolution.Deserializer());
            final Gson gson = gsonBuilder.create();
            final List<IconPack> iconPacks = gson.fromJson(fileReader, listType);
            androidIcons = iconPacks.get(0);
            materialIcons = iconPacks.get(1);

            if (export) {
                new Task.Modal(null, "Prepare Android Drawable Importer", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        progressIndicator.setIndeterminate(true);
                        final File archiveFile = ResourceLoader.getBundledResource("icon_packs.zip");
                        final File bundledResource = ResourceLoader.getBundledResource("icon_packs.properties");
                        final File localResource = new File(ResourceLoader.getExportPath(), "icon_packs.properties");

                        try {
                            assert archiveFile != null;
                            ZipUtil.extract(archiveFile, ResourceLoader.getExportPath(), null, true);
                            assert bundledResource != null;
                            FileUtils.copyFile(bundledResource, localResource);
                        } catch (IOException e) {
                            LOGGER.error(e);
                        }
                    }
                }.queue();
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }

        controllerFactory = new DefaultControllerFactory(androidIcons, materialIcons);
    }

    @Override
    public void disposeComponent() {
        controllerFactory.tearDown();
        controllerFactory = null;
    }

    public IControllerFactory getControllerFactory() {
        return controllerFactory;
    }
}