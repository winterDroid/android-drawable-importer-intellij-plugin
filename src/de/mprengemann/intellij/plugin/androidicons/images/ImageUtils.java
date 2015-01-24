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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import de.mprengemann.intellij.plugin.androidicons.forms.Wrong9PatchException;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

    public static final String TARGET_FILE_PATTERN = "%s/drawable-%s/%s.png";

    public static void updateImage(JLabel imageContainer, File imageFile) {
        if (!imageFile.exists()) {
            return;
        }
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (img == null) {
            return;
        }
        int imageWidth = img.getWidth();
        int imageHeight = img.getHeight();
        int imageViewWidth = imageContainer.getWidth();
        int imageViewHeight = imageContainer.getHeight();
        double factor = getScaleFactorToFit(new Dimension(imageWidth, imageHeight),
                                            new Dimension(imageViewWidth, imageViewHeight));
        factor = Math.min(factor, 1f);
        imageWidth = (int) (factor * imageWidth);
        imageHeight = (int) (factor * imageHeight);
        if (imageWidth <= 0 || imageHeight <= 0 ||
            imageViewWidth <= 0 || imageViewHeight <= 0) {
            return;
        }
        BufferedImage tmp = UIUtil.createImage(imageViewWidth, imageViewHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int x = (imageViewWidth - imageWidth) / 2;
        int y = (imageViewHeight - imageHeight) / 2;
        g2.drawImage(img, x, y, imageWidth, imageHeight, null);
        g2.dispose();
        imageContainer.setIcon(new ImageIcon(tmp));
    }

    public static double getScaleFactor(int iMasterSize, int iTargetSize) {
        if (iMasterSize == 0) {
            return 0d;
        }
        return (double) iTargetSize / (double) iMasterSize;
    }

    public static double getScaleFactorToFit(Dimension original, Dimension toFit) {
        double dScale = 1d;
        if (original != null && toFit != null) {
            double dScaleWidth = getScaleFactor(original.width, toFit.width);
            double dScaleHeight = getScaleFactor(original.height, toFit.height);
            dScale = Math.min(dScaleHeight, dScaleWidth);
        }
        return dScale;
    }

    public static BufferedImage resizeNormalImage(ImageInformation information) throws IOException {
        BufferedImage image = ImageIO.read(information.getImageFile());
        return resizeNormalImage(image, information);
    }

    private static BufferedImage resizeNormalImage(BufferedImage image,
                                                   ImageInformation information) throws IOException {
        int newWidth = information.getImageWidth();
        int newHeight = information.getImageHeight();
        if (newWidth <= 0 || newHeight <= 0) {
            newWidth = image.getWidth();
            newHeight = image.getHeight();
        }

        if (information.getFactor() >= 0) {
            newWidth = (int) (newWidth * information.getFactor());
            newHeight = (int) (newHeight * information.getFactor());
        }

        return resizeNormalImage(image, newWidth, newHeight, information);
    }

    private static BufferedImage resizeNormalImage(BufferedImage image,
                                                   int newWidth,
                                                   int newHeight,
                                                   ImageInformation information) throws IOException {
        if (information.getFactor() == 1f) {
            return image;
        }
        BufferedImage resizedImage = null;
        switch (information.getAlgorithm()) {
            case SCALR:
                Scalr.Method scalrMethod = (Scalr.Method) information.getMethod();
                resizedImage = Scalr.resize(image, scalrMethod, newWidth, newHeight, Scalr.OP_ANTIALIAS);
                break;
            case THUMBNAILATOR:
                return Thumbnails.of(image)
                                 .size(newWidth, newHeight)
                                 .asBufferedImage();
        }
        return resizedImage;
    }

    public static BufferedImage resizeNinePatchImage(Project project,
                                                     ImageInformation information) throws IOException {
        BufferedImage image = ImageIO.read(information.getImageFile());
        if (information.getFactor() == 1f) {
            return image;
        }

        int newWidth = information.getImageWidth();
        int newHeight = information.getImageHeight();
        if (newWidth <= 0 || newHeight <= 0) {
            newWidth = image.getWidth();
            newHeight = image.getHeight();
        }

        if (information.getFactor() >= 0) {
            newWidth = (int) (newWidth * information.getFactor());
            newHeight = (int) (newHeight * information.getFactor());
        }

        BufferedImage trimmedImage = trim9PBorder(image);
        ImageInformation trimmedImageInformation = ImageInformation.newBuilder(information)
                                                                  .setExportName(getExportName("trimmed", information.getExportName()))
                                                                  .build(project);
        saveImageTempFile(trimmedImage, trimmedImageInformation);
        trimmedImage = resizeNormalImage(trimmedImage, newWidth, newHeight, trimmedImageInformation);
        saveImageTempFile(trimmedImage, ImageInformation.newBuilder(trimmedImageInformation)
                                                        .setExportName(getExportName("trimmedResized", information.getExportName()))
                                                        .build(project));

        BufferedImage borderImage;

        int w = trimmedImage.getWidth();
        int h = trimmedImage.getHeight();

        try {
            borderImage = generateBordersImage(image, w, h);
        } catch (Exception e) {
            return null;
        }

        int[] rgbArray = new int[w * h];
        trimmedImage.getRGB(0, 0, w, h, rgbArray, 0, w);
        borderImage.setRGB(1, 1, w, h, rgbArray, 0, w);

        return borderImage;
    }

    public static String getExportName(String prefix, String name) {
        String exportName;
        if (!StringUtils.isEmpty(prefix.trim())) {
            exportName = prefix + "_" + name;
        } else {
            exportName = name;
        }
        return exportName;
    }

    private static BufferedImage trim9PBorder(BufferedImage inputImage) {
        BufferedImage trimedImage = UIUtil.createImage(
            inputImage.getWidth() - 2, inputImage.getHeight() - 2,
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = trimedImage.createGraphics();
        g.drawImage(inputImage, 0, 0, trimedImage.getWidth(),
                    trimedImage.getHeight(), 1, 1, inputImage.getWidth() - 1,
                    inputImage.getHeight() - 1, null);
        g.dispose();
        return trimedImage;
    }

    private static void enforceBorderColors(BufferedImage inputImage) {
        Graphics2D g = inputImage.createGraphics();
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(1, 1, inputImage.getWidth() - 2, inputImage.getHeight() - 2);
        g.dispose();
        int w = inputImage.getWidth();
        int h = inputImage.getHeight();
        int[] rgb = new int[w * h];

        inputImage.getRGB(0, 0, w, h, rgb, 0, w);

        for (int i = 0; i < rgb.length; i++) {
            if ((0xff000000 & rgb[i]) != 0) {
                rgb[i] = 0xff000000;
            }
        }
        inputImage.setRGB(0, 0, w, h, rgb, 0, w);
    }

    private static BufferedImage generateBordersImage(BufferedImage source,
                                                      int trimmedWidth,
                                                      int trimmedHeight) throws Wrong9PatchException, IOException {
        BufferedImage finalBorder = UIUtil.createImage(trimmedWidth + 2, trimmedHeight + 2, BufferedImage.TYPE_INT_ARGB);
        int cutW = source.getWidth() - 2;
        int cutH = source.getHeight() - 2;

        // left border
        BufferedImage leftBorder = UIUtil.createImage(1, cutH, BufferedImage.TYPE_INT_ARGB);
        leftBorder.setRGB(0, 0, 1, cutH,
                          source.getRGB(0, 1, 1, cutH, null, 0, 1), 0, 1);
        verifyBorderImage(leftBorder);
        leftBorder = resizeBorder(leftBorder, 1, trimmedHeight);
        finalBorder.setRGB(0, 1, 1, trimmedHeight,
                           leftBorder.getRGB(0, 0, 1, trimmedHeight, null, 0, 1), 0, 1);

        // right border
        BufferedImage rightBorder = UIUtil.createImage(1, cutH, BufferedImage.TYPE_INT_ARGB);
        rightBorder.setRGB(0, 0, 1, cutH,
                           source.getRGB(cutW + 1, 1, 1, cutH, null, 0, 1), 0, 1);
        verifyBorderImage(rightBorder);
        rightBorder = resizeBorder(rightBorder, 1, trimmedHeight);
        finalBorder.setRGB(trimmedWidth + 1, 1, 1, trimmedHeight, rightBorder
            .getRGB(0, 0, 1, trimmedHeight, null, 0, 1), 0, 1);

        // top border
        BufferedImage topBorder = UIUtil.createImage(cutW, 1, BufferedImage.TYPE_INT_ARGB);
        topBorder.setRGB(0, 0, cutW, 1,
                         source.getRGB(1, 0, cutW, 1, null, 0, cutW), 0, cutW);
        verifyBorderImage(topBorder);
        topBorder = resizeBorder(topBorder, trimmedWidth, 1);
        finalBorder.setRGB(1, 0, trimmedWidth, 1, topBorder.getRGB(0, 0,
                                                                  trimmedWidth, 1, null, 0, trimmedWidth), 0, trimmedWidth);

        // bottom border
        BufferedImage bottomBorder = UIUtil.createImage(cutW, 1, BufferedImage.TYPE_INT_ARGB);
        bottomBorder.setRGB(0, 0, cutW, 1,
                            source.getRGB(1, cutH + 1, cutW, 1, null, 0, cutW),
                            0, cutW);
        verifyBorderImage(bottomBorder);
        bottomBorder = resizeBorder(bottomBorder, trimmedWidth, 1);
        finalBorder.setRGB(1, trimmedHeight + 1, trimmedWidth, 1,
                           bottomBorder.getRGB(0, 0, trimmedWidth, 1, null, 0,
                                               trimmedWidth), 0, trimmedWidth);

        return finalBorder;
    }

    private static BufferedImage resizeBorder(final BufferedImage border,
                                              int targetWidth,
                                              int targetHeight) throws IOException {
        if (targetWidth > border.getWidth()
            || targetHeight > border.getHeight()) {
            BufferedImage endImage = rescaleBorder(border, targetWidth, targetHeight);
            enforceBorderColors(endImage);
            return endImage;
        }

        int w = border.getWidth();
        int h = border.getHeight();
        int[] data = border.getRGB(0, 0, w, h, null, 0, w);
        int[] newData = new int[targetWidth * targetHeight];

        float widthRatio = (float) Math.max(targetWidth - 1, 1) / (float) Math.max(w - 1, 1);
        float heightRatio = (float) Math.max(targetHeight - 1, 1) / (float) Math.max(h - 1, 1);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((0xff000000 & data[y * w + x]) != 0) {
                    int newX = Math.min(Math.round(x * widthRatio), targetWidth - 1);
                    int newY = Math.min(Math.round(y * heightRatio), targetHeight - 1);
                    newData[newY * targetWidth + newX] = data[y * w + x];
                }
            }
        }

        BufferedImage img = UIUtil.createImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, targetWidth, targetHeight, newData, 0, targetWidth);

        return img;
    }

    private static void verifyBorderImage(BufferedImage border) throws Wrong9PatchException {
        int[] rgb = border.getRGB(0, 0, border.getWidth(), border.getHeight(),
                                  null, 0, border.getWidth());
        for (int aRgb : rgb) {
            if ((0xff000000 & aRgb) != 0) {
                if (aRgb != 0xff000000 && aRgb != 0xffff0000) {
                    throw new Wrong9PatchException();
                }
            }
        }
    }

    private static BufferedImage rescaleBorder(BufferedImage image, int targetWidth, int targetHeight) {
        if (targetWidth == 0) {
            targetWidth = 1;
        }
        if (targetHeight == 0) {
            targetHeight = 1;
        }
        
        if (targetHeight > 1 && targetWidth > 1) {
            throw new Wrong9PatchException();
        }

        int w = image.getWidth();
        int h = image.getHeight();
        int[] data = image.getRGB(0, 0, w, h, null, 0, w);
        int[] newData = new int[targetWidth * targetHeight];
        
        for (int x=0; x < w; x++) {
            for (int y=0; y < h; y++) {
                newData[y * targetWidth + x] = 0x00;
            }
        }
        
        List<Integer> startPositions = new ArrayList<Integer>();
        List<Integer> endPositions = new ArrayList<Integer>();

        boolean inBlock = false;
        if (targetHeight == 1) {
            for (int x = 0; x < w; x++) {
                if ((0xff000000 & data[x]) != 0) {
                    if (!inBlock) {
                        inBlock = true;
                        startPositions.add(x);
                    }
                } else if (inBlock) {
                    endPositions.add(x - 1);
                    inBlock = false;
                }
            }
            if (inBlock) {
                endPositions.add(w - 1);
            }
        } else {
            for (int y = 0; y < h; y++) {
                if ((0xff000000 & data[y]) != 0) {
                    if (!inBlock) {
                        inBlock = true;
                        startPositions.add(y);
                    }
                } else if (inBlock) {
                    endPositions.add(y - 1);
                    inBlock = false;
                }
            }
            if (inBlock) {
                endPositions.add(h - 1);
            }
        }
        try {
            SplineInterpolator interpolator = new SplineInterpolator();
            PolynomialSplineFunction function =
                interpolator.interpolate(new double[] {0f, 1f, Math.max(w - 1, h - 1)},
                                         new double[] {0f, 1f, Math.max(targetHeight - 1, targetWidth - 1)});
            for (int i = 0; i < startPositions.size(); i++) {
                int start = startPositions.get(i);
                int end = endPositions.get(i);
                for (int j = (int) function.value(start); j <= (int) function.value(end); j++) {
                    newData[j] = 0xff000000;
                }
            }

            BufferedImage img = UIUtil.createImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            img.setRGB(0, 0, targetWidth, targetHeight, newData, 0, targetWidth);
            return img;
        } catch (Exception e) {
            Logger.getInstance(ImageUtils.class).error("resizeBorder", e);
        }

        return null;
    }

    public static File saveImageTempFile(BufferedImage resizeImageJpg,
                                         ImageInformation imageInformation) throws IOException {
        File exportFile = imageInformation.getTempImage();
        if (exportFile != null) {
            if (!exportFile.getParentFile().exists()) {
                FileUtils.forceMkdir(exportFile.getParentFile());
            }
            ImageIO.write(resizeImageJpg, "PNG", exportFile);
            return exportFile;
        } else {
            throw new IOException("Couldn't find .idea path.");
        }
    }

    public static File getTargetFile(String path, Resolution resolution, String exportName) {
        return new File(String.format(TARGET_FILE_PATTERN, path, resolution.toString(), exportName));
    }

    public static File getTargetFile(ImageInformation information) {
        return getTargetFile(information.getExportPath(), information.getResolution(), information.getExportName());
    }
}
