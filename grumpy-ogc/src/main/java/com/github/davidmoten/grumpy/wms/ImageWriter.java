package com.github.davidmoten.grumpy.wms;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * Writes encoded images to an {@link OutputStream}.
 */
public interface ImageWriter {

    /**
     * Writes encoded images to an {@link OutputStream}.
     * 
     * @param image
     *            to be written
     * @param os
     *            {@link OutputStream} to write to
     * @param imageType
     *            type of image as per formatName specification of
     *            {@link ImageIO#write(java.awt.image.RenderedImage, String, OutputStream)}
     * @throws IOException
     */
    void writeImage(BufferedImage image, OutputStream os, String imageType)
            throws IOException;
}
