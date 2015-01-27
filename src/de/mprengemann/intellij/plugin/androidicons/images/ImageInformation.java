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

package de.mprengemann.intellij.plugin.androidicons.images;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import org.imgscalr.Scalr;

import java.io.File;

public class ImageInformation {

    private static final int KEEP = -1;
    private static final float UNSET = -1f;

    private final File imageFile;
    private final Resolution resolution;
    private final float factor;
    private final int imageWidth;
    private final int imageHeight;
    private final int targetWidth;
    private final int targetHeight;
    private final String exportPath;
    private final String exportName;
    private final boolean ninePatch;
    private ResizeAlgorithm algorithm;
    private Object method;
    private String tempDir;

    private ImageInformation(File imageFile,
                             Resolution resolution,
                             float factor,
                             int imageWidth,
                             int imageHeight,
                             int targetWidth,
                             int targetHeight,
                             String exportPath,
                             String exportName,
                             boolean isNinePatch,
                             ResizeAlgorithm algorithm,
                             Object method, 
                             String tempDir) {
        this.imageFile = imageFile;
        this.resolution = resolution;
        this.factor = factor;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.exportPath = exportPath;
        this.exportName = exportName;
        this.ninePatch = isNinePatch;
        this.algorithm = algorithm;
        this.method = method;
        this.tempDir = tempDir;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(ImageInformation imageInformation) {
        return new Builder(imageInformation);
    }

    public File getTempImage() {
        return RefactorHelper.getTempImageFile(tempDir, resolution, exportName);
    }

    public File getTargetFile() {
        return ImageUtils.getTargetFile(this);
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

    public int getTargetWidth() {
        return targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
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

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public static class Builder {

        private File imageFile = null;
        private String exportPath = null;
        private String exportName = null;

        // Optional parameters
        private float factor = ImageInformation.UNSET;
        private Resolution resolution = Resolution.XHDPI;
        private int targetWidth = ImageInformation.KEEP;
        private int targetHeight = ImageInformation.KEEP;
        private boolean ninePatch = false;
        private ResizeAlgorithm algorithm = ResizeAlgorithm.SCALR;
        private Object method = Scalr.Method.AUTOMATIC;
        private int imageWidth = 0;
        private int imageHeight = 0;

        private Builder() {
        }

        private Builder(ImageInformation imageInformation) {
            this.imageFile = imageInformation.imageFile;
            this.resolution = imageInformation.resolution;
            this.factor = imageInformation.factor;
            this.imageWidth = imageInformation.imageWidth;
            this.imageHeight = imageInformation.imageHeight;
            this.targetWidth = imageInformation.targetWidth;
            this.targetHeight = imageInformation.targetHeight;
            this.exportPath = imageInformation.exportPath;
            this.exportName = imageInformation.exportName;
            this.ninePatch = imageInformation.ninePatch;
            this.algorithm = imageInformation.algorithm;
            this.method = imageInformation.method;
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

        public Builder setTargetWidth(int targetWidth) {
            this.targetWidth = targetWidth;
            return this;
        }

        public Builder setTargetHeight(int targetHeight) {
            this.targetHeight = targetHeight;
            return this;
        }

        public Builder setImageWidth(int imageWidth) {
            this.imageWidth = imageWidth;
            return this;
        }

        public Builder setImageHeight(int imageHeight) {
            this.imageHeight = imageHeight;
            return this;
        }

        public Builder setNinePatch(boolean ninePatch) {
            this.ninePatch = ninePatch;
            return this;
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

        public ImageInformation build(Project project) {
            VirtualFile workspaceFile = project.getWorkspaceFile();
            assert workspaceFile != null;
            VirtualFile ideaDir = workspaceFile.getParent();
            return new ImageInformation(this.imageFile,
                                        this.resolution,
                                        this.factor,
                                        this.imageWidth,
                                        this.imageHeight,
                                        this.targetWidth,
                                        this.targetHeight,
                                        this.exportPath,
                                        this.exportName,
                                        this.ninePatch,
                                        this.algorithm,
                                        this.method,
                                        ideaDir.getCanonicalPath());
        }
    }
}