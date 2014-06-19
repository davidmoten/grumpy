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

    private final double maxCellSizeKm;

    public RectangleSamplerGrid(double maxCellSizeKm) {
        this.maxCellSizeKm = maxCellSizeKm;
    }

    @Override
    public List<Point> sample(Rectangle region, Projector projector) {
        // TODO implement
        return RectangleUtil.corners(region);
    }
}
