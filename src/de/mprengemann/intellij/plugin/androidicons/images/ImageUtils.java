package de.mprengemann.intellij.plugin.androidicons.images;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import de.mprengemann.intellij.plugin.androidicons.ui.Wrong9PatchException;
import de.mprengemann.intellij.plugin.androidicons.util.RefactorHelper;
import de.mprengemann.intellij.plugin.androidicons.util.ResizeAlgorithm;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    public static BufferedImage resizeNormalImage(ResizeAlgorithm algorithm,
                                                  Object method,
                                                  ScalingImageInformation information) throws IOException {
        return resizeNormalImage(algorithm,
                                 method,
                                 information.getImageFile(),
                                 information.getFactor(),
                                 information.getTargetWidth(),
                                 information.getTargetHeight());
    }

    public static BufferedImage resizeNormalImage(ResizeAlgorithm algorithm,
                                                  Object method,
                                                  File imageFile,
                                                  float scaleFactor,
                                                  int targetWidth,
                                                  int targetHeight) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        return resizeNormalImage(algorithm, method, image, scaleFactor, targetWidth, targetHeight);
    }

    public static BufferedImage resizeNormalImage(ResizeAlgorithm algorithm,
                                                  Object method,
                                                  BufferedImage image,
                                                  float scaleFactor,
                                                  int targetWidth,
                                                  int targetHeight) throws IOException {
        int newWidth = (int) (scaleFactor * targetWidth);
        int newHeight = (int) (scaleFactor * targetHeight);
        BufferedImage resizedImage = null;
        switch (algorithm) {
            case SCALR:
                Scalr.Method scalrMethod = (Scalr.Method) method;
                resizedImage = Scalr.resize(image, scalrMethod, newWidth, newHeight, Scalr.OP_ANTIALIAS);
                break;
            case THUMBNAILATOR:
                return Thumbnails.of(image)
                                 .size(newWidth, newHeight)
                                 .asBufferedImage();
        }
        return resizedImage;
    }

    public static BufferedImage resizeNinePatchImage(ResizeAlgorithm algorithm,
                                                     Object method,
                                                     Project project,
                                                     ScalingImageInformation information) throws IOException {
        return resizeNinePatchImage(algorithm,
                                    method,
                                    project,
                                    information.getFactor(),
                                    information.getTargetWidth(),
                                    information.getTargetHeight(),
                                    information.getImageFile(),
                                    information.getResolution(),
                                    information.getExportName());
    }

    public static BufferedImage resizeNinePatchImage(ResizeAlgorithm algorithm,
                                                     Object method,
                                                     Project project,
                                                     float scaleFactor,
                                                     int targetWidth,
                                                     int targetHeight,
                                                     File imageFile,
                                                     Resolution resolution,
                                                     String name) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        int type = BufferedImage.TYPE_INT_ARGB;

        int newWidth = (int) (scaleFactor * (targetWidth + 2));
        int newHeight = (int) (scaleFactor * (targetHeight + 2));

        BufferedImage trimedImage = trim9PBorder(image, type);
        saveImageTempFile(resolution, trimedImage, project, getExportName("trimmed", name));
        trimedImage = resizeNormalImage(algorithm, method, trimedImage, 1f, newWidth, newHeight);
        saveImageTempFile(resolution, trimedImage, project, getExportName("trimmedResized", name));

        BufferedImage borderImage;

        int w = trimedImage.getWidth();
        int h = trimedImage.getHeight();

        try {
            borderImage = generateBordersImage(image, w, h, type);
        } catch (Exception e) {
            return null;
        }

        int[] rgbArray = new int[w * h];
        trimedImage.getRGB(0, 0, w, h, rgbArray, 0, w);
        borderImage.setRGB(1, 1, w, h, rgbArray, 0, w);

        return borderImage;
    }

    public static String getExportName(String name) {
        return getExportName("", name);
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

    public static BufferedImage trim9PBorder(BufferedImage inputImage, int type) {
        BufferedImage trimedImage = UIUtil.createImage(inputImage.getWidth() - 2, inputImage.getHeight() - 2, type);
        Graphics2D g = trimedImage.createGraphics();
        g.drawImage(inputImage, 0, 0, trimedImage.getWidth(),
                    trimedImage.getHeight(), 1, 1, inputImage.getWidth() - 1,
                    inputImage.getHeight() - 1, null);
        g.dispose();
        return trimedImage;
    }

    public static BufferedImage generateBordersImage(BufferedImage source,
                                                     int trimedWidth,
                                                     int trimedHeight,
                                                     int type) throws Wrong9PatchException {
        BufferedImage finalBorder = UIUtil.createImage(trimedWidth + 2, trimedHeight + 2, type);
        int cutW = source.getWidth() - 2;
        int cutH = source.getHeight() - 2;

        // left border
        BufferedImage leftBorder = UIUtil.createImage(1, cutH, type);
        leftBorder.setRGB(0, 0, 1, cutH, source.getRGB(0, 1, 1, cutH, null, 0, 1), 0, 1);
        verifyBorderImage(leftBorder);
        leftBorder = resizeBorder(leftBorder, 1, trimedHeight, type);
        finalBorder.setRGB(0, 1, 1, trimedHeight, leftBorder.getRGB(0, 0, 1, trimedHeight, null, 0, 1), 0, 1);

        // right border
        BufferedImage rightBorder = UIUtil.createImage(1, cutH, type);
        rightBorder.setRGB(0, 0, 1, cutH, source.getRGB(cutW + 1, 1, 1, cutH, null, 0, 1), 0, 1);
        verifyBorderImage(rightBorder);
        rightBorder = resizeBorder(rightBorder, 1, trimedHeight, type);
        finalBorder.setRGB(trimedWidth + 1,
                           1,
                           1,
                           trimedHeight,
                           rightBorder.getRGB(0, 0, 1, trimedHeight, null, 0, 1),
                           0,
                           1);

        // top border
        BufferedImage topBorder = UIUtil.createImage(cutW, 1, type);
        topBorder.setRGB(0, 0, cutW, 1, source.getRGB(1, 0, cutW, 1, null, 0, cutW), 0, cutW);
        verifyBorderImage(topBorder);
        topBorder = resizeBorder(topBorder, trimedWidth, 1, type);
        finalBorder.setRGB(1,
                           0,
                           trimedWidth,
                           1,
                           topBorder.getRGB(0, 0, trimedWidth, 1, null, 0, trimedWidth),
                           0,
                           trimedWidth);

        // bottom border
        BufferedImage bottomBorder = UIUtil.createImage(cutW, 1, type);
        bottomBorder.setRGB(0, 0, cutW, 1, source.getRGB(1, cutH + 1, cutW, 1, null, 0, cutW), 0, cutW);
        verifyBorderImage(bottomBorder);
        bottomBorder = resizeBorder(bottomBorder, trimedWidth, 1, type);
        finalBorder.setRGB(1,
                           trimedHeight + 1,
                           trimedWidth,
                           1,
                           bottomBorder.getRGB(0, 0, trimedWidth, 1, null, 0, trimedWidth),
                           0,
                           trimedWidth);

        return finalBorder;
    }

    public static BufferedImage resizeBorder(final BufferedImage border, int targetWidth, int targetHeight, int type) {
        if (targetWidth > border.getWidth() || targetHeight > border.getHeight()) {
            BufferedImage endImage = Scalr.resize(border, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
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

        BufferedImage img = UIUtil.createImage(targetWidth, targetHeight, type);
        img.setRGB(0, 0, targetWidth, targetHeight, newData, 0, targetWidth);

        return img;
    }

    public static void verifyBorderImage(BufferedImage border) throws Wrong9PatchException {
        int[] rgb = border.getRGB(0, 0, border.getWidth(), border.getHeight(), null, 0, border.getWidth());
        for (int aRgb : rgb) {
            if ((0xff000000 & aRgb) != 0) {
                if (aRgb != 0xff000000 && aRgb != 0xffff0000) {
                    throw new Wrong9PatchException();
                }
            }
        }
    }

    public static void enforceBorderColors(BufferedImage inputImage) {
        Graphics2D g = inputImage.createGraphics();
        g.setBackground(new JBColor(new Color(0, 0, 0, 0), null));
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
        inputImage.setRGB(0, 0, 0x0);
        inputImage.setRGB(0, h - 1, 0x0);
        inputImage.setRGB(w - 1, h - 1, 0x0);
        inputImage.setRGB(w - 1, 0, 0x0);
    }

    public static File saveImageTempFile(Resolution resolution,
                                         BufferedImage resizeImageJpg,
                                         Project project,
                                         String exportName) throws IOException {
        File exportFile = RefactorHelper.getTempImageFile(project, resolution, exportName);
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
        return new File(String.format(TARGET_FILE_PATTERN, path, resolution, exportName));
    }

    public static File getTargetFile(ScalingImageInformation information) {
        return getTargetFile(information.getExportPath(), information.getResolution(), information.getExportName());
    }
}
