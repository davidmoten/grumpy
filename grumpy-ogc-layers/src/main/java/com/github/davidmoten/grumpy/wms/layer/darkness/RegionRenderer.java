package com.github.davidmoten.grumpy.wms.layer.darkness;

import java.awt.Graphics2D;

import com.github.davidmoten.grumpy.projection.Projector;
import com.github.davidmoten.grumpy.util.Bounds;

public interface RegionRenderer<T> {
	void renderRegion(Graphics2D g, Projector projector, Bounds geoBounds,
			final T t);
}
