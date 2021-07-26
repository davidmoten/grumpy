package com.github.davidmoten.grumpy.wms.reduction;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import com.github.davidmoten.grumpy.projection.Projector;

/**
 * Samples bounds by creating a grid starting at top left corner of
 * min(maxSizeKm,widthKm) by min(maxSizeKm, heightKm). Always includes bottom
 * and right edge points as well.
 */
public class RectangleSamplerGrid implements RectangleSampler {

    @Override
    public List<Point> sample(Rectangle region, Projector projector) {
        return RectangleUtil.corners(region);
    }
}
