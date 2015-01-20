package de.mprengemann.intellij.plugin.androidicons.util;

import org.imgscalr.Scalr;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public enum ResizeAlgorithm {
    SCALR("Scalr"),
    GRAPHICS("Graphics"),
    IMAGE("Image"),
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
            case GRAPHICS:
                methods = Arrays.asList("Default",
                                        "Bilinear",
                                        "Bicubic",
                                        "Nearest neighbor");
                break;
            case IMAGE:
                methods = Arrays.asList("Default",
                                        "Smooth",
                                        "Fast",
                                        "Replicate",
                                        "Area averaging");
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
            case GRAPHICS:
                if ("Bilinear".equals(method)) {
                    return RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                } else if ("Bicubic".equals(method)) {
                    return RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                } else if ("Nearest neighbor".equals(method)) {
                    return RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                } else if ("Default".equals(method)) {
                    return null;
                }
                break;
            case IMAGE:
                if ("Default".equals(method)) {
                    return Image.SCALE_DEFAULT;
                } else if ("Smooth".equals(method)) {
                    return Image.SCALE_SMOOTH;
                } else if ("Fast".equals(method)) {
                    return Image.SCALE_FAST;
                } else if ("Replicate".equals(method)) {
                    return Image.SCALE_REPLICATE;
                } else if ("Area averaging".equals(method)) {
                    return Image.SCALE_AREA_AVERAGING;
                }
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
        } else if (GRAPHICS.name.equals(algorithm)) {
            return GRAPHICS;
        } else if (IMAGE.name.equals(algorithm)) {
            return IMAGE;
        } else if (THUMBNAILATOR.name.equals(algorithm)) {
            return THUMBNAILATOR;
        }
        throw new IllegalArgumentException("Algorithm doesn't exist: " + algorithm);
    }
}