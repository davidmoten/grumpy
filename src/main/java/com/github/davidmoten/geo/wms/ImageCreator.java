package com.github.davidmoten.geo.wms;

import java.awt.image.BufferedImage;

public interface ImageCreator {
	BufferedImage getImage(WmsRequest request);
}
