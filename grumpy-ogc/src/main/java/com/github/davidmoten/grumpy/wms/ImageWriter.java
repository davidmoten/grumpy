package com.github.davidmoten.grumpy.wms;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ImageWriter {
	void writeImage(BufferedImage image, ByteArrayOutputStream os,
			String imageType) throws IOException;
}
