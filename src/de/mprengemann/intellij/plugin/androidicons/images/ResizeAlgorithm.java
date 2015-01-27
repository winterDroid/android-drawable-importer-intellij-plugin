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

import org.imgscalr.Scalr;

import java.util.Arrays;
import java.util.List;

public enum ResizeAlgorithm {
    SCALR("Scalr"),
    THUMBNAILATOR("Thumbnailator");

    private final String name;

    private ResizeAlgorithm(String name) {
        this.name = name;
    }

    public List<String> getMethods() {
        List<String> methods;
        switch (this) {
            case SCALR:
                methods = Arrays.asList("Automatic",
                                        "Speed",
                                        "Balanced",
                                        "Quality",
                                        "Ultra Quality");
                break;
            case THUMBNAILATOR:
                methods = Arrays.asList("Default");
                break;
            default:
                throw new IllegalArgumentException();
        }
        return methods;
    }

    public Object getMethod(String method) {
        switch (this) {
            case SCALR:
                if ("Automatic".equals(method)) {
                    return Scalr.Method.AUTOMATIC;
                } else if ("Speed".equals(method)) {
                    return Scalr.Method.SPEED;
                } else if ("Balanced".equals(method)) {
                    return Scalr.Method.BALANCED;
                } else if ("Quality".equals(method)) {
                    return Scalr.Method.QUALITY;
                } else if ("Ultra Quality".equals(method)) {
                    return Scalr.Method.ULTRA_QUALITY;
                }
                break;
            case THUMBNAILATOR:
            default:
                return null;
        }
        throw new IllegalArgumentException("Method doesn't exist: " + method);
    }

    @Override
    public String toString() {
        return name;
    }

    public static ResizeAlgorithm from(String algorithm) {
        if (SCALR.name.equals(algorithm)) {
            return SCALR;
        } else if (THUMBNAILATOR.name.equals(algorithm)) {
            return THUMBNAILATOR;
        }
        throw new IllegalArgumentException("Algorithm doesn't exist: " + algorithm);
    }
}