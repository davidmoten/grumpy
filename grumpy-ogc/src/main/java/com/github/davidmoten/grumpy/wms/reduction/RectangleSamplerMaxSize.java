package com.github.davidmoten.grumpy.wms.reduction;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

/**
 * Samples bounds by creating a grid starting at top left corner of
 * min(maxSizeKm,widthKm) by min(maxSizeKm, heightKm). Always includes bottom
 * and right edge points as well.
 */
public class RectangleSamplerMaxSize implements RectangleSampler {

    private final double maxSizeKm;

    public RectangleSamplerMaxSize(double maxSizeKm) {
        this.maxSizeKm = maxSizeKm;
    }

    @Override
    public List<Point> sample(Rectangle region) {
        // TODO implement
        return null;
    }
}
