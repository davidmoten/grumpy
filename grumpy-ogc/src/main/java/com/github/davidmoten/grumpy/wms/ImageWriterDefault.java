package com.github.davidmoten.grumpy.wms;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageWriterDefault implements ImageWriter {

	@Override
	public void writeImage(BufferedImage image, ByteArrayOutputStream os,
			String imageType) throws IOException {
		ImageIO.write(image, imageType, os);
	}

}
