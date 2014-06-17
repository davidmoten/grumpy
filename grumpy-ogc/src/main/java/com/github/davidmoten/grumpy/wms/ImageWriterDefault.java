package com.github.davidmoten.grumpy.wms;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Writes images using {@link ImageIO}. This can be a bit slow. Significant
 * performance gains have been obtained using <a
 * href="http://objectplanet.com/pngencoder/">PNGEncoder</a>). An implementation
 * has not been included with grumpy because the library is not open source.
 */
public class ImageWriterDefault implements ImageWriter {

    @Override
    public void writeImage(BufferedImage image, ByteArrayOutputStream os, String imageType) throws IOException {
        ImageIO.write(image, imageType, os);
    }

}
