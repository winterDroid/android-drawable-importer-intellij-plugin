package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.openapi.vfs.VirtualFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * User: marcprengemann
 * Date: 04.04.14
 * Time: 13:50
 */
public class ImagePanel extends JPanel {
  private BufferedImage image;

  public ImagePanel() {
  }

  public void setImage(File file) {
    try {
      this.image = ImageIO.read(file);
      invalidate();
    } catch (Exception ignored) {}
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (image != null) {

      g.drawImage(image, 0, 0, null);
    }
  }
}
