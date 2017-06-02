package com.vaadin.addon.spreadsheet.test.tb3;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

class ImageUtils {
    final private static int TRANPARENCY_MASK = 0xBB000000;

    static void makeHeaderTransparent(File errorFile, int headerHeight) {

        BufferedImage inImage;

        try {
            inImage = ImageIO.read(errorFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ImageProducer ip = new FilteredImageSource(inImage.getSource(),
            new Filter(headerHeight));
        Image outImage = Toolkit.getDefaultToolkit().createImage(ip);

        try {
            ImageIO.write(imageToBufferedImage(outImage), "PNG", errorFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ;

    static private BufferedImage imageToBufferedImage(Image image) {
        BufferedImage dest = new BufferedImage(image.getWidth(null),
            image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = dest.createGraphics();

        g2.drawImage(image, 0, 0, null);

        g2.dispose();
        return dest;
    }

    private static class Filter extends RGBImageFilter {
        private int headerHeight;

        public Filter(int headerHeight) {
            this.headerHeight = headerHeight;
        }

        public int filterRGB(int x, int y, int rgb) {
            if (y < headerHeight)
                return TRANPARENCY_MASK | (rgb & 0xFFFFFF);
            else
                return rgb;
        }
    }
}
