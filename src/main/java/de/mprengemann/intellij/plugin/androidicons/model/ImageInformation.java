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
import de.mprengemann.intellij.plugin.androidicons.images.ResizeAlgorithm;
import org.imgscalr.Scalr;

import java.io.File;

public class ImageInformation {

    public static final String TARGET_FILE_PATTERN = "%s/drawable-%s/%s.%s";
    public static final String TMP_ROOT_DIR = "plugin-images";
    private final File imageFile;
    private final Resolution resolution;
    private final float factor;
    private final String exportPath;
    private final Format format;
    private final String exportName;
    private final boolean ninePatch;
    private ResizeAlgorithm algorithm;
    private Object method;

    private ImageInformation(File imageFile,
                             Resolution resolution,
                             float factor,
                             String exportPath,
                             Format format,
                             String exportName,
                             boolean isNinePatch,
                             ResizeAlgorithm algorithm,
                             Object method) {
        this.imageFile = imageFile;
        this.resolution = resolution;
        this.factor = factor;
        this.exportPath = exportPath;
        this.format = format;
        this.exportName = exportName;
        this.ninePatch = isNinePatch;
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
        return new File(getTempDir(), String.format("%s/%s", resolution.toString().toLowerCase(), exportName));
    }

    public File getImageFile() {
        return imageFile;
    }

    public Resolution getResolution() {
        return resolution;
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

    public File getTargetFile() {
        return new File(String.format(TARGET_FILE_PATTERN,
                                      exportPath,
                                      resolution.toString().toLowerCase(),
                                      exportName,
                                      format.toString().toLowerCase()));
    }

    public static class Builder {

        private File imageFile = null;
        private String exportPath = null;
        private String exportName = null;
        private float factor = 1f;

        // Optional parameters
        private Resolution resolution = Resolution.XHDPI;
        private boolean ninePatch = false;
        private ResizeAlgorithm algorithm = ResizeAlgorithm.SCALR;
        private Object method = Scalr.Method.AUTOMATIC;
        private Format format = Format.PNG;

        private Builder() {
        }

        private Builder(ImageInformation imageInformation) {
            this.imageFile = imageInformation.imageFile;
            this.resolution = imageInformation.resolution;
            this.factor = imageInformation.factor;
            this.exportPath = imageInformation.exportPath;
            this.exportName = imageInformation.exportName;
            this.ninePatch = imageInformation.ninePatch;
            this.algorithm = imageInformation.algorithm;
            this.method = imageInformation.method;
            this.format = imageInformation.format;
        }

        public Builder setExportName(String exportName) {
            this.exportName = exportName;
            return this;
        }

        public Builder setExportPath(String exportPath) {
            this.exportPath = exportPath;
            return this;
        }

        public Builder setResolution(Resolution resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder setFactor(float factor) {
            this.factor = factor;
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

        public ImageInformation build() {
            return new ImageInformation(this.imageFile,
                                        this.resolution,
                                        this.factor,
                                        this.exportPath,
                                        this.format,
                                        this.exportName,
                                        this.ninePatch,
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

        public Resolution getResolution() {
            return resolution;
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
    }
}