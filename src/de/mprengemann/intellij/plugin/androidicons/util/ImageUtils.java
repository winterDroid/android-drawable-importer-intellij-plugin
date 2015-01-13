package de.mprengemann.intellij.plugin.androidicons.util;

import com.intellij.util.ui.UIUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

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
        double factor = getScaleFactorToFit(new Dimension(imageWidth, imageHeight), new Dimension(imageViewWidth, imageViewHeight));
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

}
