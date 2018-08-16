/*
 * Copyright 2015 Marc Prengemann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package de.mprengemann.intellij.plugin.androidicons.model;

import com.intellij.openapi.application.PathManager;
import de.mprengemann.intellij.plugin.androidicons.controllers.defaults.DefaultsController;
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;

import java.io.File;
import java.util.Locale;

public class ImageInformation {

    public static final String TARGET_FILE_PATTERN = "%s/%s-%s/%s.%s";
    public static final String TMP_ROOT_DIR = "plugin-images";
    private final File imageFile;
    private final Resolution targetResolution;
    private final float factor;
    private final String exportPath;
    private final Format format;
    private final Destination destination;
    private final String exportName;
    private final boolean ninePatch;
    private final boolean vector;
    private ResizeAlgorithm algorithm;
    private Object method;

    private ImageInformation(File imageFile,
                             Resolution targetResolution,
                             float factor,
                             String exportPath,
                             Format format,
                             Destination destination,
                             String exportName,
                             boolean isNinePatch,
                             boolean isVector,
                             ResizeAlgorithm algorithm,
                             Object method) {
        this.imageFile = imageFile;
        this.targetResolution = targetResolution;
        this.factor = factor;
        this.exportPath = exportPath;
        this.format = format;
        this.destination = destination;
        this.exportName = exportName;
        this.ninePatch = isNinePatch;
        this.vector = isVector;
        this.algorithm = algorithm;
        this.method = method;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(ImageInformation imageInformation) {
        return new Builder(imageInformation);
    }

    public static File getTempDir() {
        return new File(PathManager.getPluginTempPath(), TMP_ROOT_DIR);
    }

    public File getTempImage() {
        return new File(getTempDir(), String.format("%s/%s",
                                                    targetResolution.toString().toLowerCase(Locale.ENGLISH),
                                                    exportName));
    }

    public File getImageFile() {
        return imageFile;
    }

    public Resolution getTargetResolution() {
        return targetResolution;
    }

    public float getFactor() {
        return factor;
    }

    public String getExportPath() {
        return exportPath;
    }

    public String getExportName() {
        return exportName;
    }

    public boolean isNinePatch() {
        return ninePatch;
    }

    public ResizeAlgorithm getAlgorithm() {
        return algorithm;
    }

    public Object getMethod() {
        return method;
    }

    public Format getFormat() {
        return format;
    }

    public Destination getDestination() {
        return destination;
    }

    public File getTargetFile() {
        return new File(String.format(TARGET_FILE_PATTERN,
                                      exportPath,
                                      destination.getFolderName(),
                                      targetResolution.toString().toLowerCase(Locale.ENGLISH),
                                      exportName,
                                      format.toString().toLowerCase()));
    }

    public boolean isVector() {
        return vector;
    }

    public static class Builder {

        private File imageFile = null;
        private String exportPath = null;
        private String exportName = null;
        private float factor = 1f;

        // Optional parameters
        private boolean ninePatch = false;
        private boolean vector = false;
        private Resolution targetResolution = Resolution.XHDPI;
        private ResizeAlgorithm algorithm = DefaultsController.DEFAULT_ALGORITHM;
        private Object method = DefaultsController.DEFAULT_ALGORITHM.getMethod(DefaultsController.DEFAULT_METHOD);
        private Format format = DefaultsController.DEFAULT_FORMAT;
        private Destination destination = DefaultsController.DEFAULT_DESTINATION;

        private Builder() {
        }

        private Builder(ImageInformation imageInformation) {
            this.imageFile = imageInformation.imageFile;
            this.targetResolution = imageInformation.targetResolution;
            this.factor = imageInformation.factor;
            this.exportPath = imageInformation.exportPath;
            this.exportName = imageInformation.exportName;
            this.ninePatch = imageInformation.ninePatch;
            this.vector = imageInformation.vector;
            this.algorithm = imageInformation.algorithm;
            this.method = imageInformation.method;
            this.format = imageInformation.format;
            this.destination = imageInformation.destination;
        }

        public Builder setExportName(String exportName) {
            this.exportName = exportName;
            return this;
        }

        public Builder setExportPath(String exportPath) {
            this.exportPath = exportPath;
            return this;
        }

        public Builder setTargetResolution(Resolution targetResolution) {
            this.targetResolution = targetResolution;
            return this;
        }

        public Builder setFactor(float factor) {
            this.factor = factor;
            return this;
        }

        public Builder setVector(boolean vector) {
            this.vector = vector;
            return this;
        }

        public Builder setNinePatch(boolean ninePatch) {
            this.ninePatch = ninePatch;
            return setFormat(format);
        }

        public Builder setAlgorithm(ResizeAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder setMethod(Object method) {
            this.method = method;
            return this;
        }

        public Builder setImageFile(File imageFile) {
            this.imageFile = imageFile;
            if (exportName == null) {
                exportName = imageFile.getName();
            }
            return this;
        }

        public Builder setFormat(Format format) {
            this.format = isNinePatch() ? Format.PNG : format;
            return this;
        }

        public Builder setDestination(Destination destination) {
            this.destination = destination;
            return this;
        }

        public ImageInformation build() {
            return new ImageInformation(this.imageFile,
                                        this.targetResolution,
                                        this.factor,
                                        this.exportPath,
                                        this.format,
                                        this.destination,
                                        this.exportName,
                                        this.ninePatch,
                                        this.vector,
                                        this.algorithm,
                                        this.method);
        }

        public File getImageFile() {
            return imageFile;
        }

        public String getExportPath() {
            return exportPath;
        }

        public String getExportName() {
            return exportName;
        }

        public float getFactor() {
            return factor;
        }

        public Resolution getTargetResolution() {
            return targetResolution;
        }

        public boolean isNinePatch() {
            return ninePatch;
        }

        public boolean isVector() {
            return vector;
        }

        public ResizeAlgorithm getAlgorithm() {
            return algorithm;
        }

        public Object getMethod() {
            return method;
        }

        public Format getFormat() {
            return format;
        }
    }
}